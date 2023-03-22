package com.company.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;


import static com.company.service.LoginAttemptService.LOCK_DURATION_TIME_MINUTES;
import static org.junit.jupiter.api.Assertions.*;

@ContextConfiguration(classes = {LoginAttemptService.class})
@ExtendWith(SpringExtension.class)
public class LoginAttemptServiceTest
{
    @Autowired
    private LoginAttemptService underTest;


    @Test
    void shouldAddUserToCache()
    {
        String username = "testuser";

        this.underTest.addUserToLoginAttemptCache(username);
        assertFalse(this.underTest.hasExceededMaxAttempt(username));

        this.underTest.addUserToLoginAttemptCache(username);
        this.underTest.addUserToLoginAttemptCache(username);
        this.underTest.addUserToLoginAttemptCache(username);
        this.underTest.addUserToLoginAttemptCache(username);

        assertTrue(this.underTest.hasExceededMaxAttempt(username));
    }

    @Test
    public void testEvictUserFromLoginAttemptCache_Should_EvictUser()
    {
        String username = "testuser";

        this.underTest.addUserToLoginAttemptCache(username);
        this.underTest.addUserToLoginAttemptCache(username);
        this.underTest.addUserToLoginAttemptCache(username);
        this.underTest.addUserToLoginAttemptCache(username);
        this.underTest.addUserToLoginAttemptCache(username);

        assertTrue(this.underTest.hasExceededMaxAttempt(username));

        this.underTest.evictUserFromLoginAttemptCache(username);

        assertFalse(this.underTest.hasExceededMaxAttempt(username));
    }


    @Test
    void testHasExceededMaxAttempt_Should_ExceedMaxAttempts()
    {
        String username = "testuser";

        this.underTest.addUserToLoginAttemptCache(username);
        this.underTest.addUserToLoginAttemptCache(username);
        this.underTest.addUserToLoginAttemptCache(username);

        assertFalse(this.underTest.hasExceededMaxAttempt(username));

        this.underTest.addUserToLoginAttemptCache(username);
        this.underTest.addUserToLoginAttemptCache(username);

        assertTrue(this.underTest.hasExceededMaxAttempt(username));
    }


    @Test
    void testHasExceededMaxAttempt_Should_ExpireCache() throws InterruptedException
    {
        String username = "testuser";

        this.underTest.addUserToLoginAttemptCache(username);
        this.underTest.addUserToLoginAttemptCache(username);
        this.underTest.addUserToLoginAttemptCache(username);
        this.underTest.addUserToLoginAttemptCache(username);
        this.underTest.addUserToLoginAttemptCache(username);

        assertTrue(this.underTest.hasExceededMaxAttempt(username));

        // Wait for the cache to expire
        Thread.sleep(LOCK_DURATION_TIME_MINUTES * 60 * 1000);

        assertFalse(this.underTest.hasExceededMaxAttempt(username));
    }
}
