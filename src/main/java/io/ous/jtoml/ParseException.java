package io.ous.jtoml;

public class ParseException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4337092235327691417L;


	public ParseException(String message, int line, int at, Throwable cause) {
		super(message+" at "+line+":"+at, cause);
	}
	
	public ParseException(String message, int line, int at) {
		super(message+" at "+line+":"+at);
	}
}
