package com.company.controller;

import com.company.builder.ProjectBuilder;
import com.company.builder.UserBuilder;
import com.company.builder.UserDTOBuilder;
import com.company.dto.ProjectDTO;
import com.company.dto.UserDTO;
import com.company.entity.Project;
import com.company.entity.User;
import com.company.enumeration.RoleEnum;
import com.company.jwt.JwtTokenProvider;
import com.company.jwt.filter.JwtAccessDeniedHandler;
import com.company.jwt.filter.JwtAuthenticationEntryPoint;
import com.company.jwt.filter.JwtAuthorizationFilter;
import com.company.service.ProjectService;
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

import java.util.ArrayList;
import java.util.List;

import static com.company.constant.ExceptionConstant.NOT_ENOUGH_PERMISSION;
import static com.company.constant.ProjectConstant.PARTICIPANTS_ADDED_SUCCESSFULLY;
import static com.company.constant.ProjectConstant.PARTICIPANT_UNASSIGNED_SUCCESSFULLY;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@WebMvcTest(controllers = ProjectController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(SpringExtension.class)
public class ProjectControllerTest
{
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProjectService projectService;

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

    private User projectLeader;
    private UserDTO projectLeaderDTO;
    private ProjectDTO projectDTO;
    private ProjectDTO editedProjectDTO;
    private List<User> participants;
    private List<Project> entitiesList;
    private List<ProjectDTO> projectDTOList;


    @BeforeEach
    public void setUp()
    {
        this.projectLeader = new UserBuilder()
                .withRole(RoleEnum.ROLE_PROJECT_LEADER)
                .build();

        this.projectLeaderDTO = new UserDTOBuilder()
                .build();

        this.projectDTO = new ProjectDTOBuilder()
                .withProjectLeader(this.projectLeaderDTO)
                .build();

        this.editedProjectDTO = new ProjectDTOBuilder()
                .withProjectLeader(this.projectLeaderDTO)
                .withName("PROJECT_2")
                .withDescription("DESCRIPTION_2")
                .build();

        Long participantId = 2L;

        this.participants = new ArrayList<>();
        for (int i = 0; i < 10; i++)
        {
            User user = new UserBuilder()
                    .withId(participantId)
                    .build();

            this.participants.add(user);
            participantId++;
        }

        this.entitiesList = new ArrayList<>();
        this.projectDTOList = new ArrayList<>();

        Long projectId = 1L;
        for (int i = 0; i < 10; i++) {
            Project project = new ProjectBuilder()
                    .withId(projectId)
                    .withProjectLeader(this.projectLeader)
                    .build();

            ProjectDTO projectDTO = new ProjectDTOBuilder()
                    .withId(projectId)
                    .withProjectLeader(this.projectLeaderDTO)
                    .build();

            this.entitiesList.add(project);
            this.projectDTOList.add(projectDTO);

            projectId++;
        }
    }




    @Test
    @WithMockUser(authorities = "project_leader:create")
    public void testAddProject_Should_Return200_And_ProjectDTO() throws Exception
    {
        Long projectLeaderId = this.projectLeader.getId();
        String newName = this.projectDTO.name();
        String newDescription = this.projectDTO.description();

        when(this.projectService.addOrUpdate
                (projectLeaderId, null, newName, newDescription)).thenReturn(this.projectDTO);

        ResultActions response = this.mockMvc.perform(post("/project/" + projectLeaderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(this.projectDTO)));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name", CoreMatchers.is(this.projectDTO.name())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.description", CoreMatchers.is(this.projectDTO.description())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.projectLeader.id", CoreMatchers.is(this.projectDTO.projectLeader().id().intValue())));
    }




    @Test
    @WithMockUser(authorities = "invalid:create")
    public void testAddProject_For_InvalidAuthority_Should_Return200_And_ProjectDTO() throws Exception
    {
        Long projectLeaderId = this.projectLeader.getId();

        ResultActions response = this.mockMvc.perform(post("/project/" + projectLeaderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(this.projectDTO)));

        response.andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", CoreMatchers.is(NOT_ENOUGH_PERMISSION)));
    }




    @Test
    @WithMockUser(authorities = "project_leader:update")
    public void testUpdateProject_Should_Return200_And_ProjectDTO() throws Exception
    {
        Long projectLeaderId = this.projectLeader.getId();
        String currentName = this.projectDTO.name();
        String newName = this.editedProjectDTO.name();
        String newDescription = this.editedProjectDTO.description();

        when(this.projectService.addOrUpdate(projectLeaderId, currentName, newName, newDescription)).thenReturn(this.editedProjectDTO);

        ResultActions response = this.mockMvc.perform(put("/project/" + projectLeaderId)
                .param("currentName", currentName)
                .param("newName", newName)
                .param("newDescription", newDescription));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name", CoreMatchers.is(this.editedProjectDTO.name())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.description", CoreMatchers.is(this.editedProjectDTO.description())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.projectLeader.id", CoreMatchers.is(this.editedProjectDTO.projectLeader().id().intValue())));
    }




    @Test
    @WithMockUser(authorities = "invalid:create")
    public void testUpdateProject_For_InvalidAuthority_Should_Return200_And_ProjectDTO() throws Exception
    {
        Long projectLeaderId = this.projectLeader.getId();

        ResultActions response = this.mockMvc.perform(post("/project/" + projectLeaderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(this.projectDTO)));

        response.andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", CoreMatchers.is(NOT_ENOUGH_PERMISSION)));
    }




    @Test
    @WithMockUser(authorities = "project_leader:delete")
    public void testDeleteProject_Should_Return_200_And_ProjectDTOIfProjectDeleted() throws Exception
    {
        Long projectId = this.projectDTO.id();

        when(this.projectService.deleteProject(projectId)).thenReturn(this.projectDTO);

        ResultActions response = this.mockMvc.perform(delete("/project/" + projectId));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", CoreMatchers.is(this.projectDTO.id().intValue())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name", CoreMatchers.is(this.projectDTO.name())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.description", CoreMatchers.is(this.projectDTO.description())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.projectLeader.id", CoreMatchers.is(this.projectDTO.projectLeader().id().intValue())));
    }




    @Test
    @WithMockUser(authorities = "invalid:delete")
    public void testDeleteProject_For_InvalidAuthority_Should_Return_403() throws Exception
    {
        Long projectId = this.projectDTO.id();

        ResultActions response = this.mockMvc.perform(delete("/project/" + projectId));

        response.andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", CoreMatchers.is(NOT_ENOUGH_PERMISSION)));
    }




    @Test
    @WithMockUser(authorities = "project_leader:update")
    public void testAddParticipants_Should_Return_200_And_MessageIfParticipantsAdded() throws Exception
    {
        Long projectId = this.projectDTO.id();

        doNothing().when(this.projectService).addParticipants(projectId, this.participants);
        ResultActions response = this.mockMvc.perform(put("/project/" + projectId + "/participants")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(this.participants)));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", CoreMatchers.is(PARTICIPANTS_ADDED_SUCCESSFULLY)));
    }




    @Test
    @WithMockUser(authorities = "invalid:update")
    public void testAddParticipants_For_InvalidAuthority_Should_Return_200_And_MessageIfParticipantsAdded() throws Exception
    {
        Long projectId = this.projectDTO.id();

        ResultActions response = this.mockMvc.perform(put("/project/" + projectId + "/participants")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(this.participants)));

        response.andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", CoreMatchers.is(NOT_ENOUGH_PERMISSION)));
    }




    @Test
    @WithMockUser(authorities = "project_leader:update")
    public void testUnassignParticipant_Should_Return_200_And_MessageIfParticipantUnassigned() throws Exception
    {
        Long projectId = this.projectDTO.id();
        Long participantId = this.participants.get(0).getId();

        doNothing().when(this.projectService).unassignParticipant(projectId, participantId);
        ResultActions response = this.mockMvc.perform(delete("/project/" + projectId + "/" + participantId));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", CoreMatchers.is(PARTICIPANT_UNASSIGNED_SUCCESSFULLY)));
    }




    @Test
    @WithMockUser(authorities = "invalid:update")
    public void testUnassignParticipant_For_InvalidAuthority_Should_Return_200_And_MessageIfParticipantUnassigned() throws Exception
    {
        Long projectId = this.projectDTO.id();
        Long participantId = this.participants.get(0).getId();

        ResultActions response = this.mockMvc.perform(delete("/project/" + projectId + "/" + participantId));

        response.andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", CoreMatchers.is(NOT_ENOUGH_PERMISSION)));
    }




    @Test
    @WithMockUser(authorities = "user:read")
    public void testGetProjectsByParticipantId_Should_Return_200_And_ProjectDTOPage() throws Exception
    {
        Long participantId = this.participants.get(0).getId();
        int page = 0;
        int size = 5;
        String sortOrder = "name";
        boolean ascending = true;

        Page<ProjectDTO> projectDTOPage = new PageImpl<>(this.projectDTOList.subList(0, size));
        when(this.projectService.getProjectsByParticipantId(participantId, page, size, sortOrder, ascending)).thenReturn(projectDTOPage);

        ResultActions response = this.mockMvc.perform(get("/project/projects/participant")
                .param("participantId", participantId.toString())
                .param("page", Integer.toString(page))
                .param("size", Integer.toString(size))
                .param("sortOrder", sortOrder)
                .param("ascending", Boolean.toString(ascending)));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.size()", CoreMatchers.is(projectDTOPage.getContent().size())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements", CoreMatchers.is((int) projectDTOPage.getTotalElements())));
    }




    @Test
    @WithMockUser(authorities = "invalid:read")
    public void testGetProjectsByParticipantId_For_InvalidAuthority_Should_Return403() throws Exception
    {
        Long participantId = this.participants.get(0).getId();
        int page = 0;
        int size = 5;
        String sortOrder = "name";
        boolean ascending = true;

        ResultActions response = this.mockMvc.perform(get("/project/projects/participant")
                .param("participantId", participantId.toString())
                .param("page", Integer.toString(page))
                .param("size", Integer.toString(size))
                .param("sortOrder", sortOrder)
                .param("ascending", Boolean.toString(ascending)));

        response.andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", CoreMatchers.is(NOT_ENOUGH_PERMISSION)));
    }




    @Test
    @WithMockUser(authorities = "project_leader:read")
    public void testGetProjectsByProjectLeaderId_Should_Return_200_And_ProjectDTOPage() throws Exception
    {
        Long projectLeaderId = this.projectLeader.getId();
        int page = 0;
        int size = 5;
        String sortOrder = "name";
        boolean ascending = true;

        Page<ProjectDTO> projectDTOPage = new PageImpl<>(this.projectDTOList.subList(0, size));
        when(this.projectService.getProjectsByProjectLeaderId(projectLeaderId, page, size, sortOrder, ascending)).thenReturn(projectDTOPage);

        ResultActions response = this.mockMvc.perform(get("/project/projects/project-leader")
                .param("projectLeaderId", projectLeaderId.toString())
                .param("page", Integer.toString(page))
                .param("size", Integer.toString(size))
                .param("sortOrder", sortOrder)
                .param("ascending", Boolean.toString(ascending)));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.size()", CoreMatchers.is(projectDTOPage.getContent().size())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements", CoreMatchers.is((int) projectDTOPage.getTotalElements())));
    }




    @Test
    @WithMockUser(authorities = "invalid:read")
    public void testGetProjectsByProjectLeaderId_For_InvalidAuthority_Should_Return403() throws Exception
    {
        Long projectLeaderId = this.projectLeader.getId();
        int page = 0;
        int size = 5;
        String sortOrder = "name";
        boolean ascending = true;

        ResultActions response = this.mockMvc.perform(get("/project/projects/project-leader")
                .param("projectLeaderId", projectLeaderId.toString())
                .param("page", Integer.toString(page))
                .param("size", Integer.toString(size))
                .param("sortOrder", sortOrder)
                .param("ascending", Boolean.toString(ascending)));

        response.andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", CoreMatchers.is(NOT_ENOUGH_PERMISSION)));
    }
}
