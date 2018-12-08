package com.tutorial.context;

import com.tutorial.beans.FatalBeanException;

/**
 * Exception thrown during application context initialization.
 * 
 * @author Liufeng
 * Created on 2018年12月2日 下午2:50:39
 */
public class ApplicationContextException extends FatalBeanException {
	
	/**
	 * Create a new <code>ApplicationContextException</code>
	 * with the specified detail message and no root cause.
	 * @param msg the detail message
	 */
	public ApplicationContextException(String msg) {
		super(msg);
	}
	
	/**
	 * Create a new <code>ApplicationContextException</code>
	 * with the specified detail message and the given root cause.
	 * @param msg the detail message
	 * @param cause the root cause
	 */
	public ApplicationContextException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
