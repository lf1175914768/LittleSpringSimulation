package com.tutorial.beans.factory;

import com.tutorial.beans.FatalBeanException;

/**
 * Exception that indicates an expression evaluation attempt having failed.
 * 
 * @author Liufeng
 * Created on 2018年11月17日 下午4:32:34
 */
public class BeanExpressionException extends FatalBeanException {
	
	/**
	 * Create a new BeanExpressionException with the specified message.
	 * @param msg the detail message
	 */
	public BeanExpressionException(String msg) {
		super(msg);
	}
	
	/**
	 * Create a new BeanExpressionException with the specified message
	 * and root cause.
	 * @param msg the detail message
	 * @param cause the root cause
	 */
	public BeanExpressionException(String msg, Throwable cause) {
		super(msg, cause);
	}
	
}
