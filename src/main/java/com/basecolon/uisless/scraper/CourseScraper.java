package com.basecolon.uisless.scraper;

import com.basecolon.uisless.model.CourseModel;
import com.basecolon.uisless.model.UserModel;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CourseScraper {
    private final UserModel user;
    private final String term;

    public CourseScraper(UserModel user, String term) {
        this.user = user;
        this.term = term;
    }

    public List<CourseModel> getClassRecords() {
        final List<HtmlTableRow> rows = getCourseTableRows();

        return toCourseModels(rows);
    }

	public void parseCourseRow(CourseModel course, HtmlTableCell cell, int headerIndex) {
		CourseHeader cellTitle = CourseHeader.values()[headerIndex];
        String content = cell.getTextContent();

        switch(cellTitle) {
			case OPEN:
                course.open = parseOpen(content);
                break;
			case CRN:
                //TODO: From here we would want to visit
                course.crn = content;
                break;
            case SUBJECT:
                course.subject = content;
                break;
            case COURSE_NUMBER:
                course.courseNumber = content;
                break;
            case SECTION:
                course.section = content;
                break;
            case CAMPUS:
                course.campus = content;
                break;
            case CREDITS:
                course.credits = content;
                break;
            case TITLE:
                course.title = content;
                break;
			case DAYS:
                course.days = content;
                break;
            case TIME:
                course.time = content;
                break;
            case REMAINING_SEATS:
                course.remainingSeats = content;
                break;
            case WL_ACT:
                course.waitListActual = content;
                break;
            case WL_REM:
                course.waitListRemaining = content;
                break;
            case INSTRUCTOR:
                course.instructor = content;
                break;
            case DATE:
                course.date = content;
                break;
		}
	}

    private String parseOpen(String content) {
        switch(content) {
            case "C":
                return "Closed";
            case "NR":
                return "Not available for registration";
            case "SR":
                return "Student restrictions";
            case "add to worksheet":
                return "Open";
            default:
                return "";
        }
    }
	
	public List<CourseModel> toCourseModels(List<HtmlTableRow> rows) {
        List<CourseModel> courses = new ArrayList<>();

        for(int i = 0; i < rows.size(); i++) {
            HtmlTableRow row = rows.get(i);
            int colspanJump = 0;

            boolean isBlankRow = parseOpen(row.getCell(0).getTextContent()).equals("");

            if(isBlankRow) {
                continue;
            }

            final CourseModel course = new CourseModel();

            for (int j = 0; j < 13; j++) {
                final HtmlTableCell cell = row.getCell(j);

                // Parse it and add it to our prepared statement, and increment the statement's index counter
                parseCourseRow(course, cell, j + colspanJump);

                //If a colspan is specified, jump ahead by (colspan - 1)
                if (!cell.getAttribute("colspan").equals(DomElement.ATTRIBUTE_NOT_DEFINED)) {
                    colspanJump += Integer.parseInt(cell.getAttribute("colspan")) - 1;
                }
            }

            System.out.println("-------------- Parsed row: " + i + "/" + (rows.size() - 1));
            courses.add(course);
		}

        return courses;
	}

    // Saves every course as an HtmlTableRow object in the "coursePageTableRows" List
    private List<HtmlTableRow> getCourseTableRows() {
        List<HtmlTableRow> rows = null;

        try(final WebClient webClient = new WebClient(BrowserVersion.getDefault())) {
            // Ensure that timeouts don't happen
            webClient.setJavaScriptTimeout(0);

            // Get the login form, and then gets the submit button, user ID, and password fields from the form
            final HtmlPage loginPage = webClient.getPage("http://apollo.stjohns.edu");
            final HtmlForm form = loginPage.getFormByName("loginform");
            final HtmlSubmitInput submitButton = form.getInputByValue("Login");
            final HtmlPasswordInput userid = form.getInputByName("sid");
            final HtmlPasswordInput password = form.getInputByName("PIN");

            // Log into UIS
            userid.setValueAttribute(user.xNumber);
            password.setValueAttribute(user.pin);

            // Submits the username and password, and then brings you to the "look up classes" page
            HtmlPage lookupCoursesPage = submitButton.click();
            lookupCoursesPage = lookupCoursesPage.getAnchorByText("Student").click();
            lookupCoursesPage = lookupCoursesPage.getAnchorByText("Registration").click();
            lookupCoursesPage = lookupCoursesPage.getAnchorByText("Look-up Classes to Add").click();

            // Gets the term-selector form, its submit button, and the term selector box, sets the term to "Spring 2014", and submits
            final HtmlForm termSelectorForm = lookupCoursesPage.getForms().get(1);
            final HtmlSubmitInput termSelectorSubmit = termSelectorForm.getInputByValue("Submit");
            final HtmlSelect termSelector = termSelectorForm.getSelectByName("p_term");
            termSelector.setSelectedAttribute(termSelector.getOptionByText(term), true);
            lookupCoursesPage = termSelectorSubmit.click();

            //Moves to the advanced search page
            final HtmlForm subjectSelectorForm = lookupCoursesPage.getForms().get(1);
            final HtmlSubmitInput subjectSelectorSubmit = subjectSelectorForm.getInputByValue("Advanced Search");
            lookupCoursesPage = subjectSelectorSubmit.click();

            //Select every element in the selection box (which has the name "sel_subj") and submit
            final HtmlForm advancedSelectorForm = lookupCoursesPage.getForms().get(1);
            final HtmlSubmitInput advancedSelectorSubmit = advancedSelectorForm.getInputByValue("Section Search");
            final HtmlSelect advancedSelector = advancedSelectorForm.getSelectByName("sel_subj");
            final List<HtmlOption> majors = advancedSelector.getOptions();

            for (HtmlOption option : majors) {
                advancedSelector.setSelectedAttribute(option, true);
            }

            lookupCoursesPage = advancedSelectorSubmit.click();

            rows = ((HtmlTable)(lookupCoursesPage.getByXPath("//table[@class='datadisplaytable']").get(0))).getRows();
        } catch(IOException e) {
            e.printStackTrace();
        }

        return rows != null ? rows : Collections.EMPTY_LIST;
    }
}
