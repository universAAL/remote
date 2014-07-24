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

import java.util.HashMap;
import java.util.Map;

import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.utils.LogUtils;

/**
*
* @author <a href="mailto:stefano.lenzi@isti.cnr.it">Stefano "Kismet" Lenzi</a>
* @version $LastChangedRevision$ ($LastChangedDate$)
*
*/
public class LoggerFactory {

    private class UAALLogger implements Logger {

        final Class<?> claz;
        final ModuleContext mc;
        final String[] oneMessage = new String[1];


        private UAALLogger(ModuleContext mc, Class<?> logName) {
            this.mc = mc;
            this.claz = logName;
        }

        public void info(String msg) {
            LogUtils.logInfo(mc, claz, "unspecified method", msg);
        }

        public void debug(String msg) {
            LogUtils.logDebug(mc, claz, "unspecified method", msg);
        }

        public void debug(String msg, Throwable t) {
            oneMessage[0] = msg;
            LogUtils.logError(mc, claz, "unspecified method", oneMessage, t);
        }

        public void error(String msg, Throwable t) {
            oneMessage[0] = msg;
            LogUtils.logError(mc, claz, "unspecified method", oneMessage, t);
        }

        public void warning(String msg) {
            LogUtils.logWarn(mc, claz, "unspecified method", msg);
        }

    }

    final private Map<String, Logger> loggers = new HashMap<String, Logger>(128);
    final private ModuleContext mc;

    private static Map<String, LoggerFactory> factories = null;

    private LoggerFactory(ModuleContext mc) {
        this.mc = mc;
    }

    private static void lazyLoad() {
        factories = new HashMap<String, LoggerFactory>(32);
    }

    public static LoggerFactory createLoggerFactory(ModuleContext mc) {
        synchronized (LoggerFactory.class) {
            if (factories == null) {
                lazyLoad();
            }
        }
        LoggerFactory factory = null;
        synchronized (factories) {
            factory = factories.get(mc.getID());
            if (factory != null) {
                return factory;
            }
            factory = new LoggerFactory(mc);
            factories.put(mc.getID(), factory);
        }
        return factory;
    }

    public Logger getLogger(Class<?> clazz) {
        Logger log = null;
        String logName = clazz.getName();
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
