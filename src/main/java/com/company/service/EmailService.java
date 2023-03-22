package com.company.service;

import javax.mail.MessagingException;

public interface EmailService
{
    void sendNewEmail(String email, String mailMessage) throws MessagingException;
}
