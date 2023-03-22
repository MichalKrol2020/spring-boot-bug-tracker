package com.company.exception;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.company.domain.HttpResponse;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.persistence.NoResultException;
import java.io.IOException;
import java.util.Objects;

import static com.company.constant.ExceptionConstant.*;

@RestControllerAdvice
public class ExceptionHandler implements ErrorController
{
    // ACCOUNT

    @org.springframework.web.bind.annotation.ExceptionHandler(DisabledException.class)
    public ResponseEntity<HttpResponse> accountDisabledException()
    {
        return createHttpResponse(HttpStatus.BAD_REQUEST, ACCOUNT_DISABLED);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(AccountExpiredException.class)
    public ResponseEntity<HttpResponse> accountExpiredException()
    {
        return createHttpResponse(HttpStatus.UNAUTHORIZED, ACCOUNT_HAS_EXPIRED);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(LockedException.class)
    public ResponseEntity<HttpResponse> accountLockedException()
    {
        return createHttpResponse(HttpStatus.UNAUTHORIZED, ACCOUNT_LOCKED);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(AccountAlreadyActivatedException.class)
    public ResponseEntity<HttpResponse> accountAlreadyActivatedException(AccountAlreadyActivatedException accountAlreadyActivatedException)
    {
        return createHttpResponse(HttpStatus.BAD_REQUEST, accountAlreadyActivatedException.getMessage());
    }


    //TOKEN

    @org.springframework.web.bind.annotation.ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<HttpResponse> tokenExpiredException(TokenExpiredException tokenExpiredException)
    {
        return createHttpResponse(HttpStatus.UNAUTHORIZED, tokenExpiredException.getMessage());
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<HttpResponse> invalidTokenException(InvalidTokenException invalidTokenException)
    {
        return createHttpResponse(HttpStatus.UNAUTHORIZED, invalidTokenException.getMessage());
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(EmailTokenNotFoundException.class)
    public ResponseEntity<HttpResponse> confirmationTokenNotFoundException(EmailTokenNotFoundException emailTokenNotFoundException)
    {
        return createHttpResponse(HttpStatus.BAD_REQUEST, emailTokenNotFoundException.getMessage());
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(EmailTokenExpiredException.class)
    public ResponseEntity<HttpResponse> emailTokenExpiredException(EmailTokenExpiredException emailTokenExpiredException)
    {
        return createHttpResponse(HttpStatus.BAD_REQUEST, emailTokenExpiredException.getMessage());
    }


    //AUTHENTICATION

    @org.springframework.web.bind.annotation.ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<HttpResponse> accessDeniedException()
    {
        return createHttpResponse(HttpStatus.FORBIDDEN, NOT_ENOUGH_PERMISSION);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<HttpResponse> badCredentialsException()
    {
        return createHttpResponse(HttpStatus.BAD_REQUEST, INCORRECT_CREDENTIALS);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(EmailExistsException.class)
    public ResponseEntity<HttpResponse> emailExistsException(EmailExistsException emailExistsException)
    {
        return createHttpResponse(HttpStatus.BAD_REQUEST, emailExistsException.getMessage());
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<HttpResponse> userNotFoundException(UserNotFoundException userNotFoundException)
    {
        return createHttpResponse(HttpStatus.BAD_REQUEST, userNotFoundException.getMessage());
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(InvalidEmailException.class)
    public ResponseEntity<HttpResponse> invalidEmailException(InvalidEmailException invalidEmailException)
    {
        return createHttpResponse(HttpStatus.BAD_REQUEST, invalidEmailException.getMessage());
    }
    @org.springframework.web.bind.annotation.ExceptionHandler(ContainsWhitespaceException.class)
    public ResponseEntity<HttpResponse> containsWhitespaceException(ContainsWhitespaceException containsWhitespaceException)
    {
        return createHttpResponse(HttpStatus.BAD_REQUEST, containsWhitespaceException.getMessage());
    }


    // RESET PASSWORD

    @org.springframework.web.bind.annotation.ExceptionHandler(PasswordsMatchException.class)
    public ResponseEntity<HttpResponse> passwordsMatchException(PasswordsMatchException passwordsMatchException)
    {
        return createHttpResponse(HttpStatus.BAD_REQUEST, passwordsMatchException.getMessage());
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(EmailAlreadySentException.class)
    public ResponseEntity<HttpResponse> emailAlreadySentException(EmailAlreadySentException emailAlreadySentException)
    {
        return createHttpResponse(HttpStatus.BAD_REQUEST, emailAlreadySentException.getMessage());
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(PasswordAlreadyChangedException.class)
    public ResponseEntity<HttpResponse> passwordAlreadyChangedException(PasswordAlreadyChangedException passwordAlreadyChangedException)
    {
        return createHttpResponse(HttpStatus.BAD_REQUEST, passwordAlreadyChangedException.getMessage());
    }


    // BUG

    @org.springframework.web.bind.annotation.ExceptionHandler(BugAlreadyExistException.class)
    public ResponseEntity<HttpResponse> bugAlreadyExistsException(BugAlreadyExistException bugAlreadyExistException)
    {
        return createHttpResponse(HttpStatus.BAD_REQUEST, bugAlreadyExistException.getMessage());
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(BugDoesNotExistException.class)
    public ResponseEntity<HttpResponse> bugDoesNotExistsException(BugDoesNotExistException bugDoesNotExistException)
    {
        return createHttpResponse(HttpStatus.BAD_REQUEST, bugDoesNotExistException.getMessage());
    }

    // PROJECT

    @org.springframework.web.bind.annotation.ExceptionHandler(ProjectDoesNotExistException.class)
    public ResponseEntity<HttpResponse> projectDoesNotExistsException(ProjectDoesNotExistException projectDoesNotExistException)
    {
        return createHttpResponse(HttpStatus.BAD_REQUEST, projectDoesNotExistException.getMessage());
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(ProjectAlreadyExistsException.class)
    public ResponseEntity<HttpResponse> projectAlreadyExistsException(ProjectAlreadyExistsException projectAlreadyExistsException)
    {
        return createHttpResponse(HttpStatus.BAD_REQUEST, projectAlreadyExistsException.getMessage());
    }


    // USER

    @org.springframework.web.bind.annotation.ExceptionHandler(UserNotAssignedException.class)
    public ResponseEntity<HttpResponse> userNotAssignedException(UserNotAssignedException userNotAssignedException)
    {
        return createHttpResponse(HttpStatus.BAD_REQUEST, userNotAssignedException.getMessage());
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(UserAlreadyAssignedException.class)
    public ResponseEntity<HttpResponse> userAlreadyAssignedException(UserAlreadyAssignedException userAlreadyAssignedException)
    {
        return createHttpResponse(HttpStatus.BAD_REQUEST, userAlreadyAssignedException.getMessage());
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(NotificationNotFoundException.class)
    public ResponseEntity<HttpResponse> notificationNotFoundException(NotificationNotFoundException notificationNotFoundException)
    {
        return createHttpResponse(HttpStatus.BAD_REQUEST, notificationNotFoundException.getMessage());
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(RoleDoesNotExistException.class)
    public ResponseEntity<HttpResponse> notificationNotFoundException(RoleDoesNotExistException roleDoesNotExistException)
    {
        return createHttpResponse(HttpStatus.BAD_REQUEST, roleDoesNotExistException.getMessage());
    }



    @org.springframework.web.bind.annotation.ExceptionHandler(EmptyListException.class)
    public ResponseEntity<HttpResponse> emptyListException(EmptyListException emptyListException)
    {
        return createHttpResponse(HttpStatus.BAD_REQUEST, emptyListException.getMessage());
    }


    @org.springframework.web.bind.annotation.ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<HttpResponse> illegalArgumentException()
    {
        return createHttpResponse(HttpStatus.BAD_REQUEST, ILLEGAL_ARGUMENT);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(IllegalAccessException.class)
    public ResponseEntity<HttpResponse> illegalAccessException(IllegalAccessException illegalAccessException)
    {
        return createHttpResponse(HttpStatus.UNAUTHORIZED, String.format(illegalAccessException.getMessage()));
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<HttpResponse> methodNotSupportedException(HttpRequestMethodNotSupportedException methodNotSupportedException)
    {
        HttpMethod supportedMethod = Objects.requireNonNull(methodNotSupportedException.getSupportedHttpMethods()).iterator().next();
        return createHttpResponse(HttpStatus.METHOD_NOT_ALLOWED, String.format(METHOD_IS_NOT_ALLOWED, supportedMethod));
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(Exception.class)
    public ResponseEntity<HttpResponse> internalServerErrorException(Exception e)
    {
        System.out.println(e.getMessage());
        e.printStackTrace();
        return createHttpResponse(HttpStatus.INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR_MSG);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(NoResultException.class)
    public ResponseEntity<HttpResponse> notFoundException(NoResultException noResultException)
    {
        return createHttpResponse(HttpStatus.NOT_FOUND, noResultException.getMessage());
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(IOException.class)
    public ResponseEntity<HttpResponse> ioException()
    {
        return createHttpResponse(HttpStatus.INTERNAL_SERVER_ERROR, ERROR_PROCESSING_FILE);
    }

    @RequestMapping(ERROR_PATH)
    public ResponseEntity<HttpResponse> notFound404()
    {
        return createHttpResponse(HttpStatus.NOT_FOUND, PAGE_NOT_FOUND);
    }

    private ResponseEntity<HttpResponse> createHttpResponse(HttpStatus httpStatus, String message)
    {
        HttpResponse httpResponse = new HttpResponse(httpStatus.value(),
                httpStatus,
                httpStatus.getReasonPhrase().toUpperCase(),
                message);

        return new ResponseEntity<>(httpResponse, httpStatus);
    }
}
