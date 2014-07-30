package org.universAAL.ri.api.manager.exceptions;

public abstract class RAPIException extends Exception{

    /**
     * 
     */
    private static final long serialVersionUID = 3656460464702955141L;
    
    RAPIException(String msg){
	super(msg);
    }

}
