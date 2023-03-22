package com.company.builder;

import com.company.domain.AuthenticationResponse;
import com.company.entity.Role;
import com.company.enumeration.RoleEnum;
import com.company.enumeration.UserSpeciality;

import java.time.LocalDateTime;

public class AuthenticationResponseBuilder
{
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private UserSpeciality speciality;
    private String profileImageUrl;
    private Role role;
    private LocalDateTime joinDate;

    public AuthenticationResponseBuilder()
    {
        this.id = 1L;
        this.firstName = "John";
        this.lastName = "Doe";
        this.email = "john.doe@email.com";
        this.speciality = UserSpeciality.BACKEND;
        this.profileImageUrl = "/img/profile";

        Role role = new Role();
        role.setName(RoleEnum.ROLE_USER);
        this.role = role;
        this.joinDate = LocalDateTime.now().minusDays(30);
    }

    public AuthenticationResponseBuilder withId(Long id)
    {
        this.id = id;
        return this;
    }

    public AuthenticationResponseBuilder withFirstName(String firstName)
    {
        this.firstName = firstName;
        return this;
    }

    public AuthenticationResponseBuilder withLastName(String lastName)
    {
        this.lastName = lastName;
        return this;
    }

    public AuthenticationResponseBuilder withEmail(String email)
    {
        this.email = email;
        return this;
    }

    public AuthenticationResponseBuilder withProfileImageUrl(String profileImageUrl)
    {
        this.profileImageUrl = profileImageUrl;
        return this;
    }

    public AuthenticationResponseBuilder withSpeciality(UserSpeciality speciality)
    {
        this.speciality = speciality;
        return this;
    }

    public AuthenticationResponseBuilder withRole(RoleEnum roleEnum)
    {
        Role role = new Role();
        role.setName(roleEnum);
        this.role = role;
        return this;
    }

    public AuthenticationResponseBuilder withJoinDate(LocalDateTime joinDate)
    {
        this.joinDate = joinDate;
        return this;
    }

    public AuthenticationResponse build()
    {
        return new AuthenticationResponse
                (
                        this.id,
                        this.firstName,
                        this.lastName,
                        this.email,
                        this.speciality.name(),
                        this.profileImageUrl,
                        this.role.getName().name(),
                        this.joinDate
                );
    }
}
