package com.company.service;

import com.company.entity.Role;
import com.company.enumeration.RoleEnum;
import com.company.repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {RoleServiceImpl.class})
@ExtendWith(SpringExtension.class)
public class RoleServiceTest
{
    @MockBean
    private RoleRepository roleRepository;

    @Autowired
    private RoleService underTest;

    @Test
    public void testGetRoleByName()
    {
        RoleEnum roleName = RoleEnum.ROLE_PROJECT_LEADER;

        Role role = new Role();
        role.setName(roleName);
        when(this.roleRepository.getRoleByName(roleName)).thenReturn(role);

        Role result = this.underTest.getRoleByName(roleName);
        assertEquals(role, result);
        assertEquals(roleName, result.getName());
    }
}
