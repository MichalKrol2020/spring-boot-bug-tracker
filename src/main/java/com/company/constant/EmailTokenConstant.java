package com.company.constant;

public class EmailTokenConstant
{
    public static final String TOKEN_NOT_FOUND = "Token not found!";
    public static final String INVALID_TOKEN = "Token is invalid!";
    public static final String PASSWORD_ALREADY_CHANGED = "Password has already been changed!";
    public static final String EMAIL_ALREADY_SENT = "Email has already been sent! Please check your mailbox.";
    public static final String ACCOUNT_ALREADY_ACTIVATED = "User already confirmed!";
    public static final String TOKEN_EXPIRED = "Token expired!";

    public static final int RESET_PASSWORD_TOKEN_LIFE_DURATION_MINUTES = 30;
    public static final int ACTIVATE_ACCOUNT_TOKEN_LIFE_DURATION_MINUTES = 15;
}
