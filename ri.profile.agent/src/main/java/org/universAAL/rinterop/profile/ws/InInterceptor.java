package org.universAAL.rinterop.profile.ws;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.cxf.message.Message;

import org.apache.cxf.configuration.security.AuthorizationPolicy;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.universAAL.rinterop.profile.agent.osgi.Activator;

/**
 * This class checks the HTTP request for Basic Authentication properties, and login the authorized user, 
 * or anonymous user if no authentication credentials were provided with the request. 
 * 
 */
public class InInterceptor extends AbstractPhaseInterceptor<Message> { 
 
  static final String USER_NAME = "userName";
  static final String PASSWORD = "password";
  
  public static final String ANONYMOUS_USER = "anonymous";
  
  public InInterceptor() {
    super(Phase.PRE_INVOKE);
  }
  
  public void handleMessage(Message message) throws Fault {
    HttpServletRequest request = (HttpServletRequest)message.get("HTTP.REQUEST");
    HttpSession httpSession = request.getSession();
    boolean newSession = false;

    String userName = (String)httpSession.getAttribute(USER_NAME);
    String password = null;
    
    if (userName != null) {
      password = (String)httpSession.getAttribute(PASSWORD);      
    } else {
      AuthorizationPolicy policy = message.get(AuthorizationPolicy.class);
      if (policy != null) {
        userName = policy.getUserName();
        password = policy.getPassword();
        newSession = true;
      } 
    }
    if (userName == null) {
    	userName = ANONYMOUS_USER;
    }
    try {

    	//TODO log in or throw exception if login not correct
    	
    	if (newSession) {
	    	httpSession.setAttribute(USER_NAME, userName);
		    if (password != null) {
		      httpSession.setAttribute(PASSWORD, password);             
		    }
    	}

    } catch (Exception e) {
      Activator.log("Exception while logging in:", e);
      throw new Fault(e);
    }    
  }  
  
}

