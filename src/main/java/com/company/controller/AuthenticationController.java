package com.company.controller;

import com.company.domain.*;
import com.company.dto.UserDTO;
import com.company.entity.User;
import com.company.enumeration.RoleEnum;
import com.company.enumeration.UserSpeciality;
import com.company.exception.*;
import com.company.exception.ExceptionHandler;
import com.company.jwt.JwtTokenProvider;
import com.company.service.UserService;
import com.company.service.mapper.AuthenticationResponseMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import java.io.IOException;

import static com.company.constant.SecurityConstant.JWT_TOKEN_HEADER;
import static com.company.constant.UserConstant.PLEASE_CHECK_YOUR_EMAIL;
import static com.company.constant.UserConstant.SUCCESSFULLY_REGISTERED;


@RestController
@CrossOrigin("http://localhost:4200")
@RequestMapping(path = "/authentication")
public class AuthenticationController extends ExceptionHandler
{
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    private final AuthenticationResponseMapper authenticationResponseMapper;


    @Autowired
    public AuthenticationController(UserService userService,
                                    JwtTokenProvider jwtTokenProvider,
                                    AuthenticationManager authenticationManager,
                                    AuthenticationResponseMapper authenticationResponseMapper)
    {
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationManager = authenticationManager;
        this.authenticationResponseMapper = authenticationResponseMapper;
    }



    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody AuthenticationRequest request)
    {
        this.authenticate(request.email(), request.password());
        User loginUser = this.userService.getUserEntityByEmail(request.email());
        UserPrincipal userPrincipal = new UserPrincipal(loginUser);
        HttpHeaders jwtHeader = this.getJwtHeader(userPrincipal);
        AuthenticationResponse response = this.authenticationResponseMapper.apply(loginUser);

        return new ResponseEntity<>(response, jwtHeader, HttpStatus.OK);
    }

    private HttpHeaders getJwtHeader(UserPrincipal userPrincipal)
    {
        HttpHeaders headers = new HttpHeaders();
        headers.add(JWT_TOKEN_HEADER, this.jwtTokenProvider.generateJwtToken(userPrincipal));

        return headers;
    }

    private void authenticate(String email, String password)
    {
        this.authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
    }



    @PostMapping("/register")
    public ResponseEntity<HttpResponse> register(@RequestBody RegisterRequest request) throws UserNotFoundException,
                                                                                              EmailExistsException,
                                                                                              IOException,
                                                                                              MessagingException,
                                                                                              InvalidEmailException,
                                                                                              ContainsWhitespaceException,
                                                                                              AccountAlreadyActivatedException,
                                                                                              EmailTokenExpiredException,
                                                                                              EmailTokenNotFoundException
    {
        UserDTO registrationUser = this.userService.addOrUpdateUser
                (null,
                        request.firstName(),
                        request.lastName(),
                        request.password(),
                        request.email(),
                        UserSpeciality.valueOf(request.speciality()),
                        true, true,
                        RoleEnum.ROLE_USER
                );

        return HttpResponse.createResponse(HttpStatus.OK,
                SUCCESSFULLY_REGISTERED +
                        registrationUser.firstName() + " " + registrationUser.lastName() +
                        PLEASE_CHECK_YOUR_EMAIL);
    }
}
