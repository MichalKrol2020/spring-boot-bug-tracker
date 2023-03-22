package com.company.controller;

import com.company.exception.EmptyListException;
import com.company.exception.ExceptionHandler;
import com.company.exception.UserNotFoundException;
import com.company.service.PDFGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.company.constant.PDFConstant.*;

@RestController
@CrossOrigin("http://localhost:4200")
@RequestMapping(path = "/pdf")
public class PDFController extends ExceptionHandler
{
    private final PDFGeneratorService pdfGeneratorService;

    @Autowired
    public PDFController(PDFGeneratorService pdfGeneratorService)
    {
        this.pdfGeneratorService = pdfGeneratorService;
    }


    @GetMapping(path = "{userId}")
    @PreAuthorize("hasAnyAuthority('user:read', 'project_leader:read')")
    public void generateReport(@PathVariable long userId,
                               HttpServletResponse response) throws UserNotFoundException,
                                                                    IOException,
                                                                    EmptyListException
    {
        response.setContentType(APPLICATION_PDF);
        DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        String currentDateTime = dateFormat.format(new Date());

        String headerValue = ATTACHMENT_FILENAME + currentDateTime + DOT_PDF;
        response.setHeader(HEADER_KEY, headerValue);
        this.pdfGeneratorService.generateReport(userId, response);
    }
}

