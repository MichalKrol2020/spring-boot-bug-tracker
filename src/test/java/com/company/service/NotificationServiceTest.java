package com.company.service;

import com.company.builder.*;
import com.company.dto.NotificationDTO;
import com.company.dto.UserDTO;
import com.company.entity.Notification;
import com.company.entity.User;
import com.company.exception.NotificationNotFoundException;
import com.company.exception.UserNotFoundException;
import com.company.repository.NotificationRepository;
import com.company.service.mapper.NotificationDTOMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;

import static com.company.constant.NotificationConstant.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ContextConfiguration(classes = {NotificationServiceImpl.class})
@ExtendWith(SpringExtension.class)
public class NotificationServiceTest
{
    @MockBean
    private NotificationRepository notificationRepository;

    @MockBean
    private UserService userService;

    @MockBean
    private NotificationDTOMapper notificationDTOMapper;

    @Captor
    private ArgumentCaptor<Notification> notificationCaptor;

    @Autowired
    private NotificationService underTest;

    private User sender;
    private User receiver;
    private Notification notification;
    private NotificationDTO notificationDTO;
    private List<Notification> entitiesList;
    private List<NotificationDTO> notificationDTOList;

    @BeforeEach
    public void setUp()
    {
        this.sender = new UserBuilder()
                .build();

        this.receiver = new UserBuilder()
                .withId(2L)
                .build();

        String title = "TITLE_1";
        String description = "DESCRIPTION_1";

        this.notification = new Notification
                (
                        title,
                        description,
                        this.sender,
                        this.receiver
                );

        UserDTO senderDTO = new UserDTOBuilder()
                .build();

        UserDTO receiverDTO = new UserDTOBuilder()
                .withId(2L)
                .build();

        this.notificationDTO = new NotificationDTOBuilder()
                .withSender(senderDTO)
                .withReceiver(receiverDTO)
                .build();

        this.entitiesList = new ArrayList<>();
        this.notificationDTOList = new ArrayList<>();
        for(int i = 0; i < 10; i++)
        {
            NotificationDTO dtoItem = new NotificationDTOBuilder()
                    .withSender(senderDTO)
                    .withReceiver(receiverDTO)
                    .build();

            Notification notificationItem =
                    new Notification(title, description, this.sender, this.receiver);

            this.notificationDTOList.add(dtoItem);
            this.entitiesList.add(notificationItem);
        }
    }


    @Test
    public void testSendNotificationFromTo_Should_ThrowException_When_SenderNotFound()
    {
        Long senderId = 5L;
        Long receiverId = 2L;
        String title = "TITLE_1";
        String description = "DESCRIPTION_1";

        when(this.userService.getUserEntityById(senderId)).thenReturn(null);
        when(this.userService.getUserEntityById(receiverId)).thenReturn(this.receiver);

        Exception exception = assertThrows
                (UserNotFoundException.class, () -> this.underTest.sendNotificationFromTo(senderId, receiverId, title, description));
        assertEquals(NOTIFICATION_CANNOT_BE_SENT + SENDER_NOT_FOUND, exception.getMessage());
    }


    @Test
    public void testSendNotificationFromTo_Should_ThrowException_When_ReceiverNotFound()
    {
        Long senderId = 1L;
        Long receiverId = 5L;
        String title = "TITLE_1";
        String description = "DESCRIPTION_1";

        when(this.userService.getUserEntityById(senderId)).thenReturn(this.sender);
        when(this.userService.getUserEntityById(receiverId)).thenReturn(null);

        Exception exception = assertThrows
                (UserNotFoundException.class, () -> this.underTest.sendNotificationFromTo(senderId, receiverId, title, description));
        assertEquals(NOTIFICATION_CANNOT_BE_SENT + RECEIVER_NOT_FOUND, exception.getMessage());
    }



    @Test
    public void testSendNotificationFromTo_Should_SendNotification() throws UserNotFoundException
    {
        Long senderId = 1L;
        Long receiverId = 2L;
        String title = "TITLE_1";
        String description = "DESCRIPTION_1";

        when(this.userService.getUserEntityById(senderId)).thenReturn(this.sender);
        when(this.userService.getUserEntityById(receiverId)).thenReturn(this.receiver);

        this.underTest.sendNotificationFromTo(senderId, receiverId, title, description);

        verify(this.notificationRepository).save(this.notificationCaptor.capture());
        Notification savedNotification = this.notificationCaptor.getValue();
        assertEquals(senderId, savedNotification.getSender().getId());
        assertEquals(receiverId, savedNotification.getReceiver().getId());
        assertEquals(title, savedNotification.getTitle());
        assertEquals(description, savedNotification.getDescription());
    }



    @Test
    public void testSetSeen_Should_ThrowException_When_NotificationNotFound()
    {
        Long notificationId = 5L;

        when(this.notificationRepository.getNotificationById(notificationId)).thenReturn(null);

        Exception exception = assertThrows
                (NotificationNotFoundException.class, () -> this.underTest.setSeen(notificationId));
        assertEquals(NOTIFICATION_NOT_FOUND, exception.getMessage());
    }


    @Test
    public void testSetSeen_Should_SetSeen() throws NotificationNotFoundException
    {
        Long notificationId = 1L;

        when(this.notificationRepository.getNotificationById(notificationId)).thenReturn(this.notification);

        assertFalse(this.notification.isSeen());

        this.underTest.setSeen(notificationId);

        verify(this.notificationRepository).save(this.notificationCaptor.capture());
        Notification savedNotification = this.notificationCaptor.getValue();
        assertTrue(savedNotification.isSeen());
    }


    @Test
    public void testGetNotificationsByReceiverId_Should_ReturnNotificationDTOPage()
    {
        Long receiverId = 2L;
        int page = 1;
        int size = 5;

        Page<Notification> entitiesPage = new PageImpl<>(this.entitiesList.subList(0, size));
        Pageable pageable = PageRequest.of(page, size);

        when(this.notificationRepository
                .getNotificationsByReceiverIdOrderBySendDateDesc(receiverId, pageable)).thenReturn(entitiesPage);

        when(this.notificationDTOMapper.apply(any(Notification.class)))
                .thenReturn(this.notificationDTOList.get(0), this.notificationDTOList.subList(1, size).toArray(new NotificationDTO[size]));

        Page<NotificationDTO> result = this.underTest.getNotificationsByReceiverId(receiverId, page, size);

        verify(this.notificationRepository).getNotificationsByReceiverIdOrderBySendDateDesc(receiverId, pageable);

        verify(this.notificationDTOMapper, times(size)).apply(any(Notification.class));

        assertEquals(size, result.getContent().size());
        for(int i = 0; i < size; i++)
        {
            NotificationDTO expected = this.notificationDTOList.get(i);
            NotificationDTO actual = result.getContent().get(i);
            assertEquals(expected, actual);
        }
    }


    @Test
    public void testGetNotificationById_Should_ReturnNotification()
    {
        Long notificationId = 1L;

        when(this.notificationRepository.getNotificationById(notificationId)).thenReturn(this.notification);
        when(this.notificationDTOMapper.apply(this.notification)).thenReturn(this.notificationDTO);

        NotificationDTO result = this.underTest.getNotificationById(notificationId);
        assertEquals(this.notificationDTO, result);
    }


    @Test
    public void testGetCountNotificationsUnseen_Should_ReturnNotificationsCount()
    {
        Long receiverId = 2L;

        int expectedCount = 7;
        when(this.notificationRepository.countAllByReceiverIdAndSeen(receiverId, false)).thenReturn(expectedCount);

        int result = this.underTest.getCountNotificationsUnseen(receiverId);
        assertEquals(expectedCount, result);
    }


    @Test
    public void testGetIndexOfNotificationRecord_Should_ReturnNotificationIndex()
    {
        Long notificationId = 1L;
        Long receiverId = 2L;

        int expectedCount = 7;
        when(this.notificationRepository.countAllByIdAfterAndReceiverId(notificationId, receiverId)).thenReturn(expectedCount);

        int result = this.underTest.getIndexOfNotificationRecord(notificationId, receiverId);
        assertEquals(expectedCount, result);
    }
}
