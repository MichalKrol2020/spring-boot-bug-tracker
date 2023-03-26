package com.company.exception;

public class RoleDoesNotExistException extends Exception
{
    public RoleDoesNotExistException(String message)
    {
        super(message);
    }
}
