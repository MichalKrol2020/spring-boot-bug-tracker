package com.company.service;

import com.company.entity.Bug;
import com.company.entity.Project;
import com.company.entity.Role;
import com.company.entity.User;
import com.company.exception.EmptyListException;
import com.company.exception.UserNotFoundException;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;

import static com.company.constant.PDFConstant.*;

@Service
public class PDFGeneratorServiceImpl implements PDFGeneratorService
{
    private final ProjectService projectService;
    private final BugService bugService;
    private final UserService userService;

    @Autowired
    public PDFGeneratorServiceImpl(ProjectService projectService,
                                   BugService bugService,
                                   UserService userService)
    {
        this.projectService = projectService;
        this.bugService = bugService;
        this.userService = userService;
    }


    @Override
    public void generateReport(Long userId, HttpServletResponse response) throws UserNotFoundException, IOException, EmptyListException
    {
        User user = this.userService.getUserEntityById(userId);
        if(user == null)
        {
            throw new UserNotFoundException(COULD_NOT_GENERATE_REPORT + USER_NOT_FOUND);
        }

        List<Project> projects = this.getProjectsBy(user);
        if(projects == null)
        {
            throw new EmptyListException(COULD_NOT_GENERATE_REPORT + NOT_PARTICIPATING_IN_PROJECTS);
        }

        try (Document document = new Document(PageSize.A4))
        {
            PdfWriter.getInstance(document, response.getOutputStream());
            document.open();

            addPreface(document, 1);
            addTitle(document, REPORT_TITLE);

            addProjectsAndBugs(document, projects);
        }
    }

    private List<Project> getProjectsBy(User user)
    {
        if(Role.isProjectLeader(user.getRole()))
        {
            return this.projectService.getProjectEntitiesByProjectLeaderId(user.getId());
        }

        if(Role.isUser(user.getRole()))
        {
            return this.projectService.getProjectEntitiesByParticipantId(user.getId());
        }

        return null;
    }

    private void addPreface(Document document, int linesCount)
    {
        Paragraph preface = new Paragraph();
        addEmptyLine(preface, linesCount);
        document.add(preface);
    }

    private void addTitle(Document document, String title)
    {

        Paragraph titleParagraph = new Paragraph(title, TITLE_FONT);
        titleParagraph.setAlignment(Paragraph.ALIGN_CENTER);
        addEmptyLine(titleParagraph, 2);
        document.add(titleParagraph);
    }

    private void addProjectLeader(Document document, String projectLeaderName)
    {
        Paragraph projectLeaderParagraph = new Paragraph(PROJECT_LEADER + projectLeaderName, PROJECT_LEADER_FONT);
        projectLeaderParagraph.setAlignment(Paragraph.ALIGN_LEFT);
        addEmptyLine(projectLeaderParagraph, 2);
        document.add(projectLeaderParagraph);
    }

    private void addProjectsAndBugs(Document document, List<Project> projects)
    {
        projects.forEach((project) ->
        {
            addProjectLeader(document, project.getProjectLeader().getFullName());

            Paragraph projectTitleParagraph = new Paragraph(project.getName(), PROJECT_NAME_FONT);
            addEmptyLine(projectTitleParagraph, 1);
            document.add(projectTitleParagraph);

            Paragraph projectDescParagraph = new Paragraph(project.getDescription(), PROJECT_DESC_FONT);
            addEmptyLine(projectDescParagraph, 2);
            document.add(projectDescParagraph);

            PdfPTable table = createBugTable(project);
            if (table == null)
            {
                Paragraph noBugsParagraph = new Paragraph(NO_BUGS_IN_PROJECT);
                document.add(noBugsParagraph);
                return;
            }

            document.add(table);

            Paragraph spacer = new Paragraph();
            addEmptyLine(spacer, 2);
            document.add(spacer);
        });
    }

    private PdfPTable createBugTable(Project project)
    {
        List<Bug> bugs = bugService.getBugEntityListByProjectId(project.getId());
        if(bugs.size() == 0)
        {
            return null;
        }

        PdfPTable table = new PdfPTable(PROJECT_LEADER_HEADERS_WIDTHS.size());
        table.setWidthPercentage(100);
        setColWidths(table, PROJECT_LEADER_HEADERS_WIDTHS.values());

        setHeaders(table, PROJECT_LEADER_HEADERS_WIDTHS.keySet());

        bugs.forEach((bug) ->
        {
            addCell(table, bug.getName());
            addCell(table, bug.getClassification().getName());
            addCell(table, bug.getSeverity().getName());
            addCell(table, bug.getStatus().getName());
            addCell(table, bug.getCreator().getFullName());

            User assignee = bug.getAssignee();
            String assigneeName;
            if(assignee == null)
            {
                assigneeName = NOT_ASSIGNED;
            } else
            {
                assigneeName = assignee.getFullName();
            }

            addCell(table, assigneeName);

            DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern(SIMPLE_DATE_FORMAT);
            String creationDate = bug.getCreationDate().format(dateFormat);
            addCell(table, creationDate);
        });

        return table;
    }

    private void setColWidths(PdfPTable table, Collection<Integer> widthsCollection)
    {
        int[] widths = widthsCollection.stream().mapToInt(Integer::intValue).toArray();
        table.setWidths(widths);
    }

    private void setHeaders(PdfPTable table, Collection<String> headers)
    {
        headers.forEach((header) ->
        {
            PdfPCell headerCell = new PdfPCell(new Phrase(header, HEADER_FONT));
            headerCell.setBackgroundColor(Color.LIGHT_GRAY);
            table.addCell(headerCell);
        });
    }

    private void addCell(PdfPTable table, String cell)
    {
        Paragraph cellParagraph = new Paragraph(cell, CELL_FONT);
        table.addCell(cellParagraph);
    }

    private void addEmptyLine(Paragraph paragraph, int linesCount)
    {
        for(int i = 0; i < linesCount; i++)
        {
            paragraph.add(new Paragraph(WHITESPACE));
        }
    }
}



