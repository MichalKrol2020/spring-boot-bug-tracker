package com.company.service;

import com.company.builder.ProjectBuilder;
import com.company.builder.UserBuilder;
import com.company.builder.UserDTOBuilder;
import com.company.dto.ProjectDTO;
import com.company.dto.UserDTO;
import com.company.entity.Project;
import com.company.entity.User;
import com.company.enumeration.RoleEnum;
import com.company.exception.*;
import com.company.repository.ProjectRepository;
import com.company.builder.ProjectDTOBuilder;
import com.company.service.mapper.ProjectDTOMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;

import static com.company.constant.ProjectConstant.*;
import static com.company.constant.ProjectConstant.PROJECT_DOES_NOT_EXIST;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ContextConfiguration(classes = {ProjectServiceImpl.class})
@ExtendWith(SpringExtension.class)
public class ProjectServiceTest
{
    @MockBean
    private ProjectRepository projectRepository;

    @MockBean
    private UserService userService;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private ProjectDTOMapper projectDTOMapper;

    @Captor
    private ArgumentCaptor<Project> projectCaptor;

    @Autowired
    private ProjectService underTest;

    private User projectLeader;
    private UserDTO projectLeaderDTO;
    private Project project;
    private ProjectDTO projectDTO;
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

        this.project = new ProjectBuilder()
                .withProjectLeader(this.projectLeader)
                .build();

        this.projectDTO = new ProjectDTOBuilder()
                .withProjectLeader(this.projectLeaderDTO)
                .build();

        Long participantId = 2L;

        this.participants = new ArrayList<>();
        for(int i = 0; i < 10; i++)
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
        for(int i = 0; i < 10; i++)
        {
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
    public void testAddOrUpdate_ForAdd_Should_ThrowException_When_ProjectLeaderNotFound()
    {
        Long projectLeaderId = 1L;
        String newName = "PROJECT_2";
        String newDescription = "DESCRIPTION_2";

        when(this.userService.getUserEntityById(projectLeaderId)).thenReturn(null);

        Exception exception = assertThrows
                (UserNotFoundException.class, () -> this.underTest.addOrUpdate(projectLeaderId, null, newName, newDescription));
        assertEquals(PROJECT_LEADER_NOT_FOUND, exception.getMessage());
    }



    @Test
    public void testAddOrUpdate_ForAdd_Should_ThrowException_When_ProjectAlreadyExists()
    {
        Long projectLeaderId = 1L;
        String newName = "PROJECT_1";
        String newDescription = "DESCRIPTION_1";

        when(this.projectRepository.findProjectByName(newName)).thenReturn(this.project);

        Exception exception = assertThrows
                (ProjectAlreadyExistsException.class, () -> this.underTest.addOrUpdate(projectLeaderId, null, newName, newDescription));
        assertEquals(PROJECT_ALREADY_EXISTS, exception.getMessage());
    }



    @Test
    public void testAddOrUpdate_ForUpdate_Should_ThrowException_When_DoesNotExist()
    {
        Long projectLeaderId = 1L;
        String currentName = "PROJECT_1";
        String newName = "PROJECT_2";
        String newDescription = "DESCRIPTION_2";

        when(this.userService.getUserEntityById(projectLeaderId)).thenReturn(this.projectLeader);
        when(this.projectRepository.findProjectByName(currentName)).thenReturn(null);

        Exception exception = assertThrows
                (ProjectDoesNotExistException.class, () -> this.underTest.addOrUpdate(projectLeaderId, currentName, newName, newDescription));
        assertEquals(PROJECT_DOES_NOT_EXIST, exception.getMessage());
    }



    @Test
    public void testAddOrUpdate_ForUpdate_Should_ThrowException_When_ProjectAlreadyExists()
    {
        Long projectLeaderId = 1L;
        String currentName = "PROJECT_1";
        String newName = "PROJECT_2";
        String newDescription = "DESCRIPTION_2";

        when(this.userService.getUserEntityById(projectLeaderId)).thenReturn(this.projectLeader);
        when(this.projectRepository.findProjectByName(currentName)).thenReturn(this.project);

        Project projectByNewName = new ProjectBuilder()
                .withId(2L)
                .withName(newName)
                .withDescription(newDescription)
                .withProjectLeader(this.projectLeader)
                .build();

        when(this.projectRepository.findProjectByName(newName)).thenReturn(projectByNewName);

        Exception exception = assertThrows
                (ProjectAlreadyExistsException.class, () -> this.underTest.addOrUpdate(projectLeaderId, currentName, newName, newDescription));
        assertEquals(PROJECT_ALREADY_EXISTS, exception.getMessage());
    }



    @Test
    public void testAddOrUpdate_ForAdd_Should_AddProject() throws UserNotFoundException, ProjectDoesNotExistException, ProjectAlreadyExistsException
    {
        Long projectLeaderId = 1L;
        String newName = "PROJECT_1";
        String newDescription = "DESCRIPTION_1";

        when(this.userService.getUserEntityById(projectLeaderId)).thenReturn(this.projectLeader);
        when(this.projectRepository.findProjectByName(newName)).thenReturn(null);
        when(this.projectDTOMapper.apply(any(Project.class))).thenReturn(this.projectDTO);

        ProjectDTO result = this.underTest.addOrUpdate(projectLeaderId, null, newName, newDescription);
        assertNotNull(result);

        assertEquals(this.projectDTO, result);

        verify(this.projectRepository).save(this.projectCaptor.capture());
        Project savedProject = this.projectCaptor.getValue();
        assertEquals(newName, savedProject.getName());
        assertEquals(newDescription, savedProject.getDescription());
        assertEquals(this.projectLeader, savedProject.getProjectLeader());
    }



    @Test
    public void testAddOrUpdate_ForUpdate_Should_AddProject() throws UserNotFoundException, ProjectDoesNotExistException, ProjectAlreadyExistsException
    {
        Long projectLeaderId = 1L;
        String currentName = "PROJECT_1";
        String newName = "PROJECT_2";
        String newDescription = "DESCRIPTION_2";



        when(this.userService.getUserEntityById(projectLeaderId)).thenReturn(this.projectLeader);
        when(this.projectRepository.findProjectByName(currentName)).thenReturn(this.project);
        when(this.projectRepository.findProjectByName(newName)).thenReturn(null);

        ProjectDTO editedProjectDTO = new ProjectDTOBuilder()
                .withName(newName)
                .withDescription(newDescription)
                .withProjectLeader(this.projectLeaderDTO)
                .build();

        when(this.projectDTOMapper.apply(any(Project.class))).thenReturn(editedProjectDTO);

        ProjectDTO result = this.underTest.addOrUpdate(projectLeaderId, null, newName, newDescription);

        assertNotNull(result);
        assertEquals(editedProjectDTO, result);

        verify(this.projectRepository).save(this.projectCaptor.capture());
        Project savedProject = this.projectCaptor.getValue();
        assertEquals(newName, savedProject.getName());
        assertEquals(newDescription, savedProject.getDescription());
        assertEquals(this.projectLeader, savedProject.getProjectLeader());
    }



    @Test
    public void testDeleteProject_Should_ThrowException_When_ProjectDoesNotExist()
    {
        Long projectId = 1L;

        when(this.projectRepository.findProjectById(projectId)).thenReturn(null);

        Exception exception = assertThrows
                (ProjectDoesNotExistException.class, () -> this.underTest.deleteProject(projectId));
        assertEquals(PROJECT_DOES_NOT_EXIST, exception.getMessage());
    }


    @Test
    public void testDeleteProject_Should_DeleteProject_And_NotifyParticipants() throws UserNotFoundException, ProjectDoesNotExistException
    {
        Long projectId = 1L;

        this.project.setParticipants(this.participants);
        when(this.projectRepository.findProjectById(projectId)).thenReturn(this.project);

        doNothing().when(this.projectRepository).delete(this.project);

        when(this.projectDTOMapper.apply(any(Project.class))).thenReturn(this.projectDTO);
        ProjectDTO result = this.underTest.deleteProject(projectId);

        for(User participant : this.participants)
        {
            verify(this.notificationService).sendNotificationFromTo
                    (this.projectLeader.getId(), participant.getId(),
                            PROJECT_HAS_BEEN_DELETED,
                            PROJECT + project.getName() + UNASSIGNED_AUTOMATICALLY);
        }

        assertEquals(this.projectDTO, result);
    }


    @Test
    public void testAddParticipants_Should_ThrowException_When_ParticipantsListIsNull()
    {
        Long projectId = 1L;

        when(this.projectRepository.findProjectById(projectId)).thenReturn(this.project);

        Exception exception = assertThrows
                (EmptyListException.class, () -> this.underTest.addParticipants(projectId, null));
        assertEquals(NO_PARTICIPANTS_SELECTED, exception.getMessage());
    }


    @Test
    public void testAddParticipants_Should_AddParticipants_And_NotifyParticipants() throws UserNotFoundException, ProjectDoesNotExistException, UserAlreadyAssignedException
    {
        Long projectId = 1L;
        List<User> participants = this.participants;

        when(this.projectRepository.findProjectById(projectId)).thenReturn(this.project);

        assertNull(this.project.getParticipants());

        this.underTest.addParticipants(projectId, participants);

        assertNotNull(this.project.getParticipants());

        verify(this.projectRepository).save(this.projectCaptor.capture());
        Project savedProject = this.projectCaptor.getValue();

        for(int i = 0; i < this.participants.size(); i++)
        {
            User expected = this.participants.get(i);
            User actual = savedProject.getParticipants().get(i);

            assertEquals(expected, actual);

            verify(this.notificationService).sendNotificationFromTo(this.projectLeader.getId(), actual.getId(),
                    NEW_ASSIGNMENT_TO_PROJECT,
                    YOU_VE_BEEN_ASSIGNED_TO + this.project.getName());
        }
    }



    @Test
    public void testUnassignParticipant_Should_ThrowException_When_ProjectDoesNotExist()
    {
        Long projectId = 1L;
        Long participantId = 2L;

        this.project.setParticipants(this.participants);
        when(this.projectRepository.findProjectById(projectId)).thenReturn(null);
        when(this.userService.getUserEntityById(participantId)).thenReturn(this.participants.get(0));

        Exception exception = assertThrows
                (ProjectDoesNotExistException.class, () -> this.underTest.unassignParticipant(projectId, participantId));
        assertEquals(PROJECT_DOES_NOT_EXIST, exception.getMessage());
    }


    @Test
    public void testUnassignParticipant_Should_ThrowException_When_ParticipantDoesNotExist()
    {
        Long projectId = 1L;
        Long participantId = 2L;

        this.project.setParticipants(this.participants);
        when(this.projectRepository.findProjectById(projectId)).thenReturn(this.project);
        when(this.userService.getUserEntityById(participantId)).thenReturn(null);

        Exception exception = assertThrows
                (UserNotFoundException.class, () -> this.underTest.unassignParticipant(projectId, participantId));
        assertEquals(PARTICIPANT_NOT_FOUND, exception.getMessage());
    }


    @Test
    public void testUnassignParticipant_Should_ThrowException_When_NoCurrentParticipants()
    {
        Long projectId = 1L;
        Long participantId = 2L;

        when(this.projectRepository.findProjectById(projectId)).thenReturn(this.project);
        when(this.userService.getUserEntityById(participantId)).thenReturn(this.participants.get(0));

        Exception exception = assertThrows
                (EmptyListException.class, () -> this.underTest.unassignParticipant(projectId, participantId));
        assertEquals(CONTAINS_NO_PARTICIPANTS, exception.getMessage());
    }


    @Test
    public void testUnassignParticipant_Should_ThrowException_When_UserNotParticipant()
    {
        Long projectId = 1L;
        Long participantId = 2L;

        User notParticipant = new UserBuilder()
                .withId(20L)
                .withRole(RoleEnum.ROLE_USER)
                .build();

        this.project.setParticipants(this.participants);
        when(this.projectRepository.findProjectById(projectId)).thenReturn(this.project);
        when(this.userService.getUserEntityById(participantId)).thenReturn(notParticipant);

        Exception exception = assertThrows
                (UserNotAssignedException.class, () -> this.underTest.unassignParticipant(projectId, participantId));
        assertEquals(USER_IS_NOT_ASSIGNED_TO_PROJECT, exception.getMessage());
    }



    @Test
    public void testUnassignParticipant_Should_UnassignParticipant() throws UserNotFoundException, ProjectDoesNotExistException, UserNotAssignedException
    {
        Long projectId = 1L;
        Long participantId = 2L;

        User participantToUnassign = this.participants.get(0);

        this.project.setParticipants(this.participants);
        when(this.projectRepository.findProjectById(projectId)).thenReturn(this.project);
        when(this.userService.getUserEntityById(participantId)).thenReturn(participantToUnassign);

        assertTrue(this.project.getParticipants().contains(participantToUnassign));
        this.underTest.unassignParticipant(projectId, participantId);

        verify(this.projectRepository).save(this.projectCaptor.capture());
        Project savedProject = this.projectCaptor.getValue();
        assertFalse(savedProject.getParticipants().contains(participantToUnassign));

        verify(this.notificationService).sendNotificationFromTo(this.projectLeader.getId(), participantId,
                YOU_VE_BEEN_UNASSIGNED_FROM_PROJECT,
                YOU_VE_BEEN_UNASSIGNED_FROM + project.getName() +
                        PLEASE_CONTACT + projectLeader.getFullName() +
                        FOR_MORE_DETAILS);
    }



    @Test
    public void testGetProjectsByParticipantId_Should_GetProjects()
    {
        Long participantId = 2L;
        int page = 1;
        int size = 5;
        String sortOrder = "name";
        boolean ascending = true;

        Page<Project> entitiesPage = new PageImpl<>(this.entitiesList.subList(0, size));
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortOrder).ascending());

        when(this.projectRepository.findProjectsByParticipantsId(participantId, pageable)).thenReturn(entitiesPage);
        when(this.projectDTOMapper.apply(any(Project.class)))
                .thenReturn(this.projectDTOList.get(0), this.projectDTOList.subList(1, size).toArray(new ProjectDTO[size]));

        Page<ProjectDTO> result = this.underTest.getProjectsByParticipantId(participantId, page, size, sortOrder, ascending);
        assertNotNull(result);

        verify(this.projectRepository).findProjectsByParticipantsId(participantId, pageable);
        verify(this.projectDTOMapper, times(size)).apply(any(Project.class));

        assertEquals(size, result.getContent().size());
        for (int i = 0; i < size; i++)
        {
            ProjectDTO expected = this.projectDTOList.get(i);
            ProjectDTO actual = result.getContent().get(i);
            assertEquals(expected, actual);
        }
    }



    @Test
    public void testGetProjectsByProjectLeaderId_Should_GetProjects()
    {
        Long projectLeaderId = 2L;
        int page = 1;
        int size = 5;
        String sortOrder = "name";
        boolean ascending = true;

        Page<Project> entitiesPage = new PageImpl<>(this.entitiesList.subList(0, size));
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortOrder).ascending());

        when(this.projectRepository.findProjectsByProjectLeaderId(projectLeaderId, pageable)).thenReturn(entitiesPage);
        when(this.projectDTOMapper.apply(any(Project.class)))
                .thenReturn(this.projectDTOList.get(0), this.projectDTOList.subList(1, size).toArray(new ProjectDTO[size]));

        Page<ProjectDTO> result = this.underTest.getProjectsByProjectLeaderId(projectLeaderId, page, size, sortOrder, ascending);
        assertNotNull(result);

        verify(this.projectRepository).findProjectsByProjectLeaderId(projectLeaderId, pageable);
        verify(this.projectDTOMapper, times(size)).apply(any(Project.class));

        assertEquals(size, result.getContent().size());
        for (int i = 0; i < size; i++)
        {
            ProjectDTO expected = this.projectDTOList.get(i);
            ProjectDTO actual = result.getContent().get(i);
            assertEquals(expected, actual);
        }
    }



    @Test
    public void testProjectEntitiesByProjectLeaderId_Should_GetProjects()
    {
        Long projectLeaderId = 2L;

        when(this.projectRepository.findAllByProjectLeaderId(projectLeaderId)).thenReturn(this.entitiesList);

        List<Project> result = this.underTest.getProjectEntitiesByProjectLeaderId(projectLeaderId);
        assertNotNull(result);

        verify(this.projectRepository).findAllByProjectLeaderId(projectLeaderId);

        for (int i = 0; i < this.entitiesList.size(); i++)
        {
            Project expected = this.entitiesList.get(i);
            Project actual = result.get(i);
            assertEquals(expected, actual);
        }
    }



    @Test
    public void testProjectEntitiesByParticipantId_Should_GetProjects()
    {
        Long participantId = 2L;

        when(this.projectRepository.findAllByParticipantsId(participantId)).thenReturn(this.entitiesList);

        List<Project> result = this.underTest.getProjectEntitiesByParticipantId(participantId);
        assertNotNull(result);

        verify(this.projectRepository).findAllByParticipantsId(participantId);

        for (int i = 0; i < this.entitiesList.size(); i++)
        {
            Project expected = this.entitiesList.get(i);
            Project actual = result.get(i);
            assertEquals(expected, actual);
        }
    }



    @Test
    public void testProjectEntityById_Should_ReturnProject()
    {
        Long projectId = 1L;

        when(this.projectRepository.findProjectById(projectId)).thenReturn(this.project);

        Project result = this.underTest.getProjectEntityById(projectId);

        verify(this.projectRepository).findProjectById(projectId);

        assertEquals(this.project, result);
    }
}
