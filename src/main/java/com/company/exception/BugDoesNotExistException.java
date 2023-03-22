package com.company.exception;

public class BugDoesNotExistException extends Exception
{
    public BugDoesNotExistException(String message)
    {
        super(message);
    }
}
