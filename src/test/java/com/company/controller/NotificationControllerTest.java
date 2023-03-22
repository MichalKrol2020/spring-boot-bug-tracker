package com.company.controller;

import com.company.builder.NotificationDTOBuilder;
import com.company.builder.UserBuilder;
import com.company.builder.UserDTOBuilder;
import com.company.dto.NotificationDTO;
import com.company.dto.UserDTO;
import com.company.entity.User;
import com.company.exception.NotificationNotFoundException;
import com.company.jwt.JwtTokenProvider;
import com.company.jwt.filter.JwtAccessDeniedHandler;
import com.company.jwt.filter.JwtAuthenticationEntryPoint;
import com.company.jwt.filter.JwtAuthorizationFilter;
import com.company.service.NotificationService;
import com.company.service.UserService;
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
import static com.company.constant.NotificationConstant.NOTIFICATION_NOT_FOUND;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

@WebMvcTest(controllers = NotificationController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(SpringExtension.class)
public class NotificationControllerTest
{
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

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

    private User sender;
    private User receiver;
    private NotificationDTO notificationDTO;
    private List<NotificationDTO> notificationDTOList;


    @BeforeEach
    public void setUp()
    {
        this.sender = new UserBuilder()
                .build();

        this.receiver = new UserBuilder()
                .withId(2L)
                .build();

        UserDTO senderDTO = new UserDTOBuilder()
                .build();

        UserDTO receiverDTO = new UserDTOBuilder()
                .withId(2L)
                .build();

        this.notificationDTO = new NotificationDTOBuilder()
                .withSender(senderDTO)
                .withReceiver(receiverDTO)
                .build();

        Long notificationId = 1L;

        this.notificationDTOList = new ArrayList<>();
        for(int i = 0; i < 10; i++)
        {
            NotificationDTO dtoItem = new NotificationDTOBuilder()
                    .withId(notificationId)
                    .withSender(senderDTO)
                    .withReceiver(receiverDTO)
                    .build();

            this.notificationDTOList.add(dtoItem);

            notificationId++;
        }
    }

    @Test
    @WithMockUser(authorities = "user:read")
    public void testGetNotificationsByReceiverId_For_User_Should_Return200_And_NotificationDTOPage() throws Exception
    {
        Long receiverId = this.receiver.getId();
        int page = 0;
        int size = 5;

        Page<NotificationDTO> notificationDTOPage = new PageImpl<>(this.notificationDTOList.subList(0, size));
        when(this.notificationService.getNotificationsByReceiverId(receiverId, page, size)).thenReturn(notificationDTOPage);

        ResultActions response = this.mockMvc.perform(get("/notification")
                .param("receiverId", "2")
                .param("page", "0")
                .param("size", "5")
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.size()", CoreMatchers.is(notificationDTOPage.getContent().size())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements", CoreMatchers.is((int) notificationDTOPage.getTotalElements())));
    }


    @Test
    @WithMockUser(authorities = "project_leader:read")
    public void testGetNotificationsByReceiverId_For_ProjectLeader_Should_Return200_And_NotificationDTOPage() throws Exception
    {
        Long receiverId = this.receiver.getId();
        int page = 0;
        int size = 5;

        Page<NotificationDTO> notificationDTOPage = new PageImpl<>(this.notificationDTOList.subList(0, size));
        when(this.notificationService.getNotificationsByReceiverId(receiverId, page, size)).thenReturn(notificationDTOPage);

        ResultActions response = this.mockMvc.perform(get("/notification")
                .param("receiverId", "2")
                .param("page", "0")
                .param("size", "5")
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.size()", CoreMatchers.is(notificationDTOPage.getContent().size())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements", CoreMatchers.is((int) notificationDTOPage.getTotalElements())));
    }



    @Test
    @WithMockUser(authorities = "invalid:read")
    public void testGetNotificationsByReceiverId_For_InvalidAuthority_Should_Return403() throws Exception
    {
        Long receiverId = this.receiver.getId();
        int page = 0;
        int size = 5;

        Page<NotificationDTO> notificationDTOPage = new PageImpl<>(this.notificationDTOList.subList(0, size));
        when(this.notificationService.getNotificationsByReceiverId(receiverId, page, size)).thenReturn(notificationDTOPage);

        ResultActions response = this.mockMvc.perform(get("/notification")
                .param("receiverId", "2")
                .param("page", "0")
                .param("size", "5")
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", CoreMatchers.is(NOT_ENOUGH_PERMISSION)));
    }


    @Test
    @WithMockUser(authorities = "user:read")
    public void testGetNotificationById_For_User_Should_Return200_And_NotificationDTO() throws Exception
    {
        Long notificationId = this.notificationDTO.id();

        when(this.notificationService.getNotificationById(notificationId)).thenReturn(this.notificationDTO);

        ResultActions response = this.mockMvc.perform(get("/notification/" + notificationId)
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", CoreMatchers.is(this.notificationDTO.id().intValue())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.title", CoreMatchers.is(this.notificationDTO.title())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.description", CoreMatchers.is(this.notificationDTO.description())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.sender.id", CoreMatchers.is(this.sender.getId().intValue())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.receiver.id", CoreMatchers.is(this.receiver.getId().intValue())));
    }



    @Test
    @WithMockUser(authorities = "project_leader:read")
    public void testGetNotificationById_For_ProjectLeader_Should_Return200_And_NotificationDTO() throws Exception
    {
        Long notificationId = this.notificationDTO.id();

        when(this.notificationService.getNotificationById(notificationId)).thenReturn(this.notificationDTO);

        ResultActions response = this.mockMvc.perform(get("/notification/" + notificationId)
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", CoreMatchers.is(this.notificationDTO.id().intValue())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.title", CoreMatchers.is(this.notificationDTO.title())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.description", CoreMatchers.is(this.notificationDTO.description())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.sender.id", CoreMatchers.is(this.sender.getId().intValue())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.receiver.id", CoreMatchers.is(this.receiver.getId().intValue())));
    }


    @Test
    @WithMockUser(authorities = "invalid:read")
    public void testGetNotificationById_For_Invalid_Should_Return403() throws Exception
    {
        Long notificationId = this.notificationDTO.id();

        when(this.notificationService.getNotificationById(notificationId)).thenReturn(this.notificationDTO);

        ResultActions response = this.mockMvc.perform(get("/notification/" + notificationId)
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", CoreMatchers.is(NOT_ENOUGH_PERMISSION)));
    }



    @Test
    @WithMockUser(authorities = "user:read")
    public void testGetCountNotificationsUnseen_For_User_Should_Return200_And_Count() throws Exception
    {
        Long receiverId = this.receiver.getId();

        when(this.notificationService.getCountNotificationsUnseen(receiverId)).thenReturn(10);

        ResultActions response = this.mockMvc.perform(get("/notification/2/unseen")
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", CoreMatchers.is(10)));
    }


    @Test
    @WithMockUser(authorities = "project_leader:read")
    public void testGetCountNotificationsUnseen_For_ProjectLeader_Should_Return200_And_Count() throws Exception
    {
        Long receiverId = this.receiver.getId();

        when(this.notificationService.getCountNotificationsUnseen(receiverId)).thenReturn(10);

        ResultActions response = this.mockMvc.perform(get("/notification/2/unseen")
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", CoreMatchers.is(10)));
    }


    @Test
    @WithMockUser(authorities = "invalid:read")
    public void testGetCountNotificationsUnseen_For_Invalid_Should_Return403() throws Exception
    {
        Long receiverId = this.receiver.getId();

        when(this.notificationService.getCountNotificationsUnseen(receiverId)).thenReturn(10);

        ResultActions response = this.mockMvc.perform(get("/notification/2/unseen")
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", CoreMatchers.is(NOT_ENOUGH_PERMISSION)));
    }


    @Test
    @WithMockUser(authorities = "user:read")
    public void testGetIndexOfNotificationRecord_For_User_Should_Return200_And_Index() throws Exception
    {
        Long notificationId = this.notificationDTO.id();
        Long receiverId = this.receiver.getId();

        when(this.notificationService.getIndexOfNotificationRecord(notificationId, receiverId)).thenReturn(10);

        ResultActions response = this.mockMvc.perform(get("/notification/index/" + notificationId + "/" + receiverId)
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", CoreMatchers.is(10)));
    }


    @Test
    @WithMockUser(authorities = "project_leader:read")
    public void testGetIndexOfNotificationRecord_For_ProjectLeader_Should_Return200_And_Index() throws Exception
    {
        Long notificationId = this.notificationDTO.id();
        Long receiverId = this.receiver.getId();

        when(this.notificationService.getIndexOfNotificationRecord(notificationId, receiverId)).thenReturn(10);

        ResultActions response = this.mockMvc.perform(get("/notification/index/" + notificationId + "/" + receiverId)
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", CoreMatchers.is(10)));
    }



    @Test
    @WithMockUser(authorities = "invalid:read")
    public void testGetIndexOfNotificationRecord_For_Invalid_Should_Return403() throws Exception
    {
        Long notificationId = this.notificationDTO.id();
        Long receiverId = this.receiver.getId();

        when(this.notificationService.getIndexOfNotificationRecord(notificationId, receiverId)).thenReturn(10);

        ResultActions response = this.mockMvc.perform(get("/notification/index/" + notificationId + "/" + receiverId)
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", CoreMatchers.is(NOT_ENOUGH_PERMISSION)));
    }



    @Test
    @WithMockUser(authorities = "user:update")
    public void testSetSeen_For_User_Should_SetNotificationSeen_And_Return204NoContent() throws Exception
    {
        Long notificationId = this.notificationDTO.id();

        ResultActions response = this.mockMvc.perform(put("/notification/" + notificationId + "/seen")
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(MockMvcResultMatchers.status().isNoContent());
    }



    @Test
    @WithMockUser(authorities = "project_leader:update")
    public void testSetSeen_For_ProjectLeader_Should_SetNotificationSeen_And_Return204NoContent() throws Exception
    {
        Long notificationId = this.notificationDTO.id();

        ResultActions response = this.mockMvc.perform(put("/notification/" + notificationId + "/seen")
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(MockMvcResultMatchers.status().isNoContent());
    }


    @Test
    @WithMockUser(authorities = "invalid:update")
    public void testSetSeen_For_Invalid_Should_NotSetNotificationSeen_And_Return403() throws Exception
    {
        Long notificationId = this.notificationDTO.id();

        ResultActions response = this.mockMvc.perform(put("/notification/" + notificationId + "/seen")
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", CoreMatchers.is(NOT_ENOUGH_PERMISSION)));
    }



    @Test
    @WithMockUser(authorities = "project_leader:update")
    public void testSetSeen_Should_ThrowException_When_NotificationNotFound() throws Exception
    {
        Long notificationId = 100L;

        doThrow(new NotificationNotFoundException(NOTIFICATION_NOT_FOUND))
                .when(this.notificationService).setSeen(notificationId);

        ResultActions response = this.mockMvc.perform(put("/notification/" + notificationId + "/seen")
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", CoreMatchers.is(NOTIFICATION_NOT_FOUND)));
    }
}
