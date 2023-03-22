package com.company.exception;

public class UserAlreadyAssignedException extends Exception
{
    public UserAlreadyAssignedException(String message)
    {
        super(message);
    }
}
