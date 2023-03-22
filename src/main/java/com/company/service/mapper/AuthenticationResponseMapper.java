package com.company.service.mapper;

import com.company.domain.AuthenticationResponse;
import com.company.entity.User;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class AuthenticationResponseMapper implements Function<User, AuthenticationResponse>
{
    @Override
    public AuthenticationResponse apply(User user)
    {
        if(user == null)
        {
            return null;
        }

        return new AuthenticationResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getSpeciality().getName(),
                user.getProfileImageUrl(),
                user.getRole().getName().name(),
                user.getJoinDate());
    }
}
