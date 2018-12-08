package com.tutorial.beans.factory.support;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import com.tutorial.util.Assert;
import com.tutorial.util.ObjectUtils;

/**
 * Extension of MethodOverride that represents an arbitrary
 * override of a method by the IoC container.
 *
 * <p>Any non-final method can be overridden, irrespective of its
 * parameters and return types.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 1.1
 */
public class ReplaceOverride extends MethodOverride {
	
	private final String methodReplacerBeanName;
	
	private List<String> typeIdentifiers = new LinkedList<String>();
	
	/**
	 * Construct a new ReplaceOverride.
	 * @param methodName the name of the method to override
	 * @param methodReplacerBeanName the bean name of the MethodReplacer
	 */
	public ReplaceOverride(String methodName, String methodReplaceBeanName) {
		super(methodName);
		Assert.notNull(methodReplaceBeanName, "Method replacer bean name must not be null");
		this.methodReplacerBeanName = methodReplaceBeanName;
	}
	
	/**
	 * Return the name of the bean implementing MethodReplacer.
	 */
	public String getMethodReplacerBeanName() {
		return this.methodReplacerBeanName;
	}
	
	/**
	 * Add a fragment of a class string, like "Exception"
	 * or "java.lang.Exc", to identify a parameter type.
	 * @param identifier a substring of the fully qualified class name
	 */
	public void addTypeIdentifier(String identifier) {
		this.typeIdentifiers.add(identifier);
	}

	@Override
	public boolean matches(Method method) {
		if(!method.getName().equals(getMethodName())) {
			return false;
		}
		if(!isOverloaded()) {
			// no overloaded: don't worry about arg type matching.
			return true;
		}
		
		// if we got to here, we need to insist on precise argument matching.
		if(this.typeIdentifiers.size() != method.getParameterTypes().length) {
			return false;
		}
		for(int i = 0; i < this.typeIdentifiers.size(); i++) {
			String identifier = this.typeIdentifiers.get(i);
			if(!method.getParameterTypes()[i].getName().contains(identifier)) {
				// This parameter cann't match.
				return false;
			}
		}
		return true;
	}
	
	@Override
	public String toString() {
		return "Replace override for method '" + getMethodName() + "; will call bean '" +
				this.methodReplacerBeanName + "'";
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof ReplaceOverride) || !super.equals(other)) {
			return false;
		}
		ReplaceOverride that = (ReplaceOverride) other;
		return (ObjectUtils.nullSafeEquals(this.methodReplacerBeanName, that.methodReplacerBeanName) &&
				ObjectUtils.nullSafeEquals(this.typeIdentifiers, that.typeIdentifiers));
	}

	@Override
	public int hashCode() {
		int hashCode = super.hashCode();
		hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(this.methodReplacerBeanName);
		hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(this.typeIdentifiers);
		return hashCode;
	}

}
