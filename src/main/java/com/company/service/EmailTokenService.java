package com.company.service;

import com.company.entity.EmailToken;
import com.company.entity.User;
import com.company.enumeration.TokenPurpose;
import com.company.exception.*;

public interface EmailTokenService
{
    void save(EmailToken emailToken);
    void delete(EmailToken emailToken);
    void setUseDate(String token) throws EmailTokenExpiredException, EmailTokenNotFoundException;

    EmailToken getEmailTokenByToken(String token);
    EmailToken getEmailTokenByUserAndPurpose(User user, TokenPurpose purpose);
}
