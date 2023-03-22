package com.company.service;

import com.company.dto.NotificationDTO;
import com.company.exception.NotificationNotFoundException;
import com.company.exception.UserNotFoundException;
import org.springframework.data.domain.Page;

public interface NotificationService
{
    void sendNotificationFromTo(Long senderId,
                                Long receiverId,
                                String title,
                                String description) throws UserNotFoundException;

    Page<NotificationDTO> getNotificationsByReceiverId(Long receiverId, int page, int size);

    NotificationDTO getNotificationById(Long notificationId);

    int getCountNotificationsUnseen(Long receiverId);

    int getIndexOfNotificationRecord(Long notificationId, Long receiverId);

    void setSeen(Long notificationId) throws NotificationNotFoundException;
}
