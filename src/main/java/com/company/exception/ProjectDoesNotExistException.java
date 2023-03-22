package com.company.exception;

public class ProjectDoesNotExistException extends Exception
{
    public ProjectDoesNotExistException(String message)
    {
        super(message);
    }
}
