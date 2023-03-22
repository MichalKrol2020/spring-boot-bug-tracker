package com.company.builder;

import com.company.dto.BugDTO;
import com.company.dto.ProjectDTO;
import com.company.dto.UserDTO;
import com.company.enumeration.BugClassification;
import com.company.enumeration.BugSeverity;
import com.company.enumeration.BugStatus;
import lombok.Singular;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BugDTOBuilder
{
    private Long id;
    private String name;
    private String description;
    private BugClassification classification;
    private BugStatus status;
    private BugSeverity severity;
    private LocalDateTime creationDate;
    private UserDTO creator;
    private UserDTO assignee;
    private ProjectDTO project;

    @Singular
    private final List<BugDTO> bugDTOList = new ArrayList<>();

    public BugDTOBuilder()
    {
        this.id = 1L;
        this.name = "BUG_" + id;
        this.description = "BUG_DESCRIPTION_" + id;
        this.classification = BugClassification.PERFORMANCE;
        this.status = BugStatus.NEW;
        this.severity = BugSeverity.HIGH;
        this.creationDate = LocalDateTime.now();
        this.creator = null;
        this.assignee = null;
        this.project = null;
    }


    public BugDTOBuilder withId(Long id)
    {
        this.id = id;
        return this;
    }

    public BugDTOBuilder withName(String name)
    {
        this.name = name;
        return this;
    }

    public BugDTOBuilder withDescription(String description)
    {
        this.description = description;
        return this;
    }

    public BugDTOBuilder withClassification(BugClassification classification)
    {
        this.classification = classification;
        return this;
    }

    public BugDTOBuilder withStatus(BugStatus status)
    {
        this.status = status;
        return this;
    }


    public BugDTOBuilder withSeverity(BugSeverity severity)
    {
        this.severity = severity;
        return this;
    }

    public BugDTOBuilder withCreationDate(LocalDateTime creationDate)
    {
        this.creationDate = creationDate;
        return this;
    }

    public BugDTOBuilder withCreator(UserDTO creator)
    {
        this.creator = creator;
        return this;
    }

    public BugDTOBuilder withAssignee(UserDTO assignee)
    {
        this.assignee = assignee;
        return this;
    }

    public BugDTOBuilder withProject(ProjectDTO project)
    {
        this.project = project;
        return this;
    }

    public void addBugDTOItem(BugDTO bugDTO)
    {
        this.bugDTOList.add(bugDTO);
    }

    public List<BugDTO> getBugDTOList()
    {
        return new ArrayList<>(this.bugDTOList);
    }

    public BugDTO build()
    {
        return new BugDTO
                (
                        this.id,
                        this.name,
                        this.classification.name(),
                        this.status.name(),
                        this.description,
                        this.severity.name(),
                        this.creationDate,
                        this.creator,
                        this.assignee,
                        this.project
                );
    }
}
