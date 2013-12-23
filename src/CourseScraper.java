import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlOption;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTable;
import com.gargoylesoftware.htmlunit.html.HtmlTableCell;
import com.gargoylesoftware.htmlunit.html.HtmlTableRow;



public class CourseScraper {

	/**
	 * @author Kevin Most
	 * @author Matthew Stavola
	 * @throws IOException 
	 * @throws MalformedURLException 
	 * @throws FailingHttpStatusCodeException 
	 * @dateCreated Nov 3, 2013
	 */
	
	private final WebClient webClient = new WebClient(BrowserVersion.FIREFOX_17); // The webclient for the main pages
	private final WebClient individualWebClient = new WebClient(BrowserVersion.FIREFOX_17); // The webclient for the individual pages. These pages are not needed once information is parsed from them, and since there will be several thousand of these pages, we put them in a separate webclient so they can be purged after use to avoid using massive amounts of memory
	private List<HtmlTableRow> coursePageTableRows; // Holds the courses as we parse them
	
	private Connection db; // Connection to our Postgres DB where the parsed courses will be added
	PreparedStatement preparedStatement = null; // Prepared statement to be inserted into our database. Prepared Statements are fast, which makes them good for inserting the 5000 or so records this program will do.
	private int updateStatementCounter = 1; // The index for each field we need to add to our Postgres Update Statement. Remember that these are 1-indexed!
	
	// START SINGLETON
	private static CourseScraper instance;
	private CourseScraper() {}
	public static synchronized CourseScraper getCourseScraper() {
		if (instance == null) {
			instance = new CourseScraper();
		}
		return instance;
	}
	public Object clone() throws CloneNotSupportedException {throw new CloneNotSupportedException();}
	// END SINGLETON
	
	
	
	public static void main(String[] args) throws Exception {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); // Make Swing less ugly

		// Open the Postgres DB (hosted by Heroku)
		Class.forName("org.postgresql.Driver");
		getCourseScraper().db = DriverManager.getConnection("jdbc:postgresql://ec2-54-204-16-232.compute-1.amazonaws.com:5432/ddo7r9212kvngn?user=srebgebxskzafy&password=MYrb7-gZcDe85JPQF06edHHWUT&ssl=true&sslfactory=org.postgresql.ssl.NonValidatingFactory");

		getCourseScraper().prepareCourseUpdateStatement();
		
		getCourseScraper().initializeCourseTableRows(); // Get the information we need from UIS and put it into a List called coursePageTableRows. This info is still raw and needs to be parsed

		getCourseScraper().addContentOnMainCoursePageToDB(); // Add the stuff from coursePageTableRows to the DB
		
		// SHUT. DOWN. EVERYTHING.
		getCourseScraper().db.close();
		getCourseScraper().webClient.closeAllWindows(); 
	}

	
	public void prepareCourseUpdateStatement() throws SQLException {
		preparedStatement = db.prepareStatement("INSERT INTO \"2014Spring\"(open, time, days, location, date, instructors, crn, subject, course_number, section, campus, credits, title, remaining_seats, waitlist_actual, waitlist_remaining) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
	}
	
	// Saves every course as an HtmlTableRow object in the "coursePageTableRows" List
	public void initializeCourseTableRows() throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		// Ensure that timeouts don't happen
		webClient.setJavaScriptTimeout(0);
		individualWebClient.setJavaScriptTimeout(0);
		
		// Get the login form, and then gets the submit button, user ID, and password fields from the form
		final HtmlPage loginPage = webClient.getPage("http://apollo.stjohns.edu");
		final HtmlForm form = loginPage.getFormByName("loginform");
		final HtmlSubmitInput submitButton = form.getInputByValue("Login");
		final HtmlPasswordInput userid = form.getInputByName("sid");
		final HtmlPasswordInput password = form.getInputByName("PIN");
		
		// Log into UIS
		userid.setValueAttribute(JOptionPane.showInputDialog("Enter your X Number"));
		password.setValueAttribute(JOptionPane.showInputDialog("Enter your PIN"));
		
		// Submits the username and password, and then brings you to the "look up classes" page
		HtmlPage lookupCoursesPage = submitButton.click();
		lookupCoursesPage = lookupCoursesPage.getAnchorByText("Student").click();
		lookupCoursesPage = lookupCoursesPage.getAnchorByText("Registration").click();
		lookupCoursesPage = lookupCoursesPage.getAnchorByText("Look-up Classes to Add").click();
		
		// Gets the term-selector form, its submit button, and the term selector box, sets the term to "Spring 2014", and submits
		final HtmlForm termSelectorForm = lookupCoursesPage.getForms().get(1);
		final HtmlSubmitInput termSelectorSubmit = termSelectorForm.getInputByValue("Submit");
		final HtmlSelect termSelector = termSelectorForm.getSelectByName("p_term");
		termSelector.setSelectedAttribute(termSelector.getOptionByText("Spring 2014"), true);
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
		for(HtmlOption option: majors) {
			advancedSelector.setSelectedAttribute(option, true);
		}
		lookupCoursesPage = advancedSelectorSubmit.click();
		
		coursePageTableRows = ((HtmlTable)(lookupCoursesPage.getByXPath("//table[@class='datadisplaytable']").get(0))).getRows();
	}
	
	// Parses and returns one column of UIS data at a time. When used in a loop, you can parse the entire row effectively using this method.
	public String parseMainTableRows(HtmlTableCell cell, int j) throws IOException, SQLException {
		MainTableHeaders cellTitle = MainTableHeaders.values()[j];

		switch (cellTitle) {
			case OPEN:
				switch (cell.getTextContent()) {
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
			case CRN:
				addContentOnIndividualCoursePageToDB((HtmlAnchor)(cell.getElementsByTagName("a").get(0)));
				return cell.getTextContent();
			case DAYS: case TIME: case INSTRUCTOR: case DATE:
				return null;
			default:
				return cell.getTextContent();
		}
		
	}
	
	public String parseIndividualTableRows(HtmlTableCell cell, int j) throws IOException {
		IndividualTableHeaders cellTitle = IndividualTableHeaders.values()[j];

		switch (cellTitle) {
			case TYPE: case SCHEDULE_TYPE:
				return null;
			default:
				return cell.getTextContent();
		}
	}
	
	public void addContentOnMainCoursePageToDB() throws IndexOutOfBoundsException, IOException, SQLException {
		for (int i = 0; i < coursePageTableRows.size(); i++) { // For each row...
		// for (int i = 0; i < 5; i++) {
			int colspanJump = 0; // The amount of columns that were "jumped" because UIS sucks and uses colspans
			if (!parseMainTableRows(coursePageTableRows.get(i).getCell(0), 0).equals("")) { // Only do this row if it is an actual course
				for (int j = 0; j < 13; j++) { // For each column...
					// Parse it and add it to our prepared statement, and increment the statement's index counter
					String parsedCell = parseMainTableRows(coursePageTableRows.get(i).getCell(j), j+colspanJump);
					if (parsedCell != null) {
						System.out.println(updateStatementCounter + ": " + parsedCell);
						preparedStatement.setString(updateStatementCounter, parsedCell);
						updateStatementCounter++;
					}
					if (!coursePageTableRows.get(i).getCell(j).getAttribute("colspan").equals(DomElement.ATTRIBUTE_NOT_DEFINED)) { // If a column's colspan is specified...
						colspanJump += Integer.parseInt(coursePageTableRows.get(i).getCell(j).getAttribute("colspan")) - 1; // ... we should jump ahead by (colspan - 1);
					}
				}
			preparedStatement.executeUpdate();
			updateStatementCounter = 1;
			}
			System.out.println("-------------- Parsed row: " + i + "/" + (coursePageTableRows.size()-1));
		}
	}
	public void addContentOnIndividualCoursePageToDB(HtmlAnchor linkToIndividualCoursePage) throws IOException, SQLException { // Appends the days, times, locations, and dates for a course
		HtmlPage individualCoursePage = individualWebClient.getPage("https://apollo.stjohns.edu" + linkToIndividualCoursePage.getHrefAttribute());
		List<HtmlTable> coursePageTableList = (List<HtmlTable>)(individualCoursePage.getByXPath("//table[@summary='This table lists the scheduled meeting times and assigned instructors for this class..']")); 
		if (coursePageTableList.size() > 0) {
			List<HtmlTableRow> individualCoursePageRows = coursePageTableList.get(0).getRows(); // Each element is a row in the HTML table
			for (int j = 0; j < 7; j++) { // For each column...
				StringBuilder statement = new StringBuilder(); // Make a StringBuilder to hold the concatenated information we get from each row in this column
				for (int i = 0; i < individualCoursePageRows.size(); i++) { // For each row in that column...
					if (individualCoursePageRows.get(i).getCells().size() > 5 && !individualCoursePageRows.get(i).getCell(0).getTextContent().equals("Type")) { // Only do this row if it has more than 6  columns and if the first column is not "Type"
						String parsedCell = parseIndividualTableRows(individualCoursePageRows.get(i).getCell(j), j);
						
						// Appends any data collected to our statement, as well as appending a pipe-delimiter if there is already information from previous rows in there
						if (parsedCell != null) {
							if (statement.length() != 0) {
								statement.append('|');
							}
							statement.append(parsedCell);
						}
					}
				}
				if (statement.toString().length() > 0) {
					
					preparedStatement.setString(updateStatementCounter, statement.toString());
					statement.setLength(0);
					updateStatementCounter++;
				}
			}
		}
		else // Don't do anything instead if there is no table, but set update statment counter to the right value
			updateStatementCounter = 7;
			
		
		// Store each cell into the array. If there are multiple rows, they will be pipe-delimited and added to one cell in this course's database record
	
		
		individualWebClient.closeAllWindows(); // Close the window to avoid using huge amounts of memory
	}
}
