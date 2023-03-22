package com.company.builder;

import com.company.entity.Bug;
import com.company.entity.Project;
import com.company.entity.User;
import com.company.enumeration.BugClassification;
import com.company.enumeration.BugSeverity;
import com.company.enumeration.BugStatus;


public class BugBuilder
{
    private Long id;

    private String name;

    private String description;

    private BugClassification classification;

    private BugStatus status;

    private BugSeverity severity;

    private User creator;

    private User assignee;

    private Project project;

    public BugBuilder()
    {
        long id = 1L;
        this.id = id;
        this.name = "BUG" + id;
        this.description = "BUG_DESCRIPTION_1";
        this.classification = BugClassification.PERFORMANCE;
        this.status = BugStatus.NEW;
        this.severity = BugSeverity.HIGH;
        this.creator = null;
        this.assignee = null;
        this.project = null;
    }

    public BugBuilder withId(Long id)
    {
        this.id = id;
        return this;
    }

    public BugBuilder withName(String name)
    {
        this.name = name;
        return this;
    }

    public BugBuilder withDescription(String description)
    {
        this.description = description;
        return this;
    }

    public BugBuilder withClassification(BugClassification classification)
    {
        this.classification = classification;
        return this;
    }

    public BugBuilder withStatus(BugStatus status)
    {
        this.status = status;
        return this;
    }

    public BugBuilder withSeverity(BugSeverity severity)
    {
        this.severity = severity;
        return this;
    }

    public BugBuilder withCreator(User creator)
    {
        this.creator = creator;
        return this;
    }

    public BugBuilder withAssignee(User assignee)
    {
        this.assignee = assignee;
        return this;
    }

    public BugBuilder withProject(Project project)
    {
        this.project = project;
        return this;
    }

    public Bug build()
    {
        Bug bug = new Bug();
        bug.setId(this.id);
        bug.setName(this.name);
        bug.setDescription(this.description);
        bug.setClassification(this.classification);
        bug.setStatus(this.status);
        bug.setSeverity(this.severity);
        bug.setCreator(this.creator);
        bug.setAssignee(this.assignee);
        bug.setProject(this.project);

        return bug;
    }
}
