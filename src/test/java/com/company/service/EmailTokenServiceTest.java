package com.company.service;

import static com.company.constant.EmailTokenConstant.TOKEN_EXPIRED;
import static com.company.constant.EmailTokenConstant.TOKEN_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.company.builder.UserBuilder;
import com.company.entity.EmailToken;
import com.company.entity.User;
import com.company.enumeration.TokenPurpose;
import com.company.exception.EmailTokenExpiredException;
import com.company.exception.EmailTokenNotFoundException;
import com.company.repository.EmailTokenRepository;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ContextConfiguration(classes = {EmailTokenServiceImpl.class})
@ExtendWith(SpringExtension.class)
class EmailTokenServiceTest
{
    @MockBean
    private EmailTokenRepository emailTokenRepository;

    @Captor
    private ArgumentCaptor<EmailToken> emailTokenCaptor;

    @Autowired
    private EmailTokenService underTest;

    private User user;

    @MockBean
    public EmailToken emailToken;


    @BeforeEach
    public void setUp()
    {
        this.user = new UserBuilder().build();
        this.emailToken = new EmailToken(this.user, TokenPurpose.RESET_PASSWORD);
    }

    @Test
    void testSave()
    {
        when(this.emailTokenRepository.save(this.emailToken)).thenReturn(this.emailToken);

        this.underTest.save(this.emailToken);

        verify(this.emailTokenRepository).save(this.emailTokenCaptor.capture());
        EmailToken savedToken = this.emailTokenCaptor.getValue();
        assertSame(this.user, this.emailToken.getUser());
        assertEquals(TokenPurpose.RESET_PASSWORD, savedToken.getPurpose());
    }


    @Test
    void testDelete()
    {
        doNothing().when(this.emailTokenRepository).delete(this.emailToken);

        this.underTest.delete(this.emailToken);

        verify(this.emailTokenRepository).delete(this.emailTokenCaptor.capture());
        EmailToken deletedToken = this.emailTokenCaptor.getValue();
        assertSame(this.user, deletedToken.getUser());
        assertEquals(TokenPurpose.RESET_PASSWORD, deletedToken.getPurpose());
    }


    @Test
    void testSetUseDate_Should_ThrowException_When_TokenNotFound()
    {
        String token = "INVALID_TOKEN";
        when(this.emailTokenRepository.findByToken(token)).thenReturn(null);

        Exception exception = assertThrows
                (EmailTokenNotFoundException.class, () -> this.underTest.setUseDate(token));
        assertEquals(TOKEN_NOT_FOUND, exception.getMessage());
    }


    @Test
    void testSetUseDate_Should_ThrowException_When_TokenExpired()
    {
        String token = "TOKEN";

        this.emailToken.setExpiryDate(LocalDateTime.now().minusMinutes(30));
        when(this.emailTokenRepository.findByToken(token)).thenReturn(this.emailToken);

        Exception exception = assertThrows
                (EmailTokenExpiredException.class, () -> this.underTest.setUseDate(token));
        assertEquals(TOKEN_EXPIRED, exception.getMessage());
    }


    @Test
    void testSetUseDate_Should_SetUseDate() throws EmailTokenExpiredException, EmailTokenNotFoundException
    {
        String token = "TOKEN";

        when(this.emailTokenRepository.findByToken(token)).thenReturn(this.emailToken);
        assertNull(this.emailToken.getUsedDate());

        this.underTest.setUseDate(token);

        verify(this.emailTokenRepository).save(this.emailTokenCaptor.capture());
        EmailToken savedToken = this.emailTokenCaptor.getValue();

        assertNotNull(savedToken.getUsedDate());
    }



    @Test
    void testGetEmailTokenByUserAndPurpose_Should_ReturnEmailToken()
    {
        User user = this.user;
        TokenPurpose purpose = TokenPurpose.RESET_PASSWORD;

        when(this.emailTokenRepository.findByUserAndPurpose(user, purpose)).thenReturn(this.emailToken);

        EmailToken result = this.underTest.getEmailTokenByUserAndPurpose(user, purpose);
        assertEquals(user, result.getUser());
        assertEquals(purpose, result.getPurpose());
    }



    @Test
    void testGetEmailTokenByToken()
    {
        String token = "TOKEN";

        when(this.emailTokenRepository.findByToken(token)).thenReturn(this.emailToken);

        EmailToken result = this.underTest.getEmailTokenByToken(token);
        assertEquals(this.user, result.getUser());
        assertEquals(TokenPurpose.RESET_PASSWORD, result.getPurpose());
    }
}

