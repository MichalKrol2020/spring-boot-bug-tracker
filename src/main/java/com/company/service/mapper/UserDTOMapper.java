package com.company.service.mapper;

import com.company.dto.UserDTO;
import com.company.entity.User;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class UserDTOMapper implements Function<User, UserDTO>
{
    @Override
    public UserDTO apply(User user)
    {
        if(user == null)
        {
            return null;
        }

        return new UserDTO(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getSpeciality().getName(),
                user.getProfileImageUrl());
    }
}
