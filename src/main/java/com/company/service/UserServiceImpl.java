package com.company.service;

import com.company.constant.EmailConstant;
import com.company.domain.UserPrincipal;
import com.company.dto.UserDTO;
import com.company.entity.EmailToken;
import com.company.entity.Role;
import com.company.entity.User;
import com.company.enumeration.RoleEnum;
import com.company.enumeration.TokenPurpose;
import com.company.enumeration.UserSpeciality;
import com.company.exception.*;
import com.company.repository.UserRepository;
import com.company.service.mapper.UserDTOMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.mail.MessagingException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;


import static com.company.constant.EmailConstant.RESET_PASSWORD;
import static com.company.constant.EmailConstant.RESET_PASSWORD_MESSAGE;
import static com.company.constant.EmailTokenConstant.*;
import static com.company.constant.FileConstant.*;
import static com.company.constant.UserConstant.*;
import static com.company.service.LoginAttemptService.LOCK_DURATION_TIME_MINUTES;
import static com.company.utils.PaginationUtils.getPageable;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

@Service
@Qualifier("userDetailsService")
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService
{
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());
    private final UserRepository userRepository;
    private final RoleService roleService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final LoginAttemptService loginAttemptService;
    private final EmailService emailService;
    private final EmailTokenService emailTokenService;

    private final UserDTOMapper userDTOMapper;

    @Autowired
    public UserServiceImpl(UserRepository userRepository,
                           RoleService roleService,
                           BCryptPasswordEncoder bCryptPasswordEncoder,
                           LoginAttemptService loginAttemptService,
                           EmailService emailService,
                           EmailTokenService emailTokenService,
                           UserDTOMapper userDTOMapper)
    {
        this.userRepository = userRepository;
        this.roleService = roleService;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.loginAttemptService = loginAttemptService;
        this.emailService = emailService;
        this.emailTokenService = emailTokenService;
        this.userDTOMapper = userDTOMapper;
    }


    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException
    {
        User user = this.userRepository.findUserByEmail(email);
        if(user == null)
        {
            throw new UsernameNotFoundException(NO_USER_FOUND_BY_EMAIL);
        }

        this.validateLoginAttempt(user);
        user.setLastLoginDate(LocalDateTime.now());
        this.userRepository.save(user);

        return new UserPrincipal(user);
    }

    private void validateLoginAttempt(User user)
    {
        if(user.isNotLocked())
        {
            if(this.loginAttemptService.hasExceededMaxAttempt(user.getEmail()))
            {
                user.setNotLocked(false);
                user.setLockDate(LocalDateTime.now());
            } else
            {
                user.setNotLocked(true);
                user.setLockDate(null);
            }
        }else
        {
            if(user.getLockDate() == null)
            {
                user.setLockDate(LocalDateTime.now());
            }

            if(user.getLockDate().plusMinutes(LOCK_DURATION_TIME_MINUTES).isBefore(LocalDateTime.now()))
            {
                user.setNotLocked(true);
                user.setLockDate(null);
            }

            this.loginAttemptService.evictUserFromLoginAttemptCache(user.getEmail());
        }
    }




    @Override
    @Transactional
    public UserDTO addOrUpdateUser(String currentEmail,
                                   String firstName,
                                   String lastName,
                                   String password,
                                   String newEmail,
                                   UserSpeciality speciality,
                                   Boolean isActive,
                                   Boolean isNotLocked,
                                   RoleEnum roleName) throws UserNotFoundException,
                                                            EmailExistsException,
                                                            InvalidEmailException,
                                                            MessagingException,
                                                            ContainsWhitespaceException
    {
        User user = this.validateUser(currentEmail, newEmail);

        if(user == null)
        {
            user = new User();
            user.setPassword(this.encodePassword(password));
            user.setProfileImageUrl(this.getTemporaryProfileImageUrl(newEmail));
        }

        Role role = roleService.getRoleByName(roleName);
        if(role == null)
        {
            throw new RoleDoesNotExistException(ROLE_DOES_NOT_EXIST);
        }

        user.setUserProperties
                (firstName, lastName, newEmail, speciality, isActive, isNotLocked, role);

        this.userRepository.save(user);

        if(currentEmail == null && !user.isActive())
        {
            this.sendActivateAccountEmail(user);
        }

        return this.userDTOMapper.apply(user);
    }

    private User validateUser(String currentEmail, String newEmail) throws UserNotFoundException,
            EmailExistsException
    {
        User userByNewEmail = this.userRepository.findUserByEmail(newEmail);

        if(StringUtils.isNotBlank(currentEmail))
        {
            User currentUser = this.userRepository.findUserByEmail(currentEmail);

            if(currentUser == null)
            {
                throw new UserNotFoundException(NO_USER_FOUND_BY_EMAIL);
            }

            if(userByNewEmail != null &&
                    !currentUser.getId().equals(userByNewEmail.getId()))
            {
                throw new EmailExistsException(EMAIL_ALREADY_EXISTS);
            }

            return currentUser;
        } else
        {
            if(userByNewEmail != null)
            {
                throw new EmailExistsException(EMAIL_ALREADY_EXISTS);
            }
        }
        return null;
    }

    private String encodePassword(String password)
    {
        return this.bCryptPasswordEncoder.encode(password);
    }

    private String getTemporaryProfileImageUrl(String username)
    {
        return ServletUriComponentsBuilder.fromCurrentContextPath().path(DEFAULT_USER_IMAGE_PATH + username).toUriString();
    }


    @Override
    @Transactional
    public void sendResetPasswordEmail(String email) throws UserNotFoundException,
                                                            MessagingException,
                                                            EmailAlreadySentException
    {
        User user = this.userRepository.findUserByEmail(email);
        if(user == null)
        {
            throw new UserNotFoundException(NO_USER_FOUND_BY_EMAIL);
        }

        EmailToken emailToken = new EmailToken(user, TokenPurpose.RESET_PASSWORD);

        EmailToken existingToken = this.emailTokenService.getEmailTokenByUserAndPurpose(user, emailToken.getPurpose());
        if(existingToken != null)
        {
            if(existingToken.getExpiryDate().isAfter(LocalDateTime.now())
                    && existingToken.getUsedDate() == null)
            {
                throw new EmailAlreadySentException(EMAIL_ALREADY_SENT);
            } else
            {
                this.emailTokenService.delete(existingToken);
            }
        }

        this.emailTokenService.save(emailToken);

        String link = RESET_PASSWORD_URL + emailToken.getToken();
        String message = String.format(RESET_PASSWORD_MESSAGE, user.getFirstName(), link, RESET_PASSWORD);
        this.emailService.sendNewEmail(user.getEmail(), message);
    }

    @Override
    @Transactional
    public void resetPassword(String token, String password) throws UserNotFoundException, PasswordAlreadyChangedException, EmailTokenExpiredException, EmailTokenNotFoundException
    {
        EmailToken emailToken = this.emailTokenService.getEmailTokenByToken(token);
        if(emailToken == null)
        {
            throw new EmailTokenNotFoundException(TOKEN_NOT_FOUND);
        }

        if(emailToken.matchTokenPurpose(TokenPurpose.RESET_PASSWORD) &&
                emailToken.getUsedDate() != null)
        {
            throw new PasswordAlreadyChangedException(PASSWORD_ALREADY_CHANGED);
        }

        User user = emailToken.getUser();
        if(user == null)
        {
            throw new UserNotFoundException(NO_USER_FOUND_BY_EMAIL);
        }

        if(this.bCryptPasswordEncoder.matches(password, user.getPassword()))
        {
            throw new PasswordsMatchException(PASSWORDS_CANNOT_MATCH);
        }

        user.setPassword(this.encodePassword(password));
        this.userRepository.save(user);
        this.emailTokenService.setUseDate(token);
    }


    @Override
    @Transactional
    public void sendActivateAccountEmail(User user) throws MessagingException
    {
        EmailToken emailToken = new EmailToken(user, TokenPurpose.CONFIRM_ACCOUNT);

        EmailToken existingToken = this.emailTokenService.getEmailTokenByUserAndPurpose(user, emailToken.getPurpose());
        if(existingToken != null)
        {
            this.emailTokenService.delete(existingToken);
        }

        this.emailTokenService.save(emailToken);

        String link = CONFIRMATION_URL + emailToken.getToken();
        String message = String.format(EmailConstant.ACTIVATION_MESSAGE, user.getFirstName(), link, link);
        this.emailService.sendNewEmail(user.getEmail(), message);
    }


    @Override
    @Transactional
    public void activateAccount(String token) throws UserNotFoundException, EmailTokenExpiredException, AccountAlreadyActivatedException, EmailTokenNotFoundException
    {
        EmailToken emailToken = this.emailTokenService.getEmailTokenByToken(token);
        if(emailToken == null)
        {
            throw new EmailTokenNotFoundException(TOKEN_NOT_FOUND);
        }

        User user = emailToken.getUser();
        if(user == null)
        {
            throw new UserNotFoundException(USER_NOT_FOUND);
        }

        if(emailToken.matchTokenPurpose(TokenPurpose.CONFIRM_ACCOUNT))
        {
            if(user.isActive())
            {
                throw new AccountAlreadyActivatedException(ACCOUNT_ALREADY_ACTIVATED);
            }

            if(!user.isActive() && emailToken.getUsedDate() != null)
            {
                throw new InvalidTokenException(INVALID_TOKEN);
            }
        }

        user.setActive(true);
        this.userRepository.save(user);
        this.emailTokenService.setUseDate(token);
    }




    @Override
    @Transactional
    public void deleteUser(Long userId)
    {
        this.userRepository.deleteById(userId);
    }




    @Override
    public Page<UserDTO> getUsers(int page, int size)
    {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> entitiesPage = this.userRepository.findAllByOrderByFirstNameAsc(pageable);

        return entitiesPage.map(this.userDTOMapper);
    }




    @Override
    public Page<UserDTO> getUserByFullNameContaining(String fullName, int page, int size)
    {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> entitiesPage = this.userRepository.findUserByFirstNameAndLastNameContaining(fullName, pageable);

        return entitiesPage.map(this.userDTOMapper);
    }




    @Override
    public Page<UserDTO> getUsersByRole(RoleEnum role, int page, int size)
    {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> entitiesPage = this.userRepository.findUsersByRoleNameOrderByFirstName(role, pageable);
        return entitiesPage.map(this.userDTOMapper);
    }


    @Override
    public Page<UserDTO> getUsersByRoleAndFullName(RoleEnum role, String fullName, int page, int size)
    {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> entitiesPage = this.userRepository.findUsersByRoleAndFullName(role, fullName, pageable);

        return entitiesPage.map(this.userDTOMapper);
    }




    @Override
    public Page<UserDTO> getUsersNotInProject(Long projectId, int page, int size)
    {
        List<User> participants = this.userRepository.findUsersByProjectsAssignedId(projectId);
        Pageable pageable = PageRequest.of(page, size);

        Page<User> entitiesPage;
        if(participants.size() != 0)
        {
            List<Long> usersIds = participants.stream().map(User::getId).toList();
            entitiesPage = this.userRepository.findUsersByRoleNameAndIdNotInOrderByFirstName(RoleEnum.ROLE_USER, usersIds, pageable);
        } else
        {
            entitiesPage = this.userRepository.findUsersByRoleNameOrderByFirstName(RoleEnum.ROLE_USER, pageable);
        }

        return entitiesPage.map(this.userDTOMapper);
    }




    @Override
    public Page<UserDTO> getUsersByFullNameAndNotInProject(String fullName, Long projectId, int page, int size)
    {
        List<User> participants = this.userRepository.findUsersByProjectsAssignedId(projectId);
        Pageable pageable = PageRequest.of(page, size);

        Page<User> entitiesPage;
        if(participants.size() != 0)
        {
            List<Long> userIds = participants.stream().map(User::getId).toList();
            entitiesPage = this.userRepository.findUsersByRoleAndFullNameAndNotInProject(RoleEnum.ROLE_USER, fullName, userIds, pageable);
        } else
        {
            entitiesPage = this.userRepository.findUsersByRoleAndFullName(RoleEnum.ROLE_USER, fullName, pageable);
        }

        return entitiesPage.map(this.userDTOMapper);
    }




    @Override
    public Page<UserDTO> getParticipantsByProjectId(Long projectId, int page, int size, String sortOrder, boolean ascending)
    {
        Pageable pageable = getPageable(page, size, sortOrder, ascending);
        Page<User> entitiesPage = this.userRepository.findUsersByProjectsAssignedId(projectId, pageable);

        return entitiesPage.map(this.userDTOMapper);
    }




    @Override
    public Page<UserDTO> getParticipantsByFullNameAndProjectId(String fullName, Long projectId, int page, int size)
    {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> entitiesPage = this.userRepository.findUsersByFullNameAndProjectsAssignedId(fullName, projectId, pageable);

        return entitiesPage.map(this.userDTOMapper);
    }




    @Override
    public Page<UserDTO> getParticipantsByProjectIdAndExcludeParticipant(Long projectId, Long participantId, int page, int size, String sortOrder, boolean ascending)
    {
        Pageable pageable = getPageable(page, size, sortOrder, ascending);
        Page<User> entitiesPage = this.userRepository.findUsersByProjectsAssignedIdAndIdNot(projectId, participantId, pageable);
        return entitiesPage.map(this.userDTOMapper);
    }




    @Override
    public Page<UserDTO> getParticipantsByFullNameAndProjectIdExcludeParticipant(String fullName, Long projectId, Long userId, int page, int size)
    {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> entitiesPage = this.userRepository.findUsersByFullNameAndProjectsAssignedIdAndIdNot(projectId, userId, fullName, pageable);
        return entitiesPage.map(this.userDTOMapper);
    }




    @Override
    public UserDTO getUserById(long id)
    {
        User user = this.userRepository.findUserById(id);
        return this.userDTOMapper.apply(user);
    }




    @Override
    public User getUserEntityById(Long id)
    {
        return this.userRepository.findUserById(id);
    }




    @Override
    public User getUserEntityByEmail(String username)
    {
        return this.userRepository.findUserByEmail(username);
    }




    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public UserDTO updateProfileImage(String currentEmail, MultipartFile profileImage) throws UserNotFoundException, EmailExistsException, IOException
    {
        User user = this.validateUser(currentEmail, null);
        if(user == null)
        {
            throw new UserNotFoundException(NO_USER_FOUND_BY_EMAIL);
        }
        this.saveProfileImage(user, profileImage);
        return this.userDTOMapper.apply(user);
    }

    private void saveProfileImage(User user, MultipartFile profileImage) throws IOException
    {
        if(profileImage != null)
        {
            Path userFolder = Paths.get(USER_FOLDER + user.getEmail()).toAbsolutePath().normalize();
            if(!Files.exists(userFolder))
            {
                Files.createDirectories(userFolder);
                System.out.println(DIRECTORY_CREATED + userFolder);
            }

            // -we have to use deleteIfExists because if we try to delete file that doesn't exist, we'll get an exception
            Files.deleteIfExists(Paths.get(userFolder + user.getEmail() + DOT + JPG_EXTENSION));

            // -we use REPLACE_EXISTING to make super sure, that the previous profile picture was deleted
            Files.copy(profileImage.getInputStream(), userFolder.resolve(user.getEmail() + DOT + JPG_EXTENSION), REPLACE_EXISTING);
            user.setProfileImageUrl(this.setProfileImageUrl(user.getEmail()));
            this.userRepository.save(user);
        }
    }

    private String setProfileImageUrl(String email)
    {
        return ServletUriComponentsBuilder.fromCurrentContextPath().path(USER_IMAGE_PATH + email + DOT + JPG_EXTENSION).toUriString();
    }
}
