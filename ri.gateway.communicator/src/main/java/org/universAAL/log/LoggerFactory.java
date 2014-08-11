/*
    Copyright 2014-2014 CNR-ISTI, http://isti.cnr.it
    Institute of Information Science and Technologies
    of the Italian National Research Council

    Copyright 2011-2014 AGH-UST, http://www.agh.edu.pl
    Faculty of Computer Science, Electronics and Telecommunications
    Department of Computer Science

    See the NOTICE file distributed with this work for additional
    information regarding copyright ownership

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 */
package org.universAAL.log;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.utils.LogUtils;

/**
 * 
 * @author <a href="mailto:stefano.lenzi@isti.cnr.it">Stefano "Kismet" Lenzi</a>
 * @version $LastChangedRevision$ ($LastChangedDate: 2014-07-24 16:42:23
 *          +0200 (Thu, 24 Jul 2014) $)
 * 
 */
public class LoggerFactory {

    private class UAALLogger implements Logger {

	final Class<?> claz;
	ModuleContext mc;
	final String[] oneMessage = new String[1];
	final Object LOCK_VAR_MC = new Object();

	private UAALLogger(final ModuleContext mc, final Class<?> logName) {
	    this.mc = mc;
	    this.claz = logName;
	}

	public void info(final String msg) {
	    synchronized (LOCK_VAR_MC) {
		LogUtils.logInfo(mc, claz, "unspecified method", msg);
	    }
	}

	public void debug(final String msg) {
	    synchronized (LOCK_VAR_MC) {
		LogUtils.logDebug(mc, claz, "unspecified method", msg);
	    }
	}

	public void debug(final String msg, final Throwable t) {
	    oneMessage[0] = msg;
	    synchronized (LOCK_VAR_MC) {
		LogUtils.logError(mc, claz, "unspecified method", oneMessage, t);
	    }
	}

	public void error(final String msg, final Throwable t) {
	    oneMessage[0] = msg;
	    synchronized (LOCK_VAR_MC) {
		LogUtils.logError(mc, claz, "unspecified method", oneMessage, t);
	    }
	}

	public void warning(final String msg) {
	    synchronized (LOCK_VAR_MC) {
		LogUtils.logWarn(mc, claz, "unspecified method", msg);
	    }
	}

	private void setModuleContext(final ModuleContext mc) {
	    synchronized (LOCK_VAR_MC) {
		this.mc = mc;
	    }
	}
    }

    final private Map<String, UAALLogger> loggers = new HashMap<String, UAALLogger>(
	    128);
    private final ModuleContext mc;

    private static Map<String, LoggerFactory> factories = null;

    private LoggerFactory(final ModuleContext mc) {
	this.mc = mc;
    }

    private static void lazyLoad() {
	LoggerFactory.factories = new HashMap<String, LoggerFactory>(32);
    }

    public static LoggerFactory createLoggerFactory(final ModuleContext mc) {
	synchronized (LoggerFactory.class) {
	    if (LoggerFactory.factories == null) {
		LoggerFactory.lazyLoad();
	    }
	}
	LoggerFactory factory = null;
	synchronized (LoggerFactory.factories) {
	    factory = LoggerFactory.factories.get(mc.getID());
	    if (factory != null) {
		return factory;
	    }
	    factory = new LoggerFactory(mc);
	    LoggerFactory.factories.put(mc.getID(), factory);
	}
	return factory;
    }

    public static void updateModuleContext(final ModuleContext mc) {
	synchronized (LoggerFactory.class) {
	    if (LoggerFactory.factories == null) {
		return;
	    }
	}
	synchronized (LoggerFactory.factories) {
	    final LoggerFactory activeLoggers = LoggerFactory.factories.get(mc
		    .getID());
	    activeLoggers.setModuleContext(mc);
	}
    }

    private void setModuleContext(final ModuleContext mc) {
	synchronized (loggers) {
	    final Collection<UAALLogger> activeLoggers = loggers.values();
	    for (final UAALLogger logger : activeLoggers) {
		logger.setModuleContext(mc);
	    }
	}
    }

    public Logger getLogger(final Class<?> clazz) {
	UAALLogger log = null;
	final String logName = clazz.getName();
	synchronized (loggers) {
	    log = loggers.get(logName);
	    if (log != null) {
		return log;
	    }
	    log = new UAALLogger(mc, clazz);
	    loggers.put(logName, log);
	}
	return log;
    }

}
