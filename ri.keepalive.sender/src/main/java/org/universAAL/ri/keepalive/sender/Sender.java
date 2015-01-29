package org.universAAL.ri.keepalive.sender;

import java.util.TimerTask;

import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.ontology.sysinfo.SystemInfo;

public class Sender extends TimerTask{

    @Override
    public void run() {
	SystemInfo sys=new SystemInfo("http://ontology.universAAL.org/SysInfo#system123");//TODO Set right URI
	sys.setAlive(true);
	ContextEvent event=new ContextEvent(sys, SystemInfo.PROP_ALIVE);
	Activator.sendEvent(event);
    }

}
