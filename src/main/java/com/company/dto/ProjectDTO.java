package com.company.dto;

public record ProjectDTO
        (
                Long id,
                String name,
                String description,
                UserDTO projectLeader
        )
{}
