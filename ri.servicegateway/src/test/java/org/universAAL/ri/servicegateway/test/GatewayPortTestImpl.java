package org.universAAL.ri.servicegateway.test;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.util.Constants;
import org.universAAL.ri.servicegateway.Activator;
import org.universAAL.ri.servicegateway.GatewayPort;

/**
 * 
 * @author eandgrg
 *
 */

public class GatewayPortTestImpl extends GatewayPort {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public String dataDir() {
	return "/notExistingDir";
    }

    public String url() {
	return "/universAALTest";
    }

    public void doGet(HttpServletRequest req, HttpServletResponse resp)
	    throws ServletException, IOException {
	doPost(req, resp);
    }

//    public static String getPostData(HttpServletRequest req) {
//	    StringBuilder sb = new StringBuilder();
//	    try {
//	        BufferedReader reader = req.getReader();
//	        reader.mark(10000);
//
//	        String line;
//	        do {
//	            line = reader.readLine();
//	            sb.append(line).append("\n");
//	        } while (line != null);
//	        reader.reset();
//	        // do NOT close the reader here, or you won't be able to get the post data twice
//	    } catch(IOException e) {
//	       // logger.warn("getPostData couldn't.. get the post data", e);  // This has happened if the request's reader is closed    
//	    }
//
//	    return sb.toString();
//	}

    
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
	    throws ServletException, IOException {

	LogUtils.logInfo(Activator.getModuleContext(), this.getClass(),
		"doPost",
		new Object[] { "Received mocked HTTP Servlet Request " + req }, null);

	// BEGIN AUTHENTICATION BLOCK
	// Check if user is authorized
//	if (!handleAuthorization(req, resp)) {
//	    LogUtils.logInfo(Activator.getModuleContext(), this.getClass(),
//		    "doPost",
//		    new Object[] { "Received unauthorized HTTP request!" },
//		    null);
//	    return;
//	}

	
	//String[] userAndPass = getUserAndPass(req.getHeader("Authorization"));

	//for test disregard Base64 encoding:
	String userAndPassWhole=req.getHeader("Authorization");
	String[] userAndPass = new String[2];;
		userAndPass[0]=req.getHeader("Authorization").substring(0, userAndPassWhole.indexOf(":"));
	userAndPass[1]=req.getHeader("Authorization").substring(userAndPassWhole.indexOf(":")+1);
	
	LogUtils.logInfo(Activator.getModuleContext(), this.getClass(),
		"doPost",
		new Object[] { "obtained user: " + userAndPass[0] + " obtained pass: "+ userAndPass[1]}, null);

	
	String userURI = userURIs.get(userAndPass[0]);
	// END AUTHENTICATION BLOCK, this block can be replaced by below
	// hardcoded line/user for e.g. testing purposes
//	 String userURI = Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX
//	 + "remoteUser";
	LogUtils.logInfo(Activator.getModuleContext(), this.getClass(),
		"doPost", new Object[] { "Received HTTP request from user: "
			+ userURI }, null);

	StringBuilder html = new StringBuilder();

	html
		.append("HTTP/1.1 200 OK Cache-Control: no-store, no-cache, must-revalidate Content-Type: text/html;charset=ISO-8859-1 Transfer-Encoding: chunked Server: Jetty(7.x.y-SNAPSHOT) <html>    <head>    <title>Some title     </title></head><body ></body></html>");

	// Set the current form in session and send response

	
//	resp.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
//	resp.setContentType("text/html");


    }
}
