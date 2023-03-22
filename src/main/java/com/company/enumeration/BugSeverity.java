package com.company.enumeration;

import lombok.Getter;

@Getter
public enum BugSeverity
{
    LOW("Low"),
    MEDIUM("Medium"),
    HIGH("High"),
    CRITICAL("Critical");

    public final String name;
    BugSeverity(String name)
    {
        this.name = name;
    }
}
