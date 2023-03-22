package com.company.service.mapper;

import com.company.dto.ProjectDTO;
import com.company.entity.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class ProjectDTOMapper implements Function<Project, ProjectDTO>
{
    private final UserDTOMapper userDTOMapper;

    @Autowired
    public ProjectDTOMapper(UserDTOMapper userDTOMapper)
    {
        this.userDTOMapper = userDTOMapper;
    }

    @Override
    public ProjectDTO apply(Project project)
    {
        if(project == null)
        {
            return null;
        }

        return new ProjectDTO(
                project.getId(),
                project.getName(),
                project.getDescription(),
                this.userDTOMapper.apply(project.getProjectLeader()));
    }
}
