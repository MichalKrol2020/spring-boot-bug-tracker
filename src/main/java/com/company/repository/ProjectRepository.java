package com.company.repository;

import com.company.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface ProjectRepository extends JpaRepository<Project, Long>
{
    Project findProjectById(Long projectId);

    Project findProjectByName(String name);

    Page<Project> findProjectsByParticipantsId(Long userId, Pageable pageable);

    Page<Project> findProjectsByProjectLeaderId(Long projectLeaderId, Pageable pageable);

    List<Project> findAllByProjectLeaderId(Long projectLeaderId);

    List<Project> findAllByParticipantsId(Long participantId);
}
