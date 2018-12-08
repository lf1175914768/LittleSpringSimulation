package com.tutorial.core;

import java.io.IOException;

/**
 * Subclass of {@link IOException} that properly handles a root cause,
 * exposing the root cause just like NestedChecked/RuntimeException does.
 *
 * <p>Proper root cause handling has not been added to standard IOException before
 * Java 6, which is why we need to do it ourselves for Java 5 compatibility purposes.
 *
 * <p>The similarity between this class and the NestedChecked/RuntimeException
 * class is unavoidable, as this class needs to derive from IOException.
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see #getMessage
 * @see #printStackTrace
 * @see org.springframework.core.NestedCheckedException
 * @see org.springframework.core.NestedRuntimeException
 */
public class NestedIOException extends IOException {

	private static final long serialVersionUID = 1L;
	
	static {
		// Eagerly load the NestedExceptionUtils class to avoid classloader deadlock
		// issues on OSGi when calling getMessage(). Reported by Don Brown; SPR-5607.
		NestedExceptionUtils.class.getName();
	}
	//????????????????????????????
	
	public NestedIOException(String msg) {
		super(msg);
	}
	
	/**
	 * Construct a <code>NestedIOException</code> with the specified detail message
	 * and nested exception.
	 * @param msg the detail message
	 * @param cause the nested exception
	 */
	public NestedIOException(String msg, Throwable cause) {
		super(msg);
		initCause(cause);
	}
	
	/**
	 * Return the detail message, including the message from the nested exception
	 * if there is one.
	 */
	@Override 
	public String toString() {
		return NestedExceptionUtils.buildMessage(super.getMessage(), getCause());
	}
	

}
