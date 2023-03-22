package com.company.repository;

import com.company.entity.Bug;
import com.company.enumeration.BugStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface BugRepository extends JpaRepository<Bug, Long>
{
    Page<Bug> getBugsByProject_Id(Long id, Pageable pageable);

    List<Bug> getAllByProjectIdOrderByCreationDateDesc(Long projectId);

    Page<Bug> getBugsByCreatorId(Long creatorId, Pageable pageable);

    Page<Bug> getBugsByAssigneeId(Long assigneeId, Pageable pageable);

    Page<Bug> getBugsByProjectProjectLeaderId(Long projectLeader, Pageable pageable);

    Bug findByNameAndProjectId(String name, Long projectId);

    Bug findBugById(Long id);

//  ALL BUGS
    long countBugsByCreatorId(Long creatorId);
    long countBugsByProjectProjectLeaderId(Long projectLeaderId);

//  BY CREATION DATE
    long countBugsByCreationDateAfter(LocalDateTime date);
    long countBugsByCreatorIdAndCreationDateAfter(Long creatorId, LocalDateTime date);
    long countBugsByProjectProjectLeaderIdAndCreationDateAfter(Long projectLeaderId, LocalDateTime date);

//  BY STATUS
    long countBugByStatus(BugStatus status);
    long countBugByAssigneeIdAndStatus(Long assigneeId, BugStatus status);
    long countBugsByProjectProjectLeaderIdAndStatus(Long projectLeaderId, BugStatus status);
}
