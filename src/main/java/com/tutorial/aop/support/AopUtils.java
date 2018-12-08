package com.tutorial.aop.support;

import com.tutorial.aop.SpringProxy;
import com.tutorial.aop.TargetClassAware;
import com.tutorial.util.Assert;
import com.tutorial.util.ClassUtils;

/**
 * Utility methods for AOP support code.
 * Mainly for internal use within Spring's AOP support.
 *
 * <p>See {@link com.tutorial.aop.framework.AopProxyUtils} for a
 * collection of framework-specific AOP utility methods which depend
 * on internals of Spring's AOP framework implementation.
 * 
 * @author Liufeng
 * Created on 2018年12月1日 下午5:56:07
 */
public abstract class AopUtils {

	/**
	 * Determine the target class of the given bean instance which might be an AOP proxy.
	 * <p>Returns the target class for an AOP proxy and the plain class else.
	 * @param candidate the instance to check (might be an AOP proxy)
	 * @return the target class (or the plain class of the given object as fallback;
	 * never <code>null</code>)
	 * @see com.tutorial.aop.TargetClassAware#getTargetClass()
	 * @see com.tutorial.aop.framework.AopProxyUtils#ultimateTargetClass(Object)
	 */
	public static Class<?> getTargetClass(Object candidate) {
		Assert.notNull(candidate, "Candidate object must not be null");
		Class<?> result = null;
		if(candidate instanceof TargetClassAware) {
			result = ((TargetClassAware) candidate).getTargetClass();
		} 
		if(result == null) {
			result = isCglibProxy(candidate) ? candidate.getClass().getSuperclass() : candidate.getClass();
		}
		return result;
	}

	/**
	 * Check whether the given object is a CGLIB proxy. Goes beyond the implementation
	 * in {@link ClassUtils#isCglibProxy(Object)} by checking also to see if the given
	 * object is an instance of {@link SpringProxy}.
	 * @param object the object to check
	 * @see ClassUtils#isCglibProxy(Object)
	 */
	public static boolean isCglibProxy(Object obj) {
		return (obj instanceof SpringProxy && ClassUtils.isCglibProxy(obj));
	}

}
