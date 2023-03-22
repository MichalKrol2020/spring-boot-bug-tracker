package com.company.exception;

public class EmailTokenExpiredException extends Exception
{
    public EmailTokenExpiredException(String message)
    {
        super(message);
    }
}
