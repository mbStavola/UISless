import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
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
		getCourseScraper().webClient.setJavaScriptTimeout(0);
		getCourseScraper().individualWebClient.setJavaScriptTimeout(0);
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); // Make Swing less ugly
		getCourseScraper().csvWriter = new FileWriter("csvout.csv"); // Writes CSV to file "csvout.csv"
		// TODO: No more hardcoded headers for CSV
		getCourseScraper().csvWriter.append("sep=|\n" +
				"Open?|" +
				"Time|" +
				"Days of Week|" +
				"Location|" +
				"Dates|" +
				"CRN|" +
				"Subject|" +
				"Course #|" +
				"Section|" +
				"Campus|" +
				"Credits|" +
				"Course Title|" +
				"Seats Remaining|" +
				"Waitlist Actual|" +
				"Waitlist Remaining|" +
				"Instructor(s)|\n"); // Appends the "sep=|" parameter so Excel knows that we're using the pipe for delimiting, and appends the heading information
		
		// TODO: These method names suck
		getCourseScraper().getCoursePageTableRows(); // Get the information we need from UIS and put it into a List called coursePageTableRows. This info is still raw and needs to be parsed
		getCourseScraper().addContentOnMainCoursePageToCsv(); // Parses the horribly-formatted data that UIS gave us

		
		
		getCourseScraper().webClient.closeAllWindows(); // SHUT. DOWN. EVERYTHING.
	}

	
	public void getCoursePageTableRows() throws FailingHttpStatusCodeException, MalformedURLException, IOException {
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
		
		// TODO: You know what we have to do here
		for (int i = 0; i < coursePageTableRows.size(); i++) {
			int colspanJump = 0; // The amount of columns that were "jumped" because UIS sucks and uses colspans
			if (coursePageTableRows.get(i).getCell(0).getTextContent().equals("SR") || coursePageTableRows.get(i).getCell(0).getTextContent().equals("C") || coursePageTableRows.get(i).getCell(0).getTextContent().trim().equals("add to worksheet")) { // Only do this row if it is an actual class
				for (int j = 0; j < 15; j++) {
					if (j+colspanJump == 0) { // Stuff for "isOpen" cell
						csvWriter.append(Boolean.toString(! (coursePageTableRows.get(i).getCell(0).getTextContent().equals("C") || coursePageTableRows.get(i).getCell(0).getTextContent().equals("NR")) ));
					}
					else if (j+colspanJump == 1) { // Stuff for "CRN" cell
						addContentOnIndividualCoursePageToCSV((HtmlAnchor) coursePageTableRows.get(i).getCell(j).getElementsByTagName("a").get(0)); // Also clicks the CSV's link so we can get the dates, times, and locations of the class
						csvWriter.append(coursePageTableRows.get(i).getCell(j).getTextContent());
					}
					else if (j+colspanJump == 6) { // Stuff for "credits" cell
						if (coursePageTableRows.get(i).getCell(j).getTextContent().indexOf('-') == -1) { // Checks if the credits field is not a range...
							csvWriter.append(coursePageTableRows.get(i).getCell(j).getTextContent());
						}
						else {
							csvWriter.append(coursePageTableRows.get(i).getCell(j).getTextContent().replaceAll("-", "~"));
						}
					}
					else if (j+colspanJump == 7) { // Stuff for "title" cells
						csvWriter.append(coursePageTableRows.get(i).getCell(j).getTextContent().replaceAll("(P)", ""));
					}
					else if (j+colspanJump == 8 || j+colspanJump == 9 || j+colspanJump == 14) {} // Don't do anything with the time, date, and day columns; those will be addressed in the individual course page
					else {
						csvWriter.append(coursePageTableRows.get(i).getCell(j).getTextContent());
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
		csvWriter.flush();
		csvWriter.close();
	}
	public void addContentOnIndividualCoursePageToCSV(HtmlAnchor linkToIndividualCoursePage) throws IOException { // Appends the days, times, locations, and dates for a course
		HtmlPage individualCoursePage = individualWebClient.getPage("https://apollo.stjohns.edu" + linkToIndividualCoursePage.getHrefAttribute());
		List<HtmlTableRow> individualCoursePageRows = ((HtmlTable)(individualCoursePage.getByXPath("//table[@class='datadisplaytable']").get(1))).getRows(); // Each element is a row in the HTML table
		
		StringBuilder[] appendToCsv = new StringBuilder[4]; // Each element will be appended to the CSV later
		for (int i = 0; i < appendToCsv.length; i++) {
			appendToCsv[i] = new StringBuilder();
		}
		
		// TODO: Same as above, this is all hardcoded stuff and we need to fix it
		for (int i = 0; i < individualCoursePageRows.size(); i++) { // We don't need "colspanJump" here because unlike on the other page, blank values actually get their own column instead of becoming part of the previous column
			if (individualCoursePageRows.get(i).getCells().size() > 5 && !individualCoursePageRows.get(i).getCell(0).getTextContent().equals("Type")) { // Only do this row if it has more than 6  columns and if the first column is not "Type"
				for (int j = 1; j < 5; j++) {
					appendToCsv[j-1].append('~');
					appendToCsv[j-1].append(individualCoursePageRows.get(i).getCell(j).getTextContent());
				}
			}
		}
		for (int i = 0; i < appendToCsv.length; i++) {
			if (appendToCsv[i].toString().length() > 0) {
				appendToCsv[i].deleteCharAt(0); // Remove the first '~'
			}
			csvWriter.append(appendToCsv[i].toString());
			csvWriter.append(csvDelimiter);
		}
		individualWebClient.closeAllWindows();
	}
}
