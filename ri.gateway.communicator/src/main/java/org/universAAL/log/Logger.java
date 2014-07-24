package org.universAAL.log;

public interface Logger {

    public void info(String msg);

    public void debug(String string);

    public void debug(String msg, Throwable t);

    public void error(String msg, Throwable t);

    public void warning(String msg);

}
