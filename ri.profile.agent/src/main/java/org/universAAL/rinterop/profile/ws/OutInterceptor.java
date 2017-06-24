package org.universAAL.rinterop.profile.ws;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

/**
 * This class is used for logout a user, after the web service method
 * invocation.
 *
 */
public class OutInterceptor extends AbstractPhaseInterceptor<Message> {

	public OutInterceptor() {
		super(Phase.PRE_PROTOCOL);
	}

	public void handleMessage(Message message) throws Fault {

		// TODO log out
		// UserSession userSession = SessionContext.getSessionContext();
		// if (userSession != null) {
		// userSession.logout();
		// SessionContext.setSessionContext(null);
		// }
	}

}
