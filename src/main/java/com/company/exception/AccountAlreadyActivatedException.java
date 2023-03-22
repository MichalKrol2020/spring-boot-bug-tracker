package com.company.exception;

public class AccountAlreadyActivatedException extends Exception
{
    public AccountAlreadyActivatedException(String message)
    {
        super(message);
    }
}
