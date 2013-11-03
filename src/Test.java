import java.io.IOException;
import java.net.MalformedURLException;
import javax.swing.JOptionPane;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;


public class Test {

	/**
	 * @author Kevin Most
	 * @throws IOException 
	 * @throws MalformedURLException 
	 * @throws FailingHttpStatusCodeException 
	 * @dateCreated Nov 3, 2013
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		
		final WebClient webClient = new WebClient(BrowserVersion.FIREFOX_17);
		
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
		final HtmlPage subjectsPage = termSelectorSubmit.click();
		
		// TODO: Select every element in the selection box (which has the name "sel_subj") and submit
		final HtmlForm subjectSelectorForm = subjectsPage.getForms().get(1);
		 
		
		System.out.println("hi");
		webClient.closeAllWindows();
	}

}
