package com.company.service;

import com.company.dto.ProjectDTO;
import com.company.entity.Project;
import com.company.entity.User;
import com.company.exception.*;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ProjectService
{
    ProjectDTO addOrUpdate(Long projectLeaderId,
                           String currentName,
                           String name,
                           String description) throws ProjectDoesNotExistException, ProjectAlreadyExistsException, UserNotFoundException;

    ProjectDTO deleteProject(Long projectId) throws ProjectDoesNotExistException, UserNotFoundException;

    void addParticipants(Long projectId, List<User> participants) throws ProjectDoesNotExistException, UserNotFoundException, UserAlreadyAssignedException;

    void unassignParticipant(Long projectId, Long participantId) throws ProjectDoesNotExistException, UserNotFoundException, UserNotAssignedException;

    Page<ProjectDTO> getProjectsByParticipantId(Long userId, int page, int size, String sortOrder, boolean ascending);

    Page<ProjectDTO> getProjectsByProjectLeaderId(Long projectLeaderId, int page, int size, String sortOrder, boolean ascending);

    List<Project> getProjectEntitiesByProjectLeaderId(Long projectLeaderId);

    List<Project> getProjectEntitiesByParticipantId(Long participantId);

    Project getProjectEntityById(Long projectId);
}
