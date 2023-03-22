package com.company.controller;

import com.company.builder.*;
import com.company.dto.BugDTO;
import com.company.dto.ProjectDTO;
import com.company.dto.UserDTO;
import com.company.entity.Bug;
import com.company.entity.Project;
import com.company.entity.User;
import com.company.enumeration.BugClassification;
import com.company.enumeration.BugSeverity;
import com.company.enumeration.BugStatus;
import com.company.enumeration.RoleEnum;
import com.company.jwt.JwtTokenProvider;
import com.company.jwt.filter.JwtAccessDeniedHandler;
import com.company.jwt.filter.JwtAuthenticationEntryPoint;
import com.company.jwt.filter.JwtAuthorizationFilter;
import com.company.service.BugService;
import com.company.service.UserService;
import com.company.builder.ProjectDTOBuilder;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.company.constant.BugConstant.*;
import static com.company.constant.BugConstant.NOTIFICATION_WAS_SENT;
import static com.company.constant.ExceptionConstant.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@WebMvcTest(controllers = BugController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(SpringExtension.class)
public class BugControllerTest
{
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BugService bugService;

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


    private User creatorUser;
    private UserDTO creatorUserDTO;
    private User projectLeader;
    private UserDTO projectLeaderDto;
    private User assignee;
    private Project project;
    private ProjectDTO projectDTO;
    private List<Bug> entitiesList;
    private Bug bug;
    private BugDTO bugDTO;
    private BugDTO editedBugDTO;
    private List<BugDTO> bugDTOList;

    @BeforeEach
    public void setUp()
    {
        this.creatorUser = new UserBuilder().build();

        this.creatorUserDTO = new UserDTOBuilder().build();

        this.projectLeader = new UserBuilder()
                .withId(2L)
                .withRole(RoleEnum.ROLE_PROJECT_LEADER)
                .build();

        this.projectLeaderDto = new UserDTOBuilder()
                .withId(2L)
                .build();

        this.assignee = new UserBuilder()
                .withId(3L)
                .build();

        this.project = new ProjectBuilder()
                .withProjectLeader(this.projectLeader)
                .build();

        this.projectDTO = new ProjectDTOBuilder()
                .withProjectLeader(this.projectLeaderDto)
                .build();

        this.bug = new BugBuilder()
                .withCreator(this.creatorUser)
                .withProject(this.project)
                .build();

        this.bugDTO = new BugDTOBuilder()
                .withCreator(this.creatorUserDTO)
                .withProject(this.projectDTO)
                .build();

        this.editedBugDTO = new BugDTOBuilder()
                .withCreator(this.creatorUserDTO)
                .withProject(this.projectDTO)
                .withName("BUG_2")
                .withDescription("DESCRIPTION_2")
                .withStatus(BugStatus.FIXED)
                .withClassification(BugClassification.SECURITY)
                .withSeverity(BugSeverity.MEDIUM)
                .build();

        Long bugId = 1L;
        this.entitiesList = new ArrayList<>();
        this.bugDTOList = new ArrayList<>();
        for(int i = 0; i < 10; i++)
        {
            BugDTO dtoItem = new BugDTOBuilder()
                    .withId(bugId)
                    .withCreator(this.creatorUserDTO)
                    .withProject(this.projectDTO)
                    .build();

            Bug bugItem = new BugBuilder().withId(bugId)
                    .withCreator(this.creatorUser)
                    .withProject(this.project)
                    .build();

            this.bugDTOList.add(dtoItem);
            this.entitiesList.add(bugItem);

            bugId++;
        }
    }


    @Test
    @WithMockUser(authorities = "user:create")
    public void testAddBug_For_User_Should_Return200_And_BugDTO() throws Exception
    {
        Long creatorId = this.creatorUser.getId();
        Long projectId = this.project.getId();
        String newName = this.bugDTO.name();
        String newDescription = this.bugDTO.description();
        String classification = this.bugDTO.classification();
        String severity = this.bugDTO.severity();

        when(this.bugService.addOrUpdate
                (
                        creatorId, null, projectId, null, newName, newDescription,
                        BugClassification.valueOf(classification),
                        BugStatus.NEW,
                        BugSeverity.valueOf(severity)
                )).thenReturn(this.bugDTO);

        ResultActions response = this.mockMvc.perform(post("/bug/" + creatorId + "/" + projectId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(this.bugDTO)));


        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name", CoreMatchers.is(newName)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.description", CoreMatchers.is(newDescription)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.project.id", CoreMatchers.is(projectId.intValue())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.classification", CoreMatchers.is(classification)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status", CoreMatchers.is(BugStatus.NEW.name())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.severity", CoreMatchers.is(severity)));
    }


    @Test
    @WithMockUser(authorities = "project_leader:create")
    public void testAddBug_For_ProjectLeader_Should_Return200_And_BugDTO() throws Exception
    {
        Long creatorId = this.creatorUser.getId();
        Long projectId = this.project.getId();
        String newName = this.bugDTO.name();
        String newDescription = this.bugDTO.description();
        String classification = this.bugDTO.classification();
        String severity = this.bugDTO.severity();

        when(this.bugService.addOrUpdate
                (
                        creatorId, null, projectId, null, newName, newDescription,
                        BugClassification.valueOf(classification),
                        BugStatus.NEW,
                        BugSeverity.valueOf(severity)
                )).thenReturn(this.bugDTO);

        ResultActions response = this.mockMvc.perform(post("/bug/" + creatorId + "/" + projectId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(this.bugDTO)));


        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name", CoreMatchers.is(newName)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.description", CoreMatchers.is(newDescription)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.project.id", CoreMatchers.is(projectId.intValue())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.classification", CoreMatchers.is(classification)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status", CoreMatchers.is(BugStatus.NEW.name())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.severity", CoreMatchers.is(severity)));
    }


    @Test
    @WithMockUser(authorities = "invalid:create")
    public void testAddBug_For_Invalid_Should_Return403() throws Exception
    {
        Long creatorId = this.creatorUser.getId();
        Long projectId = this.project.getId();

        ResultActions response = this.mockMvc.perform(post("/bug/" + creatorId + "/" + projectId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(this.bugDTO)));


        response.andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", CoreMatchers.is(NOT_ENOUGH_PERMISSION)));
    }


    @Test
    @WithMockUser(authorities = "user:update")
    public void testUpdateBug_For_User_Should_Return200_And_BugDTO() throws Exception
    {
        Long editorId = this.creatorUser.getId();
        Long projectId = this.project.getId();
        Long currentBugId = this.bugDTO.id();
        String newName = this.editedBugDTO.name();
        String newDescription = this.editedBugDTO.description();
        String classification = this.editedBugDTO.classification();
        String status = this.editedBugDTO.status();
        String severity = this.editedBugDTO.severity();

        when(this.bugService.addOrUpdate
                (
                        null, editorId, projectId, currentBugId, newName, newDescription,
                        BugClassification.valueOf(classification),
                        BugStatus.valueOf(status),
                        BugSeverity.valueOf(severity)
                )).thenReturn(this.editedBugDTO);

        ResultActions response = this.mockMvc.perform(put("/bug/" + currentBugId + "/" + editorId + "/" + projectId)
                .param("name", newName)
                .param("classification", classification)
                .param("status", status)
                .param("severity", severity)
                .param("description", newDescription)
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(this.editedBugDTO)));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name", CoreMatchers.is(newName)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.description", CoreMatchers.is(newDescription)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.project.id", CoreMatchers.is(projectId.intValue())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.classification", CoreMatchers.is(classification)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status", CoreMatchers.is(status)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.severity", CoreMatchers.is(severity)));
    }



    @Test
    @WithMockUser(authorities = "project_leader:update")
    public void testUpdateBug_For_ProjectLeader_Should_Return200_And_BugDTO() throws Exception
    {
        Long editorId = this.projectLeader.getId();
        Long projectId = this.project.getId();
        Long currentBugId = this.bugDTO.id();
        String newName = this.editedBugDTO.name();
        String newDescription = this.editedBugDTO.description();
        String classification = this.editedBugDTO.classification();
        String status = this.editedBugDTO.status();
        String severity = this.editedBugDTO.severity();

        when(this.bugService.addOrUpdate
                (
                        null, editorId, projectId, currentBugId, newName, newDescription,
                        BugClassification.valueOf(classification),
                        BugStatus.valueOf(status),
                        BugSeverity.valueOf(severity)
                )).thenReturn(this.editedBugDTO);

        ResultActions response = this.mockMvc.perform(put("/bug/" + currentBugId + "/" + editorId + "/" + projectId)
                .param("name", newName)
                .param("classification", classification)
                .param("status", status)
                .param("severity", severity)
                .param("description", newDescription)
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(this.editedBugDTO)));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name", CoreMatchers.is(newName)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.description", CoreMatchers.is(newDescription)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.project.id", CoreMatchers.is(projectId.intValue())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.classification", CoreMatchers.is(classification)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status", CoreMatchers.is(status)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.severity", CoreMatchers.is(severity)));
    }



    @Test
    @WithMockUser(authorities = "invalid:update")
    public void testUpdateBug_For_Invalid_Should_Return_403() throws Exception
    {
        Long editorId = this.projectLeader.getId();
        Long projectId = this.project.getId();
        Long currentBugId = this.bugDTO.id();
        String newName = this.editedBugDTO.name();
        String newDescription = this.editedBugDTO.description();
        String classification = this.editedBugDTO.classification();
        String status = this.editedBugDTO.status();
        String severity = this.editedBugDTO.severity();

        ResultActions response = this.mockMvc.perform(put("/bug/" + currentBugId + "/" + editorId + "/" + projectId)
                .param("name", newName)
                .param("classification", classification)
                .param("status", status)
                .param("severity", severity)
                .param("description", newDescription)
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(this.editedBugDTO)));

        response.andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", CoreMatchers.is(NOT_ENOUGH_PERMISSION)));
    }



    @Test
    @WithMockUser(authorities = "user:delete")
    public void testDeleteBug_For_User_Should_Return_200_And_MessageIfBugDeleted() throws Exception
    {
        Long bugId = this.bugDTO.id();
        Long deleterId = this.creatorUser.getId();

        when(this.bugService.deleteBug(bugId, deleterId)).thenReturn(this.bugDTO);

        ResultActions response = this.mockMvc.perform(delete("/bug/" + bugId + "/" + deleterId));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", CoreMatchers.is(BUG + this.bugDTO.name() + DELETED_SUCCESSFULLY)));
    }



    @Test
    @WithMockUser(authorities = "project_leader:delete")
    public void testDeleteBug_For_ProjectLeader_Should_Return_200_And_MessageIfBugDeleted() throws Exception
    {
        Long bugId = this.bugDTO.id();
        Long deleterId = this.projectLeader.getId();

        when(this.bugService.deleteBug(bugId, deleterId)).thenReturn(this.bugDTO);

        ResultActions response = this.mockMvc.perform(delete("/bug/" + bugId + "/" + deleterId));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", CoreMatchers.is(BUG + this.bugDTO.name() + DELETED_SUCCESSFULLY)));
    }



    @Test
    @WithMockUser(authorities = "invalid:delete")
    public void testDeleteBug_For_Invalid_Should_Return_403() throws Exception
    {
        Long bugId = this.bugDTO.id();
        Long deleterId = this.projectLeader.getId();

        ResultActions response = this.mockMvc.perform(delete("/bug/" + bugId + "/" + deleterId));

        response.andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", CoreMatchers.is(NOT_ENOUGH_PERMISSION)));
    }



    @Test
    @WithMockUser(authorities = "project_leader:update")
    public void testAssignUserToBug_Should_Return_200_And_MessageIfUserAssigned() throws Exception
    {
        Long bugId = this.bugDTO.id();
        Long assigneeId = this.assignee.getId();

        doNothing().when(this.bugService).setAssignee(bugId, assigneeId);

        ResultActions response = this.mockMvc.perform(put("/bug/" + bugId + "/" + assigneeId + "/assignee"));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", CoreMatchers.is(WORKER_SUCCESSFULLY_ASSIGNED + NOTIFICATION_WAS_SENT)));
    }



    @Test
    @WithMockUser(authorities = "user:update")
    public void testAssignUserToBug_For_User_Should_Return_403() throws Exception
    {
        Long bugId = this.bugDTO.id();
        Long assigneeId = this.assignee.getId();

        ResultActions response = this.mockMvc.perform(put("/bug/" + bugId + "/" + assigneeId + "/assignee"));

        response.andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", CoreMatchers.is(NOT_ENOUGH_PERMISSION)));
    }



    @Test
    @WithMockUser(authorities = "invalid:update")
    public void testAssignUserToBug_For_Invalid_Should_Return_403() throws Exception
    {
        Long bugId = this.bugDTO.id();
        Long assigneeId = this.assignee.getId();

        ResultActions response = this.mockMvc.perform(put("/bug/" + bugId + "/" + assigneeId + "/assignee"));

        response.andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", CoreMatchers.is(NOT_ENOUGH_PERMISSION)));
    }



    @Test
    @WithMockUser(authorities = "project_leader:update")
    public void testUnassignUserToBug_Should_Return_200_And_MessageIfUserUnassigned() throws Exception
    {
        Long bugId = this.bugDTO.id();

        doNothing().when(this.bugService).unassignWorkerFromBug(bugId);

        ResultActions response = this.mockMvc.perform(delete("/bug/" + bugId + "/assignee"));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", CoreMatchers.is(WORKER_SUCCESSFULLY_UNASSIGNED + NOTIFICATION_WAS_SENT)));
    }



    @Test
    @WithMockUser(authorities = "user:update")
    public void testUnassignUserToBug_For_User_Should_Return_403() throws Exception
    {
        Long bugId = this.bugDTO.id();

        ResultActions response = this.mockMvc.perform(delete("/bug/" + bugId + "/assignee"));

        response.andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", CoreMatchers.is(NOT_ENOUGH_PERMISSION)));
    }



    @Test
    @WithMockUser(authorities = "invalid:update")
    public void testUnassignUserFromBug_For_Invalid_Should_Return_403() throws Exception
    {
        Long bugId = this.bugDTO.id();

        ResultActions response = this.mockMvc.perform(delete("/bug/" + bugId + "/assignee"));

        response.andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", CoreMatchers.is(NOT_ENOUGH_PERMISSION)));
    }



    @Test
    @WithMockUser(authorities = "user:update")
    public void testSetStatus_For_User_Should_Return_200_And_MessageIfStatusSet() throws Exception
    {
        Long bugId = this.bugDTO.id();
        BugStatus status = BugStatus.FIXED;

        doNothing().when(this.bugService).setStatus(bugId, status);

        ResultActions response = this.mockMvc.perform(put("/bug/" + bugId + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(BugStatus.FIXED.name()));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", CoreMatchers.is(STATUS_SUCCESSFULLY_SET)));
    }



    @Test
    @WithMockUser(authorities = "project_leader:update")
    public void testSetStatus_For_ProjectLeader_Should_Return_200_And_MessageIfStatusSet() throws Exception
    {
        Long bugId = this.bugDTO.id();
        BugStatus status = BugStatus.FIXED;

        doNothing().when(this.bugService).setStatus(bugId, status);

        ResultActions response = this.mockMvc.perform(put("/bug/" + bugId + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(BugStatus.FIXED.name()));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", CoreMatchers.is(STATUS_SUCCESSFULLY_SET)));
    }



    @Test
    @WithMockUser(authorities = "invalid:update")
    public void testSetStatus_For_Invalid_Should_Return_403() throws Exception
    {
        Long bugId = this.bugDTO.id();
        String status = BugStatus.FIXED.name();

        ResultActions response = this.mockMvc.perform(put("/bug/" + bugId + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(status));

        response.andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", CoreMatchers.is(NOT_ENOUGH_PERMISSION)));
    }



    @Test
    @WithMockUser(authorities = "user:update")
    public void testSetStatus_For_InvalidStatus_Should_Return_400() throws Exception
    {
        Long bugId = this.bugDTO.id();
        String status = "INVALID";

        ResultActions response = this.mockMvc.perform(put("/bug/" + bugId + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(status));

        response.andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", CoreMatchers.is(ILLEGAL_ARGUMENT)));
    }



    @Test
    @WithMockUser(authorities = "user:read")
    public void testGetBugById_For_User_Should_Return_200_And_BugDTO() throws Exception
    {
        Long bugId = this.bugDTO.id();

        when(this.bugService.getBugById(bugId)).thenReturn(this.bugDTO);

        ResultActions response = this.mockMvc.perform(get("/bug/" + bugId));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name", CoreMatchers.is(this.bugDTO.name())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.description", CoreMatchers.is(this.bugDTO.description())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.project.id", CoreMatchers.is(this.bugDTO.project().id().intValue())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.classification", CoreMatchers.is(this.bugDTO.classification())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status", CoreMatchers.is(this.bugDTO.status())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.severity", CoreMatchers.is(this.bugDTO.severity())));
    }



    @Test
    @WithMockUser(authorities = "project_leader:read")
    public void testGetBugById_For_ProjectLeader_Should_Return_200_And_BugDTO() throws Exception
    {
        Long bugId = this.bugDTO.id();

        when(this.bugService.getBugById(bugId)).thenReturn(this.bugDTO);

        ResultActions response = this.mockMvc.perform(get("/bug/" + bugId));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name", CoreMatchers.is(this.bugDTO.name())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.description", CoreMatchers.is(this.bugDTO.description())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.project.id", CoreMatchers.is(this.bugDTO.project().id().intValue())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.classification", CoreMatchers.is(this.bugDTO.classification())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status", CoreMatchers.is(this.bugDTO.status())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.severity", CoreMatchers.is(this.bugDTO.severity())));
    }



    @Test
    @WithMockUser(authorities = "invalid:read")
    public void testGetBugById_For_InvalidAuthority_Should_Return_403() throws Exception
    {
        Long bugId = this.bugDTO.id();

        when(this.bugService.getBugById(bugId)).thenReturn(this.bugDTO);

        ResultActions response = this.mockMvc.perform(get("/bug/" + bugId));

        response.andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", CoreMatchers.is(NOT_ENOUGH_PERMISSION)));
    }




    @Test
    @WithMockUser(authorities = "user:read")
    public void testGetBugsByProjectId_For_User_Should_Return_200_And_BugDTOPage() throws Exception
    {
        Long projectId = this.bugDTO.project().id();
        int page = 0;
        int size = 5;
        String sortOrder = "name";
        boolean ascending = true;

        Page<BugDTO> bugDTOPage = new PageImpl<>(this.bugDTOList.subList(0, size));
        when(this.bugService.getBugsByProjectId(projectId, page, size, sortOrder, ascending)).thenReturn(bugDTOPage);

        ResultActions response = this.mockMvc.perform(get("/bug/bugs/project")
                .param("projectId", projectId.toString())
                .param("page", Integer.toString(page))
                .param("size", Integer.toString(size))
                .param("sortOrder", sortOrder)
                .param("ascending", Boolean.toString(ascending)));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.size()", CoreMatchers.is(bugDTOPage.getContent().size())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements", CoreMatchers.is((int) bugDTOPage.getTotalElements())));
    }



    @Test
    @WithMockUser(authorities = "project_leader:read")
    public void testGetBugsByProjectId_For_ProjectLeader_Should_Return_200_And_BugDTOPage() throws Exception
    {
        Long projectId = this.bugDTO.project().id();
        int page = 0;
        int size = 5;
        String sortOrder = "name";
        boolean ascending = true;

        Page<BugDTO> bugDTOPage = new PageImpl<>(this.bugDTOList.subList(0, size));
        when(this.bugService.getBugsByProjectId(projectId, page, size, sortOrder, ascending)).thenReturn(bugDTOPage);

        ResultActions response = this.mockMvc.perform(get("/bug/bugs/project")
                .param("projectId", projectId.toString())
                .param("page", Integer.toString(page))
                .param("size", Integer.toString(size))
                .param("sortOrder", sortOrder)
                .param("ascending", Boolean.toString(ascending)));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.size()", CoreMatchers.is(bugDTOPage.getContent().size())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements", CoreMatchers.is((int) bugDTOPage.getTotalElements())));
    }



    @Test
    @WithMockUser(authorities = "invalid:read")
    public void testGetBugsByProjectId_For_InvalidUser_Should_Return_403() throws Exception
    {
        Long projectId = this.bugDTO.project().id();
        int page = 0;
        int size = 5;
        String sortOrder = "name";
        boolean ascending = true;

        ResultActions response = this.mockMvc.perform(get("/bug/bugs/project")
                .param("projectId", projectId.toString())
                .param("page", Integer.toString(page))
                .param("size", Integer.toString(size))
                .param("sortOrder", sortOrder)
                .param("ascending", Boolean.toString(ascending)));

        response.andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", CoreMatchers.is(NOT_ENOUGH_PERMISSION)));
    }




    @Test
    @WithMockUser(authorities = "user:read")
    public void testGetBugsByCreatorId_For_User_Should_Return_200_And_BugDTOPage() throws Exception
    {
        Long creatorId = this.creatorUser.getId();
        int page = 0;
        int size = 5;
        String sortOrder = "name";
        boolean ascending = true;

        Page<BugDTO> bugDTOPage = new PageImpl<>(this.bugDTOList.subList(0, size));
        when(this.bugService.getBugsByCreatorId(creatorId, page, size, sortOrder, ascending)).thenReturn(bugDTOPage);

        ResultActions response = this.mockMvc.perform(get("/bug/bugs/creator")
                .param("creatorId", creatorId.toString())
                .param("page", Integer.toString(page))
                .param("size", Integer.toString(size))
                .param("sortOrder", sortOrder)
                .param("ascending", Boolean.toString(ascending)));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.size()", CoreMatchers.is(bugDTOPage.getContent().size())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements", CoreMatchers.is((int) bugDTOPage.getTotalElements())));
    }




    @Test
    @WithMockUser(authorities = "project_leader:read")
    public void testGetBugsByCreatorId_For_ProjectLeader_Should_Return_200_And_BugDTOPage() throws Exception
    {
        Long creatorId = this.creatorUser.getId();
        int page = 0;
        int size = 5;
        String sortOrder = "name";
        boolean ascending = true;

        Page<BugDTO> bugDTOPage = new PageImpl<>(this.bugDTOList.subList(0, size));
        when(this.bugService.getBugsByCreatorId(creatorId, page, size, sortOrder, ascending)).thenReturn(bugDTOPage);

        ResultActions response = this.mockMvc.perform(get("/bug/bugs/creator")
                .param("creatorId", creatorId.toString())
                .param("page", Integer.toString(page))
                .param("size", Integer.toString(size))
                .param("sortOrder", sortOrder)
                .param("ascending", Boolean.toString(ascending)));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.size()", CoreMatchers.is(bugDTOPage.getContent().size())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements", CoreMatchers.is((int) bugDTOPage.getTotalElements())));
    }



    @Test
    @WithMockUser(authorities = "invalid:read")
    public void testGetBugsByCreatorId_For_InvalidUser_Should_Return_403() throws Exception
    {
        Long creatorId = this.creatorUser.getId();
        int page = 0;
        int size = 5;
        String sortOrder = "name";
        boolean ascending = true;

        ResultActions response = this.mockMvc.perform(get("/bug/bugs/creator")
                .param("creatorId", creatorId.toString())
                .param("page", Integer.toString(page))
                .param("size", Integer.toString(size))
                .param("sortOrder", sortOrder)
                .param("ascending", Boolean.toString(ascending)));

        response.andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", CoreMatchers.is(NOT_ENOUGH_PERMISSION)));
    }




    @Test
    @WithMockUser(authorities = "user:read")
    public void testGetBugsByAssigneeId_Should_Return_200_And_BugDTOPage() throws Exception
    {
        Long assigneeId = this.assignee.getId();
        int page = 0;
        int size = 5;
        String sortOrder = "name";
        boolean ascending = true;

        Page<BugDTO> bugDTOPage = new PageImpl<>(this.bugDTOList.subList(0, size));
        when(this.bugService.getBugsByAssigneeId(assigneeId, page, size, sortOrder, ascending)).thenReturn(bugDTOPage);

        ResultActions response = this.mockMvc.perform(get("/bug/bugs/assignee")
                .param("assigneeId", assigneeId.toString())
                .param("page", Integer.toString(page))
                .param("size", Integer.toString(size))
                .param("sortOrder", sortOrder)
                .param("ascending", Boolean.toString(ascending)));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.size()", CoreMatchers.is(bugDTOPage.getContent().size())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements", CoreMatchers.is((int) bugDTOPage.getTotalElements())));
    }



    @Test
    @WithMockUser(authorities = "invalid:read")
    public void testGetBugsByAssigneeId_For_InvalidAuthority_Should_Return_403() throws Exception
    {
        Long assigneeId = this.assignee.getId();
        int page = 0;
        int size = 5;
        String sortOrder = "name";
        boolean ascending = true;

        ResultActions response = this.mockMvc.perform(get("/bug/bugs/assignee")
                .param("assigneeId", assigneeId.toString())
                .param("page", Integer.toString(page))
                .param("size", Integer.toString(size))
                .param("sortOrder", sortOrder)
                .param("ascending", Boolean.toString(ascending)));

        response.andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", CoreMatchers.is(NOT_ENOUGH_PERMISSION)));
    }




    @Test
    @WithMockUser(authorities = "project_leader:read")
    public void testGetBugsByProjectLeaderId_Should_Return_200_And_BugDTOPage() throws Exception
    {
        Long projectLeaderId = this.projectLeader.getId();
        int page = 0;
        int size = 5;
        String sortOrder = "name";
        boolean ascending = true;

        Page<BugDTO> bugDTOPage = new PageImpl<>(this.bugDTOList.subList(0, size));
        when(this.bugService.getBugsByProjectLeaderId(projectLeaderId, page, size, sortOrder, ascending)).thenReturn(bugDTOPage);

        ResultActions response = this.mockMvc.perform(get("/bug/bugs/project-leader")
                .param("projectLeaderId", projectLeaderId.toString())
                .param("page", Integer.toString(page))
                .param("size", Integer.toString(size))
                .param("sortOrder", sortOrder)
                .param("ascending", Boolean.toString(ascending)));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.size()", CoreMatchers.is(bugDTOPage.getContent().size())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements", CoreMatchers.is((int) bugDTOPage.getTotalElements())));
    }



    @Test
    @WithMockUser(authorities = "invalid:read")
    public void testGetBugsByProjectLeaderId_For_InvalidAuthority_Should_Return_403() throws Exception
    {
        Long projectLeaderId = this.projectLeader.getId();
        int page = 0;
        int size = 5;
        String sortOrder = "name";
        boolean ascending = true;

        ResultActions response = this.mockMvc.perform(get("/bug/bugs/project-leader")
                .param("projectLeaderId", projectLeaderId.toString())
                .param("page", Integer.toString(page))
                .param("size", Integer.toString(size))
                .param("sortOrder", sortOrder)
                .param("ascending", Boolean.toString(ascending)));

        response.andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", CoreMatchers.is(NOT_ENOUGH_PERMISSION)));
    }




    @Test
    @WithMockUser(authorities = "user:read")
    public void testGetCount_For_User_Should_Return200_And_Count() throws Exception
    {
        when(this.bugService.getCount()).thenReturn(10L);

        ResultActions response = this.mockMvc.perform(get("/bug/count"));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", CoreMatchers.is(10)));
    }




    @Test
    @WithMockUser(authorities = "project_leader:read")
    public void testGetCount_For_ProjectLeader_Should_Return200_And_Count() throws Exception
    {
        when(this.bugService.getCount()).thenReturn(10L);

        ResultActions response = this.mockMvc.perform(get("/bug/count"));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", CoreMatchers.is(10)));
    }




    @Test
    @WithMockUser(authorities = "invalid:read")
    public void testGetCount_For_InvalidAuthority_Should_Return_403() throws Exception
    {
        when(this.bugService.getCount()).thenReturn(10L);

        ResultActions response = this.mockMvc.perform(get("/bug/count"));

        response.andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", CoreMatchers.is(NOT_ENOUGH_PERMISSION)));
    }




    @Test
    @WithMockUser(authorities = "user:read")
    public void testGetCountByCreator_For_User_Should_Return200_And_Count() throws Exception
    {
        Long creatorId = this.creatorUser.getId();

        when(this.bugService.getCountByCreatorId(creatorId)).thenReturn(10L);

        ResultActions response = this.mockMvc.perform(get("/bug/count/creator/" + creatorId));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", CoreMatchers.is(10)));
    }




    @Test
    @WithMockUser(authorities = "invalid:read")
    public void testGetCountByCreator_For_InvalidAuthority_Should_Return_403() throws Exception
    {
        Long creatorId = this.creatorUser.getId();

        when(this.bugService.getCountByCreatorId(creatorId)).thenReturn(10L);

        ResultActions response = this.mockMvc.perform(get("/bug/count"));

        response.andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", CoreMatchers.is(NOT_ENOUGH_PERMISSION)));
    }




    @Test
    @WithMockUser(authorities = "project_leader:read")
    public void testGetCountByProjectLeader_Should_Return200_And_Count() throws Exception
    {
        Long projectLeaderId = this.projectLeader.getId();

        when(this.bugService.getCountByProjectLeaderId(projectLeaderId)).thenReturn(10L);

        ResultActions response = this.mockMvc.perform(get("/bug/count/project-leader/" + projectLeaderId));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", CoreMatchers.is(10)));
    }




    @Test
    @WithMockUser(authorities = "invalid:read")
    public void testGetCountByProjectLeader_For_InvalidAuthority_Should_Return_403() throws Exception
    {
        Long projectLeaderId = this.projectLeader.getId();

        when(this.bugService.getCountByProjectLeaderId(projectLeaderId)).thenReturn(10L);

        ResultActions response = this.mockMvc.perform(get("/bug/count/project-leader/" + projectLeaderId));

        response.andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", CoreMatchers.is(NOT_ENOUGH_PERMISSION)));
    }




    @Test
    @WithMockUser(authorities = "user:read")
    public void testGetCountCreatedAfter_For_User_Should_Return200_And_Count() throws Exception
    {
        int days = 30;

        when(this.bugService.getCountByCreationDateAfter(any(LocalDateTime.class))).thenReturn(10L);
        ResultActions response = this.mockMvc.perform(get("/bug/count/" +  days + "/ago"));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", CoreMatchers.is(10)));
    }




    @Test
    @WithMockUser(authorities = "project_leader:read")
    public void testGetCountCreatedAfter_For_ProjectLeader_Should_Return200_And_Count() throws Exception
    {
        int days = 30;

        when(this.bugService.getCountByCreationDateAfter(any(LocalDateTime.class))).thenReturn(10L);
        ResultActions response = this.mockMvc.perform(get("/bug/count/" +  days + "/ago"));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", CoreMatchers.is(10)));
    }




    @Test
    @WithMockUser(authorities = "invalid:read")
    public void testGetCountCreatedAfter_For_InvalidAuthority_Should_Return_403() throws Exception
    {
        int days = 30;

        when(this.bugService.getCountByCreationDateAfter(any(LocalDateTime.class))).thenReturn(10L);
        ResultActions response = this.mockMvc.perform(get("/bug/count/" +  days + "/ago"));

        response.andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", CoreMatchers.is(NOT_ENOUGH_PERMISSION)));
    }



    @Test
    @WithMockUser(authorities = "user:read")
    public void testGetCountByCreatorAndCreatedAfter_Should_Return200_And_Count() throws Exception
    {
        Long creatorId = this.creatorUser.getId();
        int days = 30;

        when(this.bugService.getCountByCreatorIdAndByCreationDateAfter(any(Long.class), any(LocalDateTime.class))).thenReturn(10L);
        ResultActions response = this.mockMvc.perform(get("/bug/count/creator/date/")
                .param("creatorId", creatorId.toString())
                .param("days", String.valueOf(days)));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", CoreMatchers.is(10)));
    }




    @Test
    @WithMockUser(authorities = "invalid:read")
    public void testGetCountByCreatorAndCreatedAfter_For_InvalidAuthority_Should_Return_403() throws Exception
    {
        Long creatorId = this.creatorUser.getId();
        int days = 30;

        ResultActions response = this.mockMvc.perform(get("/bug/count/creator/date/")
                .param("creatorId", creatorId.toString())
                .param("days", String.valueOf(days)));

        response.andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", CoreMatchers.is(NOT_ENOUGH_PERMISSION)));
    }




    @Test
    @WithMockUser(authorities = "project_leader:read")
    public void testGetCountByProjectLeaderAndCreatedAfter_Should_Return200_And_Count() throws Exception
    {
        Long projectLeaderId = this.projectLeader.getId();
        int days = 30;

        when(this.bugService.getCountByProjectLeaderIdAndCreationDateAfter(any(Long.class), any(LocalDateTime.class))).thenReturn(10L);
        ResultActions response = this.mockMvc.perform(get("/bug/count/project-leader/date/")
                .param("projectLeaderId", projectLeaderId.toString())
                .param("days", String.valueOf(days)));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", CoreMatchers.is(10)));
    }




    @Test
    @WithMockUser(authorities = "invalid:read")
    public void testGetCountByProjectLeaderAndCreatedAfter_For_InvalidAuthority_Should_Return_403() throws Exception
    {
        Long projectLeaderId = this.projectLeader.getId();
        int days = 30;

        ResultActions response = this.mockMvc.perform(get("/bug/count/project-leader/date/")
                .param("projectLeaderId", projectLeaderId.toString())
                .param("days", String.valueOf(days)));

        response.andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", CoreMatchers.is(NOT_ENOUGH_PERMISSION)));
    }




    @Test
    @WithMockUser(authorities = "user:read")
    public void testGetCountByStatus_For_User_Should_Return200_And_Count() throws Exception
    {
        String status = BugStatus.FIXED.name();

        when(this.bugService.getCountByStatus(BugStatus.valueOf(status))).thenReturn(10L);
        ResultActions response = this.mockMvc.perform(get("/bug/count/status/" + status));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", CoreMatchers.is(10)));
    }




    @Test
    @WithMockUser(authorities = "project_leader:read")
    public void testGetCountByStatus_For_ProjectLeader_Should_Return200_And_Count() throws Exception
    {
        String status = BugStatus.FIXED.name();

        when(this.bugService.getCountByStatus(BugStatus.valueOf(status))).thenReturn(10L);
        ResultActions response = this.mockMvc.perform(get("/bug/count/status/" + status));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", CoreMatchers.is(10)));
    }




    @Test
    @WithMockUser(authorities = "invalid:read")
    public void testGetCountByStatus_For_InvalidAuthority_Should_Return_403() throws Exception
    {
        String status = BugStatus.FIXED.name();

        when(this.bugService.getCountByStatus(BugStatus.valueOf(status))).thenReturn(10L);
        ResultActions response = this.mockMvc.perform(get("/bug/count/status/" + status));

        response.andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", CoreMatchers.is(NOT_ENOUGH_PERMISSION)));
    }




    @Test
    @WithMockUser(authorities = "user:read")
    public void testGetCountByStatus_For_InvalidStatus_Should_Return_400() throws Exception
    {
        String status = "INVALID";

        ResultActions response = this.mockMvc.perform(get("/bug/count/status/" + status));

        response.andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", CoreMatchers.is(ILLEGAL_ARGUMENT)));
    }




    @Test
    @WithMockUser(authorities = "user:read")
    public void testGetCountByAssigneeAndStatus_Should_Return200_And_Count() throws Exception
    {
        Long assigneeId = this.assignee.getId();
        String status = BugStatus.FIXED.name();

        when(this.bugService.getCountByAssigneeIdAndByStatus(assigneeId, BugStatus.valueOf(status))).thenReturn(10L);
        ResultActions response = this.mockMvc.perform(get("/bug/count/assignee/status")
                .param("assigneeId", assigneeId.toString())
                .param("status", status));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", CoreMatchers.is(10)));
    }




    @Test
    @WithMockUser(authorities = "invalid:read")
    public void testGetCountByAssigneeStatus_For_InvalidAuthority_Should_Return403() throws Exception
    {
        Long assigneeId = this.assignee.getId();
        String status = BugStatus.FIXED.name();

        ResultActions response = this.mockMvc.perform(get("/bug/count/assignee/status")
                .param("assigneeId", assigneeId.toString())
                .param("status", status));

        response.andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", CoreMatchers.is(NOT_ENOUGH_PERMISSION)));
    }




    @Test
    @WithMockUser(authorities = "user:read")
    public void testGetCountByAssigneeStatus_For_InvalidStatus_Should_Return_400() throws Exception
    {
        Long assigneeId = this.assignee.getId();
        String status = "INVALID";

        ResultActions response = this.mockMvc.perform(get("/bug/count/assignee/status")
                .param("assigneeId", assigneeId.toString())
                .param("status", status));

        response.andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", CoreMatchers.is(ILLEGAL_ARGUMENT)));
    }




    @Test
    @WithMockUser(authorities = "project_leader:read")
    public void testGetCountByProjectLeaderAndStatus_Should_Return200_And_Count() throws Exception
    {
        Long projectLeaderId = this.projectLeader.getId();
        String status = BugStatus.FIXED.name();

        when(this.bugService.getCountByProjectLeaderIdAndStatus(projectLeaderId, BugStatus.valueOf(status))).thenReturn(10L);
        ResultActions response = this.mockMvc.perform(get("/bug/count/project-leader/status")
                .param("projectLeaderId", projectLeaderId.toString())
                .param("status", status));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", CoreMatchers.is(10)));
    }




    @Test
    @WithMockUser(authorities = "invalid:read")
    public void testGetCountByProjectLeaderAndStatus_For_InvalidAuthority_403() throws Exception
    {
        Long assigneeId = this.assignee.getId();
        String status = BugStatus.FIXED.name();

        ResultActions response = this.mockMvc.perform(get("/bug/count/project-leader/status")
                .param("projectLeaderId", assigneeId.toString())
                .param("status", status));

        response.andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", CoreMatchers.is(NOT_ENOUGH_PERMISSION)));
    }




    @Test
    @WithMockUser(authorities = "project_leader:read")
    public void testGetCountByProjectLeaderAndStatus_For_InvalidStatus_Should_Return_400() throws Exception
    {
        Long projectLeaderId = this.projectLeader.getId();
        String status = "INVALID";

        ResultActions response = this.mockMvc.perform(get("/bug/count/project-leader/status")
                .param("projectLeaderId", projectLeaderId.toString())
                .param("status", status));

        response.andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", CoreMatchers.is(ILLEGAL_ARGUMENT)));
    }
}
