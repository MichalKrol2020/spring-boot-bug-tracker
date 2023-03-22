package com.company.entity;

import com.company.enumeration.UserSpeciality;
import com.company.exception.ContainsWhitespaceException;
import com.company.exception.InvalidEmailException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import static com.company.constant.UserConstant.*;

@Entity
@Getter
@Setter
@Table(name = "users")
public class User implements Serializable
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;

    private String lastName;

    private String password;

    @Column(name = "email", unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    private UserSpeciality speciality;

    private String profileImageUrl;

    private LocalDateTime lastLoginDate;

    @Setter(AccessLevel.NONE)
    private LocalDateTime joinDate;

    private LocalDateTime lockDate;

    private boolean isActive;

    private boolean isNotLocked;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinTable(name = "users_projects",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "project_id", referencedColumnName = "id"))
    private List<Project> projectsAssigned;

    public User()
    {
        this.joinDate = LocalDateTime.now();
    }


    public String getFullName()
    {
        return this.firstName + WHITESPACE + this.lastName;
    }


    public void setUserProperties(String firstName,
                                  String lastName,
                                  String email,
                                  UserSpeciality speciality,
                                  Boolean isActive,
                                  Boolean isNotLocked,
                                  Role role) throws ContainsWhitespaceException,
                                                    InvalidEmailException
    {
        this.firstName = this.validateName(firstName);
        this.lastName = this.validateName(lastName);
        this.email = this.validateEmail(email);
        this.speciality = speciality;

        if(isActive != null)
        {
            this.isActive = isActive;
        }


        if(isNotLocked != null)
        {
            if(this.isNotLocked() && !isNotLocked)
            {
                this.lockDate = LocalDateTime.now();
            }

            if(!this.isNotLocked() && isNotLocked)
            {
                this.lockDate = null;
            }

            this.isNotLocked = isNotLocked;
        }

        if(role != null)
        {
            this.role = role;
        }
    }


    private String validateEmail(String email) throws InvalidEmailException
    {
        if(!EmailValidator.getInstance().isValid(email))
        {
            throw new InvalidEmailException(INVALID_EMAIL);
        }

        return email;
    }

    private String validateName(String name) throws ContainsWhitespaceException
    {
        if(StringUtils.containsWhitespace(name))
        {
            throw new ContainsWhitespaceException(CONTAINS_WHITESPACE);
        }

        return name.substring(0,1).toUpperCase() + name.substring(1).toLowerCase();
    }
}
