package org.universAAL.ri.servicegateway;

import java.io.IOException;
import java.net.MalformedURLException;

import junit.framework.TestCase;

import org.junit.Test;
import org.xml.sax.SAXException;

import com.meterware.httpunit.AuthorizationRequiredException;
import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.HttpUnitOptions;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;

public class LoginServletTest extends TestCase {

    @Test(expected = IllegalArgumentException.class)
    public void testWithAuthWithoutCookies() {
	WebConversation conversation = new WebConversation();
	WebRequest request = new GetMethodWebRequest(
		"http://localhost:8080/login");
	conversation.setAuthorization("user", "pass");
	try {
	    WebResponse response = conversation.getResponse(request);
	    assertNotNull("No response received", response);
	} catch (MalformedURLException e) {

	    e.printStackTrace();
	} catch (IOException e) {

	    e.printStackTrace();
	} catch (SAXException e) {

	    e.printStackTrace();
	} catch (IllegalArgumentException e) {
	    assertEquals(e.getMessage(),
		    "Invalid number of cookies, at least username and previousUrl must be set");

	}

    }

    @Test
    public void testWithAuthAndCookies() {
	WebConversation conversation = new WebConversation();
	WebRequest request = new GetMethodWebRequest(
		"http://localhost:8080/login");
	conversation.setAuthorization("user", "pass");
	conversation.putCookie("user", "user");
	conversation.putCookie("previousUrl", "whereis");
	HttpUnitOptions.setScriptingEnabled(false);
	try {

	    WebResponse response = conversation.getResponse(request);
	    assertNotNull("No response received", response);
	} catch (MalformedURLException e) {

	    e.printStackTrace();
	} catch (IOException e) {

	    e.printStackTrace();
	} catch (SAXException e) {

	    e.printStackTrace();
	} catch (IllegalArgumentException e) {
	    assertEquals(e.getMessage(),
		    "Invalid number of cookies, at least username and previousUrl must be set");

	}

    }

    @Test(expected = AuthorizationRequiredException.class)
    public void testWithoutAuth() {

	WebConversation conversation = new WebConversation();

	WebRequest request = new GetMethodWebRequest(
		"http://localhost:8080/login");

	try {
	    WebResponse response = conversation.getResponse(request);
	    assertNotNull("No response received", response);
	} catch (MalformedURLException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (SAXException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (AuthorizationRequiredException e) {
	    assertEquals(e.getAuthenticationScheme(), "Basic");
	    assertEquals(e.getAuthenticationParameter("realm"),
		    "\"Help when outside\"");
	}

    }
}
