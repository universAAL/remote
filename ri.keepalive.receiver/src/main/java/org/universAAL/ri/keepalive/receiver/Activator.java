package org.universAAL.ri.keepalive.receiver;

import java.util.Timer;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.osgi.uAALBundleContainer;
import org.universAAL.middleware.container.utils.LogUtils;

public class Activator implements BundleActivator {
    public static BundleContext osgiContext = null;
    public static ModuleContext context = null;
    public static CSubscriber csubscriber = null;
    private Timer t;
    private org.universAAL.ri.keepalive.receiver.Checker checker;
    public static int multiplier;

    public void start(BundleContext bcontext) throws Exception {
	Activator.osgiContext = bcontext;
	Activator.context = uAALBundleContainer.THE_CONTAINER
		.registerModule(new Object[] { bcontext });
	csubscriber = new CSubscriber(context);
	t = new Timer();
	checker = new Checker();
	multiplier = 2;
	try {
	    multiplier = Integer.parseInt(System.getProperty(
		    "org.universAAL.ri.keepalive.client.period", "2"));
	} catch (Exception e) {
	    LogUtils.logError(context, Activator.class, "start",
		    "Invalid period property entered, using default (2x) : "
			    + e);
	}
	t.scheduleAtFixedRate(checker, 10000, multiplier * 36000/*00*/);
    }

    public void stop(BundleContext arg0) throws Exception {
	t.cancel();
	t = null;
	checker = null;
	csubscriber.close();
    }

}
