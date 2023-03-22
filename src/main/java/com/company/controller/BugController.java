package com.company.controller;

import com.company.domain.HttpResponse;
import com.company.dto.BugDTO;
import com.company.entity.Bug;
import com.company.enumeration.BugClassification;
import com.company.enumeration.BugSeverity;
import com.company.enumeration.BugStatus;
import com.company.exception.*;
import com.company.exception.ExceptionHandler;
import com.company.service.BugService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

import static com.company.constant.BugConstant.*;

@RestController
@CrossOrigin("http://localhost:4200")
@RequestMapping(path = "/bug")
public class BugController extends ExceptionHandler
{
    private final BugService bugService;


    @Autowired
    public BugController(BugService bugService)
    {
        this.bugService = bugService;
    }



    @PostMapping(path = "{creatorId}/{projectId}")
    @PreAuthorize("hasAnyAuthority('user:create', 'project_leader:create')")
    public ResponseEntity<BugDTO> addBug(@PathVariable(value = "creatorId") Long creatorId,
                                         @PathVariable(value = "projectId") Long projectId,
                                         @RequestBody Bug bug) throws UserNotFoundException, ProjectDoesNotExistException, BugDoesNotExistException, BugAlreadyExistException
    {

        BugDTO newBug = this.bugService.addOrUpdate(
                creatorId, null, projectId, null,
                bug.getName(),
                bug.getDescription(),
                bug.getClassification(),
                BugStatus.NEW,
                bug.getSeverity());

        return new ResponseEntity<>(newBug, HttpStatus.OK);
    }



    @PutMapping(path = "{currentBugId}/{editorId}/{projectId}")
    @PreAuthorize("hasAnyAuthority('user:update', 'project_leader:update')")
    public ResponseEntity<BugDTO> updateBug(@PathVariable(value = "editorId") Long editorId,
                                                  @PathVariable(value = "currentBugId") Long currentBugId,
                                                  @PathVariable(value = "projectId") Long projectId,
                                                  @RequestParam(value = "name") String newName,
                                                  @RequestParam(value = "classification") String classification,
                                                  @RequestParam(value = "status") String status,
                                                  @RequestParam(value = "severity") String severity,
                                                  @RequestParam(value = "description") String description) throws UserNotFoundException, BugDoesNotExistException, ProjectDoesNotExistException, BugAlreadyExistException
    {
        BugDTO updatedBug = bugService.addOrUpdate(
                null, editorId, projectId, currentBugId,
                newName,
                description,
                BugClassification.valueOf(classification),
                BugStatus.valueOf(status),
                BugSeverity.valueOf(severity));

        return new ResponseEntity<>(updatedBug, HttpStatus.OK);
    }



    @DeleteMapping(path = "{bugId}/{deleterId}")
    @PreAuthorize("hasAnyAuthority('user:delete', 'project_leader:delete')")
    public ResponseEntity<HttpResponse> deleteBug(@PathVariable(value = "bugId") Long bugId,
                                                  @PathVariable(value = "deleterId") Long deleterId) throws UserNotFoundException, BugDoesNotExistException, IllegalAccessException
    {
        BugDTO deletedBug = this.bugService.deleteBug(bugId, deleterId);
        return HttpResponse.createResponse(HttpStatus.OK, BUG + deletedBug.name() + DELETED_SUCCESSFULLY);
    }



    @PutMapping(path = "{bugId}/{assigneeId}/assignee")
    @PreAuthorize("hasAuthority('project_leader:update')")
    public ResponseEntity<HttpResponse> assignUserToBug(@PathVariable(value = "bugId") Long bugId,
                                                        @PathVariable(value = "assigneeId") Long assigneeId) throws UserNotFoundException, BugDoesNotExistException, UserAlreadyAssignedException
    {
        this.bugService.setAssignee(bugId, assigneeId);
        return HttpResponse.createResponse(HttpStatus.OK, WORKER_SUCCESSFULLY_ASSIGNED + NOTIFICATION_WAS_SENT);
    }



    @DeleteMapping(path = "{bugId}/assignee")
    @PreAuthorize("hasAuthority('project_leader:update')")
    public ResponseEntity<HttpResponse> unassignUserFromBug(@PathVariable(value = "bugId") Long bugId) throws UserNotFoundException, BugDoesNotExistException
    {
        this.bugService.unassignWorkerFromBug(bugId);
        return HttpResponse.createResponse(HttpStatus.OK, WORKER_SUCCESSFULLY_UNASSIGNED + NOTIFICATION_WAS_SENT);
    }



    @PutMapping("{bugId}/status")
    @PreAuthorize("hasAnyAuthority('user:update', 'project_leader:update')")
    public ResponseEntity<HttpResponse> setStatus(@PathVariable(value = "bugId") Long bugId,
                                                  @RequestBody String status) throws BugDoesNotExistException
    {
        this.bugService.setStatus(bugId, BugStatus.valueOf(status));
        return HttpResponse.createResponse(HttpStatus.OK, STATUS_SUCCESSFULLY_SET);
    }



    @GetMapping(path = "{id}")
    @PreAuthorize("hasAnyAuthority('user:read', 'project_leader:read')")
    public ResponseEntity<BugDTO> getBugById(@PathVariable(value = "id") Long bugId)
    {
        BugDTO bugById = this.bugService.getBugById(bugId);
        return new ResponseEntity<>(bugById, HttpStatus.OK);
    }



    @GetMapping(path = "bugs/project")
    @PreAuthorize("hasAnyAuthority('user:read', 'project_leader:read')")
    public ResponseEntity<Page<BugDTO>> getBugsByProjectId(@RequestParam(value = "projectId") Long projectId,
                                                           @RequestParam(value = "page") int page,
                                                           @RequestParam(value = "size") int size,
                                                           @RequestParam(value = "sortOrder") String sortOrder,
                                                           @RequestParam(value = "ascending") String ascending)
    {
        Page<BugDTO> bugsPaginate = this.bugService.getBugsByProjectId(projectId, page, size, sortOrder, Boolean.parseBoolean(ascending));
        return new ResponseEntity<>(bugsPaginate, HttpStatus.OK);
    }



    @GetMapping(path = "bugs/creator")
    @PreAuthorize("hasAnyAuthority('user:read', 'project_leader:read')")
    public ResponseEntity<Page<BugDTO>> getBugsByCreatorId(@RequestParam(value = "creatorId") Long creatorId,
                                                         @RequestParam(value = "page") int page,
                                                         @RequestParam(value = "size") int size,
                                                         @RequestParam(value = "sortOrder") String sortOrder,
                                                         @RequestParam(value = "ascending") String ascending)
    {
        Page<BugDTO> bugsPaginate = this.bugService.getBugsByCreatorId(creatorId, page, size, sortOrder, Boolean.parseBoolean(ascending));
        return new ResponseEntity<>(bugsPaginate, HttpStatus.OK);
    }



    @GetMapping(path = "bugs/assignee")
    @PreAuthorize("hasAnyAuthority('user:read')")
    public ResponseEntity<Page<BugDTO>> getBugsByAssigneeId(@RequestParam(value = "assigneeId") Long assigneeId,
                                                          @RequestParam(value = "page") int page,
                                                          @RequestParam(value = "size") int size,
                                                          @RequestParam(value = "sortOrder") String sortOrder,
                                                          @RequestParam(value = "ascending") String ascending)
    {
        Page<BugDTO> bugsPaginate = this.bugService.getBugsByAssigneeId(assigneeId, page, size, sortOrder, Boolean.parseBoolean(ascending));
        return new ResponseEntity<>(bugsPaginate, HttpStatus.OK);
    }



    @GetMapping(path = "bugs/project-leader")
    @PreAuthorize("hasAnyAuthority('project_leader:read')")
    public ResponseEntity<Page<BugDTO>> getBugsByProjectLeaderId(@RequestParam(value = "projectLeaderId") Long projectLeaderId,
                                                               @RequestParam(value = "page") int page,
                                                               @RequestParam(value = "size") int size,
                                                               @RequestParam(value = "sortOrder") String sortOrder,
                                                               @RequestParam(value = "ascending") String ascending)

    {
        Page<BugDTO> bugsPaginate = this.bugService.getBugsByProjectLeaderId(projectLeaderId, page, size, sortOrder, Boolean.parseBoolean(ascending));
        return new ResponseEntity<>(bugsPaginate, HttpStatus.OK);
    }



    @GetMapping(path = "count")
    @PreAuthorize("hasAnyAuthority('user:read', 'project_leader:read')")
    public ResponseEntity<Long> getCount()
    {
        long count = this.bugService.getCount();
        return new ResponseEntity<>(count, HttpStatus.OK);
    }



    @GetMapping(path = "count/creator/{creatorId}")
    @PreAuthorize("hasAnyAuthority('user:read')")
    public ResponseEntity<Long> getCountByCreator(@PathVariable(value = "creatorId") Long creatorId)
    {
        long count = this.bugService.getCountByCreatorId(creatorId);
        return new ResponseEntity<>(count, HttpStatus.OK);
    }



    @GetMapping(path = "count/project-leader/{projectLeaderId}")
    @PreAuthorize("hasAnyAuthority('project_leader:read')")
    public ResponseEntity<Long> getCountByProjectLeader(@PathVariable(value = "projectLeaderId") Long projectLeaderId)
    {
        long count = this.bugService.getCountByProjectLeaderId(projectLeaderId);
        return new ResponseEntity<>(count, HttpStatus.OK);
    }



    @GetMapping(path = "count/{days}/ago")
    @PreAuthorize("hasAnyAuthority('user:read', 'project_leader:read')")
    public ResponseEntity<Long> getCountCreatedAfter(@PathVariable(value = "days") int daysAgo)
    {
        LocalDateTime date = this.getDateAfter(daysAgo);
        long count = this.bugService.getCountByCreationDateAfter(date);
        return new ResponseEntity<>(count, HttpStatus.OK);
    }



    @GetMapping(path = "count/creator/date")
    @PreAuthorize("hasAnyAuthority('user:read')")
    public ResponseEntity<Long> getCountByCreatorAndCreatedAfter(@RequestParam(value = "creatorId") Long creatorId,
                                                                 @RequestParam(value = "days") int daysAgo)
    {
        LocalDateTime date = this.getDateAfter(daysAgo);
        long count = this.bugService.getCountByCreatorIdAndByCreationDateAfter(creatorId, date);
        return new ResponseEntity<>(count, HttpStatus.OK);
    }



    @GetMapping(path = "count/project-leader/date")
    @PreAuthorize("hasAnyAuthority('project_leader:read')")
    public ResponseEntity<Long> getCountByProjectLeaderAndCreatedAfter(@RequestParam(value = "projectLeaderId") Long projectLeaderId,
                                                                       @RequestParam(value = "days") int daysAgo)
    {
        LocalDateTime date = this.getDateAfter(daysAgo);
        long count = this.bugService.getCountByProjectLeaderIdAndCreationDateAfter(projectLeaderId, date);
        return new ResponseEntity<>(count, HttpStatus.OK);
    }

    private LocalDateTime getDateAfter(int daysAgo)
    {
        return LocalDateTime.now().minusDays(daysAgo);
    }



    @GetMapping(path = "count/status/{status}")
    @PreAuthorize("hasAnyAuthority('user:read', 'project_leader:read')")
    public ResponseEntity<Long> getCountByStatus(@PathVariable(value = "status") String status)
    {
        long count = this.bugService.getCountByStatus(BugStatus.valueOf(status));
        return new ResponseEntity<>(count, HttpStatus.OK);
    }



    @GetMapping(path = "count/assignee/status")
    @PreAuthorize("hasAnyAuthority('user:read')")
    public ResponseEntity<Long> getCountByAssigneeAndStatus(@RequestParam(value = "assigneeId") Long assigneeId,
                                                            @RequestParam(value = "status") String status)
    {
        long count = this.bugService.getCountByAssigneeIdAndByStatus(assigneeId, BugStatus.valueOf(status));
        return new ResponseEntity<>(count, HttpStatus.OK);
    }



    @GetMapping(path = "count/project-leader/status")
    @PreAuthorize("hasAnyAuthority('project_leader:read')")
    public ResponseEntity<Long> getCountByProjectLeaderAndStatus(@RequestParam(value = "projectLeaderId") Long projectLeaderId,
                                                                 @RequestParam(value = "status") String status)
    {
        long count = this.bugService.getCountByProjectLeaderIdAndStatus(projectLeaderId, BugStatus.valueOf(status));
        return new ResponseEntity<>(count, HttpStatus.OK);
    }
}
