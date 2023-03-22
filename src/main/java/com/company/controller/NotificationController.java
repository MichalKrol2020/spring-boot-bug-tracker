package com.company.controller;

import com.company.dto.NotificationDTO;
import com.company.exception.ExceptionHandler;
import com.company.exception.NotificationNotFoundException;
import com.company.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin("http://localhost:4200")
@RequestMapping("/notification")
public class NotificationController extends ExceptionHandler
{
    private final NotificationService notificationService;


    @Autowired
    public NotificationController(NotificationService notificationService)
    {
        this.notificationService = notificationService;
    }



    @GetMapping
    @PreAuthorize("hasAnyAuthority('user:read', 'project_leader:read')")
    public ResponseEntity<Page<NotificationDTO>> getNotificationsByReceiverId(@RequestParam long receiverId,
                                                                              @RequestParam int page,
                                                                              @RequestParam int size)
    {
        Page<NotificationDTO> notifications = this.notificationService.getNotificationsByReceiverId(receiverId, page, size);
        return new ResponseEntity<>(notifications, HttpStatus.OK);
    }



    @GetMapping(path = "{notificationId}")
    @PreAuthorize("hasAnyAuthority('user:read', 'project_leader:read')")
    public ResponseEntity<NotificationDTO> getNotificationById(@PathVariable long notificationId)
    {
        NotificationDTO notification = this.notificationService.getNotificationById(notificationId);
        return new ResponseEntity<>(notification, HttpStatus.OK);
    }



    @GetMapping(path = "{receiverId}/unseen")
    @PreAuthorize("hasAnyAuthority('user:read', 'project_leader:read')")
    public ResponseEntity<Integer> getCountNotificationsUnseen(@PathVariable() long receiverId)
    {
        int count = this.notificationService.getCountNotificationsUnseen(receiverId);
        return new ResponseEntity<>(count, HttpStatus.OK);
    }



    @GetMapping(path = "index/{notificationId}/{receiverId}")
    @PreAuthorize("hasAnyAuthority('user:read', 'project_leader:read')")
    public ResponseEntity<Integer> getIndexOfNotificationRecord(@PathVariable() long notificationId,
                                                                @PathVariable() long receiverId)
    {
        int count = this.notificationService.getIndexOfNotificationRecord(notificationId, receiverId);
        return new ResponseEntity<>(count, HttpStatus.OK);
    }



    @PutMapping(path = "{notificationId}/seen")
    @PreAuthorize("hasAnyAuthority('user:update', 'project_leader:update')")
    public ResponseEntity<Void> setNotificationSeen(@PathVariable() long notificationId) throws NotificationNotFoundException
    {
        this.notificationService.setSeen(notificationId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
