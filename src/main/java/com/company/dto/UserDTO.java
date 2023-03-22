package com.company.dto;

public record UserDTO
        (
                Long id,
                String firstName,
                String lastName,
                String email,
                String speciality,
                String profileImageUrl
        ) {}
