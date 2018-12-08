package com.tutorial.beans;

import java.io.PrintStream;
import java.io.PrintWriter;

import com.tutorial.util.Assert;
import com.tutorial.util.ObjectUtils;

/**
 * Combined exception, composed of individual PropertyAccessException instances.
 * An object of this class is created at the beginning of the binding
 * process, and errors added to it as necessary.
 *
 * <p>The binding process continues when it encounters application-level
 * PropertyAccessExceptions, applying those changes that can be applied
 * and storing rejected changes in an object of this class.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 18 April 2001
 */
@SuppressWarnings("serial")
public class PropertyBatchUpdateException extends BeansException {
	
	private PropertyAccessException[] propertyAccessExceptions;

	/**
	 * Create a new PropertyBatchUpdateException.
	 * @param propertyAccessExceptions the List of PropertyAccessExceptions
	 */
	public PropertyBatchUpdateException(PropertyAccessException[] propertyAccessExceptions) {
		super(null);
		Assert.notEmpty(propertyAccessExceptions, "As least 1 PropertyAccessException required");
		this.propertyAccessExceptions = propertyAccessExceptions;
	}
	
	/**
	 * If this returns 0, no errors were encountered during binding.
	 */
	public final int getExceptionCount() {
		return this.propertyAccessExceptions.length;
	}
	
	/**
	 * Return an array of the propertyAccessExceptions stored in this object.
	 * <p>Will return the empty array (not <code>null</code>) if there were no errors.
	 */
	public final PropertyAccessException[] getPropertyAccessExceptions() {
		return this.propertyAccessExceptions;
	}

	/**
	 * Return the exception for this field, or <code>null</code> if there isn't any.
	 */
	public PropertyAccessException getPropertyAccessException(String propertyName) {
		for (PropertyAccessException pae : this.propertyAccessExceptions) {
			if (ObjectUtils.nullSafeEquals(propertyName, pae.getPropertyName())) {
				return pae;
			}
		}
		return null;
	}
	
	@Override
	public String getMessage() {
		StringBuilder sb = new StringBuilder("Failed properties: ");
		for(int i = 0; i < this.propertyAccessExceptions.length; i++) {
			sb.append(this.propertyAccessExceptions[i].getMessage());
			if(i < this.propertyAccessExceptions.length - 1) {
				sb.append("; ");
			}
		}
		return sb.toString();
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getName()).append("; nested PropertyAccessExceptions (");
		sb.append(getExceptionCount()).append(") are : ");
		for(int i = 0; i < this.propertyAccessExceptions.length; i++) {
			sb.append('\n').append("PropertyAccessException ").append(i + 1).append(": ");
			sb.append(this.propertyAccessExceptions[i]);
		}
		return sb.toString();
	}
	
	@Override
	public void printStackTrace(PrintStream ps) {
		synchronized(ps) {
			ps.println(getClass().getName() + "; nested PropertyAccessException details (" +
					getExceptionCount() + ") are:");
			for (int i = 0; i < this.propertyAccessExceptions.length; i++) {
				ps.println("PropertyAccessException " + (i + 1) + ":");
				this.propertyAccessExceptions[i].printStackTrace(ps);
			}
		}
	}

	@Override
	public void printStackTrace(PrintWriter pw) {
		synchronized (pw) {
			pw.println(getClass().getName() + "; nested PropertyAccessException details (" +
					getExceptionCount() + ") are:");
			for (int i = 0; i < this.propertyAccessExceptions.length; i++) {
				pw.println("PropertyAccessException " + (i + 1) + ":");
				this.propertyAccessExceptions[i].printStackTrace(pw);
			}
		}
	}

	@Override
	public boolean contains(Class<?> exType) {
		if (exType == null) {
			return false;
		}
		if (exType.isInstance(this)) {
			return true;
		}
		for (PropertyAccessException pae : this.propertyAccessExceptions) {
			if (pae.contains(exType)) {
				return true;
			}
		}
		return false;
	}
	
}
