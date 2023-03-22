package com.company.service;

import com.company.entity.EmailToken;
import com.company.entity.User;
import com.company.enumeration.TokenPurpose;
import com.company.exception.*;
import com.company.repository.EmailTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;

import static com.company.constant.EmailTokenConstant.*;

@Service
@Transactional(readOnly = true)
public class EmailTokenServiceImpl implements EmailTokenService
{
    private final EmailTokenRepository emailTokenRepository;


    @Autowired
    public EmailTokenServiceImpl(EmailTokenRepository emailTokenRepository)
    {
        this.emailTokenRepository = emailTokenRepository;
    }


    @Override
    @Transactional
    public void save(EmailToken emailToken)
    {
        this.emailTokenRepository.save(emailToken);
    }



    @Override
    @Transactional
    public void delete(EmailToken emailToken)
    {
        this.emailTokenRepository.delete(emailToken);
    }



    @Override
    @Transactional
    public void setUseDate(String token) throws EmailTokenExpiredException,
                                                         EmailTokenNotFoundException
    {
        EmailToken emailToken = this.validateToken(token);
        emailToken.setUsedDate(LocalDateTime.now());

        this.emailTokenRepository.save(emailToken);
    }

    private EmailToken validateToken(String token) throws EmailTokenNotFoundException,
                                                             EmailTokenExpiredException
    {
        EmailToken emailToken = this.emailTokenRepository.findByToken(token);

        if(emailToken == null)
        {
            throw new EmailTokenNotFoundException(TOKEN_NOT_FOUND);
        }

        if(emailToken.getExpiryDate().isBefore(LocalDateTime.now()))
        {
            throw new EmailTokenExpiredException(TOKEN_EXPIRED);
        }

        return emailToken;
    }


    @Override
    public EmailToken getEmailTokenByUserAndPurpose(User user, TokenPurpose purpose)
    {
        return this.emailTokenRepository.findByUserAndPurpose(user, purpose);
    }

    @Override
    public EmailToken getEmailTokenByToken(String token)
    {
        return this.emailTokenRepository.findByToken(token);
    }
}
