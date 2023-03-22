package com.company.exception;

public class EmailAlreadySentException extends Exception
{
    public EmailAlreadySentException(String message)
    {
        super(message);
    }
}
