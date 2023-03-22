package com.company.service;

import com.company.dto.NotificationDTO;
import com.company.entity.Notification;
import com.company.entity.User;
import com.company.exception.NotificationNotFoundException;
import com.company.exception.UserNotFoundException;
import com.company.repository.NotificationRepository;
import com.company.service.mapper.NotificationDTOMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.company.constant.NotificationConstant.*;
import static com.company.constant.UserConstant.USER_NOT_FOUND;

@Service
@Transactional(readOnly = true)
public class NotificationServiceImpl implements NotificationService
{
    private final NotificationRepository notificationRepository;
    private final UserService userService;

    private final NotificationDTOMapper notificationDTOMapper;

    @Autowired
    public NotificationServiceImpl(NotificationRepository notificationRepository,
                                   UserService userService,
                                   NotificationDTOMapper notificationDTOMapper)
    {
        this.notificationRepository = notificationRepository;
        this.userService = userService;
        this.notificationDTOMapper = notificationDTOMapper;
    }


    @Override
    @Transactional
    public void sendNotificationFromTo(Long senderId,
                                       Long receiverId,
                                       String title,
                                       String description) throws UserNotFoundException
    {
        User sender = this.userService.getUserEntityById(senderId);
        if (sender == null)
        {
            throw new UserNotFoundException(NOTIFICATION_CANNOT_BE_SENT + SENDER_NOT_FOUND);
        }

        User receiver = this.userService.getUserEntityById(receiverId);
        if(receiver == null)
        {
            throw new UserNotFoundException(NOTIFICATION_CANNOT_BE_SENT + RECEIVER_NOT_FOUND);
        }

        Notification notification
                = new Notification(title, description, sender, receiver);

        this.notificationRepository.save(notification);
    }



    @Override
    @Transactional
    public void setSeen(Long notificationId) throws NotificationNotFoundException
    {
        Notification notification = this.notificationRepository.getNotificationById(notificationId);
        if(notification == null)
        {
            throw new NotificationNotFoundException(NOTIFICATION_NOT_FOUND);
        }

        notification.setSeen(true);
        this.notificationRepository.save(notification);
    }



    @Override
    public Page<NotificationDTO> getNotificationsByReceiverId(Long receiverId, int page, int size)
    {
        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> entitiesPage = this.notificationRepository.getNotificationsByReceiverIdOrderBySendDateDesc(receiverId, pageable);

        return entitiesPage.map(this.notificationDTOMapper);
    }



    @Override
    public NotificationDTO getNotificationById(Long notificationId)
    {
        Notification notificationEntity = this.notificationRepository.getNotificationById(notificationId);
        return this.notificationDTOMapper.apply(notificationEntity);
    }


    @Override
    public int getCountNotificationsUnseen(Long receiverId) {return this.notificationRepository.countAllByReceiverIdAndSeen(receiverId, false);}


    @Override
    public int getIndexOfNotificationRecord(Long notificationId, Long receiverId)
    {
        return this.notificationRepository.countAllByIdAfterAndReceiverId(notificationId, receiverId);
    }
}
