package com.company.enumeration;

import lombok.Getter;

@Getter
public enum BugStatus
{
    NEW("New"),
    ASSIGNED("Assigned"),
    OPEN("Open"),
    FIXED("Fixed"),
    PENDING_RETEST("Pending Retest"),
    RETEST("Retest"),
    VERIFIED("Verified"),
    CLOSED("Closed");

    private final String name;

    BugStatus(String name)
    {
        this.name = name;
    }
}
