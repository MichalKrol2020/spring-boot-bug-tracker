package com.company.service;

import com.sun.mail.smtp.SMTPTransport;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.Session;

import static com.company.constant.EmailConstant.*;

@Service
public class EmailServiceImpl implements EmailService
{
    @Override
    public void sendNewEmail(String email, String mailMessage) throws MessagingException
    {
        Message message = createEmail(email, mailMessage);
        SMTPTransport smtpTransport = (SMTPTransport) getEmailSession().getTransport(SIMPLE_MAIL_TRANSFER_PROTOCOL);
        smtpTransport.connect(SMTP_SERVER, USERNAME, PASSWORD);
        smtpTransport.sendMessage(message, message.getAllRecipients());
        smtpTransport.close();
    }

    private Message createEmail(String email, String mailMessage) throws MessagingException
    {
        Message message = new MimeMessage(getEmailSession());
        message.setFrom(new InternetAddress(FROM_EMAIL));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email, false));
        message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(CC_EMAIL, false));
        message.setSubject(EMAIL_SUBJECT);

        message.setContent(mailMessage, MIME_TYPE);
        message.setSentDate(new Date());
        message.saveChanges();

        return message;
    }

    private Session getEmailSession()
    {
        Properties properties = System.getProperties();
        properties.put(SMTP_HOST, SMTP_SERVER);
        properties.put(MAIL_SMTP_SSL_TRUST, SMTP_SERVER);
        properties.put(SMTP_PROTOCOLS, TLS_PROTOCOL);
        properties.put(SMTP_AUTH, true);
        properties.put(SMTP_PORT, DEFAULT_PORT);
        properties.put(SMTP_STARTTLS_ENABLE, true);
        properties.put(SMTP_STARTTLS_REQUIRED, true);

        return Session.getInstance(properties, null);
    }
}
