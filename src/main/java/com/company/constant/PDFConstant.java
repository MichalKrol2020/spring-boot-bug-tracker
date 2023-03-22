package com.company.constant;

import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;

import java.util.LinkedHashMap;
import java.util.Map;

public class PDFConstant
{
    public static final String APPLICATION_PDF = "application/pdf";
    public static final String DATE_FORMAT = "yyyy-MM-dd:hh:mm:ss";
    public static final String SIMPLE_DATE_FORMAT = "dd-MM-yyyy";
    public static final String HEADER_KEY = "Content-Disposition";
    public static final String ATTACHMENT_FILENAME = "attachment; filename=report_";
    public static final String DOT_PDF = ".pdf";

    public static final String REPORT_TITLE = "Projects Report";

    public static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 25);
    public static final Font PROJECT_LEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA, 14);
    public static final Font PROJECT_NAME_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 15);
    public static final Font PROJECT_DESC_FONT = FontFactory.getFont(FontFactory.HELVETICA, 12);
    public static final Font HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
    public static final Font CELL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 9);
    public static final Map<String, Integer> PROJECT_LEADER_HEADERS_WIDTHS = new LinkedHashMap<>()
    {{
        put("BUG NAME", 30);
        put("CLASSIFICATION", 22);
        put("SEVERITY", 15);
        put("STATUS", 15);
        put("CREATOR", 20);
        put("ASSIGNEE", 20);
        put("CREATION DATE", 20);
    }};

    public static final String NO_BUGS_IN_PROJECT = "Project doesn't contain any bugs yet or all bugs have been fixed.";

    public static final String COULD_NOT_GENERATE_REPORT = "Could not generate report because ";
    public static final String USER_NOT_FOUND = "user could not be found!";
    public static final String PROJECT_LEADER_NOT_FOUND = "project leader could not be found!";
    public static final String NOT_PARTICIPATING_IN_PROJECTS = "you are not participating in any project!";

    public static final String PROJECT_LEADER = "Project leader: ";
    public static final String NOT_ASSIGNED = "Not assigned";
    public static final String WHITESPACE = " ";
}
