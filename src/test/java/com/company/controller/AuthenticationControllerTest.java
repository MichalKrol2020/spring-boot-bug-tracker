package com.company.controller;

import com.company.builder.AuthenticationResponseBuilder;
import com.company.builder.UserBuilder;
import com.company.builder.UserDTOBuilder;
import com.company.domain.AuthenticationRequest;
import com.company.domain.AuthenticationResponse;
import com.company.domain.RegisterRequest;
import com.company.domain.UserPrincipal;
import com.company.dto.UserDTO;
import com.company.entity.User;
import com.company.jwt.JwtTokenProvider;
import com.company.jwt.filter.JwtAccessDeniedHandler;
import com.company.jwt.filter.JwtAuthenticationEntryPoint;
import com.company.jwt.filter.JwtAuthorizationFilter;
import com.company.service.UserService;
import com.company.service.mapper.AuthenticationResponseMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDateTime;

import static com.company.constant.ExceptionConstant.ACCOUNT_LOCKED;
import static com.company.constant.SecurityConstant.JWT_TOKEN_HEADER;
import static com.company.constant.UserConstant.PLEASE_CHECK_YOUR_EMAIL;
import static com.company.constant.UserConstant.SUCCESSFULLY_REGISTERED;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@WebMvcTest(controllers = AuthenticationController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(SpringExtension.class)
public class AuthenticationControllerTest
{
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private AuthenticationResponseMapper authenticationResponseMapper;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private JwtAuthorizationFilter authorizationFilter;

    @MockBean
    private JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @MockBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @MockBean
    private UserService userService;

    @MockBean
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private User user;
    private UserDTO userDTO;
    private RegisterRequest registerRequest;
    private AuthenticationRequest authenticationRequest;
    private AuthenticationResponse authenticationResponse;

    @BeforeEach
    public void setUp()
    {
        this.user = new UserBuilder().build();
        this.userDTO = new UserDTOBuilder().build();
        this.authenticationRequest = new AuthenticationRequest(this.user.getEmail(), this.user.getPassword());
        this.authenticationResponse = new AuthenticationResponseBuilder().build();
        this.registerRequest = new RegisterRequest
                (
                        this.user.getFirstName(),
                        this.user.getLastName(),
                        this.user.getPassword(),
                        this.user.getEmail(),
                        this.user.getSpeciality().name()
                );
    }


    @Test
    public void testLogin_Should_Return_200_And_AuthenticationResponse() throws Exception
    {
        String token = "VALID_TOKEN";
        when(this.userService.getUserEntityByEmail(this.user.getEmail())).thenReturn(this.user);
        when(this.authenticationResponseMapper.apply(this.user)).thenReturn(this.authenticationResponse);
        when(this.jwtTokenProvider.generateJwtToken(any(UserPrincipal.class))).thenReturn(token);

        ResultActions response = this.mockMvc.perform(post("/authentication/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(authenticationRequest)));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.header().stringValues(JWT_TOKEN_HEADER, token))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", CoreMatchers.is(this.authenticationResponse.id().intValue())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.firstName", CoreMatchers.is(this.authenticationResponse.firstName())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.lastName", CoreMatchers.is(this.authenticationResponse.lastName())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email", CoreMatchers.is(this.authenticationResponse.email())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.speciality", CoreMatchers.is(this.authenticationResponse.speciality())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.profileImageUrl", CoreMatchers.is(this.authenticationResponse.profileImageUrl())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.role", CoreMatchers.is(this.authenticationResponse.role())));
    }



    @Test
    public void testLogin_Should_ThrowException_IfAccountIsLocked() throws Exception
    {
        this.user.setNotLocked(false);
        this.user.setLockDate(LocalDateTime.now());

        when(this.authenticationManager.authenticate
                (any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(LockedException.class);

        ResultActions response = this.mockMvc.perform(post("/authentication/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(this.authenticationRequest)));

        response.andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", CoreMatchers.is(ACCOUNT_LOCKED)));
    }



    @Test
    public void testRegister_Should_Return_200_And_ResponseMessage() throws Exception
    {
        when(this.userService.addOrUpdateUser
                (
                        null,
                        this.user.getFirstName(),
                        this.user.getLastName(),
                        this.user.getPassword(),
                        this.user.getEmail(),
                        this.user.getSpeciality(),
                        true,
                        true,
                        this.user.getRole().getName()
                )).thenReturn(this.userDTO);

        ResultActions response = this.mockMvc.perform(post("/authentication/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(this.registerRequest)));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", CoreMatchers.is(
                        SUCCESSFULLY_REGISTERED +
                                this.userDTO.firstName() + " " + this.userDTO.lastName() +
                                PLEASE_CHECK_YOUR_EMAIL)));
    }
}
