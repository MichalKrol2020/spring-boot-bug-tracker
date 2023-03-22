package com.company.service;

import com.company.entity.Role;
import com.company.enumeration.RoleEnum;

public interface RoleService
{
    Role getRoleByName(RoleEnum roleName);
}
