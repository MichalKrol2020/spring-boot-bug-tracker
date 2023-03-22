package com.company.domain;

import java.time.LocalDateTime;

public record AuthenticationResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String speciality,
        String profileImageUrl,
        String role,
        LocalDateTime joinDate)
{}