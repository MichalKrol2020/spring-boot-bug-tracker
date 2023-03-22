package com.company.constant;

public class SecurityConstant
{
    public static final long EXPIRATION_TIME = 86_400_000;
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String JWT_TOKEN_HEADER = "Jwt-Token";
    public static final String TOKEN_CANNOT_BE_VERIFIED = "Token cannot be verified";
    public static final String ISSUER = "Company Name";
    public static final String AUDIENCE = "User Management Portal";
    public static final String AUTHORITIES = "authorities";
    public static final String FORBIDDEN_MESSAGE = "You need to log in to access this page!";
    public static final String ACCESS_DENIED_MESSAGE = "You don't have permission to access this page!";
    public static final String OPTIONS_HTTP_METHOD = "OPTIONS";
    public static final String [] PUBLIC_URLS = {"/authentication/login", "/authentication/register", "/user/image/**", "/user/account/**"};

    public static final String ALLOWED_ORIGIN = "http://localhost:4200";
    public static final String [] ALLOWED_HEADERS = {"Origin", "Access-Control-Allow-Origin", "Content-Type",
                                                     "Accept", "Jwt-Token", "Authorization", "Origin, Accept", "X-Requested-With",
                                                     "Access-Control-Request-Method", "Access-Control-Request-Headers", "Content-Disposition"};

    public static final String [] EXPOSED_HEADERS = {"Origin", "Content-Type", "Accept", "Jwt-Token", "Authorization",
                                                     "Access-Control-Allow-Origin", "Access-Control-Allow-Credentials", "Content-Disposition"};

    public static final String [] ALLOWED_METHODS = {"GET", "POST", "PUT", "DELETE", "OPTIONS"};

    public static final String PATTERN = "/**";
}