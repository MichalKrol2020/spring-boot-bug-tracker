package com.company.builder;

import com.company.dto.ProjectDTO;
import com.company.dto.UserDTO;

public class ProjectDTOBuilder
{
    private Long id;
    private String name;
    private String description;
    private UserDTO projectLeader;

    public ProjectDTOBuilder()
    {
        long id = 1L;
        this.id = id;
        this.name = "PROJECT_" + id;
        this.description = "PROJECT_DESCRIPTION_" + id;
        this.projectLeader = null;
    }

    public ProjectDTOBuilder withId(Long id)
    {
        this.id = id;
        return this;
    }

    public ProjectDTOBuilder withName(String name)
    {
        this.name = name;
        return this;
    }

    public ProjectDTOBuilder withDescription(String description)
    {
        this.description = description;
        return this;
    }

    public ProjectDTOBuilder withProjectLeader(UserDTO projectLeader)
    {
        this.projectLeader = projectLeader;
        return this;
    }

    public ProjectDTO build()
    {
        return new ProjectDTO
                (
                        this.id,
                        this.name,
                        this.description,
                        this.projectLeader
                );
    }
}
