package com.company.dto;

import java.time.LocalDateTime;

public record NotificationDTO(
        Long id,
        String title,
        String description,
        LocalDateTime sendDate,
        boolean seen,
        UserDTO sender,
        UserDTO receiver
)
{}
