package org.universaal.ri.api.manager.server;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.universaal.ri.api.manager.Activator;
import org.universaal.ri.api.manager.RemoteAPI;

/**
 * An HTTP Servlet that handles the calls to the Remote API server.
 * 
 * @author alfiva
 * 
 */
public class RemoteServlet extends javax.servlet.http.HttpServlet{

    private static final long serialVersionUID = -1931914654539856412L;
    
    /**
     * Singleton instance of the Remote API implementation
     */
    private RemoteAPI remoteAPI;

    /**
     * Basic constructor
     * 
     * @param rAPI
     *            The singleton instance of the Remote API implementation
     */
    public RemoteServlet(RemoteAPI rAPI) {
	remoteAPI = rAPI;
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
	    throws ServletException, IOException {
	doPost(req, resp);
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
	    throws ServletException, IOException {
	resp.setContentType("text/plain");
	resp.setStatus(HttpServletResponse.SC_ACCEPTED);

	String authHeader = req.getHeader("Authorization");
	if(authHeader==null) authHeader = req.getParameter(RemoteAPI.KEY_AUTH);// TODO For testing
	String method = req.getParameter(RemoteAPI.KEY_METHOD);
	String param = req.getParameter(RemoteAPI.KEY_PARAM);

	if (authHeader == null || method == null || param == null) {
	    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
	    resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
		    "Missing parameter");
	    return;
	}

	if (remoteAPI == null) {
	    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
		    "API not available");
	    return;
	} else {
	    String servResp = null;
	    try {
		if (RemoteAPI.METHOD_REGISTER.equals(method)) {
		    remoteAPI.register(authHeader, param);
		    Activator.getPersistence().storeRegister(authHeader, param);
		} else if (RemoteAPI.METHOD_SENDC.equals(method)) {
		    remoteAPI.sendC(authHeader, param);
		} else if (RemoteAPI.METHOD_SUBSCRIBEC.equals(method)) {
		    remoteAPI.subscribeC(authHeader, param);
		    Activator.getPersistence().storeSubscriber(authHeader, param);
		} else if (RemoteAPI.METHOD_CALLS.equals(method)) {
		    servResp = remoteAPI.callS(authHeader, param);
		} else if (RemoteAPI.METHOD_PROVIDES.equals(method)) {
		    remoteAPI.provideS(authHeader, param);
		    Activator.getPersistence().storeCallee(authHeader, param);
		} else if (RemoteAPI.METHOD_UNREGISTER.equals(method)) {
		    remoteAPI.unregister(authHeader);
		    Activator.getPersistence().removeRegister(authHeader);
		} else {
		    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		    resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
			    "No such method");
		    return;
		}
	    } catch (Exception e) {
		resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
			e.toString()+"\n"+e.getMessage());
		return;
	    }
	    resp.setStatus(HttpServletResponse.SC_OK);
	    PrintWriter os = resp.getWriter();
	    if (servResp != null)
		os.print(servResp);
	    os.flush();
	    os.close();
	}
    }
}
