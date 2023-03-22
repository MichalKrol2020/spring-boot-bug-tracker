package com.company.repository;

import com.company.entity.Role;
import com.company.enumeration.RoleEnum;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long>
{
    Role getRoleByName(RoleEnum name);
}
