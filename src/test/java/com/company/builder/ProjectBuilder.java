package com.company.builder;

import com.company.entity.Bug;
import com.company.entity.Project;
import com.company.entity.User;

import java.util.ArrayList;
import java.util.List;

public class ProjectBuilder
{
    private Long id;

    private String name;

    private String description;

    private User projectLeader;

    private List<User> participants;

    private List<Bug> bugs;

    public ProjectBuilder()
    {
        long id = 1L;
        this.id = id;
        this.name = "PROJECT_" + id;
        this.description = "PROJECT_DESCRIPTION_" + id;
        this.projectLeader = null;
        this.participants = null;
    }

    public ProjectBuilder withId(Long id)
    {
        this.id = id;
        return this;
    }

    public ProjectBuilder withName(String name)
    {
        this.name = name;
        return this;
    }

    public ProjectBuilder withDescription(String description)
    {
        this.description = description;
        return this;
    }

    public ProjectBuilder withProjectLeader(User projectLeader)
    {
        this.projectLeader = projectLeader;
        return this;
    }

    public void addParticipant(User user)
    {
        if(this.participants == null)
        {
            this.participants = new ArrayList<>();
        }

        this.participants.add(user);
    }

    public void addBug(Bug bug)
    {
        if(this.bugs == null)
        {
            this.bugs = new ArrayList<>();
        }

        this.bugs.add(bug);
    }

    public List<Bug> getBugs()
    {
        return new ArrayList<>(this.bugs);
    }

    public Project build()
    {
        Project project = new Project();
        project.setId(this.id);
        project.setName(this.name);
        project.setDescription(this.description);
        project.setProjectLeader(this.projectLeader);
        project.setParticipants(this.participants);
        project.setBugs(this.bugs);

        return project;
    }
}
