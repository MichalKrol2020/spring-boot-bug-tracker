package com.company.exception;

public class PasswordAlreadyChangedException extends Exception
{
    public PasswordAlreadyChangedException(String message)
    {
        super(message);
    }
}
