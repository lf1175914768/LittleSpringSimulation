package com.tutorial.beans.factory.support;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Set of method overrides, determining which, if any, methods on a
 * managed object the Spring IoC container will override at runtime.
 *
 * <p>The currently supported {@link MethodOverride} variants are
 * {@link LookupOverride} and {@link ReplaceOverride}.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 1.1
 * @see MethodOverride
 */
public class MethodOverrides {
	
	private final Set<MethodOverride> overrides = new HashSet<MethodOverride>(0);
	
	public MethodOverrides() {}
	
	public MethodOverrides(MethodOverrides other) {
		addOverrides(other);
	}

	public void addOverrides(MethodOverrides other) {
		if(other != null) {
			this.overrides.addAll(other.getOverrides());
		}
	}
	
	public void addOverride(MethodOverride override) {
		this.overrides.add(override);
	}
	
	public boolean isEmpty() {
		return this.overrides.isEmpty();
	}
	
	/**
	 * Return the override for the given method, if any.
	 * @param method method to check for overrides for
	 * @return the method override, or <code>null</code> if none
	 */
	public MethodOverride getOverride(Method method) {
		for(Iterator<MethodOverride> it = this.overrides.iterator(); it.hasNext();) {
			MethodOverride methodOverride = it.next();
			if(methodOverride.matches(method)) {
				return methodOverride;
			}
		}
		return null;
	}

	/**
	 * Return all method overrides contained by this object.
	 * @return Set of MethodOverride objects
	 * @see MethodOverride
	 */
	public Set<MethodOverride> getOverrides() {
		return overrides;
	}
	
	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof MethodOverrides)) {
			return false;
		}
		MethodOverrides that = (MethodOverrides) other;
		return this.overrides.equals(that.overrides);

	}

	@Override
	public int hashCode() {
		return this.overrides.hashCode();
	}

}
