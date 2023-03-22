package com.company.exception;

public class ProjectAlreadyExistsException extends Exception
{
     public ProjectAlreadyExistsException(String message)
     {
         super(message);
     }
}
