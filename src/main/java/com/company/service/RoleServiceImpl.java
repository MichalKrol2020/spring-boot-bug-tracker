package com.company.service;

import com.company.entity.Role;
import com.company.enumeration.RoleEnum;
import com.company.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional(readOnly = true)
public class RoleServiceImpl implements RoleService
{
    private final RoleRepository roleRepository;

    @Autowired
    public RoleServiceImpl(RoleRepository roleRepository)
    {
        this.roleRepository = roleRepository;
    }

    @Override
    public Role getRoleByName(RoleEnum roleName)
    {
        return this.roleRepository.getRoleByName(roleName);
    }
}
