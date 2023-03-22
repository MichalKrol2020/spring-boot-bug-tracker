package com.company.dto;

import java.time.LocalDateTime;

public record BugDTO
        (
                Long id,
                String name,
                String description,
                String classification,
                String status,
                String severity,
                LocalDateTime creationDate,
                UserDTO creator,
                UserDTO assignee,
                ProjectDTO project
        )
{}
