import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
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
	private HtmlPage coursePage; // The page that lists every single course and its free seats, professor, date/time, etc.
	private List<Course> courses = new ArrayList<Course>();
	private FileWriter csvWriter;
	
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
		getCourseScraper().csvWriter = new FileWriter("csvout.csv"); // Writes CSV to file "csvout.csv"
		getCourseScraper().openCoursePage(); // Get to the page we actually need
		
		
		// Get the table containing every class, and then store all of its rows into a List
		final HtmlTable coursePageTable = (HtmlTable) getCourseScraper().coursePage.getByXPath("//table[@class='datadisplaytable']").get(0);
		List<HtmlTableRow> coursePageTableRows = coursePageTable.getRows();
		
		for (int i = 0; i < coursePageTableRows.size(); i++) {
			if (coursePageTableRows.get(i).getCells().size() > 10 && !coursePageTableRows.get(i).getCell(0).getTextContent().equals("Select")) { // Only do this row if it has more than 10 columns and if the first column is not "Select"
				for (int j = 0; j < 15; j++) {
					if (j == 0) { // Stuff for "isOpen" cell
						System.out.print(coursePageTableRows.get(i).getCell(j).getTextContent().equals("add to worksheet"));
					}
					else if (j == 6) { // Stuff for "credits" cell
						if (coursePageTableRows.get(i).getCell(j).getTextContent().indexOf('-') == -1) { // Checks if the credits field is not a range...
							System.out.print(coursePageTableRows.get(i).getCell(j).getTextContent());
						}
						else {
							System.out.print(coursePageTableRows.get(i).getCell(j).getTextContent().replaceAll("-", "~"));
						}
						
					}
					else if (j == 9) { // Stuff for "starTime" and "endTime" cells
						if (coursePageTableRows.get(i).getCell(j).getTextContent().length() > 10) {
							System.out.print(coursePageTableRows.get(i).getCell(j).getTextContent().substring(0,8));
							System.out.print("~");
							System.out.print(coursePageTableRows.get(i).getCell(j).getTextContent().substring(9, 16));
						}
						else {
							System.out.print(coursePageTableRows.get(i).getCell(j).getTextContent());
						}
					}
					else if (j == 14) {
						System.out.print(coursePageTableRows.get(i).getCell(j).getTextContent().substring(0, 5));
						System.out.print("~");
						System.out.print(coursePageTableRows.get(i).getCell(j).getTextContent().substring(6, 11));
					}
					else {
						System.out.print(coursePageTableRows.get(i).getCell(j).getTextContent());
					}
					System.out.print("\t");
				}
			System.out.println();
			}
		}
		
		
		getCourseScraper().webClient.closeAllWindows();
	}

	
	public void openCoursePage() throws FailingHttpStatusCodeException, MalformedURLException, IOException {
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
		final HtmlPage page1 = submitButton.click();
		final HtmlPage studentPage = page1.getAnchorByText("Student").click();
		final HtmlPage registrationPage = studentPage.getAnchorByText("Registration").click();
		final HtmlPage lookupClassesPage = registrationPage.getAnchorByText("Look-up Classes to Add").click();
		
		// Gets the term-selector form, its submit button, and the term selector box, sets the term to "Spring 2014", and submits
		final HtmlForm termSelectorForm = lookupClassesPage.getForms().get(1);
		final HtmlSubmitInput termSelectorSubmit = termSelectorForm.getInputByValue("Submit");
		final HtmlSelect termSelector = termSelectorForm.getSelectByName("p_term");
		termSelector.setSelectedAttribute(termSelector.getOptionByText("Spring 2014"), true);
		final HtmlPage subjectPage = termSelectorSubmit.click();
		
		//Moves to the advanced search page
		final HtmlForm subjectSelectorForm = subjectPage.getForms().get(1);
		final HtmlSubmitInput subjectSelectorSubmit = subjectSelectorForm.getInputByValue("Advanced Search");
		final HtmlPage advancedPage = subjectSelectorSubmit.click();

		//Select every element in the selection box (which has the name "sel_subj") and submit
		final HtmlForm advancedSelectorForm = advancedPage.getForms().get(1);
		final HtmlSubmitInput advancedSelectorSubmit = advancedSelectorForm.getInputByValue("Section Search");
		final HtmlSelect advancedSelector = advancedSelectorForm.getSelectByName("sel_subj");
		final List<HtmlOption> majors = advancedSelector.getOptions();
		for(HtmlOption option: majors) {
			advancedSelector.setSelectedAttribute(option, true);
		}
		coursePage = advancedSelectorSubmit.click();
	}
}
