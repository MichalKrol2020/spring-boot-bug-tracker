package com.company.domain;

public record AuthenticationRequest
        (
                String email,
                String password
        )
{}
