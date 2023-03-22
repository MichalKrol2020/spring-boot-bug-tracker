package com.company.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

@Getter
@Setter
public class HttpResponse
{
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy hh:mm:ss", timezone = "Poland")
    private LocalDateTime timeStamp;
    private int httpStatusCode;
    private HttpStatus httpStatus;
    private String reason;
    private String message;

    public HttpResponse(int httpStatusCode, HttpStatus httpStatus, String reason, String message)
    {
        this.timeStamp = LocalDateTime.now();
        this.httpStatusCode = httpStatusCode;
        this.httpStatus = httpStatus;
        this.reason = reason;
        this.message = message;
    }

    public static ResponseEntity<HttpResponse> createResponse(HttpStatus httpStatus, String message)
    {
        return new ResponseEntity<>
                (
                        new HttpResponse(httpStatus.value(), httpStatus, httpStatus.getReasonPhrase().toUpperCase(), message),
                        httpStatus
                );
    }
}
