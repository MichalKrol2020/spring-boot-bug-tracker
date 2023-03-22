package com.company.builder;

import com.company.entity.Project;
import com.company.entity.Role;
import com.company.entity.User;
import com.company.enumeration.RoleEnum;
import com.company.enumeration.UserSpeciality;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UserBuilder
{
    private Long id;

    private String firstName;

    private String lastName;

    private String password;

    private String email;

    private String profileImageUrl;

    private LocalDateTime lastLoginDate;

    private LocalDateTime lockDate;

    private boolean isActive;

    private boolean isNotLocked;

    private UserSpeciality speciality;

    private Role role;

    private final List<Project> projectsAssigned = new ArrayList<>();

    public UserBuilder()
    {
        this.id = 1L;
        this.firstName = "John";
        this.lastName = "Doe";
        this.password = "password";
        this.email = "john.doe@email.com";
        this.profileImageUrl = "/img/profile";
        this.lastLoginDate = LocalDateTime.now();
        this.lockDate = null;
        this.isActive = true;
        this.isNotLocked = true;
        this.speciality = UserSpeciality.BACKEND;

        Role role = new Role();
        role.setName(RoleEnum.ROLE_USER);
        this.role = role;
    }

    public UserBuilder withId(Long id)
    {
        this.id = id;
        return this;
    }

    public UserBuilder withFirstName(String firstName)
    {
        this.firstName = firstName;
        return this;
    }

    public UserBuilder withLastName(String lastName)
    {
        this.lastName = lastName;
        return this;
    }

    public UserBuilder withPassword(String password)
    {
        this.password = password;
        return this;
    }

    public UserBuilder withEmail(String email)
    {
        this.email = email;
        return this;
    }

    public UserBuilder withProfileImageUrl(String profileImageUrl)
    {
        this.profileImageUrl = profileImageUrl;
        return this;
    }

    public UserBuilder withLastLoginDate(LocalDateTime lastLoginDate)
    {
        this.lastLoginDate = lastLoginDate;
        return this;
    }

    public UserBuilder withLockDate(LocalDateTime lockDate)
    {
        this.lockDate = lockDate;
        return this;
    }

    public UserBuilder withActive(boolean isActive)
    {
        this.isActive = isActive;
        return this;
    }

    public UserBuilder withNotLocked(boolean isNotLocked)
    {
        this.isNotLocked = isNotLocked;
        return this;
    }

    public UserBuilder withSpeciality(UserSpeciality speciality)
    {
        this.speciality = speciality;
        return this;
    }

    public UserBuilder withRole(RoleEnum roleEnum)
    {
        Role role = new Role();
        role.setName(roleEnum);
        this.role = role;
        return this;
    }

    public void addProjectItem(Project project)
    {
        this.projectsAssigned.add(project);
    }

    public List<Project> getProjectsAssigned()
    {
        return new ArrayList<>(projectsAssigned);
    }

    public User build()
    {
        User user = new User();
        user.setId(this.id);
        user.setFirstName(this.firstName);
        user.setLastName(this.lastName);
        user.setPassword(this.password);
        user.setEmail(this.email);
        user.setProfileImageUrl(this.profileImageUrl);
        user.setLastLoginDate(this.lastLoginDate);
        user.setLockDate(this.lockDate);
        user.setActive(this.isActive);
        user.setNotLocked(this.isNotLocked);
        user.setSpeciality(this.speciality);
        user.setRole(this.role);
        user.setProjectsAssigned(this.projectsAssigned);

        return user;
    }
}
