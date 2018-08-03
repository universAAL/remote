package org.universAAL.ri.rest.manager;

import junit.framework.Assert;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

import org.universAAL.itests.IntegrationTest;

public class ArtifactIT extends IntegrationTest {

    private static final String URL = "http://localhost:9000/uaal/";
    private static final String CALLBACK_GEN = "http://localhost:8080/receiver/";
    private static final String CALLBACK_CTXT = "http://localhost:8080/receiver/context";
    private static final String CALLBACK_SERV = "http://localhost:8080/receiver/service";
    private static final String USR = "test";
    private static final String PWD = "test";

    public void test0Composite() {
	// Check all artifacts in the log
	logAllBundles();
    }

    public void test1_SPACE() {
	// Send Event before creating space
	boolean fail = false;
	try {
	    post(URL+"spaces/test/context/publishers/device1", USR, PWD, "application/json", 
		    Body.EVENT);
	} catch (Exception e) {
	    fail = true;
	}
	Assert.assertTrue("FAIL: Accepted call to API before created Space", fail);
	// Create space
	fail = false;
	try {
	    if(!post(URL+"spaces", USR, PWD, "application/json", 
		    Body.CREATE_SPACE
		    .replace(Body.R_SPACE, "test")
		    .replace(Body.R_CALLBACK, CALLBACK_GEN))
		    .equals("201")) fail = true;
	} catch (Exception e) {
	    fail = true;
	}
	Assert.assertFalse("FAIL: Could not create Space", fail);
    }
    
    public void test2_CONTEXT() {
	// Create publisher
	boolean fail = false;
	try {
	    if(!post(URL+"spaces/test/context/publishers", USR, PWD, "application/json", 
		    Body.CREATE_PUBLISHER
		    .replace(Body.R_ID, "device1"))
		    .equals("201")) fail = true;
	} catch (Exception e) {
	    fail = true;
	}
	Assert.assertFalse("FAIL: Could not create publisher", fail);
	// Create subscriber
	fail = false;
	try {
	    if(!post(URL+"spaces/test/context/subscribers", USR, PWD, "application/json", 
		    Body.CREATE_SUBSCRIBER
		    .replace(Body.R_ID, "device1"))
		    .replace(Body.R_CALLBACK, CALLBACK_CTXT)
		    .equals("201")) fail = true;
	} catch (Exception e) {
	    fail = true;
	}
	Assert.assertFalse("FAIL: Could not create subscriber", fail);
	// Send Event
	fail = false;
	try {
	    post(URL+"spaces/test/context/publishers/device1", USR, PWD, "application/json", 
		    Body.EVENT);
	} catch (Exception e) {
	    fail = true;
	}
	Assert.assertFalse("FAIL: Could not send event", fail);
    }
    
    public void test3_SERVICE() {
	// Create caller
	boolean fail = false;
	try {
	    if(!post(URL + "spaces/test/service/callers", USR, PWD, "application/json", 
		    Body.R_CALLER
		    .replace(Body.R_CALLER, "default"))
		    .equals("201")) fail = true;
	} catch (Exception e) {
	    fail = true;
	}
	Assert.assertFalse("FAIL: Could not create caller", fail);
	// Create callee
	fail = false;
	try {
	    if(!post(URL+"spaces/test/service/callees", USR, PWD, "application/json", 
		    Body.CREATE_CALLEE
		    .replace(Body.R_ID, "device1")
		    .replace(Body.R_TYPE, "http://ontology.universAAL.org/Device.owl#TemperatureSensor")
		    .replace(Body.R_CALLBACK, CALLBACK_SERV))
		    .equals("201")) fail = true;
	} catch (Exception e) {
	    fail = true;
	}
	Assert.assertFalse("FAIL: Could not create callee", fail);
	// Send request
	fail = false;
	try {
	    post(URL+"spaces/test/service/callers/default", USR, PWD, "application/json", 
		    Body.REQUEST
		    .replace(Body.R_ID, "device1"));
	} catch (Exception e) {
	    fail = true;
	}
	Assert.assertFalse("FAIL: Could not send request", fail);
    }

    public static String invoke(String url, String usr, String pwd,
	    String method, String type, String body) throws Exception {
	HttpURLConnection conn = null;
	try {
	    byte[] data = null;
	    if (body != null) {
		data = body.getBytes(Charset.forName("UTF-8"));
	    }
	    String auth = "Basic "
		    + Base64.encodeBytes((usr + ":" + pwd).getBytes());
	    URL server = new URL(url);

	    conn = (HttpURLConnection) server.openConnection();
	    conn.setRequestMethod(method);
	    conn.setInstanceFollowRedirects(false);
	    conn.setDoOutput(true);
	    conn.setDoInput(true);
	    conn.setUseCaches(false);
	    conn.setReadTimeout(30000);
	    conn.setRequestProperty("Content-Type", type);
	    conn.setRequestProperty("charset", "utf-8");
	    if (data != null) {
		conn.setRequestProperty("Content-Length",
			"" + Integer.toString(data.length));
	    }
	    conn.setRequestProperty("Authorization", auth);

	    DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
	    if (data != null) {
		wr.write(data);
	    }
	    wr.flush();
	    wr.close();

	    if (conn.getResponseCode() < 200 || conn.getResponseCode() > 299) {
		throw new Exception("Unsuccessful server response: "
			+ conn.getResponseCode());
	    }

	    BufferedReader rd = new BufferedReader(
		    new InputStreamReader(conn.getInputStream(), "UTF-8"));
	    String line = rd.readLine();
	    StringBuilder result = new StringBuilder();
	    while (line != null) {
		result.append(line);
		line = rd.readLine();
	    }
	    if (!result.toString().isEmpty()) {
		return result.toString();
	    }else{
		return Integer.toString(conn.getResponseCode());
	    }
	} finally {
	    // close the connection and set all objects to null
	    if (conn != null) {
		conn.disconnect();
	    }
	}
    }

    public static String post(String url, String usr, String pwd, String type,
	    String body) throws Exception {
	return invoke(url, usr, pwd, "POST", type, body);
    }

    public static String put(String url, String usr, String pwd, String type,
	    String body) throws Exception {
	return invoke(url, usr, pwd, "PUT", type, body);
    }

    public static void delete(String url, String usr, String pwd)
	    throws Exception {
	invoke(url, usr, pwd, "DELETE", "application/json", null);
    }
}
