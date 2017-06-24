package org.universAAL.rinterop.profile.ws;

//import java.security.AccessController;
//import java.security.PrivilegedAction;
import java.util.concurrent.Executor;

/**
 * Uses to execute the WEB Service command on the AccessControlContext for the
 * logged user.
 *
 * @author Ivailo Bakalov
 */
public class UniversAALExecutor implements Executor {

	public void execute(Runnable command) {
		// if (SessionContext.getSessionContext() != null) {
		// AccessController.doPrivileged(new Exe(command),
		// SessionContext.getSessionContext().getACC());
		// } else {
		command.run();
		// }
	}

	/**
	 * A simple PrivilegedAction implementation for command execution.
	 */
	// class Exe implements PrivilegedAction<Object> {
	// Runnable command;
	//
	// Exe(Runnable command) {
	// this.command = command;
	// }
	//
	// public Object run() {
	// command.run();
	// return null;
	// }
	// }

}
