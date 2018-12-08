package com.tutorial.core.convert;

import com.tutorial.core.NestedRuntimeException;

/**
 * Base class for exceptions thrown by the conversion system.
 *
 * @author Keith Donald
 * @since 3.0 
 */
@SuppressWarnings("serial")
public class ConversionException extends NestedRuntimeException {

	/**
	 * Construct a new conversion exception.
	 * @param message the exception message
	 */
	public ConversionException(String msg) {
		super(msg);
	}

	/**
	 * Construct a new conversion exception.
	 * @param message the exception message
	 * @param cause the cause
	 */
	public ConversionException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
