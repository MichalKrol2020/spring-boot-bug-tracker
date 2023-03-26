package com.company.exception;

public class PasswordsMatchException extends Exception
{
    public PasswordsMatchException(String message)
    {
        super(message);
    }
}
