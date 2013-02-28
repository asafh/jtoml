package io.ous.jtoml;

public class ParseException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4337092235327691417L;

	public ParseException() {
	}
	
	public ParseException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public ParseException(String message) {
		super(message);
	}
	
	public ParseException(Throwable cause) {
		super(cause);
	}

}
