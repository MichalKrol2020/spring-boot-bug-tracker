package com.company.service;

import com.company.dto.BugDTO;
import com.company.entity.Bug;
import com.company.entity.Project;
import com.company.entity.User;
import com.company.enumeration.BugClassification;
import com.company.enumeration.BugSeverity;
import com.company.enumeration.BugStatus;
import com.company.exception.*;
import com.company.repository.BugRepository;
import com.company.service.mapper.BugDTOMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.company.constant.BugConstant.*;
import static com.company.utils.PaginationUtils.getPageable;

@Service
@Transactional(readOnly = true)
public class BugServiceImpl implements BugService
{
    private final BugRepository bugRepository;
    private final ProjectService projectService;
    private final UserService userService;
    private final NotificationService notificationService;

    private final BugDTOMapper bugDTOMapper;

    @Autowired
    public BugServiceImpl(BugRepository bugRepository,
                          ProjectService projectService,
                          UserService userService,
                          NotificationService notificationService,
                          BugDTOMapper bugDTOMapper)
    {
        this.bugRepository = bugRepository;
        this.projectService = projectService;
        this.userService = userService;
        this.notificationService = notificationService;
        this.bugDTOMapper = bugDTOMapper;
    }


    @Override
    @Transactional
    public BugDTO addOrUpdate(Long creatorId,
                              Long editorId,
                              Long projectId,
                              Long currentBugId,
                              String newName,
                              String description,
                              BugClassification classification,
                              BugStatus status,
                              BugSeverity severity) throws ProjectDoesNotExistException,
                                                      UserNotFoundException,
                                                      BugDoesNotExistException,
                                                      BugAlreadyExistException
    {
        Project project = this.projectService.getProjectEntityById(projectId);
        if(project == null)
        {
            throw new ProjectDoesNotExistException(PROJECT_DOES_NOT_EXIST);
        }

        Bug bug = this.validateBug(currentBugId, projectId, newName);
        if(bug == null)
        {
            bug = this.initializeBug(creatorId);
        }

        String currentBugName = bug.getName();

        bug.setName(newName);
        bug.setClassification(classification);
        bug.setSeverity(severity);
        bug.setStatus(status);
        bug.setDescription(description);
        bug.setProject(project);

        this.notifyAboutCreationOrEditing(currentBugId, editorId, currentBugName, bug);

        this.bugRepository.save(bug);
        return this.bugDTOMapper.apply(bug);
    }
    
    private Bug validateBug(Long currentId, Long projectId, String name) throws BugDoesNotExistException, BugAlreadyExistException
    {
        Bug bugByNameAndProject = this.bugRepository.findByNameAndProjectId(name, projectId);

        if(currentId != null)
        {
            Bug bugByCurrentId = this.bugRepository.findBugById(currentId);
            
            if(bugByCurrentId == null)
            {
                throw new BugDoesNotExistException(BUG_DOES_NOT_EXIST);
            }

            if(bugByNameAndProject != null &&
                    !bugByCurrentId.getId().equals(bugByNameAndProject.getId()))
            {
                throw new BugAlreadyExistException(BUG_ALREADY_EXISTS);
            }

            return bugByCurrentId;
        } else
        {
            if(bugByNameAndProject != null)
            {
                throw new BugAlreadyExistException(BUG_ALREADY_EXISTS);
            }
        }

        return null;
    }

    private Bug initializeBug(Long creatorId) throws UserNotFoundException
    {
        User creator = this.userService.getUserEntityById(creatorId);
        if(creator == null)
        {
            throw new UserNotFoundException(COULD_NOT_FIND_THE_USER);
        }

        Bug bug = new Bug();
        bug.setCreator(creator);

        return bug;
    }

    private void notifyAboutCreationOrEditing(Long currentBugId, Long editorId, String currentBugName, Bug bug) throws UserNotFoundException
    {
        Project project = bug.getProject();
        User creator = bug.getCreator();
        User projectLeader = project.getProjectLeader();

        if(currentBugId == null)
        {
            if(!projectLeader.getId().equals(creator.getId()))
            {
                this.notificationService.sendNotificationFromTo(creator.getId(), projectLeader.getId(), NEW_ISSUE_IN_ONE_OF_YOUR_PROJECTS,
                        NEW_ISSUE + bug.getName() + HAS_BEEN_REPORTED_IN + project.getName() + BY + creator.getFullName());
            }
        } else
        {
            if(projectLeader.getId().equals(editorId) && !projectLeader.getId().equals(creator.getId()))
            {
                this.notificationService.sendNotificationFromTo(projectLeader.getId(), creator.getId(), REPORTED_ISSUE_HAS_BEEN_EDITED,
                        REPORTED_ISSUE + currentBugName + HAS_BEEN_EDITED_BY + projectLeader.getFullName() +
                                PLEASE_CONTACT_YOUR_PROJECT_LEADER_FOR_MORE_INFORMATION);
            }
        }
    }


    @Override
    public BugDTO getBugById(Long bugId)
    {
        Bug bug = this.bugRepository.findBugById(bugId);
        return this.bugDTOMapper.apply(bug);
    }



    @Override
    public Page<BugDTO> getBugsByProjectId(Long projectId, int page, int size, String sortOrder, boolean ascending)
    {
        Pageable pageable = getPageable(page, size, sortOrder, ascending);
        Page<Bug> entitiesPage = this.bugRepository.getBugsByProject_Id(projectId, pageable);
        return entitiesPage.map(this.bugDTOMapper);
    }



    @Override
    public List<Bug> getBugEntityListByProjectId(Long projectId)
    {
        return this.bugRepository.getAllByProjectIdOrderByCreationDateDesc(projectId);
    }



    @Override
    public Page<BugDTO> getBugsByCreatorId(Long creatorId, int page, int size, String sortOrder, boolean ascending)
    {
        Pageable pageable = getPageable(page, size, sortOrder, ascending);
        Page<Bug> entitiesPage = this.bugRepository.getBugsByCreatorId(creatorId, pageable);

        return entitiesPage.map(this.bugDTOMapper);
    }



    @Override
    public Page<BugDTO> getBugsByAssigneeId(Long assigneeId, int page, int size, String sortOrder, boolean ascending)
    {
        Pageable pageable = getPageable(page, size, sortOrder, ascending);
        Page<Bug> entitiesPage = this.bugRepository.getBugsByAssigneeId(assigneeId, pageable);

        return entitiesPage.map(this.bugDTOMapper);
    }



    @Override
    public Page<BugDTO> getBugsByProjectLeaderId(Long projectLeaderId, int page, int size, String sortOrder, boolean ascending)
    {
        Pageable pageable = getPageable(page, size, sortOrder, ascending);
        Page<Bug> entitiesPage = this.bugRepository.getBugsByProjectProjectLeaderId(projectLeaderId, pageable);

        return entitiesPage.map(this.bugDTOMapper);
    }




    @Override
    @Transactional
    public BugDTO deleteBug(Long bugId, Long deleterId) throws BugDoesNotExistException, UserNotFoundException, IllegalAccessException
    {
        Bug bugToDelete = this.bugRepository.findBugById(bugId);
        if(bugToDelete == null)
        {
            throw new BugDoesNotExistException(BUG_DOES_NOT_EXIST);
        }

        User creator = bugToDelete.getCreator();
        if(bugToDelete.getAssignee() != null && creator.getId().equals(deleterId))
        {
            throw new IllegalAccessException(NO_PERMISSION_TO_DELETE_BUG);
        }

        this.bugRepository.delete(bugToDelete);
        this.notifyAboutDeletion(bugToDelete, deleterId);

        return this.bugDTOMapper.apply(bugToDelete);
    }

    private void notifyAboutDeletion(Bug bugToDelete, Long deleterId) throws UserNotFoundException
    {
        User creator = bugToDelete.getCreator();
        Project project = bugToDelete.getProject();
        User projectLeader = project.getProjectLeader();
        User assignee = bugToDelete.getAssignee();

        if(assignee != null)
        {
            this.sendNotificationAboutDeleting(projectLeader.getId(), assignee.getId(), bugToDelete, YOU_VE_BEEN_UNASSIGNED_AUTOMATICALLY);
        }

        Long creatorId = creator.getId();
        if(creatorId.equals(projectLeader.getId()))
        {
            return;
        }

        if(deleterId.equals(creatorId))
        {
            this.sendNotificationAboutDeleting(creatorId, projectLeader.getId(), bugToDelete, PLEASE_CONTACT + creator.getFullName() + FOR_MORE_INFORMATION);
            return;
        }

        if(deleterId.equals(projectLeader.getId()))
        {
            this.sendNotificationAboutDeleting(projectLeader.getId(), creatorId, bugToDelete, PLEASE_CONTACT_YOUR_PROJECT_LEADER_FOR_MORE_INFORMATION);
        }
    }


    private void sendNotificationAboutDeleting(Long senderId, Long receiverId, Bug deletedBug, String messageContext) throws UserNotFoundException
    {
        Project project = deletedBug.getProject();

        this.notificationService.sendNotificationFromTo(senderId, receiverId,
                ISSUE_DELETED, BUG + deletedBug.getName() + HAS_BEEN_DELETED_FROM_PROJECT + project.getName() + messageContext);
    }


    @Override
    @Transactional
    public void setAssignee(Long bugId, Long assigneeId) throws UserNotFoundException, BugDoesNotExistException, UserAlreadyAssignedException
    {
        User assignee = this.userService.getUserEntityById(assigneeId);
        if(assignee == null)
        {
            throw new UserNotFoundException(COULD_NOT_FIND_THE_USER);
        }

        Bug bug = this.bugRepository.findBugById(bugId);
        if(bug == null)
        {
            throw new BugDoesNotExistException(BUG_DOES_NOT_EXIST);
        }

        Project project = bug.getProject();
        User projectLeader = project.getProjectLeader();

        User previousAssignee = bug.getAssignee();
        if(previousAssignee != null)
        {
            if(previousAssignee.getId().equals(assignee.getId()))
            {
                throw new UserAlreadyAssignedException(USER_ALREADY_ASSIGNED);
            }

            this.notificationService.sendNotificationFromTo(projectLeader.getId(), previousAssignee.getId(),
                    ASSIGNMENT_CANCELLED,
                    YOUR_ASSIGNMENT_TO + bug.getName() + HAS_BEEN_CANCELLED +
                            PLEASE_CONTACT_YOUR_PROJECT_LEADER_FOR_MORE_INFORMATION);
        }

        bug.setAssignee(assignee);
        if(bug.getStatus().equals(BugStatus.NEW))
        {
            bug.setStatus(BugStatus.ASSIGNED);
        }

        this.bugRepository.save(bug);

        this.notificationService.sendNotificationFromTo(projectLeader.getId(), assignee.getId(),
                NEW_ASSIGNMENT,
                YOU_VE_BEEN_ASSIGNED_TO + bug.getName() + PLEASE_CONTACT_YOUR_PROJECT_LEADER_FOR_MORE_INFORMATION);
    }


    @Override
    @Transactional
    public void unassignWorkerFromBug(Long bugId) throws BugDoesNotExistException, UserNotFoundException
    {
        Bug bug = this.bugRepository.findBugById(bugId);
        if(bug == null)
        {
            throw new BugDoesNotExistException(BUG_DOES_NOT_EXIST);
        }

        User previousAssignee = bug.getAssignee();
        if(previousAssignee == null)
        {
            throw new UserNotFoundException(COULD_NOT_FIND_THE_USER);
        }

        Project project = bug.getProject();
        User projectLeader = project.getProjectLeader();

        bug.setAssignee(null);

        if(bug.getStatus().equals(BugStatus.ASSIGNED))
        {
            bug.setStatus(BugStatus.NEW);
        }

        this.bugRepository.save(bug);

        this.notificationService.sendNotificationFromTo(projectLeader.getId(), previousAssignee.getId(),
                ASSIGNMENT_CANCELLED,
                YOUR_ASSIGNMENT_TO + bug.getName() + HAS_BEEN_CANCELLED +
                        PLEASE_CONTACT_YOUR_PROJECT_LEADER_FOR_MORE_INFORMATION);
    }



    @Override
    @Transactional
    public void setStatus(Long bugId, BugStatus status) throws BugDoesNotExistException
    {
        Bug bug = this.bugRepository.findBugById(bugId);
        if(bug == null)
        {
            throw new BugDoesNotExistException(BUG_DOES_NOT_EXIST);
        }

        bug.setStatus(status);
        this.bugRepository.save(bug);
    }



    @Override
    public long getCountByCreatorId(Long creatorId) {return this.bugRepository.countBugsByCreatorId(creatorId);}

    @Override
    public long getCountByCreationDateAfter(LocalDateTime date) {return this.bugRepository.countBugsByCreationDateAfter(date);}

    @Override
    public long getCountByStatus(BugStatus status)
    {
        return this.bugRepository.countBugByStatus(status);
    }

    @Override
    public long getCountByCreatorIdAndByCreationDateAfter(Long creatorId, LocalDateTime date) {return this.bugRepository.countBugsByCreatorIdAndCreationDateAfter(creatorId, date);}

    @Override
    public long getCountByAssigneeIdAndByStatus(Long userId, BugStatus status) {return this.bugRepository.countBugByAssigneeIdAndStatus(userId, status);}

    @Override
    public long getCount()
    {
        return this.bugRepository.count();
    }

    @Override
    public long getCountByProjectLeaderId(Long projectLeaderId) {return this.bugRepository.countBugsByProjectProjectLeaderId(projectLeaderId);}

    @Override
    public long getCountByProjectLeaderIdAndCreationDateAfter(Long projectLeaderId, LocalDateTime date)
    {
        return this.bugRepository.countBugsByProjectProjectLeaderIdAndCreationDateAfter(projectLeaderId, date);
    }

    @Override
    public long getCountByProjectLeaderIdAndStatus(Long projectLeaderId, BugStatus status)
    {
        return this.bugRepository.countBugsByProjectProjectLeaderIdAndStatus(projectLeaderId, status);
    }
}
