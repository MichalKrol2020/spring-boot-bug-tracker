package com.company.enumeration;

import lombok.Getter;

@Getter
public enum BugClassification
{
    FUNCTIONAL("Functional"),
    PERFORMANCE("Performance"),
    USABILITY("Usability"),
    COMPATIBILITY("Compatibility"),
    SECURITY("Security");

    private final String name;
    BugClassification(String name)
    {
        this.name = name;
    }
}
