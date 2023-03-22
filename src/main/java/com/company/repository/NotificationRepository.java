package com.company.repository;

import com.company.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long>
{
    Page<Notification> getNotificationsByReceiverIdOrderBySendDateDesc(Long receiverId, Pageable pageable);

    Notification getNotificationById(Long notificationId);

    int countAllByReceiverIdAndSeen(Long receiveId, boolean seen);

    int countAllByIdAfterAndReceiverId(Long notificationId, Long receiverId);
}
