import java.io.IOException;
import java.net.MalformedURLException;

import javax.swing.JOptionPane;


import org.junit.Assert;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;
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
		final HtmlPage loginPage = webClient.getPage("http://apollo.stjohns.edu");		
		
		
		final String pageAsXml = loginPage.asXml();
		final String pageAsText = loginPage.asText();
		
		// DO THINGS STARTING HERE
		
		// Get the login form, and then gets the submit button, user ID, and password fields from the form
		final HtmlForm form = loginPage.getFormByName("loginform");
		final HtmlSubmitInput submitbutton = form.getInputByValue("Login");
		final HtmlPasswordInput userid = form.getInputByName("sid");
		final HtmlPasswordInput password = form.getInputByName("PIN");
		
		// Log into UIS
		userid.setValueAttribute(JOptionPane.showInputDialog("Enter your X-number (including the \"X\")"));
		password.setValueAttribute(JOptionPane.showInputDialog("Enter your password"));
		
		// Submits the username and password, and gets the landing page that you are brought to (stored as "page1")
		final HtmlPage page1 = submitbutton.click();
		
		// As an example, we can now get the link that has the text "Financial Aid" and store it into the variable "financialAidLink"
		final HtmlAnchor financialAidLink = page1.getAnchorByText("Financial Aid");
		// We can then print out the URL that the link targets with .getHrefAttribute
		System.out.println(financialAidLink.getHrefAttribute());
		
		webClient.closeAllWindows();
	}

}
