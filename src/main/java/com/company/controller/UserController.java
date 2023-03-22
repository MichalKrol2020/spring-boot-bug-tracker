package com.company.controller;

import com.company.domain.*;
import com.company.dto.UserDTO;
import com.company.enumeration.RoleEnum;
import com.company.enumeration.UserSpeciality;
import com.company.exception.*;
import com.company.exception.ExceptionHandler;
import com.company.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import javax.mail.MessagingException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.company.constant.FileConstant.*;
import static com.company.constant.UserConstant.*;
import static org.springframework.http.MediaType.IMAGE_JPEG_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;


@RestController
@CrossOrigin("http://localhost:4200")
@RequestMapping(path = {"/","/user"})
public class UserController extends ExceptionHandler
{
    private final UserService userService;

    @Autowired
    UserController(UserService userService)
    {
        this.userService = userService;
    }



    @PostMapping
    @PreAuthorize("hasAnyAuthority('admin:create')")
    public ResponseEntity<UserDTO> addUser(@RequestParam("firstName") String firstName,
                                           @RequestParam("lastName") String lastName,
                                           @RequestParam("password") String password,
                                           @RequestParam("email") String email,
                                           @RequestParam("speciality") String speciality,
                                           @RequestParam("isActive") String isActive,
                                           @RequestParam("isNotLocked") String isNotLocked,
                                           @RequestParam("role") String role) throws UserNotFoundException,
                                                                                                      MessagingException,
                                                                                                      EmailExistsException,
                                                                                                      IOException,
                                                                                                      ContainsWhitespaceException,
                                                                                                      InvalidEmailException,
                                                                                                      AccountAlreadyActivatedException,
                                                                                                      EmailTokenExpiredException,
                                                                                                      EmailTokenNotFoundException
    {
        UserDTO newUser = userService.addOrUpdateUser
                (null, firstName, lastName, password, email, UserSpeciality.valueOf(speciality),
                        Boolean.parseBoolean(isActive), Boolean.parseBoolean(isNotLocked), RoleEnum.valueOf(role));

        return new ResponseEntity<>(newUser, HttpStatus.OK);
    }



    @PutMapping
    @PreAuthorize("hasAnyAuthority('user:update', 'project_leader:update')")
    public ResponseEntity<UserDTO> updateUser(@RequestParam(value = "currentEmail") String currentEmail,
                                              @RequestParam(value = "firstName") String firstName,
                                              @RequestParam(value = "lastName") String lastName,
                                              @RequestParam(value = "email") String email,
                                              @RequestParam(value = "speciality") String speciality) throws UserNotFoundException,
                                                                                                              MessagingException,
                                                                                                              EmailExistsException,
                                                                                                              IOException,
                                                                                                              ContainsWhitespaceException,
                                                                                                              InvalidEmailException,
                                                                                                              AccountAlreadyActivatedException,
                                                                                                              EmailTokenExpiredException,
                                                                                                              EmailTokenNotFoundException
    {
        UserDTO updatedUser = userService.addOrUpdateUser
                (currentEmail, firstName, lastName,null, email, UserSpeciality.valueOf(speciality), null,
                        null, null);

        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }



    @PutMapping("account/email")
    public ResponseEntity<HttpResponse> sendResetPasswordEmail(@RequestBody String email) throws UserNotFoundException,
                                                                                                 MessagingException,
                                                                                                 AccountAlreadyActivatedException,
                                                                                                 EmailTokenExpiredException,
                                                                                                 EmailTokenNotFoundException,
                                                                                                 EmailAlreadySentException
    {
        this.userService.sendResetPasswordEmail(email);
        return HttpResponse.createResponse(HttpStatus.OK, PASSWORD_RESET_EMAIL_SENT);
    }



    @PutMapping(path = "account/password")
    public ResponseEntity<HttpResponse> resetPassword(@RequestParam(value = "token") String token,
                                                      @RequestBody String password) throws UserNotFoundException,
                                                                                           MessagingException,
                                                                                           AccountAlreadyActivatedException,
                                                                                           EmailTokenExpiredException,
                                                                                           EmailTokenNotFoundException,
                                                                                           PasswordAlreadyChangedException
    {
        this.userService.resetPassword(token, password);
        return HttpResponse.createResponse(HttpStatus.OK, PASSWORD_HAS_BEEN_RESET);
    }



    @PutMapping(path = "account")
    public ResponseEntity<HttpResponse> activateAccount(@RequestBody String token) throws AccountAlreadyActivatedException,
                                                                                          EmailTokenExpiredException,
                                                                                          EmailTokenNotFoundException,
                                                                                          PasswordAlreadyChangedException,
                                                                                          UserNotFoundException
    {
        this.userService.activateAccount(token);
        return HttpResponse.createResponse(HttpStatus.OK, ACCOUNT_ACTIVATED);
    }



    @DeleteMapping(path = "{id}")
    @PreAuthorize("hasAnyAuthority('admin:delete')")
    public ResponseEntity<HttpResponse> deleteUser(@PathVariable(value = "id") Long userId)
    {
        this.userService.deleteUser(userId);
        return HttpResponse.createResponse(HttpStatus.OK, USER_DELETED_SUCCESSFULLY);
    }



    @GetMapping(path = "users")
    @PreAuthorize("hasAnyAuthority('user:read', 'project_leader:read')")
    public ResponseEntity<Page<UserDTO>> listAllUsers(@RequestParam(value = "page") int page,
                                                      @RequestParam(value = "size") int size)
    {
        Page<UserDTO> userPage = this.userService.getUsers(page, size);
        return new ResponseEntity<>(userPage, HttpStatus.OK);
    }



    @GetMapping(path = "users/name")
    @PreAuthorize("hasAnyAuthority('user:read', 'project_leader:read')")
    public ResponseEntity<Page<UserDTO>> listUsersByFullName(@RequestParam(value = "fullName") String fullName,
                                                             @RequestParam(value = "page") int page,
                                                             @RequestParam(value = "size") int size)
    {
        Page<UserDTO> userPage = this.userService.getUserByFullNameContaining(fullName, page, size);
        return new ResponseEntity<>(userPage, HttpStatus.OK);
    }



    @GetMapping(path = "users/role")
    @PreAuthorize("hasAnyAuthority('user:read', 'project_leader:read')")
    public ResponseEntity<Page<UserDTO>> listUsersByRole(@RequestParam(value = "role") String role,
                                                         @RequestParam(value = "page") int page,
                                                         @RequestParam(value = "size") int size)
    {
        Page<UserDTO> userPage = this.userService.getUsersByRole(RoleEnum.valueOf(role), page, size);
        return new ResponseEntity<>(userPage, HttpStatus.OK);
    }



    @GetMapping(path = "users/role/name")
    @PreAuthorize("hasAnyAuthority('project_leader:read')")
    public ResponseEntity<Page<UserDTO>> listUsersByRoleAndFullName(@RequestParam(value = "role") String role,
                                                                    @RequestParam(value = "fullName") String fullName,
                                                                    @RequestParam(value = "page") int page,
                                                                    @RequestParam(value = "size") int size)
    {
        Page<UserDTO> userPage = this.userService.getUsersByRoleAndFullName(RoleEnum.valueOf(role), fullName, page, size);
        return new ResponseEntity<>(userPage, HttpStatus.OK);
    }



    @GetMapping(path = "users/not-participants")
    @PreAuthorize("hasAnyAuthority('project_leader:read')")
    public ResponseEntity<Page<UserDTO>> listUsersNotInProject( @RequestParam(value = "projectId") Long projectId,
                                                                @RequestParam(value = "page") int page,
                                                                @RequestParam(value = "size") int size)
    {
        Page<UserDTO> userPage = this.userService.getUsersNotInProject(projectId, page, size);
        return new ResponseEntity<>(userPage, HttpStatus.OK);
    }



    @GetMapping(path = "users/name/not-participants")
    @PreAuthorize("hasAnyAuthority('project_leader:read')")
    public ResponseEntity<Page<UserDTO>> listUsersByFullNameAndNotInProject(@RequestParam(value = "fullName") String fullName,
                                                                            @RequestParam(value = "projectId") Long projectId,
                                                                            @RequestParam(value = "page") int page,
                                                                            @RequestParam(value = "size") int size)
    {
        Page<UserDTO> userPage = this.userService.getUsersByFullNameAndNotInProject(fullName, projectId, page, size);
        return new ResponseEntity<>(userPage, HttpStatus.OK);
    }



    @GetMapping(path = "participants")
    @PreAuthorize("hasAnyAuthority('project_leader:read')")
    public ResponseEntity<Page<UserDTO>> listParticipantsByProjectId(@RequestParam(value = "projectId") Long projectId,
                                                                     @RequestParam(value = "page") int page,
                                                                     @RequestParam(value = "size") int size,
                                                                     @RequestParam(value = "sortOrder") String sortOrder,
                                                                     @RequestParam(value = "ascending") String ascending)
    {
        Page<UserDTO> usersPage = this.userService.getParticipantsByProjectId(projectId, page, size, sortOrder, Boolean.parseBoolean(ascending));
        return new ResponseEntity<>(usersPage, HttpStatus.OK);
    }



    @GetMapping(path = "participants/name")
    @PreAuthorize("hasAnyAuthority('project_leader:read')")
    public ResponseEntity<Page<UserDTO>> listParticipantsByFullNameAndProjectId(@RequestParam(value = "fullName") String fullName,
                                                                                @RequestParam(value = "projectId") Long projectId,
                                                                                @RequestParam(value = "page") int page,
                                                                                @RequestParam(value = "size") int size)
    {
        Page<UserDTO> usersPage = this.userService.getParticipantsByFullNameAndProjectId(fullName, projectId, page, size);
        return new ResponseEntity<>(usersPage, HttpStatus.OK);
    }



    @GetMapping(path = "participants/exclude")
    @PreAuthorize("hasAnyAuthority('project_leader:read')")
    public ResponseEntity<Page<UserDTO>> listParticipantsByProjectIdExcludeParticipant(@RequestParam(value = "projectId") Long projectId,
                                                                                       @RequestParam(value = "participantId") Long participantId,
                                                                                       @RequestParam(value = "page") int page,
                                                                                       @RequestParam(value = "size") int size,
                                                                                       @RequestParam(value = "sortOrder") String sortOrder,
                                                                                       @RequestParam(value = "ascending") String ascending)
    {
        Page<UserDTO> usersPage = this.userService.getParticipantsByProjectIdAndExcludeParticipant(projectId, participantId, page, size, sortOrder, Boolean.parseBoolean(ascending));
        return new ResponseEntity<>(usersPage, HttpStatus.OK);
    }



    @GetMapping(path = "participants/name/exclude")
    @PreAuthorize("hasAnyAuthority('project_leader:read')")
    public ResponseEntity<Page<UserDTO>> listParticipantsByFullNameAndProjectIdExcludeParticipant(@RequestParam String fullName,
                                                                                                  @RequestParam(value = "projectId") Long projectId,
                                                                                                  @RequestParam(value = "participantId") Long participantId,
                                                                                                  @RequestParam(value = "page") int page,
                                                                                                  @RequestParam(value = "size") int size)
    {
        Page<UserDTO> usersPage = this.userService.getParticipantsByFullNameAndProjectIdExcludeParticipant(fullName, projectId, participantId, page, size);
        return new ResponseEntity<>(usersPage, HttpStatus.OK);
    }



    @GetMapping(path = "{id}")
    @PreAuthorize("hasAnyAuthority('user:read', 'project_leader:read')")
    public ResponseEntity<UserDTO> getUser(@PathVariable(value = "id") Long userId)
    {
        UserDTO user = this.userService.getUserById(userId);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }



    @PutMapping(consumes = {MULTIPART_FORM_DATA_VALUE})
    @PreAuthorize("hasAnyAuthority('user:update', 'project_leader:update')")
    public ResponseEntity<UserDTO> updateProfileImage(@RequestParam(value = "email") String email,
                                                      @RequestParam(value = "profileImage") MultipartFile profileImage) throws UserNotFoundException,
                                                                                                                               EmailExistsException,
                                                                                                                               IOException
    {
        UserDTO updatedUser = this.userService.updateProfileImage(email, profileImage);

        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }



    @GetMapping(path = "image/{username}/{fileName}", produces = IMAGE_JPEG_VALUE)
    public byte[] getProfileImage(@PathVariable("username") String username,
                                  @PathVariable("fileName") String filename) throws IOException
    {
        return Files.readAllBytes(Paths.get(USER_FOLDER + username + FORWARD_SLASH + filename));
    }



    @GetMapping(path = "image/profile/{username}", produces = IMAGE_JPEG_VALUE)
    public byte[] getTempProfileImage(@PathVariable("username") String username) throws IOException
    {

        URL url = new URL(TEMP_PROFILE_IMAGE_BASE_URL + username);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        try(InputStream inputStream = url.openStream())
        {
            int bytesRead;
            byte[] chunk = new byte[1024];

            while ((bytesRead = inputStream.read(chunk)) > 0)
            {
                byteArrayOutputStream.write(chunk, 0, bytesRead);
            }
        }

        return byteArrayOutputStream.toByteArray();
    }
}
