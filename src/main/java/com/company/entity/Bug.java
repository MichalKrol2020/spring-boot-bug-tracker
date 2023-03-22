package com.company.entity;

import com.company.enumeration.BugClassification;
import com.company.enumeration.BugSeverity;
import com.company.enumeration.BugStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "bug")
public class Bug implements Serializable
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    private BugClassification classification;

    @JsonIgnore
    @Enumerated(EnumType.STRING)
    private BugStatus status;

    @Enumerated(EnumType.STRING)
    private BugSeverity severity;

    @Setter(AccessLevel.NONE)
    private LocalDateTime creationDate;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name="creator_id", nullable = false)
    private User creator;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="assignee_id")
    private User assignee;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    public Bug()
    {
        this.creationDate = LocalDateTime.now();
    }
}
