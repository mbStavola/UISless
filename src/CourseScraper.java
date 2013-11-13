import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;
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
	
	private final WebClient webClient = new WebClient(BrowserVersion.FIREFOX_17); // The entire webclient
	private final WebClient individualWebClient = new WebClient(BrowserVersion.FIREFOX_17);
	private List<HtmlTableRow> coursePageTableRows; 
	
	private FileWriter csvWriter;
	private final char csvDelimiter = '|';
	
	private Connection db;
	private final String dbTableHeaders = "\"Open\"	Boolean, " +
			"\"Start Time\"	time without time zone[], " +
			"\"End Time\"	time without time zone[], " +
			"\"Days of Week\"	character(6)[], " +
			"\"Location\"	character(80)[], " +
			"\"Start Date\"	date[], " +
			"\"End Date\"	date[], " +
			"\"CRN\"	integer	primary key	not null, " +
			"\"Subject\"	character(5), " +
			"\"Course #\"	character(10), " +
			"\"Section\"	character(5), " +
			"\"Campus\"	character(1), " +
			"\"Credits\"	character(10)[], " +
			"\"Course Title\"	character(80), " +
			"\"Seats Remaining\"	integer, " +
			"\"Waitlist Actual\"	integer, " +
			"\"Waitlist Remaining\"	integer, " +
			"\"Instructor(s)\"	character(100)[]";
	
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
		

		getCourseScraper().getCoursePageTableRows(); // Get the information we need from UIS and put it into a List called coursePageTableRows. This info is still raw and needs to be parsed
		getCourseScraper().addContentOnMainCoursePageToCsv(); // Parses the horribly-formatted data that UIS gave us
//		getCourseScraper().databaseThings();
		
		
		getCourseScraper().csvWriter.flush();
		getCourseScraper().csvWriter.close();
		getCourseScraper().webClient.closeAllWindows(); // SHUT. DOWN. EVERYTHING.
	}

	public void databaseThings() throws SQLException, ClassNotFoundException {
		Class.forName("org.postgresql.Driver");
		getCourseScraper().db = DriverManager.getConnection("jdbc:postgresql://kevinmost.no-ip.org:5432/UISless", JOptionPane.showInputDialog("Enter Postgres username"), JOptionPane.showInputDialog("Enter Postgres password"));
		db.createStatement().executeUpdate("CREATE TABLE \"UISless\" (" + dbTableHeaders + ")");
		db.close();
		System.out.println("Done");
		
	}
	public void getCoursePageTableRows() throws FailingHttpStatusCodeException, MalformedURLException, IOException {
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
	public void addContentOnMainCoursePageToCsv() throws IndexOutOfBoundsException, IOException {
		csvWriter = new FileWriter("csvout.csv");
		for (int i = 0; i < coursePageTableRows.size(); i++) { // For each row...
			int colspanJump = 0; // The amount of columns that were "jumped" because UIS sucks and uses colspans
			if (coursePageTableRows.get(i).getCell(0).getTextContent().equals("SR") || coursePageTableRows.get(i).getCell(0).getTextContent().equals("NR") ||coursePageTableRows.get(i).getCell(0).getTextContent().equals("C") || coursePageTableRows.get(i).getCell(0).getTextContent().trim().equals("add to worksheet")) { // Only do this row if it is an actual class
				for (int j = 0; j < 15; j++) { // For each column...
					switch (j+colspanJump) { // j+colspanJump is the field that we're currently on in the UIS HTML table
						case 0: // Checkbox field
							csvWriter.append(Boolean.toString(! (coursePageTableRows.get(i).getCell(0).getTextContent().equals("C") || coursePageTableRows.get(i).getCell(0).getTextContent().equals("NR")) ));
							break;
						case 1: // CRN field
							addContentOnIndividualCoursePageToCSV((HtmlAnchor) coursePageTableRows.get(i).getCell(j).getElementsByTagName("a").get(0)); // Also clicks the CSV's link so we can get the dates, times, and locations of the class
							csvWriter.append(coursePageTableRows.get(i).getCell(j).getTextContent());
							break;
						case 6: // Credits field
							csvWriter.append(coursePageTableRows.get(i).getCell(j).getTextContent().replaceAll("-", "~"));
							break;
						case 8: case 9: case 14: // Days, time, and instructor (skip these fields, done in individual course page)
							break;
						default:
							csvWriter.append(coursePageTableRows.get(i).getCell(j).getTextContent());
							break;
					}
					if (j+colspanJump != 8 && j + colspanJump != 9 && j+colspanJump != 14) {
						csvWriter.append(csvDelimiter);
					}
					
					if (!coursePageTableRows.get(i).getCell(j).getAttribute("colspan").equals(DomElement.ATTRIBUTE_NOT_DEFINED)) { // If a column's colspan is specified...
						colspanJump += Integer.parseInt(coursePageTableRows.get(i).getCell(j).getAttribute("colspan")) - 1; // ... we should jump ahead by (colspan - 1);
					}
				}
			csvWriter.append("\n");
			
			}
		System.out.println("Parsed row: " + i + "/" + (coursePageTableRows.size()-1));
		}
	}
	public void addContentOnIndividualCoursePageToCSV(HtmlAnchor linkToIndividualCoursePage) throws IOException { // Appends the days, times, locations, and dates for a course
		HtmlPage individualCoursePage = individualWebClient.getPage("https://apollo.stjohns.edu" + linkToIndividualCoursePage.getHrefAttribute());
		List<HtmlTableRow> individualCoursePageRows = ((HtmlTable)(individualCoursePage.getByXPath("//table[@class='datadisplaytable']").get(1))).getRows(); // Each element is a row in the HTML table
		
		// Create an array of elements to add to the DB
		StringBuilder[] appendToCsv = new StringBuilder[4];
		for (int i = 0; i < appendToCsv.length; i++) {
			appendToCsv[i] = new StringBuilder();
		}
		
		// Store each element into the array
		// If there are multiple rows, separate them with a '~' character, but keep them within the same element in the array
		for (int i = 0; i < individualCoursePageRows.size(); i++) {
			if (individualCoursePageRows.get(i).getCells().size() > 5 && !individualCoursePageRows.get(i).getCell(0).getTextContent().equals("Type")) { // Only do this row if it has more than 6  columns and if the first column is not "Type"
				for (int j = 0; j < appendToCsv.length; j++) {
					appendToCsv[j].append('~');
					appendToCsv[j].append(individualCoursePageRows.get(i).getCell(j+1).getTextContent());
				}
			}
		}
		
		// Take the array and write it out to the database
		for (int i = 0; i < appendToCsv.length; i++) {
			if (appendToCsv[i].toString().length() > 0) {
				appendToCsv[i].deleteCharAt(0); // Remove the first '~'
			}
			csvWriter.append(appendToCsv[i].toString());
			csvWriter.append(csvDelimiter);
		}
		individualWebClient.closeAllWindows(); // Close the window to avoid using huge amounts of memory
	}
}
