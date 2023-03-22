package com.company.exception;

public class EmptyListException extends RuntimeException
{
    public EmptyListException(String message)
    {
        super(message);
    }
}
