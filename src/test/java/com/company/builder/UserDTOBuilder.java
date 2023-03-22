package com.company.builder;

import com.company.dto.UserDTO;
import com.company.enumeration.UserSpeciality;

public class UserDTOBuilder
{
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private UserSpeciality speciality;
    private String profileImgUrl;


    public UserDTOBuilder()
    {
        this.id = 1L;
        this.firstName = "John";
        this.lastName = "Doe";
        this.email = "john.doe@email.com";
        this.speciality = UserSpeciality.BACKEND;
        this.profileImgUrl = "/img/profile";
    }

    public UserDTOBuilder withId(Long id)
    {
        this.id = id;
        return this;
    }

    public UserDTOBuilder withFirstName(String firstName)
    {
        this.firstName = firstName;
        return this;
    }

    public UserDTOBuilder withLstName(String lastName)
    {
        this.lastName = lastName;
        return this;
    }

    public UserDTOBuilder withEmail(String email)
    {
        this.email = email;
        return this;
    }

    public UserDTOBuilder withSpeciality(UserSpeciality speciality)
    {
        this.speciality = speciality;
        return this;
    }

    public UserDTOBuilder withProfileImgUrl(String imgUrl)
    {
        this.profileImgUrl = imgUrl;
        return this;
    }

    public UserDTO build()
    {
        return new UserDTO
                (
                        this.id,
                        this.firstName,
                        this.lastName,
                        this.email,
                        this.speciality.name(),
                        this.profileImgUrl
                );
    }
}
