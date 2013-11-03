import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;

import javax.swing.JOptionPane;

import be.roam.hue.doj.Doj;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;


public class CourseScraper {

	/**
	 * @author Kevin Most
	 * @throws IOException 
	 * @throws MalformedURLException 
	 * @throws FailingHttpStatusCodeException 
	 * @dateCreated Nov 3, 2013
	 */
	
	private final WebClient webClient = new WebClient(BrowserVersion.FIREFOX_17); // The entire webclient
	private HtmlPage coursePage; // The page that lists every single course and its free seats, professor, date/time, etc.
	private Doj coursePageDoj = Doj.on(coursePage); // Doj allows us to traverse the DOM in the coursePage
	
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
		getCourseScraper().openCoursePage();
		
		// Pull the 20th row, then pull the 6th cell in that row (these are just random test values), and then get the text that that cell contains
		System.out.println(getCourseScraper().coursePageDoj.get("tr", 19).get("td", 5).text()); // This shit does not do what I want it to do yet
		
		
		
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
		final String[] majors = {"ACC", "ACTV", "ACT", "ADGV", "ADS", "ADLW", "ADV", "ALSK", "ALH", "AADA", 
				"GRA", "ANT", "ARA", "ART", "ASC", "AUD", "BANK", "HBB", "BIO", "BIT", "BUS", "BUSI", "BLW", 
				"CANL", "CHE", "CHI", "CIVL", "CLS", "CPP", "AUST", "CSD", "COM", "CIS", "CSC", "CUS", "CSS", 
				"CONL", "CRJ", "CJL", "CRIM", "CRM", "CFR", "DS", "DFR", "DRRS", "DNY", "DRM", "ECO", "EDU", 
				"EDUL", "ESLS", "ESLW", "ENG", "EBS", "ERM", "ENVR", "ESP", "FAML", "FIN", "FRE", "FSA", "GENP", 
				"GEO", "GER", "MGD", "GOV", "HHS", "HLTH", "HSA", "HCI", "HIN", "HIS", "HCS", "HON", "HMT", "HSC", 
				"INDR", "IPP", "ICS", "INPR", "IEP", "INTL", "IB", "ICP", "ICM", "INSL", "ITA", "JPN", "JOU", "KOR", 
				"LABR", "LAC", "LLT", "LAT", "LAW", "LETH", "LGMT", "LRWR", "LES", "LEIC", "MLS", "LIS", "LIN", 
				"MMLM", "MGT", "MKT", "MTH", "MCM", "MSC", "GRM", "MUS", "NET", "PAS", "PHS", "PHM", "PHR", "PHI", 
				"PHO", "PHY", "PSC", "POR", "PROP", "PSY", "MPH", "PRL", "RAD", "RCT", "RMI", "RUS", "SCI", "SEC", 
				"SOC", "SPA", "SPE", "SPG", "SPM", "SFPR", "TAX", "TAXL", "TVF", "TLC", "THE", "THEO", "TORT", "TOX", 
				"TNLP", "ESTA", "USLS"};
		for(String s: majors) {
			advancedSelector.setSelectedAttribute(advancedSelector.getOptionByValue(s), true);
		}
		coursePage = advancedSelectorSubmit.click();
	}
}
