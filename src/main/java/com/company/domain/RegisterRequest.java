package com.company.domain;


public record RegisterRequest
        (
                String firstName,
                String lastName,
                String password,
                String email,
                String speciality
        )
{}
