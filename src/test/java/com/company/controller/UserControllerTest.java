package com.company.controller;

import com.company.builder.UserBuilder;
import com.company.builder.UserDTOBuilder;
import com.company.dto.UserDTO;
import com.company.entity.User;
import com.company.enumeration.RoleEnum;
import com.company.enumeration.UserSpeciality;
import com.company.jwt.JwtTokenProvider;
import com.company.jwt.filter.JwtAccessDeniedHandler;
import com.company.jwt.filter.JwtAuthenticationEntryPoint;
import com.company.jwt.filter.JwtAuthorizationFilter;
import com.company.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import java.util.ArrayList;
import java.util.List;

import static com.company.constant.ExceptionConstant.NOT_ENOUGH_PERMISSION;
import static com.company.constant.UserConstant.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(SpringExtension.class)
public class UserControllerTest
{
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private JwtAuthorizationFilter authorizationFilter;

    @MockBean
    private JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @MockBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @MockBean
    private UserService userService;

    @MockBean
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private ObjectMapper objectMapper;


    private User user;
    private UserDTO userDTO;
    private User editedUser;
    private UserDTO editedUserDTO;
    private List<UserDTO> userDTOList;

    @BeforeEach
    public void setUp()
    {
        String newFirstName = "James";
        String newLastName = "Dean";
        String newEmail = "james.dean@email.com";
        UserSpeciality speciality = UserSpeciality.DATABASE_DESIGN;

        this.user = new UserBuilder().build();
        this.userDTO = new UserDTOBuilder().build();

        this.editedUser = new UserBuilder()
                .withFirstName(newFirstName)
                .withLastName(newLastName)
                .withEmail(newEmail)
                .withSpeciality(speciality)
                .build();

        this.editedUserDTO = new UserDTOBuilder()
                .withFirstName(newFirstName)
                .withLstName(newLastName)
                .withEmail(newEmail)
                .withSpeciality(speciality)
                .build();

        Long userId = 1L;
        this.userDTOList = new ArrayList<>();
        for(int i = 0; i < 10; i++)
        {
            UserDTO userDTO = new UserDTOBuilder()
                    .withId(userId)
                    .build();

            this.userDTOList.add(userDTO);

            userId++;
        }
    }


    @Test
    @WithMockUser(authorities = "admin:create")
    public void testAddUser_Should_Return_200_And_UserDTO() throws Exception
    {
        String firstName = this.user.getFirstName();
        String lastName = this.user.getLastName();
        String password = this.user.getPassword();
        String email = this.user.getEmail();
        String speciality = this.user.getSpeciality().name();
        String role = this.user.getRole().getName().name();
        String isActive = "true";
        String isNotLocked = "true";

        when(this.userService.addOrUpdateUser(
                null,
                firstName, lastName, password, email, UserSpeciality.valueOf(speciality),
                Boolean.parseBoolean(isActive), Boolean.parseBoolean(isNotLocked), RoleEnum.valueOf(role))).thenReturn(this.userDTO);

        ResultActions response = this.mockMvc.perform(post("/user")
                .param("firstName", firstName)
                .param("lastName", lastName)
                .param("password", password)
                .param("email", email)
                .param("speciality", speciality)
                .param("isActive", isActive)
                .param("isNotLocked", isNotLocked)
                .param("role", role));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", CoreMatchers.is(this.userDTO.id().intValue())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.firstName", CoreMatchers.is(this.userDTO.firstName())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.lastName", CoreMatchers.is(this.userDTO.lastName())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email", CoreMatchers.is(this.userDTO.email())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.speciality", CoreMatchers.is(this.userDTO.speciality())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.profileImageUrl", CoreMatchers.is(this.userDTO.profileImageUrl())));
    }




    @Test
    @WithMockUser(authorities = "invalid:create")
    public void testAddUser_For_InvalidAuthority_Should_Return_200_And_UserDTO() throws Exception
    {
        String firstName = this.user.getFirstName();
        String lastName = this.user.getLastName();
        String password = this.user.getPassword();
        String email = this.user.getEmail();
        String speciality = this.user.getSpeciality().name();
        String role = this.user.getRole().getName().name();
        String isActive = "true";
        String isNotLocked = "false";

        ResultActions response = this.mockMvc.perform(post("/user")
                .param("firstName", firstName)
                .param("lastName", lastName)
                .param("password", password)
                .param("email", email)
                .param("speciality", speciality)
                .param("isActive", isActive)
                .param("isNotLocked", isNotLocked)
                .param("role", role));

        response.andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", CoreMatchers.is(NOT_ENOUGH_PERMISSION)));
    }




    @Test
    @WithMockUser(authorities = "user:update")
    public void testUpdateUser_Should_Return_200_And_UserDTO() throws Exception
    {
        String currentEmail = this.user.getEmail();
        String newFirstName = this.editedUser.getFirstName();
        String newLastName = this.editedUser.getLastName();
        String newEmail = this.editedUser.getEmail();
        String speciality = this.user.getSpeciality().name();

        when(this.userService.addOrUpdateUser(
                currentEmail, newFirstName, newLastName, null, newEmail, UserSpeciality.valueOf(speciality),
                null, null, null)).thenReturn(this.editedUserDTO);

        ResultActions response = this.mockMvc.perform(put("/user")
                .param("currentEmail", currentEmail)
                .param("firstName", newFirstName)
                .param("lastName", newLastName)
                .param("email", newEmail)
                .param("speciality", speciality));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", CoreMatchers.is(this.editedUserDTO.id().intValue())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.firstName", CoreMatchers.is(this.editedUserDTO.firstName())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.lastName", CoreMatchers.is(this.editedUserDTO.lastName())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email", CoreMatchers.is(this.editedUserDTO.email())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.speciality", CoreMatchers.is(this.editedUserDTO.speciality())));
    }



    @Test
    @WithMockUser(authorities = "project_leader:update")
    public void testUpdateUser_For_ProjectLeader_Should_Return_200_And_UserDTO() throws Exception
    {
        String currentEmail = this.user.getEmail();
        String newFirstName = this.editedUser.getFirstName();
        String newLastName = this.editedUser.getLastName();
        String newEmail = this.editedUser.getEmail();
        String speciality = this.user.getSpeciality().name();

        when(this.userService.addOrUpdateUser(
                currentEmail, newFirstName, newLastName, null, newEmail, UserSpeciality.valueOf(speciality),
                null, null, null)).thenReturn(this.editedUserDTO);

        ResultActions response = this.mockMvc.perform(put("/user")
                .param("currentEmail", currentEmail)
                .param("firstName", newFirstName)
                .param("lastName", newLastName)
                .param("email", newEmail)
                .param("speciality", speciality));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", CoreMatchers.is(this.editedUserDTO.id().intValue())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.firstName", CoreMatchers.is(this.editedUserDTO.firstName())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.lastName", CoreMatchers.is(this.editedUserDTO.lastName())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email", CoreMatchers.is(this.editedUserDTO.email())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.speciality", CoreMatchers.is(this.editedUserDTO.speciality())));
    }




    @Test
    @WithMockUser(authorities = "invalid:update")
    public void testUpdateUser_For_InvalidAuthority_Should_Return_200_And_UserDTO() throws Exception
    {
        String currentEmail = this.user.getEmail();
        String newFirstName = this.editedUser.getFirstName();
        String newLastName = this.editedUser.getLastName();
        String newEmail = this.editedUser.getEmail();
        String speciality = this.user.getSpeciality().name();

        ResultActions response = this.mockMvc.perform(put("/user")
                .param("currentEmail", currentEmail)
                .param("firstName", newFirstName)
                .param("lastName", newLastName)
                .param("email", newEmail)
                .param("speciality", speciality));

        response.andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", CoreMatchers.is(NOT_ENOUGH_PERMISSION)));
    }




    @Test
    public void testSendResetPasswordEmail_Should_Return_200_And_UserDTO() throws Exception
    {
        String email = this.user.getEmail();

        doNothing().when(this.userService).sendResetPasswordEmail(email);

        ResultActions response = this.mockMvc.perform(put("/user/account/email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(email));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", CoreMatchers.is(PASSWORD_RESET_EMAIL_SENT)));
    }




    @Test
    public void testResetPassword_Should_Return_200_And_MessageIfPasswordReset() throws Exception
    {
        String token = "VALID_TOKEN";
        String email = this.user.getEmail();

        doNothing().when(this.userService).resetPassword(token, email);

        ResultActions response = this.mockMvc.perform(put("/user/account/password")
                .param("token", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(email));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", CoreMatchers.is(PASSWORD_HAS_BEEN_RESET)));
    }




    @Test
    public void testActivateAccount_Should_Return_200_And_MessageIfAccountActivated() throws Exception
    {
        String token = "VALID_TOKEN";
        doNothing().when(this.userService).activateAccount(token);

        ResultActions response = this.mockMvc.perform(put("/user/account")
                .contentType(MediaType.APPLICATION_JSON)
                .content(token));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", CoreMatchers.is(ACCOUNT_ACTIVATED)));
    }




    @Test
    @WithMockUser(authorities = "admin:delete")
    public void testDeleteUser_Should_Return_200_And_UserDTO() throws Exception
    {
        Long userId = this.user.getId();

        doNothing().when(this.userService).deleteUser(userId);

        ResultActions response = this.mockMvc.perform(delete("/user/" + userId));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", CoreMatchers.is(USER_DELETED_SUCCESSFULLY)));
    }




    @Test
    @WithMockUser(authorities = "invalid:delete")
    public void testDeleteUser_For_InvalidAuthority_Should_Return_200_And_UserDTO() throws Exception
    {
        Long userId = this.user.getId();

        ResultActions response = this.mockMvc.perform(delete("/user/" + userId));

        response.andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", CoreMatchers.is(NOT_ENOUGH_PERMISSION)));
    }




    @Test
    @WithMockUser(authorities = "user:read")
    public void testGetUsersByFullName_For_User_Should_Return_200_And_UserDTOPage() throws Exception
    {
        String fullName = "John Doe";
        int page = 0;
        int size = 5;

        Page<UserDTO> userDTOPage = new PageImpl<>(this.userDTOList.subList(0, size));
        when(this.userService.getUserByFullNameContaining(fullName, page, size)).thenReturn(userDTOPage);

        ResultActions response = this.mockMvc.perform(get("/user/users/name")
                .param("fullName", fullName)
                .param("page", Integer.toString(page))
                .param("size", Integer.toString(size)));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.size()", CoreMatchers.is(userDTOPage.getContent().size())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements", CoreMatchers.is((int) userDTOPage.getTotalElements())));
    }




    @Test
    @WithMockUser(authorities = "project_leader:read")
    public void testGetUsersByFullName_For_ProjectLeader_Should_Return_200_And_UserDTOPage() throws Exception
    {
        String fullName = "John Doe";
        int page = 0;
        int size = 5;

        Page<UserDTO> userDTOPage = new PageImpl<>(this.userDTOList.subList(0, size));
        when(this.userService.getUserByFullNameContaining(fullName, page, size)).thenReturn(userDTOPage);

        ResultActions response = this.mockMvc.perform(get("/user/users/name")
                .param("fullName", fullName)
                .param("page", Integer.toString(page))
                .param("size", Integer.toString(size)));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.size()", CoreMatchers.is(userDTOPage.getContent().size())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements", CoreMatchers.is((int) userDTOPage.getTotalElements())));
    }




    @Test
    @WithMockUser(authorities = "user:read")
    public void testGetUsersByRole_For_User_Should_Return_200_And_UserDTOPage() throws Exception
    {
        String role = RoleEnum.ROLE_USER.name();
        int page = 0;
        int size = 5;

        Page<UserDTO> userDTOPage = new PageImpl<>(this.userDTOList.subList(0, size));
        when(this.userService.getUsersByRole(RoleEnum.valueOf(role), page, size)).thenReturn(userDTOPage);

        ResultActions response = this.mockMvc.perform(get("/user/users/role")
                .param("role", role)
                .param("page", Integer.toString(page))
                .param("size", Integer.toString(size)));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.size()", CoreMatchers.is(userDTOPage.getContent().size())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements", CoreMatchers.is((int) userDTOPage.getTotalElements())));
    }




    @Test
    @WithMockUser(authorities = "project_leader:read")
    public void testGetUsersByRole_For_ProjectLeader_Should_Return_200_And_UserDTOPage() throws Exception
    {
        String role = RoleEnum.ROLE_USER.name();
        int page = 0;
        int size = 5;

        Page<UserDTO> userDTOPage = new PageImpl<>(this.userDTOList.subList(0, size));
        when(this.userService.getUsersByRole(RoleEnum.valueOf(role), page, size)).thenReturn(userDTOPage);

        ResultActions response = this.mockMvc.perform(get("/user/users/role")
                .param("role", role)
                .param("page", Integer.toString(page))
                .param("size", Integer.toString(size)));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.size()", CoreMatchers.is(userDTOPage.getContent().size())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements", CoreMatchers.is((int) userDTOPage.getTotalElements())));
    }




    @Test
    @WithMockUser(authorities = "project_leader:read")
    public void testGetUsersByRoleAndFullName_Should_Return_200_And_UserDTOPage() throws Exception
    {
        String role = RoleEnum.ROLE_USER.name();
        String fullName = "John Doe";
        int page = 0;
        int size = 5;

        Page<UserDTO> userDTOPage = new PageImpl<>(this.userDTOList.subList(0, size));
        when(this.userService.getUsersByRoleAndFullName(RoleEnum.valueOf(role), fullName, page, size)).thenReturn(userDTOPage);

        ResultActions response = this.mockMvc.perform(get("/user/users/role/name")
                .param("role", role)
                .param("fullName", fullName)
                .param("page", Integer.toString(page))
                .param("size", Integer.toString(size)));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.size()", CoreMatchers.is(userDTOPage.getContent().size())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements", CoreMatchers.is((int) userDTOPage.getTotalElements())));
    }




    @Test
    @WithMockUser(authorities = "project_leader:read")
    public void testGetUsersNotINProject_Should_Return_200_And_UserDTOPage() throws Exception
    {
        Long projectId = 1L;
        int page = 0;
        int size = 5;

        Page<UserDTO> userDTOPage = new PageImpl<>(this.userDTOList.subList(0, size));
        when(this.userService.getUsersNotInProject(projectId, page, size)).thenReturn(userDTOPage);

        ResultActions response = this.mockMvc.perform(get("/user/users/not-participants")
                .param("projectId", projectId.toString())
                .param("page", Integer.toString(page))
                .param("size", Integer.toString(size)));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.size()", CoreMatchers.is(userDTOPage.getContent().size())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements", CoreMatchers.is((int) userDTOPage.getTotalElements())));
    }




    @Test
    @WithMockUser(authorities = "project_leader:read")
    public void testGetUsersByFullNameAndNotINProject_Should_Return_200_And_UserDTOPage() throws Exception
    {
        String fullName = "John Doe";
        Long projectId = 1L;
        int page = 0;
        int size = 5;

        Page<UserDTO> userDTOPage = new PageImpl<>(this.userDTOList.subList(0, size));
        when(this.userService.getUsersByFullNameAndNotInProject(fullName, projectId, page, size)).thenReturn(userDTOPage);

        ResultActions response = this.mockMvc.perform(get("/user/users/name/not-participants")
                .param("fullName", fullName)
                .param("projectId", projectId.toString())
                .param("page", Integer.toString(page))
                .param("size", Integer.toString(size)));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.size()", CoreMatchers.is(userDTOPage.getContent().size())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements", CoreMatchers.is((int) userDTOPage.getTotalElements())));
    }




    @Test
    @WithMockUser(authorities = "project_leader:read")
    public void testGetParticipantsByProjectId_Should_Return_200_And_UserDTOPage() throws Exception
    {
        Long projectId = 1L;
        int page = 0;
        int size = 5;
        String sortOrder = "firstName";
        String ascending = "true";

        Page<UserDTO> userDTOPage = new PageImpl<>(this.userDTOList.subList(0, size));
        when(this.userService.getParticipantsByProjectId(projectId, page, size, sortOrder, Boolean.parseBoolean(ascending))).thenReturn(userDTOPage);

        ResultActions response = this.mockMvc.perform(get("/user/participants")
                .param("projectId", projectId.toString())
                .param("page", Integer.toString(page))
                .param("size", Integer.toString(size))
                .param("sortOrder", sortOrder)
                .param("ascending", ascending));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.size()", CoreMatchers.is(userDTOPage.getContent().size())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements", CoreMatchers.is((int) userDTOPage.getTotalElements())));
    }




    @Test
    @WithMockUser(authorities = "project_leader:read")
    public void testGetParticipantsByFullNameAndProjectId_Should_Return_200_And_UserDTOPage() throws Exception
    {
        String fullName = "John Doe";
        Long projectId = 1L;
        int page = 0;
        int size = 5;

        Page<UserDTO> userDTOPage = new PageImpl<>(this.userDTOList.subList(0, size));
        when(this.userService.getParticipantsByFullNameAndProjectId(fullName, projectId, page, size)).thenReturn(userDTOPage);

        ResultActions response = this.mockMvc.perform(get("/user/participants/name")
                .param("fullName", fullName)
                .param("projectId", projectId.toString())
                .param("page", Integer.toString(page))
                .param("size", Integer.toString(size)));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.size()", CoreMatchers.is(userDTOPage.getContent().size())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements", CoreMatchers.is((int) userDTOPage.getTotalElements())));
    }




    @Test
    @WithMockUser(authorities = "project_leader:read")
    public void testGetParticipantsByProjectIdAndExcludeParticipant_Should_Return_200_And_UserDTOPage() throws Exception
    {
        Long projectId = 1L;
        Long participantId = 1L;
        int page = 0;
        int size = 5;
        String sortOrder = "firstName";
        String ascending = "true";

        Page<UserDTO> userDTOPage = new PageImpl<>(this.userDTOList.subList(0, size));
        when(this.userService.getParticipantsByProjectIdAndExcludeParticipant(projectId, participantId, page, size, sortOrder, Boolean.parseBoolean(ascending))).thenReturn(userDTOPage);

        ResultActions response = this.mockMvc.perform(get("/user/participants/exclude")
                .param("projectId", projectId.toString())
                .param("participantId", participantId.toString())
                .param("page", Integer.toString(page))
                .param("size", Integer.toString(size))
                .param("sortOrder", sortOrder)
                .param("ascending", ascending));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.size()", CoreMatchers.is(userDTOPage.getContent().size())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements", CoreMatchers.is((int) userDTOPage.getTotalElements())));
    }




    @Test
    @WithMockUser(authorities = "user:read")
    public void testGetUser_For_User_Should_Return_200_And_UserDTO() throws Exception
    {
        Long userId = this.user.getId();

        when(this.userService.getUserById(userId)).thenReturn(this.userDTO);

        ResultActions response = this.mockMvc.perform(get("/user/" + userId)
                .param("id", userId.toString()));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", CoreMatchers.is(this.userDTO.id().intValue())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.firstName", CoreMatchers.is(this.userDTO.firstName())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.lastName", CoreMatchers.is(this.userDTO.lastName())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email", CoreMatchers.is(this.userDTO.email())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.speciality", CoreMatchers.is(this.userDTO.speciality())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.profileImageUrl", CoreMatchers.is(this.userDTO.profileImageUrl())));
    }




    @Test
    @WithMockUser(authorities = "project_leader:read")
    public void testGetUser_For_ProjectLeader_Should_Return_200_And_UserDTO() throws Exception
    {
        Long userId = this.user.getId();

        when(this.userService.getUserById(userId)).thenReturn(this.userDTO);

        ResultActions response = this.mockMvc.perform(get("/user/" + userId)
                .param("id", userId.toString()));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", CoreMatchers.is(this.userDTO.id().intValue())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.firstName", CoreMatchers.is(this.userDTO.firstName())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.lastName", CoreMatchers.is(this.userDTO.lastName())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email", CoreMatchers.is(this.userDTO.email())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.speciality", CoreMatchers.is(this.userDTO.speciality())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.profileImageUrl", CoreMatchers.is(this.userDTO.profileImageUrl())));
    }
}
