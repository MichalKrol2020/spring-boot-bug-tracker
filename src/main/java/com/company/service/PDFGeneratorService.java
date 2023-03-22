package com.company.service;

import com.company.exception.EmptyListException;
import com.company.exception.UserNotFoundException;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface PDFGeneratorService
{
    void generateReport(Long userId, HttpServletResponse response) throws UserNotFoundException, IOException, EmptyListException;
}
