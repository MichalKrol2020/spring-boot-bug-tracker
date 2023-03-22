package com.company.service.mapper;

import com.company.dto.BugDTO;
import com.company.entity.Bug;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class BugDTOMapper implements Function<Bug, BugDTO>
{
    private final UserDTOMapper userDTOMapper;
    private final ProjectDTOMapper projectDTOMapper;

    @Autowired
    public BugDTOMapper(UserDTOMapper userDTOMapper, ProjectDTOMapper projectDTOMapper)
    {
        this.userDTOMapper = userDTOMapper;
        this.projectDTOMapper = projectDTOMapper;
    }

    @Override
    public BugDTO apply(Bug bug)
    {
        if(bug == null)
        {
            return null;
        }

        return new BugDTO(
                bug.getId(),
                bug.getName(),
                bug.getDescription(),
                bug.getClassification().getName(),
                bug.getStatus().getName(),
                bug.getSeverity().getName(),
                bug.getCreationDate(),
                this.userDTOMapper.apply(bug.getCreator()),
                this.userDTOMapper.apply(bug.getAssignee()),
                this.projectDTOMapper.apply(bug.getProject()));
    }
}
