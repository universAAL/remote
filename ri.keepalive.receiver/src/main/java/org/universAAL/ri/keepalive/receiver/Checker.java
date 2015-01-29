package org.universAAL.ri.keepalive.receiver;

import java.util.HashMap;
import java.util.Iterator;
import java.util.TimerTask;

import org.universAAL.middleware.container.utils.LogUtils;

public class Checker extends TimerTask {

    @Override
    public void run() {
	long now = System.currentTimeMillis();
	HashMap<String, Long> current = (HashMap<String, Long>) CSubscriber
		.getLastknownof().clone();
	if (!current.isEmpty()) {
	    Iterator<String> keys = current.keySet().iterator();
	    while (keys.hasNext()) {
		String key = keys.next();
		Long tst = current.get(key);
		if ((now - tst) > (Activator.multiplier * 36000/*00*/)) {// If lastknown tst is before "multiplier hours" ago
		    LogUtils.logDebug(Activator.context, Checker.class, "run", "Missing keep-alive signal from tenant "+key);
		    System.out.println("Missing keep-alive signal from tenant "+key);
		    // TODO Mail?
		}
	    }
	}
    }

}
