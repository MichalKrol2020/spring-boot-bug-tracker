package com.company.service;

import com.company.dto.BugDTO;
import com.company.entity.Bug;
import com.company.enumeration.BugClassification;
import com.company.enumeration.BugSeverity;
import com.company.enumeration.BugStatus;
import com.company.exception.*;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

public interface BugService
{
    BugDTO addOrUpdate(Long creatorId,
                       Long editorId,
                       Long projectId,
                       Long currentBugId,
                       String newName,
                       String description,
                       BugClassification classification,
                       BugStatus status,
                       BugSeverity severity) throws ProjectDoesNotExistException, UserNotFoundException, BugDoesNotExistException, BugAlreadyExistException;

    BugDTO deleteBug(Long bugId, Long deleterId) throws BugDoesNotExistException, UserNotFoundException, IllegalAccessException;

    BugDTO getBugById(Long bugId);

    Page<BugDTO> getBugsByProjectId(Long projectId, int page, int size, String sortOrder, boolean ascending);

    List<Bug> getBugEntityListByProjectId(Long projectId);

    Page<BugDTO> getBugsByCreatorId(Long creatorId, int page, int size, String sortOrder, boolean ascending);

    Page<BugDTO> getBugsByAssigneeId(Long assigneeId, int page, int size, String sortOrder, boolean ascending);

    Page<BugDTO> getBugsByProjectLeaderId(Long projectLeaderId, int page, int size, String sortOrder, boolean ascending);

    void setAssignee(Long bugId, Long assigneeId) throws UserNotFoundException, BugDoesNotExistException, UserAlreadyAssignedException;

    void unassignWorkerFromBug(Long bugId) throws BugDoesNotExistException, UserNotFoundException;

    void setStatus(Long bugId, BugStatus status) throws BugDoesNotExistException;

//  ALL BUGS
    long getCount();
    long getCountByCreatorId(Long creatorId);
    long getCountByProjectLeaderId(Long projectLeaderId);

//  BY CREATION DATE
    long getCountByCreationDateAfter(LocalDateTime date);
    long getCountByCreatorIdAndByCreationDateAfter(Long userId, LocalDateTime date);
    long getCountByProjectLeaderIdAndCreationDateAfter(Long projectLeaderId, LocalDateTime date);

//  BY STATUS
    long getCountByStatus(BugStatus status);
    long getCountByAssigneeIdAndByStatus(Long userId, BugStatus status);
    long getCountByProjectLeaderIdAndStatus(Long projectLeaderId, BugStatus status);

}
