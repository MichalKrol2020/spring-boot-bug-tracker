package com.company.service;

import com.company.dto.ProjectDTO;
import com.company.entity.Project;
import com.company.entity.User;
import com.company.exception.*;
import com.company.repository.ProjectRepository;
import com.company.service.mapper.ProjectDTOMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static com.company.constant.ProjectConstant.*;
import static com.company.utils.PaginationUtils.getPageable;

@Service
@Transactional(readOnly = true)
public class ProjectServiceImpl implements ProjectService
{
    private final ProjectRepository projectRepository;
    private final UserService userService;
    private final NotificationService notificationService;
    private final ProjectDTOMapper projectDTOMapper;

    @Autowired
    public ProjectServiceImpl(ProjectRepository projectRepository,
                              UserService userService,
                              NotificationService notificationService,
                              ProjectDTOMapper projectDTOMapper)
    {
        this.projectRepository = projectRepository;
        this.userService = userService;
        this.notificationService = notificationService;
        this.projectDTOMapper = projectDTOMapper;
    }


    @Override
    @Transactional
    public ProjectDTO addOrUpdate(Long projectLeaderId, String currentName, String newName, String newDescription) throws ProjectDoesNotExistException,
                                                                                                                          ProjectAlreadyExistsException,
                                                                                                                          UserNotFoundException
    {
        Project project = this.validateProject(currentName, newName);
        if(project == null)
        {
            project = this.initializeProject(projectLeaderId);
        }

        project.setName(newName);
        project.setDescription(newDescription);

        this.projectRepository.save(project);

        return this.projectDTOMapper.apply(project);
    }

    private Project validateProject(String currentName, String newName) throws ProjectDoesNotExistException, ProjectAlreadyExistsException
    {
        Project projectByNewName = this.projectRepository.findProjectByName(newName);

        if(StringUtils.isNotBlank(currentName))
        {
            Project currentProject = this.projectRepository.findProjectByName(currentName);
            if(currentProject == null)
            {
                throw new ProjectDoesNotExistException(PROJECT_DOES_NOT_EXIST);
            }

            if(projectByNewName != null && !currentProject.getId().equals(projectByNewName.getId()))
            {
                throw new ProjectAlreadyExistsException(PROJECT_ALREADY_EXISTS);
            }

            return currentProject;
        } else
        {
            if(projectByNewName != null)
            {
                throw new ProjectAlreadyExistsException(PROJECT_ALREADY_EXISTS);
            }
        }

        return null;
    }

    private Project initializeProject(Long projectLeaderId) throws UserNotFoundException
    {
        User projectLeader = this.userService.getUserEntityById(projectLeaderId);
        if(projectLeader == null)
        {
            throw new UserNotFoundException(PROJECT_LEADER_NOT_FOUND);
        }

        Project project = new Project();
        project.setProjectLeader(projectLeader);

        return project;
    }



    @Override
    @Transactional
    public ProjectDTO deleteProject(Long projectId) throws ProjectDoesNotExistException, UserNotFoundException
    {
        Project project = this.projectRepository.findProjectById(projectId);
        if(project == null)
        {
            throw new ProjectDoesNotExistException(PROJECT_DOES_NOT_EXIST);
        }

        List<User> participants = project.getParticipants();
        this.projectRepository.delete(project);

        for(User participant: participants)
        {
            this.notifyAboutDeleting(participant, project);
        }

        return this.projectDTOMapper.apply(project);
    }

    private void notifyAboutDeleting(User participant, Project project) throws UserNotFoundException
    {
        User projectLeader = project.getProjectLeader();
        this.notificationService.sendNotificationFromTo(projectLeader.getId(), participant.getId(),
                PROJECT_HAS_BEEN_DELETED,
                PROJECT + project.getName() + UNASSIGNED_AUTOMATICALLY);
    }



    @Override
    @Transactional
    public void addParticipants(Long projectId, List<User> participants) throws ProjectDoesNotExistException, UserNotFoundException, UserAlreadyAssignedException
    {
        if(participants == null)
        {
            throw new EmptyListException(NO_PARTICIPANTS_SELECTED);
        }

        Project project = this.projectRepository.findProjectById(projectId);
        if(project == null)
        {
            throw new ProjectDoesNotExistException(PROJECT_DOES_NOT_EXIST);
        }

        List<User> currentParticipants = project.getParticipants();
        if(currentParticipants == null)
        {
           currentParticipants = new ArrayList<>();
        }

        for(User participant: participants)
        {
            if(currentParticipants.contains(participant))
            {
                throw new UserAlreadyAssignedException(PARTICIPANT_ALREADY_ASSIGNED);
            }
        }

        currentParticipants.addAll(participants);
        project.setParticipants(currentParticipants);
        this.projectRepository.save(project);

        for(User participant: participants)
        {
            this.notifyAboutAssignment(participant, project);
        }
    }

    private void notifyAboutAssignment(User participant, Project project) throws UserNotFoundException
    {
        User projectLeader = project.getProjectLeader();
        this.notificationService.sendNotificationFromTo(projectLeader.getId(), participant.getId(),
                NEW_ASSIGNMENT_TO_PROJECT,
                YOU_VE_BEEN_ASSIGNED_TO + project.getName());
    }



    @Override
    @Transactional
    public void unassignParticipant(Long projectId, Long participantId) throws ProjectDoesNotExistException, UserNotFoundException, UserNotAssignedException
    {
        Project project = this.projectRepository.findProjectById(projectId);
        if(project == null)
        {
            throw new ProjectDoesNotExistException(PROJECT_DOES_NOT_EXIST);
        }


        User participant = this.userService.getUserEntityById(participantId);
        if(participant == null)
        {
            throw new UserNotFoundException(PARTICIPANT_NOT_FOUND);
        }

        List<User> participants = project.getParticipants();
        if(participants == null)
        {
            throw new EmptyListException(CONTAINS_NO_PARTICIPANTS);
        }

        if(participants.remove(participant))
        {
            this.notifyAboutCancellingAssignment(participant, project);
            this.projectRepository.save(project);
        } else
        {
            throw new UserNotAssignedException(USER_IS_NOT_ASSIGNED_TO_PROJECT);
        }
    }

    private void notifyAboutCancellingAssignment(User participant, Project project) throws UserNotFoundException
    {
        User projectLeader = project.getProjectLeader();
        this.notificationService.sendNotificationFromTo(projectLeader.getId(), participant.getId(), YOU_VE_BEEN_UNASSIGNED_FROM_PROJECT,
                YOU_VE_BEEN_UNASSIGNED_FROM + project.getName() +
                        PLEASE_CONTACT + projectLeader.getFullName() +
                        FOR_MORE_DETAILS);
    }



    @Override
    public Page<ProjectDTO> getProjectsByParticipantId(Long participantId, int page, int size, String sortOrder, boolean ascending)
    {
        Pageable pageable = getPageable(page, size, sortOrder, ascending);
        Page<Project> entitiesPage = this.projectRepository.findProjectsByParticipantsId(participantId, pageable);

        return entitiesPage.map(this.projectDTOMapper);
    }



    @Override
    public Page<ProjectDTO> getProjectsByProjectLeaderId(Long projectLeaderId, int page, int size, String sortOrder, boolean ascending)
    {
        Pageable pageable = getPageable(page, size, sortOrder, ascending);
        Page<Project> entitiesPage = this.projectRepository.findProjectsByProjectLeaderId(projectLeaderId, pageable);

        return entitiesPage.map(this.projectDTOMapper);
    }



    @Override
    public List<Project> getProjectEntitiesByProjectLeaderId(Long projectLeaderId)
    {
        return this.projectRepository.findAllByProjectLeaderId(projectLeaderId);
    }

    @Override
    public List<Project> getProjectEntitiesByParticipantId(Long participantId)
    {
        return this.projectRepository.findAllByParticipantsId(participantId);
    }

    @Override
    public Project getProjectEntityById(Long projectId)
    {
        return this.projectRepository.findProjectById(projectId);
    }
}
