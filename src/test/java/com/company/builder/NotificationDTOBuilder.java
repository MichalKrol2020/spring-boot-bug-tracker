package com.company.builder;

import com.company.dto.NotificationDTO;
import com.company.dto.UserDTO;

import java.time.LocalDateTime;

public class NotificationDTOBuilder
{
    private Long id;
    private String title;
    private String description;
    private LocalDateTime sendDate;
    private boolean seen;
    private UserDTO sender;
    private UserDTO receiver;

    public NotificationDTOBuilder()
    {
        long id = 1L;
        this.id = id;
        this.title = "NOTIFICATION_" + id;
        this.description = "DESCRIPTION_" + id;
        this.sendDate = LocalDateTime.now();
        this.seen = false;
        this.sender = null;
        this.receiver = null;
    }

    public NotificationDTOBuilder withId(Long id)
    {
        this.id = id;
        return this;
    }


    public NotificationDTOBuilder withTitle(String title)
    {
        this.title = title;
        return this;
    }


    public NotificationDTOBuilder withDescription(String description)
    {
        this.description = description;
        return this;
    }


    public NotificationDTOBuilder withSendDate(LocalDateTime sendDate)
    {
        this.sendDate = sendDate;
        return this;
    }


    public NotificationDTOBuilder withSeen(boolean seen)
    {
        this.seen = seen;
        return this;
    }


    public NotificationDTOBuilder withSender(UserDTO sender)
    {
        this.sender = sender;
        return this;
    }


    public NotificationDTOBuilder withReceiver(UserDTO receiver)
    {
        this.receiver = receiver;
        return this;
    }


    public NotificationDTO build()
    {
        return new NotificationDTO
                (
                        this.id,
                        this.title,
                        this.description,
                        this.sendDate,
                        this.seen,
                        this.sender,
                        this.receiver
                );
    }
}
