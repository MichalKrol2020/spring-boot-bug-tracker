package com.company.listeners;

import com.company.domain.UserPrincipal;
import com.company.service.LoginAttemptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationSuccessListener
{
    private final LoginAttemptService loginAttemptService;

    @Autowired
    public AuthenticationSuccessListener(LoginAttemptService loginAttemptService)
    {
        this.loginAttemptService = loginAttemptService;
    }

    @EventListener
    public void onAuthenticationEventSuccess(AuthenticationSuccessEvent event)
    {
        Object principal = event.getAuthentication().getPrincipal();
        if(principal instanceof UserPrincipal userPrincipal)
        {
            loginAttemptService.addUserToLoginAttemptCache(userPrincipal.getUsername());
        }
    }
}
