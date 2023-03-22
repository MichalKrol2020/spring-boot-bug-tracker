package com.company.service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;


@Service
public class LoginAttemptService
{
    public static final int MAXIMUM_NUMBER_OF_ATTEMPTS = 5;
    public static final int ATTEMPT_INCREMENT = 1;
    public static final int LOCK_DURATION_TIME_MINUTES = 5;
    public static final int MAXIMUM_SIZE = 100;


    private final LoadingCache<String, Integer> loginAttemptCache;

    public LoginAttemptService()
    {
        super();

        this.loginAttemptCache = CacheBuilder
                .newBuilder()
                .expireAfterWrite(LOCK_DURATION_TIME_MINUTES, TimeUnit.MINUTES)
                .maximumSize(MAXIMUM_SIZE)
                .build(new CacheLoader<>()
                {
                    @Override
                    public Integer load(String key) throws Exception
                    {
                        return 0;
                    }
                });
    }

    public void evictUserFromLoginAttemptCache(String username)
    {
        this.loginAttemptCache.invalidate(username);
    }

    public void addUserToLoginAttemptCache(String username)
    {
        int attempts;

        try
        {
            attempts = ATTEMPT_INCREMENT + this.loginAttemptCache.get(username);
        }catch (ExecutionException e)
        {
            attempts = 0;
        }

        this.loginAttemptCache.put(username, attempts);
    }


    public boolean hasExceededMaxAttempt(String username)
    {
        try
        {
            return this.loginAttemptCache.get(username) >= MAXIMUM_NUMBER_OF_ATTEMPTS;
        } catch (ExecutionException e)
        {
            return false;
        }
    }
}

