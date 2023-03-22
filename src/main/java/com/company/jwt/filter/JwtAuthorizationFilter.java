package com.company.jwt.filter;

import com.company.jwt.JwtTokenProvider;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import static com.company.constant.SecurityConstant.OPTIONS_HTTP_METHOD;
import static com.company.constant.SecurityConstant.TOKEN_PREFIX;

@Component
public class JwtAuthorizationFilter extends OncePerRequestFilter
{
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public JwtAuthorizationFilter(JwtTokenProvider jwtTokenProvider)
    {
        this.jwtTokenProvider = jwtTokenProvider;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException
    {
        if(request.getMethod().equalsIgnoreCase(OPTIONS_HTTP_METHOD))
        {
            response.setStatus(HttpStatus.OK.value());
        } else
        {
            String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

            if(authorizationHeader == null || !authorizationHeader.startsWith(TOKEN_PREFIX))
            {
                filterChain.doFilter(request, response);
                return;
            }

            String token = authorizationHeader.substring(TOKEN_PREFIX.length());
            String username = this.jwtTokenProvider.getSubject(token);

            if(this.jwtTokenProvider.isTokenValid(username, token) && SecurityContextHolder.getContext().getAuthentication() == null)
            {
                List<GrantedAuthority> authorities = this.jwtTokenProvider.getAuthorities(token);
                Authentication authentication = this.jwtTokenProvider.getAuthentication(username, authorities, request);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else
            {
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }
}
