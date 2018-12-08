package com.tutorial.beans;

import com.tutorial.core.NestedRuntimeException;
import com.tutorial.util.ObjectUtils;

/**
 * Abstract superclass for all exceptions thrown in the beans package
 * and subpackages.
 *
 * <p>Note that this is a runtime (unchecked) exception. Beans exceptions
 * are usually fatal; there is no reason for them to be checked.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
@SuppressWarnings("serial")
public abstract class BeansException extends NestedRuntimeException {

	/**
	 * Create a new BeansException with the specified message.
	 * @param msg the detail message
	 */
	public BeansException(String msg) {
		super(msg);
	}
	
	/**
	 * Create a new BeansException with the specified message
	 * and root cause.
	 * @param msg the detail message
	 * @param cause the root cause
	 */
	public BeansException(String msg, Throwable cause) {
		super(msg, cause);
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(!(obj instanceof BeansException)) {
			return false;
		}
		BeansException other = (BeansException) obj;
		return (getMessage().equals(other.getMessage()) &&
				ObjectUtils.nullSafeEquals(getCause(), other.getCause()));
	}
	
	@Override
	public int hashCode() {
		return getMessage().hashCode();
	}

}
