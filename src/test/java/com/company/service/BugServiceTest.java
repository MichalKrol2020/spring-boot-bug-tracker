package com.company.service;

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
import com.company.exception.*;
import com.company.repository.BugRepository;
import com.company.service.mapper.BugDTOMapper;
import com.company.builder.ProjectDTOBuilder;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.company.constant.BugConstant.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ContextConfiguration(classes = {BugServiceImpl.class})
@ExtendWith(SpringExtension.class)
public class BugServiceTest
{
    @MockBean
    private ProjectService projectService;

    @MockBean
    private UserService userService;

    @MockBean
    private BugRepository bugRepository;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private BugDTOMapper bugDTOMapper;

    @Captor
    private ArgumentCaptor<Bug> bugCaptor;

    @Autowired
    private BugService underTest;

    private User creator;
    private UserDTO creatorDTO;
    private User projectLeader;
    private UserDTO projectLeaderDto;
    private User assignee;
    private Project project;
    private ProjectDTO projectDTO;
    private List<Bug> entitiesList;
    private Bug bug;
    private BugDTO bugDTO;
    private List<BugDTO> bugDTOList;

    @BeforeEach
    public void setUp()
    {
        this.creator = new UserBuilder().build();

        this.creatorDTO = new UserDTOBuilder().build();

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
                .withCreator(this.creator)
                .withProject(this.project)
                .build();

        this.bugDTO = new BugDTOBuilder()
                .withCreator(this.creatorDTO)
                .withProject(projectDTO)
                .build();

        Long bugId = 1L;
        this.entitiesList = new ArrayList<>();
        this.bugDTOList = new ArrayList<>();
        for(int i = 0; i < 10; i++)
        {
            BugDTO dtoItem = new BugDTOBuilder()
                    .withId(bugId)
                    .withCreator(this.creatorDTO)
                    .withProject(this.projectDTO)
                    .build();

            Bug bugItem = new BugBuilder().withId(bugId)
                    .withCreator(this.creator)
                    .withProject(this.project)
                    .build();

            this.bugDTOList.add(dtoItem);
            this.entitiesList.add(bugItem);

            bugId++;
        }
    }


    @Test
    public void testAddOrUpdate_Should_ThrowException_When_ProjectDoesNotExist()
    {
        Long creatorId = 1L;
        Long projectId = 3L;
        String newName = "BUG_1";
        String description = "BUG_DESCRIPTION_1";
        BugClassification classification = BugClassification.PERFORMANCE;
        BugStatus status = BugStatus.NEW;
        BugSeverity severity = BugSeverity.HIGH;

        when(this.projectService.getProjectEntityById(projectId)).thenReturn(null);
        when(this.userService.getUserEntityById(creatorId)).thenReturn(this.creator);
        when(this.bugRepository.findByNameAndProjectId(newName, projectId)).thenReturn(null);

        when(this.bugDTOMapper.apply(any(Bug.class))).thenReturn(this.bugDTO);

        Exception exception = assertThrows
                (
                        ProjectDoesNotExistException.class,
                        () -> this.underTest.addOrUpdate(creatorId,
                                null, projectId, null, newName, description, classification, status, severity)
                );
        assertEquals(PROJECT_DOES_NOT_EXIST, exception.getMessage());
    }


    @Test
    public void testAddOrUpdate_ForUpdate_Should_ThrowException_When_BugByCurrentIdDoesNotExist()
    {
        Long editorId = 2L;
        Long projectId = 3L;
        Long currentBugId = 4L;
        String newName = "BUG_1";
        String description = "BUG_DESCRIPTION_1";
        BugClassification classification = BugClassification.PERFORMANCE;
        BugStatus status = BugStatus.NEW;
        BugSeverity severity = BugSeverity.HIGH;

        when(this.projectService.getProjectEntityById(projectId)).thenReturn(this.project);
        when(this.bugRepository.findByNameAndProjectId(newName, projectId)).thenReturn(null);
        when(this.bugRepository.findBugById(currentBugId)).thenReturn(null);

        when(this.bugDTOMapper.apply(any(Bug.class))).thenReturn(this.bugDTO);

        Exception exception = assertThrows
                (
                        BugDoesNotExistException.class,
                        () -> this.underTest.addOrUpdate(null, editorId, projectId, currentBugId, newName, description, classification, status, severity)
                );
        assertEquals(BUG_DOES_NOT_EXIST, exception.getMessage());
    }


    @Test
    public void testAddOrUpdate_ForAdd_Should_ThrowException_When_BugAlreadyExist()
    {
        Long creatorId = 1L;
        Long projectId = 3L;
        String newName = "BUG_1";
        String description = "BUG_DESCRIPTION_1";
        BugClassification classification = BugClassification.PERFORMANCE;
        BugStatus status = BugStatus.NEW;
        BugSeverity severity = BugSeverity.HIGH;

        when(this.projectService.getProjectEntityById(projectId)).thenReturn(this.project);
        when(this.userService.getUserEntityById(creatorId)).thenReturn(this.creator);
        when(this.bugRepository.findByNameAndProjectId(newName, projectId)).thenReturn(this.bug);

        when(this.bugDTOMapper.apply(any(Bug.class))).thenReturn(this.bugDTO);

        Exception exception = assertThrows
                (
                        BugAlreadyExistException.class,
                        () -> this.underTest.addOrUpdate(creatorId, null, projectId, null, newName, description, classification, status, severity)
                );
        assertEquals(BUG_ALREADY_EXISTS, exception.getMessage());
    }


    @Test
    public void testAddOrUpdate_ForUpdate_Should_ThrowException_When_BugAlreadyExist()
    {
        Long editorId = 2L;
        Long projectId = 3L;
        Long currentBugId = 4L;
        String newName = "BUG_1";
        String description = "BUG_DESCRIPTION_1";
        BugClassification classification = BugClassification.PERFORMANCE;
        BugStatus status = BugStatus.NEW;
        BugSeverity severity = BugSeverity.HIGH;

        Bug bugByNameAndProject = new BugBuilder()
                .withId(2L)
                .withName(newName)
                .withProject(this.project)
                .build();

        when(this.projectService.getProjectEntityById(projectId)).thenReturn(this.project);
        when(this.bugRepository.findByNameAndProjectId(newName, projectId)).thenReturn(bugByNameAndProject);
        when(this.bugRepository.findBugById(currentBugId)).thenReturn(this.bug);

        when(this.bugDTOMapper.apply(any(Bug.class))).thenReturn(this.bugDTO);

        Exception exception = assertThrows
                (
                        BugAlreadyExistException.class,
                        () -> this.underTest.addOrUpdate(null, editorId, projectId, currentBugId, newName, description, classification, status, severity)
                );
        assertEquals(BUG_ALREADY_EXISTS, exception.getMessage());
    }


    @Test
    public void testAddOrUpdate_ForAdd_Should_ThrowException_When_CreatorWasNotFound()
    {
        Long creatorId = 1L;
        Long projectId = 3L;
        String newName = "BUG_1";
        String description = "BUG_DESCRIPTION_1";
        BugClassification classification = BugClassification.PERFORMANCE;
        BugStatus status = BugStatus.NEW;
        BugSeverity severity = BugSeverity.HIGH;

        when(this.projectService.getProjectEntityById(projectId)).thenReturn(this.project);
        when(this.userService.getUserEntityById(creatorId)).thenReturn(null);
        when(this.bugRepository.findByNameAndProjectId(newName, projectId)).thenReturn(null);

        when(this.bugDTOMapper.apply(any(Bug.class))).thenReturn(this.bugDTO);

        Exception exception = assertThrows
                (
                        UserNotFoundException.class,
                        () -> this.underTest.addOrUpdate(creatorId, null, projectId, null, newName, description, classification, status, severity)
                );
        assertEquals(COULD_NOT_FIND_THE_USER, exception.getMessage());
    }


    @Test
    public void testAddOrUpdate_ForAdd_And_CreatorIsNotProjectLeader_Should_AddBug_And_NotifyProjectLeader() throws Exception
    {
        Long creatorId = 1L;
        Long projectId = 3L;
        String newName = "BUG_1";
        String description = "BUG_DESCRIPTION_1";
        BugClassification classification = BugClassification.PERFORMANCE;
        BugStatus status = BugStatus.NEW;
        BugSeverity severity = BugSeverity.HIGH;

        when(this.projectService.getProjectEntityById(projectId)).thenReturn(this.project);
        when(this.userService.getUserEntityById(creatorId)).thenReturn(this.creator);
        when(this.bugRepository.findByNameAndProjectId(newName, projectId)).thenReturn(null);

        when(this.bugDTOMapper.apply(any(Bug.class))).thenReturn(this.bugDTO);

        BugDTO result = this.underTest.addOrUpdate(creatorId, null, projectId, null, newName, description, classification, status, severity);

        // Verify the result
        assertNotNull(result);
        assertNotNull(result.creationDate());

        assertEquals(newName, result.name());
        assertEquals(description, result.description());
        assertEquals(classification.name(), result.classification());
        assertEquals(status.name(), result.status());
        assertEquals(severity.name(), result.severity());
        assertEquals(creatorId, result.creator().id());

        // Verify that the bug was saved
        verify(this.bugRepository).save(this.bugCaptor.capture());
        Bug savedBug = this.bugCaptor.getValue();
        assertEquals(newName, savedBug.getName());
        assertEquals(description, savedBug.getDescription());
        assertEquals(classification, savedBug.getClassification());
        assertEquals(status, savedBug.getStatus());
        assertEquals(severity, savedBug.getSeverity());
        assertEquals(this.project, savedBug.getProject());

        // Verify that the notification was sent
        verify(this.notificationService).sendNotificationFromTo(creatorId, this.projectLeader.getId(), NEW_ISSUE_IN_ONE_OF_YOUR_PROJECTS,
                NEW_ISSUE + newName + HAS_BEEN_REPORTED_IN + this.project.getName() + BY + this.creator.getFullName());
    }



    @Test
    public void testAddOrUpdate_ForAdd_And_CreatorIsProjectLeader_ShouldAddBug() throws Exception
    {
        Long creatorId = 2L;
        Long projectId = 3L;
        String newName = "BUG_1";
        String description = "BUG_DESCRIPTION_1";
        BugClassification classification = BugClassification.PERFORMANCE;
        BugStatus status = BugStatus.NEW;
        BugSeverity severity = BugSeverity.HIGH;

        when(this.projectService.getProjectEntityById(projectId)).thenReturn(this.project);
        when(this.userService.getUserEntityById(creatorId)).thenReturn(this.projectLeader);
        when(this.bugRepository.findByNameAndProjectId(newName, projectId)).thenReturn(null);

        BugDTO bugDTO = new BugDTOBuilder()
                .withCreator(this.projectLeaderDto)
                .withProject(this.projectDTO).build();

        when(this.bugDTOMapper.apply(any(Bug.class))).thenReturn(bugDTO);

        BugDTO result = this.underTest.addOrUpdate(creatorId, null, projectId, null, newName, description, classification, status, severity);

        // Verify the result
        assertNotNull(result);
        assertNotNull(result.creationDate());

        assertEquals(newName, result.name());
        assertEquals(description, result.description());
        assertEquals(classification.name(), result.classification());
        assertEquals(status.name(), result.status());
        assertEquals(severity.name(), result.severity());
        assertEquals(creatorId, result.creator().id());

        // Verify that the bug was saved
        verify(this.bugRepository).save(this.bugCaptor.capture());
        Bug savedBug = this.bugCaptor.getValue();
        assertEquals(newName, savedBug.getName());
        assertEquals(description, savedBug.getDescription());
        assertEquals(classification, savedBug.getClassification());
        assertEquals(status, savedBug.getStatus());
        assertEquals(severity, savedBug.getSeverity());
        assertEquals(this.project, savedBug.getProject());
    }



    @Test
    public void testAddOrUpdate_ForUpdate_And_ProjectLeaderIsEditorNotCreator_ShouldUpdateBug_And_NotifyCreator() throws Exception
    {
        Long editorId = 2L;
        Long projectId = 3L;
        Long currentBugId = 1L;
        String newName = "BUG_2";
        String description = "BUG_DESCRIPTION_2";
        BugClassification classification = BugClassification.SECURITY;
        BugStatus status = BugStatus.ASSIGNED;
        BugSeverity severity = BugSeverity.LOW;

        when(this.projectService.getProjectEntityById(projectId)).thenReturn(this.project);
        when(this.bugRepository.findByNameAndProjectId(newName, projectId)).thenReturn(null);
        when(this.bugRepository.findBugById(currentBugId)).thenReturn(this.bug);

        BugDTO editedBugDto = new BugDTOBuilder()
                .withId(currentBugId)
                .withName(newName)
                .withDescription(description)
                .withCreator(this.creatorDTO)
                .withProject(this.projectDTO)
                .withClassification(classification)
                .withStatus(status)
                .withSeverity(severity)
                .build();

        when(this.bugDTOMapper.apply(any(Bug.class))).thenReturn(editedBugDto);

        String currentBugName = this.bug.getName();
        BugDTO result = this.underTest.addOrUpdate(null, editorId, projectId, currentBugId, newName, description, classification, status, severity);

        // Verify the result
        assertNotNull(result);
        assertNotNull(result.creationDate());

        assertEquals(editedBugDto, result);

        // Verify that the bug was saved
        verify(this.bugRepository).save(this.bugCaptor.capture());
        Bug savedBug = this.bugCaptor.getValue();
        assertEquals(newName, savedBug.getName());
        assertEquals(description, savedBug.getDescription());
        assertEquals(classification, savedBug.getClassification());
        assertEquals(status, savedBug.getStatus());
        assertEquals(severity, savedBug.getSeverity());
        assertEquals(this.project, savedBug.getProject());


        // Verify that the notification was sent
        verify(this.notificationService).sendNotificationFromTo(this.projectLeader.getId(), this.creator.getId(), REPORTED_ISSUE_HAS_BEEN_EDITED,
                REPORTED_ISSUE + currentBugName + HAS_BEEN_EDITED_BY + projectLeader.getFullName() +
                        PLEASE_CONTACT_YOUR_PROJECT_LEADER_FOR_MORE_INFORMATION);
    }



    @Test
    public void testAddOrUpdate_ForUpdate_And_ProjectLeaderIsEditorAndCreator_ShouldUpdateBug() throws Exception
    {
        Long editorId = 2L;
        Long projectId = 3L;
        Long currentBugId = 1L;
        String newName = "BUG_2";
        String description = "BUG_DESCRIPTION_2";
        BugClassification classification = BugClassification.SECURITY;
        BugStatus status = BugStatus.ASSIGNED;
        BugSeverity severity = BugSeverity.LOW;

        this.bug.setCreator(this.projectLeader);

        when(this.projectService.getProjectEntityById(projectId)).thenReturn(this.project);
        when(this.bugRepository.findByNameAndProjectId(newName, projectId)).thenReturn(null);
        when(this.bugRepository.findBugById(currentBugId)).thenReturn(this.bug);

        BugDTO editedBugDto = new BugDTOBuilder()
                .withId(currentBugId)
                .withName(newName)
                .withDescription(description)
                .withCreator(this.projectLeaderDto)
                .withProject(this.projectDTO)
                .withClassification(classification)
                .withStatus(status)
                .withSeverity(severity)
                .build();

        when(this.bugDTOMapper.apply(any(Bug.class))).thenReturn(editedBugDto);

        BugDTO result = this.underTest.addOrUpdate(null, editorId, projectId, currentBugId, newName, description, classification, status, severity);

        // Verify the result
        assertNotNull(result);
        assertNotNull(result.creationDate());

        assertEquals(newName, result.name());
        assertEquals(description, result.description());
        assertEquals(classification.name(), result.classification());
        assertEquals(status.name(), result.status());
        assertEquals(severity.name(), result.severity());
        assertEquals(this.projectLeader.getId(), result.creator().id());

        // Verify that the bug was saved
        verify(this.bugRepository).save(this.bugCaptor.capture());
        Bug savedBug = this.bugCaptor.getValue();
        assertEquals(newName, savedBug.getName());
        assertEquals(description, savedBug.getDescription());
        assertEquals(classification, savedBug.getClassification());
        assertEquals(status, savedBug.getStatus());
        assertEquals(severity, savedBug.getSeverity());
        assertEquals(this.project, savedBug.getProject());
    }



    @Test
    public void testAddOrUpdate_ForUpdate_And_CreatorIsEditor_ShouldUpdate() throws Exception
    {
        Long editorId = 1L;
        Long projectId = 3L;
        Long currentBugId = 1L;
        String newName = "BUG_2";
        String description = "BUG_DESCRIPTION_2";
        BugClassification classification = BugClassification.SECURITY;
        BugStatus status = BugStatus.ASSIGNED;
        BugSeverity severity = BugSeverity.LOW;

        when(this.projectService.getProjectEntityById(projectId)).thenReturn(this.project);
        when(this.bugRepository.findByNameAndProjectId(newName, projectId)).thenReturn(null);
        when(this.bugRepository.findBugById(currentBugId)).thenReturn(this.bug);

        BugDTO editedBugDto = new BugDTOBuilder()
                .withId(currentBugId)
                .withName(newName)
                .withDescription(description)
                .withCreator(this.creatorDTO)
                .withProject(this.projectDTO)
                .withClassification(classification)
                .withStatus(status)
                .withSeverity(severity)
                .build();

        when(this.bugDTOMapper.apply(any(Bug.class))).thenReturn(editedBugDto);

        BugDTO result = this.underTest.addOrUpdate(null, editorId, projectId, currentBugId, newName, description, classification, status, severity);

        // Verify the result
        assertNotNull(result);
        assertNotNull(result.creationDate());

        assertEquals(newName, result.name());
        assertEquals(description, result.description());
        assertEquals(classification.name(), result.classification());
        assertEquals(status.name(), result.status());
        assertEquals(severity.name(), result.severity());
        assertEquals(this.creator.getId(), result.creator().id());

        // Verify that the bug was saved
        verify(this.bugRepository).save(this.bugCaptor.capture());
        Bug savedBug = this.bugCaptor.getValue();
        assertEquals(newName, savedBug.getName());
        assertEquals(description, savedBug.getDescription());
        assertEquals(classification, savedBug.getClassification());
        assertEquals(status, savedBug.getStatus());
        assertEquals(severity, savedBug.getSeverity());
        assertEquals(this.project, savedBug.getProject());
    }



    @Test
    public void testGetBugById_Should_ReturnBugDTO()
    {
        Long bugId = 1L;

        when(this.bugRepository.findBugById(bugId)).thenReturn(this.bug);
        when(this.bugDTOMapper.apply(any(Bug.class))).thenReturn(this.bugDTO);

        BugDTO result = this.underTest.getBugById(bugId);

        assertNotNull(result);
        assertEquals(this.bugDTO, result);

        Bug bugById = this.bugRepository.findBugById(bugId);
        assertEquals(this.bug, bugById);
    }



    @Test
    public void testGetBugsByProjectId_Should_ReturnBugDTOPage()
    {
        Long projectId = 1L;
        int page = 1;
        int size = 5;
        String sortOrder = "name";
        boolean ascending = true;

        Page<Bug> entitiesPage = new PageImpl<>(this.entitiesList.subList(0, size));
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortOrder).ascending());

        when(this.bugRepository.getBugsByProject_Id(projectId, pageable)).thenReturn(entitiesPage);
        when(this.bugDTOMapper.apply(any(Bug.class)))
                .thenReturn(this.bugDTOList.get(0), this.bugDTOList.subList(1, size).toArray(new BugDTO[size]));

        Page<BugDTO> result = this.underTest.getBugsByProjectId(projectId, page, size, sortOrder, ascending);

        // Verify that the repository method was called with the correct parameters
        verify(this.bugRepository).getBugsByProject_Id(projectId, pageable);
        // Verify that the mapper was called for each entity in the page
        verify(this.bugDTOMapper, times(size)).apply(any(Bug.class));

        assertEquals(size, result.getContent().size());
        for (int i = 0; i < size; i++)
        {
            BugDTO expected = this.bugDTOList.get(i);
            BugDTO actual = result.getContent().get(i);
            assertEquals(expected, actual);
        }
    }


    @Test
    public void testGetBugEntityListByProjectId_Should_ReturnBugList()
    {
        Long projectId = 1L;

        when(this.bugRepository.getAllByProjectIdOrderByCreationDateDesc(projectId)).thenReturn(this.entitiesList);

        List<Bug> result = this.underTest.getBugEntityListByProjectId(projectId);

        verify(this.bugRepository).getAllByProjectIdOrderByCreationDateDesc(projectId);

        assertEquals(this.entitiesList.size(), result.size());
        for (int i = 0; i < this.entitiesList.size(); i++)
        {
            Bug expected = this.entitiesList.get(i);
            Bug actual = result.get(i);
            assertEquals(expected, actual);
        }
    }


    @Test
    public void testGetBugsByCreatorId_Should_ReturnBugDTOPage()
    {
        Long creatorId = 1L;
        int page = 1;
        int size = 5;
        String sortOrder = "name";
        boolean ascending = true;

        Page<Bug> entitiesPage = new PageImpl<>(this.entitiesList.subList(0,size));
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortOrder).ascending());
        when(this.bugRepository.getBugsByCreatorId(creatorId, pageable)).thenReturn(entitiesPage);

        when(this.bugDTOMapper.apply(any(Bug.class)))
                .thenReturn(this.bugDTOList.get(0),this.bugDTOList.subList(1, size).toArray(new BugDTO[size]));

        Page<BugDTO> result = this.underTest.getBugsByCreatorId(creatorId, page, size, sortOrder, ascending);

        // Verify that the repository method was called with the correct parameters
        verify(this.bugRepository).getBugsByCreatorId(creatorId, pageable);

        // Verify that the mapper was called for each entity in the page
        verify(this.bugDTOMapper, times(size)).apply(any(Bug.class));

        assertEquals(size, result.getContent().size());
        for (int i = 0; i < size; i++)
        {
            BugDTO expected = this.bugDTOList.get(i);
            BugDTO actual = result.getContent().get(i);
            assertEquals(expected, actual);
        }
    }



    @Test
    public void testGetBugsByAssigneeId_Should_ReturnBugDTOPage()
    {
        Long assigneeId = 1L;
        int page = 1;
        int size = 5;
        String sortOrder = "name";
        boolean ascending = true;

        Page<Bug> entitiesPage = new PageImpl<>(this.entitiesList.subList(0, size));
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortOrder).ascending());
        when(this.bugRepository.getBugsByAssigneeId(assigneeId, pageable)).thenReturn(entitiesPage);

        when(this.bugDTOMapper.apply(any(Bug.class)))
                .thenReturn(this.bugDTOList.get(0), this.bugDTOList.subList(1, size).toArray(new BugDTO[size]));

        Page<BugDTO> result = this.underTest.getBugsByAssigneeId(assigneeId, page, size, sortOrder, ascending);

        // Verify that the repository method was called with the correct parameters
        verify(this.bugRepository).getBugsByAssigneeId(assigneeId, pageable);

        // Verify that the mapper was called for each entity in the page
        verify(this.bugDTOMapper, times(size)).apply(any(Bug.class));

        assertEquals(size, result.getContent().size());
        for (int i = 0; i < size; i++)
        {
            BugDTO expected = this.bugDTOList.get(i);
            BugDTO actual = result.getContent().get(i);
            assertEquals(expected, actual);
        }
    }


    @Test
    public void testGetBugsByProjectLeaderId_Should_ReturnBugDTOPage()
    {
        Long projectLeaderId = 1L;
        int page = 1;
        int size = 5;
        String sortOrder = "name";
        boolean ascending = true;

        Page<Bug> entitiesPage = new PageImpl<>(this.entitiesList.subList(0, size));
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortOrder).ascending());
        when(this.bugRepository.getBugsByProjectProjectLeaderId(projectLeaderId, pageable)).thenReturn(entitiesPage);

        when(this.bugDTOMapper.apply(any(Bug.class)))
                .thenReturn(this.bugDTOList.get(0), this.bugDTOList.subList(1, size).toArray(new BugDTO[size]));

        Page<BugDTO> result = this.underTest.getBugsByProjectLeaderId(projectLeaderId, page, size, sortOrder, ascending);

        // Verify that the repository method was called with the correct parameters
        verify(this.bugRepository).getBugsByProjectProjectLeaderId(projectLeaderId, pageable);

        // Verify that the mapper was called for each entity in the page
        verify(this.bugDTOMapper, times(size)).apply(any(Bug.class));

        assertEquals(size, result.getContent().size());
        for (int i = 0; i < size; i++)
        {
            BugDTO expected = this.bugDTOList.get(i);
            BugDTO actual = result.getContent().get(i);
            assertEquals(expected, actual);
        }
    }


    @Test
    public void testDeleteBug_Should_ThrowException_When_BugDoesNotExist()
    {
        Long bugId = 1L;
        Long deleterId = 2L;

        this.bug.setStatus(BugStatus.ASSIGNED);
        this.bug.setAssignee(this.assignee);

        when(this.bugRepository.findBugById(bugId)).thenReturn(null);

        Exception exception = assertThrows(BugDoesNotExistException.class, () -> this.underTest.deleteBug(bugId, deleterId));
        assertEquals(exception.getMessage(), BUG_DOES_NOT_EXIST);
    }


    @Test
    public void testDeleteBug_Should_ThrowException_When_AssigneeNotNull_And_CreatorIsDeleter()
    {
        Long bugId = 1L;
        Long deleterId = 1L;

        this.bug.setStatus(BugStatus.ASSIGNED);
        this.bug.setAssignee(this.assignee);

        when(this.bugRepository.findBugById(bugId)).thenReturn(this.bug);

        Exception exception = assertThrows(IllegalAccessException.class, () -> this.underTest.deleteBug(bugId, deleterId));
        assertEquals(exception.getMessage(), NO_PERMISSION_TO_DELETE_BUG);
    }


    @Test
    public void testDeleteBug_CreatorIsProjectLeader_And_AssigneeNotNull_ShouldDeleteBug_And_NotifyAssignee() throws UserNotFoundException, BugDoesNotExistException, IllegalAccessException
    {
        Long bugId = 1L;
        Long deleterId = 2L;

        this.bug.setStatus(BugStatus.ASSIGNED);
        this.bug.setAssignee(this.assignee);

        when(this.bugRepository.findBugById(bugId)).thenReturn(this.bug);
        doNothing().when(this.bugRepository).delete(this.bug);

        BugDTO bugDTO = new BugDTOBuilder()
                .withStatus(BugStatus.ASSIGNED)
                .build();
        when(this.bugDTOMapper.apply(this.bug)).thenReturn(bugDTO);

        BugDTO result = this.underTest.deleteBug(bugId, deleterId);

        verify(this.notificationService).sendNotificationFromTo(this.projectLeader.getId(), this.assignee.getId(), ISSUE_DELETED,
                BUG + this.bug.getName() + HAS_BEEN_DELETED_FROM_PROJECT + this.project.getName() +
                        YOU_VE_BEEN_UNASSIGNED_AUTOMATICALLY);

        verify(this.bugRepository).findBugById(bugId);
        verify(this.bugRepository).delete(this.bug);

        assertEquals(bugDTO, result);
    }


    @Test
    public void testDeleteBug_CreatorIsDeleter_ShouldDeleteBug_And_Notify_ProjectLeader() throws UserNotFoundException, BugDoesNotExistException, IllegalAccessException
    {
        Long bugId = 1L;
        Long deleterId = 1L;

        when(this.bugRepository.findBugById(bugId)).thenReturn(this.bug);

        doNothing().when(this.bugRepository).delete(this.bug);

        BugDTO bugDTO = new BugDTOBuilder()
                .withStatus(BugStatus.ASSIGNED)
                .build();

        when(this.bugDTOMapper.apply(this.bug)).thenReturn(bugDTO);

        BugDTO result = this.underTest.deleteBug(bugId, deleterId);

        verify(this.notificationService).sendNotificationFromTo(creator.getId(), this.projectLeader.getId(),
                ISSUE_DELETED,
                BUG + this.bug.getName() + HAS_BEEN_DELETED_FROM_PROJECT + this.project.getName() +
                        PLEASE_CONTACT + creator.getFullName() + FOR_MORE_INFORMATION);

        verify(this.bugRepository).findBugById(bugId);
        verify(this.bugRepository).delete(this.bug);

        assertEquals(bugDTO, result);
    }


    @Test
    public void testDeleteBug_CreatorIsNotProjectLeader_And_ProjectLeaderIsDeleter_Should_DeleteBug_And_Notify_Assignee_And_Creator() throws UserNotFoundException, BugDoesNotExistException, IllegalAccessException
    {
        Long bugId = 1L;
        Long deleterId = 2L;

        Bug bugToDelete = new BugBuilder()
                .withCreator(this.creator)
                .withStatus(BugStatus.ASSIGNED)
                .withProject(this.project)
                .withAssignee(this.assignee)
                .build();

        when(this.bugRepository.findBugById(bugId)).thenReturn(bugToDelete);

        doNothing().when(this.bugRepository).delete(bugToDelete);

        BugDTO bugDTO = new BugDTOBuilder()
                .withStatus(BugStatus.ASSIGNED)
                .build();

        when(this.bugDTOMapper.apply(bugToDelete)).thenReturn(bugDTO);

        BugDTO result = this.underTest.deleteBug(bugId, deleterId);

        verify(this.notificationService).sendNotificationFromTo(projectLeader.getId(), this.assignee.getId(), ISSUE_DELETED,
                BUG + bugToDelete.getName() + HAS_BEEN_DELETED_FROM_PROJECT + this.project.getName() +
                        YOU_VE_BEEN_UNASSIGNED_AUTOMATICALLY);

        verify(notificationService).sendNotificationFromTo(this.projectLeader.getId(), this.creator.getId(),
                ISSUE_DELETED,
                BUG + bugToDelete.getName() + HAS_BEEN_DELETED_FROM_PROJECT + this.project.getName() +
                        PLEASE_CONTACT_YOUR_PROJECT_LEADER_FOR_MORE_INFORMATION);

        verify(this.bugRepository).findBugById(bugId);
        verify(this.bugRepository).delete(bugToDelete);

        assertEquals(bugDTO, result);
    }


    @Test
    public void testSetAssignee_Should_ThrowException_When_AssigneeDoesNotExist()
    {
        Long bugId = 1L;
        Long assigneeId = 3L;

        when(this.userService.getUserEntityById(assigneeId)).thenReturn(null);
        when(this.bugRepository.findBugById(bugId)).thenReturn(this.bug);

        Exception exception = assertThrows(UserNotFoundException.class, () -> this.underTest.setAssignee(bugId, assigneeId));
        assertEquals(COULD_NOT_FIND_THE_USER, exception.getMessage());
    }


    @Test
    public void testSetAssignee_Should_ThrowException_When_BugDoesNotExist()
    {
        Long bugId = 1L;
        Long assigneeId = 3L;

        when(this.userService.getUserEntityById(assigneeId)).thenReturn(this.assignee);
        when(this.bugRepository.findBugById(bugId)).thenReturn(null);

        Exception exception = assertThrows(BugDoesNotExistException.class, () -> this.underTest.setAssignee(bugId, assigneeId));
        assertEquals(BUG_DOES_NOT_EXIST, exception.getMessage());
    }


    @Test
    public void testSetAssignee_Should_ThrowException_When_UserAlreadyAssigned()
    {
        Long bugId = 1L;
        Long assigneeId = 3L;

        this.bug.setAssignee(this.assignee);

        when(this.userService.getUserEntityById(assigneeId)).thenReturn(this.assignee);
        when(this.bugRepository.findBugById(bugId)).thenReturn(this.bug);

        Exception exception = assertThrows(UserAlreadyAssignedException.class, () -> this.underTest.setAssignee(bugId, assigneeId));
        assertEquals(USER_ALREADY_ASSIGNED, exception.getMessage());
    }



    @Test
    public void testSetAssignee_PreviousAssigneeIsNull_Should_SetAssignee_And_ChangeStatusToASSIGNED() throws UserNotFoundException, BugDoesNotExistException, UserAlreadyAssignedException
    {
        Long bugId = 1L;
        Long assigneeId = 3L;

        User projectLeader = this.project.getProjectLeader();

        when(this.userService.getUserEntityById(assigneeId)).thenReturn(this.assignee);
        when(this.bugRepository.findBugById(bugId)).thenReturn(this.bug);

        this.underTest.setAssignee(bugId, assigneeId);

        verify(this.bugRepository).save(this.bugCaptor.capture());
        Bug savedBug = this.bugCaptor.getValue();
        assertEquals(this.assignee, savedBug.getAssignee());

        verify(this.notificationService).sendNotificationFromTo(projectLeader.getId(), this.assignee.getId(), NEW_ASSIGNMENT,
                YOU_VE_BEEN_ASSIGNED_TO + this.bug.getName() + PLEASE_CONTACT_YOUR_PROJECT_LEADER_FOR_MORE_INFORMATION);

        assertEquals(BugStatus.ASSIGNED, this.bug.getStatus());
    }


    @Test
    public void testSetAssignee_PreviousAssigneeNotNull_Should_SetAssignee_And_Notify_PreviousAssignee_And_NewAssignee() throws UserNotFoundException, BugDoesNotExistException, UserAlreadyAssignedException
    {
        Long bugId = 1L;
        Long assigneeId = 1L;

        this.bug.setAssignee(this.assignee);

        User projectLeader = this.project.getProjectLeader();
        User newAssignee = new UserBuilder().withId(assigneeId).build();

        when(this.userService.getUserEntityById(assigneeId)).thenReturn(newAssignee);
        when(this.bugRepository.findBugById(bugId)).thenReturn(this.bug);

        this.underTest.setAssignee(bugId, assigneeId);

        verify(this.bugRepository).save(this.bugCaptor.capture());
        Bug savedBug = this.bugCaptor.getValue();
        assertEquals(newAssignee, savedBug.getAssignee());

        verify(this.notificationService).sendNotificationFromTo(projectLeader.getId(), this.assignee.getId(), ASSIGNMENT_CANCELLED,
                YOUR_ASSIGNMENT_TO + this.bug.getName() + HAS_BEEN_CANCELLED +
                        PLEASE_CONTACT_YOUR_PROJECT_LEADER_FOR_MORE_INFORMATION);

        verify(this.notificationService).sendNotificationFromTo(projectLeader.getId(), newAssignee.getId(), NEW_ASSIGNMENT,
                YOU_VE_BEEN_ASSIGNED_TO + this.bug.getName() + PLEASE_CONTACT_YOUR_PROJECT_LEADER_FOR_MORE_INFORMATION);
    }


    @Test
    public void testSetAssignee_PreviousStatusNotNew_Should_SetAssignee_And_NotChaneStatus() throws UserNotFoundException, BugDoesNotExistException, UserAlreadyAssignedException
    {
        Long bugId = 1L;
        Long assigneeId = 1L;

        this.bug.setAssignee(this.assignee);
        this.bug.setStatus(BugStatus.PENDING_RETEST);

        User newAssignee = new UserBuilder().withId(assigneeId).build();

        when(this.userService.getUserEntityById(assigneeId)).thenReturn(newAssignee);
        when(this.bugRepository.findBugById(bugId)).thenReturn(this.bug);

        this.underTest.setAssignee(bugId, assigneeId);

        verify(this.bugRepository).save(this.bugCaptor.capture());
        Bug savedBug = this.bugCaptor.getValue();
        assertEquals(newAssignee, savedBug.getAssignee());

        assertEquals(BugStatus.PENDING_RETEST, this.bug.getStatus());
        assertEquals(assigneeId, this.bug.getAssignee().getId());
    }


    @Test
    public void testUnassignWorkerFromBug_Should_ThrowException_When_BugDoesNotExist()
    {
        Long bugId = 1L;

        when(this.bugRepository.findBugById(bugId)).thenReturn(null);

        Exception exception = assertThrows
                (BugDoesNotExistException.class, () -> this.underTest.unassignWorkerFromBug(bugId));
        assertEquals(BUG_DOES_NOT_EXIST, exception.getMessage());
    }


    @Test
    public void testUnassignWorkerFromBug_Should_ThrowException_When_PreviousAssigneeIsNull()
    {
        Long bugId = 1L;

        when(this.bugRepository.findBugById(bugId)).thenReturn(this.bug);

        Exception exception = assertThrows
                (UserNotFoundException.class, () -> this.underTest.unassignWorkerFromBug(bugId));
        assertEquals(COULD_NOT_FIND_THE_USER, exception.getMessage());
    }


    @Test
    public void testUnassignWorkerFromBug_Should_UnassignWorker_And_ChangeStatusTo_NEW() throws UserNotFoundException, BugDoesNotExistException
    {
        Long bugId = 1L;

        this.bug.setAssignee(this.assignee);
        this.bug.setStatus(BugStatus.ASSIGNED);

        when(this.bugRepository.findBugById(bugId)).thenReturn(this.bug);

        this.underTest.unassignWorkerFromBug(bugId);

        verify(this.bugRepository).save(this.bugCaptor.capture());
        Bug savedBug = this.bugCaptor.getValue();
        assertNull(savedBug.getAssignee());
        assertEquals(BugStatus.NEW, savedBug.getStatus());

        assertNull(this.bug.getAssignee());
        assertEquals(BugStatus.NEW, this.bug.getStatus());

        verify(this.notificationService).sendNotificationFromTo(this.projectLeader.getId(), this.assignee.getId(),
                ASSIGNMENT_CANCELLED,
                YOUR_ASSIGNMENT_TO + bug.getName() + HAS_BEEN_CANCELLED +
                        PLEASE_CONTACT_YOUR_PROJECT_LEADER_FOR_MORE_INFORMATION);
    }


    @Test
    public void testUnassignWorkerFromBug_Should_UnassignWorker_And_NotChangeStatus() throws UserNotFoundException, BugDoesNotExistException
    {
        Long bugId = 1L;

        this.bug.setAssignee(this.assignee);
        this.bug.setStatus(BugStatus.PENDING_RETEST);

        when(this.bugRepository.findBugById(bugId)).thenReturn(this.bug);

        this.underTest.unassignWorkerFromBug(bugId);

        verify(this.bugRepository).save(this.bugCaptor.capture());
        Bug savedBug = this.bugCaptor.getValue();
        assertNull(savedBug.getAssignee());
        assertEquals(BugStatus.PENDING_RETEST, savedBug.getStatus());

        assertNull(this.bug.getAssignee());
        assertEquals(BugStatus.PENDING_RETEST, this.bug.getStatus());

        verify(this.notificationService).sendNotificationFromTo(this.projectLeader.getId(), this.assignee.getId(),
                ASSIGNMENT_CANCELLED,
                YOUR_ASSIGNMENT_TO + bug.getName() + HAS_BEEN_CANCELLED +
                        PLEASE_CONTACT_YOUR_PROJECT_LEADER_FOR_MORE_INFORMATION);
    }


    @Test
    public void testSetStatus_Should_ThrowException_When_BugDoesNotExist()
    {
        Long bugId = 1L;
        BugStatus status = BugStatus.FIXED;

        when(this.bugRepository.findBugById(bugId)).thenReturn(null);

        Exception exception = assertThrows
                (BugDoesNotExistException.class, () -> this.underTest.setStatus(bugId, status));
        assertEquals(BUG_DOES_NOT_EXIST, exception.getMessage());
    }


    @Test
    public void testSetStatus_Should_SetStatus() throws BugDoesNotExistException
    {
        Long bugId = 1L;
        BugStatus status = BugStatus.FIXED;

        when(this.bugRepository.findBugById(bugId)).thenReturn(this.bug);

        this.underTest.setStatus(bugId, status);

        verify(this.bugRepository).save(this.bugCaptor.capture());
        Bug savedBug = this.bugCaptor.getValue();
        assertEquals(status, savedBug.getStatus());

        assertEquals(status, this.bug.getStatus());
    }


    @Test
    public void testGetCountByCreatorId_ShouldGetCount()
    {
        long creatorId = 1L;

        when(this.bugRepository.countBugsByCreatorId(creatorId)).thenReturn(5L);

        Long countByCreator = this.underTest.getCountByCreatorId(creatorId);
        assertEquals(5L, countByCreator);
    }


    @Test
    public void testGetCountByCreationDateAfter_ShouldGetCount()
    {
        LocalDateTime date = LocalDateTime.now().minusDays(30);

        when(this.bugRepository.countBugsByCreationDateAfter(date)).thenReturn(5L);

        Long countByCreationDate = this.underTest.getCountByCreationDateAfter(date);
        assertEquals(5L, countByCreationDate);
    }


    @Test
    public void testGetCountByStatus_ShouldGetCount()
    {
        BugStatus status = BugStatus.FIXED;

        when(this.bugRepository.countBugByStatus(status)).thenReturn(5L);

        Long countByStatus = this.underTest.getCountByStatus(status);
        assertEquals(5L, countByStatus);
    }


    @Test
    public void testGetCountByCreatorIdAndByCreationDateAfter_ShouldGetCount()
    {
        Long creatorId = 1L;
        LocalDateTime date = LocalDateTime.now().minusDays(30);

        when(this.bugRepository.countBugsByCreatorIdAndCreationDateAfter(creatorId, date)).thenReturn(5L);

        Long countByCreatorAndDate = this.underTest.getCountByCreatorIdAndByCreationDateAfter(creatorId, date);
        assertEquals(5L, countByCreatorAndDate);
    }


    @Test
    public void testGetCountByAssigneeIdAndByStatus_ShouldGetCount()
    {
        Long assigneeId = 3L;
        BugStatus status = BugStatus.FIXED;

        when(this.bugRepository.countBugByAssigneeIdAndStatus(assigneeId, status)).thenReturn(5L);

        Long countByAssigneeAndStatus = this.underTest.getCountByAssigneeIdAndByStatus(assigneeId, status);
        assertEquals(5L, countByAssigneeAndStatus);
    }


    @Test
    public void testGetCount_ShouldGetCount()
    {
        when(this.bugRepository.count()).thenReturn(5L);

        Long count = this.underTest.getCount();
        assertEquals(5L, count);
    }


    @Test
    public void testGetCountByProjectLeaderId_ShouldGetCount()
    {
        Long projectLeaderId = 2L;

        when(this.bugRepository.countBugsByProjectProjectLeaderId(projectLeaderId)).thenReturn(5L);

        Long countByProjectLeader = this.underTest.getCountByProjectLeaderId(projectLeaderId);
        assertEquals(5L, countByProjectLeader);
    }


    @Test
    public void testGetCountByProjectLeaderIdAndCreationDateAfter_ShouldGetCount()
    {
        Long projectLeaderId = 2L;
        LocalDateTime date = LocalDateTime.now().minusDays(30);

        when(this.bugRepository.countBugsByProjectProjectLeaderIdAndCreationDateAfter(projectLeaderId, date)).thenReturn(5L);

        Long countByProjectLeaderAndCreationDate = this.underTest.getCountByProjectLeaderIdAndCreationDateAfter(projectLeaderId, date);
        assertEquals(5L, countByProjectLeaderAndCreationDate);
    }


    @Test
    public void testGetCountByProjectLeaderIdAndStatus_ShouldGetCount()
    {
        Long projectLeaderId = 2L;
        BugStatus status = BugStatus.FIXED;

        when(this.bugRepository.countBugsByProjectProjectLeaderIdAndStatus(projectLeaderId, status)).thenReturn(5L);

        Long countByProjectLeaderAndStatus = this.underTest.getCountByProjectLeaderIdAndStatus(projectLeaderId, status);
        assertEquals(5L, countByProjectLeaderAndStatus);
    }
}
