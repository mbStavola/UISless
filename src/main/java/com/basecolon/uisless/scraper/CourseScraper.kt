package com.basecolon.uisless.scraper

import com.basecolon.uisless.model.CourseModel
import com.basecolon.uisless.model.UserModel
import com.gargoylesoftware.htmlunit.BrowserVersion
import com.gargoylesoftware.htmlunit.WebClient
import com.gargoylesoftware.htmlunit.html.*

import java.io.IOException
import java.util.ArrayList
import java.util.Collections

class CourseScraper(private val user: UserModel, private val term: String) {

    val classRecords: List<CourseModel>
        get() {
            val rows = courseTableRows

            return toCourseModels(rows)
        }

    private fun parseCourseRow(course: CourseModel, cell: HtmlTableCell, headerIndex: Int) {
        val cellTitle = CourseHeader.values()[headerIndex]
        val content = cell.textContent

        when (cellTitle) {
            CourseHeader.OPEN -> course.open = parseOpen(content)
            CourseHeader.CRN ->
                //TODO: From here we would want to visit
                course.crn = content
            CourseHeader.SUBJECT -> course.subject = content
            CourseHeader.COURSE_NUMBER -> course.courseNumber = content
            CourseHeader.SECTION -> course.section = content
            CourseHeader.CAMPUS -> course.campus = content
            CourseHeader.CREDITS -> course.credits = content
            CourseHeader.TITLE -> course.title = content
            CourseHeader.DAYS -> course.days = content
            CourseHeader.TIME -> course.time = content
            CourseHeader.REMAINING_SEATS -> course.remainingSeats = content
            CourseHeader.WL_ACT -> course.waitListActual = content
            CourseHeader.WL_REM -> course.waitListRemaining = content
            CourseHeader.INSTRUCTOR -> course.instructor = content
            CourseHeader.DATE -> course.date = content
        }
    }

    private fun parseOpen(content: String): String {
        when (content) {
            "C" -> return "Closed"
            "NR" -> return "Not available for registration"
            "SR" -> return "Student restrictions"
            "add to worksheet" -> return "Open"
            else -> return ""
        }
    }

    private fun toCourseModels(rows: List<HtmlTableRow>): List<CourseModel> {
        val courses = ArrayList<CourseModel>()

        for (i in rows.indices) {
            val row = rows[i]
            var colspanJump = 0

            val isBlankRow = parseOpen(row.getCell(0).textContent) == ""

            if (isBlankRow) {
                continue
            }

            val course = CourseModel()

            for (j in 0..12) {
                val cell = row.getCell(j)

                // Parse it and add it to our prepared statement, and increment the statement's index counter
                parseCourseRow(course, cell, j + colspanJump)

                //If a colspan is specified, jump ahead by (colspan - 1)
                if (cell.getAttribute("colspan") != DomElement.ATTRIBUTE_NOT_DEFINED) {
                    colspanJump += Integer.parseInt(cell.getAttribute("colspan")) - 1
                }
            }

            println("-------------- Parsed row: $i/${rows.size - 1}")
            courses.add(course)
        }

        return courses
    }

    // Saves every course as an HtmlTableRow object in the "coursePageTableRows" List
    private // Ensure that timeouts don't happen
            // Get the login form, and then gets the submit button, user ID, and password fields from the form
            // Log into UIS
            // Submits the username and password, and then brings you to the "look up classes" page
            // Gets the term-selector form, its submit button, and the term selector box, sets the term to "Spring 2014", and submits
            //Moves to the advanced search page
            //Select every element in the selection box (which has the name "sel_subj") and submit
    val courseTableRows: List<HtmlTableRow>
        get() {
            var rows: List<HtmlTableRow>? = null

            try {
                WebClient(BrowserVersion.getDefault()).use { webClient ->
                    webClient.setJavaScriptTimeout(0)
                    val loginPage = webClient.getPage("http://apollo.stjohns.edu")
                    val form = loginPage.getFormByName("loginform")
                    val submitButton = form.getInputByValue("Login")
                    val userid = form.getInputByName("sid")
                    val password = form.getInputByName("PIN")
                    userid.setValueAttribute(user.xNumber!!)
                    password.setValueAttribute(user.pin!!)
                    var lookupCoursesPage = submitButton.click()
                    lookupCoursesPage = lookupCoursesPage.getAnchorByText("Student").click()
                    lookupCoursesPage = lookupCoursesPage.getAnchorByText("Registration").click()
                    lookupCoursesPage = lookupCoursesPage.getAnchorByText("Look-up Classes to Add").click()
                    val termSelectorForm = lookupCoursesPage.getForms().get(1)
                    val termSelectorSubmit = termSelectorForm.getInputByValue("Submit")
                    val termSelector = termSelectorForm.getSelectByName("p_term")
                    termSelector.setSelectedAttribute(termSelector.getOptionByText(term), true)
                    lookupCoursesPage = termSelectorSubmit.click()
                    val subjectSelectorForm = lookupCoursesPage.getForms().get(1)
                    val subjectSelectorSubmit = subjectSelectorForm.getInputByValue("Advanced Search")
                    lookupCoursesPage = subjectSelectorSubmit.click()
                    val advancedSelectorForm = lookupCoursesPage.getForms().get(1)
                    val advancedSelectorSubmit = advancedSelectorForm.getInputByValue("Section Search")
                    val advancedSelector = advancedSelectorForm.getSelectByName("sel_subj")
                    val majors = advancedSelector.getOptions()

                    for (option in majors) {
                        advancedSelector.setSelectedAttribute(option, true)
                    }

                    lookupCoursesPage = advancedSelectorSubmit.click()

                    rows = (lookupCoursesPage.getByXPath("//table[@class='datadisplaytable']").get(0) as HtmlTable).rows
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }

            return if (rows != null) rows else Collections.EMPTY_LIST
        }
}
