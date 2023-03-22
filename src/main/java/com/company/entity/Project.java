package com.company.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "project")
public class Project implements Serializable
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "users_projects",
            joinColumns = @JoinColumn(name = "project_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"))
    private List<User> participants;
    
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "project_leader_id", nullable = false)
    private User projectLeader;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "project")
    private List<Bug> bugs;

    public Project() {}
}
