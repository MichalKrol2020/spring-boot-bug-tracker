package com.company.service;

import com.company.dto.UserDTO;
import com.company.entity.User;
import com.company.enumeration.RoleEnum;
import com.company.enumeration.UserSpeciality;
import com.company.exception.*;
import org.springframework.data.domain.Page;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;
import java.io.IOException;

public interface UserService extends UserDetailsService
{
    UserDTO addOrUpdateUser(String currentEmail,
                            String firstName,
                            String lastName,
                            String password,
                            String email,
                            UserSpeciality speciality,
                            Boolean isActive,
                            Boolean isNotLocked,
                            RoleEnum role) throws UserNotFoundException, AccountAlreadyActivatedException,
                                                     EmailExistsException, InvalidEmailException,
                                                     MessagingException, ContainsWhitespaceException,
                                                     EmailTokenExpiredException, EmailTokenNotFoundException, IOException;

    void sendResetPasswordEmail(String email) throws UserNotFoundException, MessagingException, EmailTokenExpiredException, AccountAlreadyActivatedException, EmailTokenNotFoundException, EmailAlreadySentException;
    void resetPassword(String token, String password) throws UserNotFoundException, MessagingException, PasswordAlreadyChangedException, EmailTokenExpiredException, AccountAlreadyActivatedException, EmailTokenNotFoundException;
    void sendActivateAccountEmail(User user) throws MessagingException;
    void activateAccount(String token) throws UserNotFoundException, PasswordAlreadyChangedException, EmailTokenExpiredException, AccountAlreadyActivatedException, EmailTokenNotFoundException;

    void deleteUser(Long userId);

    Page<UserDTO> getUsers(int page, int size);

    Page<UserDTO> getUserByFullNameContaining(String name, int page, int size);

    Page<UserDTO> getUsersByRole(RoleEnum role, int page, int size);

    Page<UserDTO> getUsersByRoleAndFullName(RoleEnum role, String fullName, int page, int size);

    Page<UserDTO> getUsersNotInProject(Long projectId, int page, int size);

    Page<UserDTO> getUsersByFullNameAndNotInProject(String fullName, Long projectId, int page, int size);

    Page<UserDTO> getParticipantsByProjectId(Long projectId, int page, int size, String sortOrder, boolean ascending);

    Page<UserDTO> getParticipantsByFullNameAndProjectId(String fullName, Long projectId, int page, int size);

    Page<UserDTO> getParticipantsByProjectIdAndExcludeParticipant(Long projectId, Long participantId, int page, int size, String sortOrder, boolean ascending);

    Page<UserDTO> getParticipantsByFullNameAndProjectIdExcludeParticipant(String fullName, Long projectId, Long userId, int page, int size);

    UserDTO getUserById(long id);

    User getUserEntityById(Long id);

    User getUserEntityByEmail(String username);

    UserDTO updateProfileImage(String username, MultipartFile profileImage) throws UserNotFoundException, EmailExistsException, IOException;
}
