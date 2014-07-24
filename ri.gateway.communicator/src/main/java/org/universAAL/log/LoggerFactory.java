package org.universAAL.log;

import java.util.HashMap;
import java.util.Map;

import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.utils.LogUtils;

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
