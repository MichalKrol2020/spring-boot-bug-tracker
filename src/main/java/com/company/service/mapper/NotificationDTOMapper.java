package com.company.service.mapper;

import com.company.dto.NotificationDTO;
import com.company.entity.Notification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class NotificationDTOMapper implements Function<Notification, NotificationDTO>
{
    private final UserDTOMapper userDTOMapper;

    @Autowired
    public NotificationDTOMapper(UserDTOMapper userDTOMapper)
    {
        this.userDTOMapper = userDTOMapper;
    }

    @Override
    public NotificationDTO apply(Notification notification)
    {
        if(notification == null)
        {
            return null;
        }

        return new NotificationDTO(
                notification.getId(),
                notification.getTitle(),
                notification.getDescription(),
                notification.getSendDate(),
                notification.isSeen(),
                this.userDTOMapper.apply(notification.getSender()),
                this.userDTOMapper.apply(notification.getReceiver()));
    }
}
