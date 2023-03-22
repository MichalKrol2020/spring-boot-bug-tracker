package com.company.enumeration;

import lombok.Getter;

@Getter
public enum UserSpeciality
{
    BACKEND("Backend"),
    FRONTEND("Front End"),
    DEVOPS("Devops"),
    UI_DESIGN("UI Design"),
    DATABASE_DESIGN("Database Design");

    private final String name;

    UserSpeciality(String name)
    {
        this.name = name;
    }
}
