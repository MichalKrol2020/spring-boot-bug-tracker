package com.company.controller;

import com.company.domain.HttpResponse;
import com.company.dto.ProjectDTO;
import com.company.entity.Project;
import com.company.entity.User;
import com.company.exception.*;
import com.company.exception.ExceptionHandler;
import com.company.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.company.constant.ProjectConstant.PARTICIPANTS_ADDED_SUCCESSFULLY;
import static com.company.constant.ProjectConstant.PARTICIPANT_UNASSIGNED_SUCCESSFULLY;

@RestController
@CrossOrigin("http://localhost:4200")
@RequestMapping(path = "/project")
public class ProjectController extends ExceptionHandler
{
    private final ProjectService projectService;

    @Autowired
    public ProjectController(ProjectService projectService)
    {
        this.projectService = projectService;
    }


    @PostMapping(path = "{projectLeaderId}")
    @PreAuthorize("hasAnyAuthority('project_leader:create')")
    public ResponseEntity<ProjectDTO> addProject(@PathVariable(value = "projectLeaderId") Long projectLeaderId,
                                                 @RequestBody Project project) throws ProjectDoesNotExistException, ProjectAlreadyExistsException, UserNotFoundException
    {
        ProjectDTO newProject = this.projectService.addOrUpdate(projectLeaderId, null, project.getName(), project.getDescription());
        return new ResponseEntity<>(newProject, HttpStatus.OK);
    }



    @PutMapping(path = "{projectLeaderId}")
    @PreAuthorize("hasAnyAuthority('project_leader:update')")
    public ResponseEntity<ProjectDTO> updateProject(@PathVariable(value = "projectLeaderId") Long projectLeaderId,
                                                  @RequestParam(value = "currentName") String currentName,
                                                  @RequestParam(value = "newName") String newName,
                                                  @RequestParam(value = "newDescription") String newDescription) throws ProjectDoesNotExistException, ProjectAlreadyExistsException, UserNotFoundException
    {
        ProjectDTO editedProject = this.projectService.addOrUpdate(projectLeaderId, currentName, newName, newDescription);
        return new ResponseEntity<>(editedProject, HttpStatus.OK);
    }



    @DeleteMapping(path = "{projectId}")
    @PreAuthorize("hasAnyAuthority('project_leader:delete')")
    public ResponseEntity<ProjectDTO> deleteProject(@PathVariable(value = "projectId") Long projectId) throws ProjectDoesNotExistException, UserNotFoundException
    {
        ProjectDTO deletedProject = this.projectService.deleteProject(projectId);
        return new ResponseEntity<>(deletedProject, HttpStatus.OK);
    }



    @PutMapping(path = "{projectId}/participants")
    @PreAuthorize("hasAnyAuthority('project_leader:update')")
    public ResponseEntity<HttpResponse> addParticipants(@PathVariable(value = "projectId") Long projectId,
                                                        @RequestBody List<User> participants) throws ProjectDoesNotExistException, UserNotFoundException, UserAlreadyAssignedException
    {
        this.projectService.addParticipants(projectId, participants);
        return HttpResponse.createResponse(HttpStatus.OK, PARTICIPANTS_ADDED_SUCCESSFULLY);
    }



    @DeleteMapping(path = "{projectId}/{participantId}")
    @PreAuthorize("hasAnyAuthority('project_leader:update')")
    public ResponseEntity<HttpResponse> unassignParticipant(@PathVariable(value = "projectId") Long projectId,
                                                            @PathVariable(value = "participantId") Long participantId) throws ProjectDoesNotExistException, UserNotFoundException, UserNotAssignedException
    {
        this.projectService.unassignParticipant(projectId, participantId);
        return HttpResponse.createResponse(HttpStatus.OK, PARTICIPANT_UNASSIGNED_SUCCESSFULLY);
    }



    @GetMapping(path = "projects/participant")
    @PreAuthorize("hasAnyAuthority('user:read')")
    public ResponseEntity<Page<ProjectDTO>> getProjectsByParticipantId(@RequestParam("participantId") Long participantId,
                                                                       @RequestParam("page") int page,
                                                                       @RequestParam("size") int size,
                                                                       @RequestParam("sortOrder") String sortOrder,
                                                                       @RequestParam("ascending") String ascending)
    {
        Page<ProjectDTO> projects = projectService.getProjectsByParticipantId(participantId, page, size, sortOrder, Boolean.parseBoolean(ascending));
        return new ResponseEntity<>(projects, HttpStatus.OK);
    }



    @GetMapping("projects/project-leader")
    @PreAuthorize("hasAnyAuthority('project_leader:read')")
    public ResponseEntity<Page<ProjectDTO>> getProjectsByProjectLeaderId(@RequestParam("projectLeaderId") Long projectLeaderId,
                                                                         @RequestParam("page") int page,
                                                                         @RequestParam("size") int size,
                                                                         @RequestParam("sortOrder") String sortOrder,
                                                                         @RequestParam("ascending") String ascending)
    {
        Page<ProjectDTO> projects = projectService.getProjectsByProjectLeaderId(projectLeaderId, page, size, sortOrder, Boolean.parseBoolean(ascending));
        return new ResponseEntity<>(projects, HttpStatus.OK);
    }
}

