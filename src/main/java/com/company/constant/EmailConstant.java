package com.company.constant;

public class EmailConstant
{
    public static final String SIMPLE_MAIL_TRANSFER_PROTOCOL = "smtp";
    public static final String USERNAME = "user@email.com";
    public static final String PASSWORD = "password";
    public static final String FROM_EMAIL = "user@email.com";
    public static final String CC_EMAIL = "";
    public static final String EMAIL_SUBJECT = "Company - New Password";
    public static final String SMTP_SERVER = "smtp";
    public static final String SMTP_PROTOCOLS = "mail.smtp.ssl.protocols";
    public static final String TLS_PROTOCOL = "TLSv1.2";
    public static final String MAIL_SMTP_SSL_TRUST = "mail.smtp.ssl.trust";
    public static final String SMTP_HOST = "mail.smtp.host";
    public static final String SMTP_AUTH = "mail.smtp.auth";
    public static final String SMTP_PORT = "mail.smtp.port";
    public static final int DEFAULT_PORT = 123;
    public static final String SMTP_STARTTLS_ENABLE = "mail.smtp.starttls.enable";
    public static final String SMTP_STARTTLS_REQUIRED = "mail.smtp.starttls.required";
    public static final String MIME_TYPE = "text/html; charset=utf-8";
    public static final String NEW_LINE_HTML = "<br>";
    public static final String RESET_PASSWORD = "Reset password!";

    public static final String ACTIVATION_MESSAGE =   "Hello Dear %s!" +
                                                      NEW_LINE_HTML +
                                                      "Confirmation link:" +
                                                      NEW_LINE_HTML +
                                                      "<a href='%s'>%s</a>" +
                                                      NEW_LINE_HTML +
                                                      NEW_LINE_HTML +
                                                      "The support team :)";

    public static final String RESET_PASSWORD_MESSAGE = "Hello Dear %s!" +
                                                        NEW_LINE_HTML +
                                                        "Reset password link: " +
                                                        NEW_LINE_HTML +
                                                        "<a href='%s'>%s</a>" +
                                                        NEW_LINE_HTML +
                                                        NEW_LINE_HTML +
                                                        "The support team :)";

}
