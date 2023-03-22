package com.company.entity;

import com.company.enumeration.RoleEnum;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "role")
public class Role implements Serializable
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private RoleEnum name;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "role_id", nullable = false)
    private List<Authority> authorities;

    public Role() {}

    public static boolean isProjectLeader(Role role)
    {
        return role.getName().equals(RoleEnum.ROLE_PROJECT_LEADER);
    }

    public static boolean isUser(Role role)
    {
        return role.getName().equals(RoleEnum.ROLE_USER);
    }
}
