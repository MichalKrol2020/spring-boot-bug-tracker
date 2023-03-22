package com.company.domain;

import com.company.entity.Authority;
import com.company.entity.Role;
import com.company.entity.User;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
public class UserPrincipal implements UserDetails
{
    private User user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities()
    {
        Role role = this.user.getRole();
        List<Authority> authorities = role.getAuthorities();

        return authorities.stream().map(authority -> new SimpleGrantedAuthority(authority.getName())).collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return this.user.getPassword();
    }

    @Override
    public String getUsername()
    {
        return this.user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return this.user.isActive();
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.user.isNotLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
