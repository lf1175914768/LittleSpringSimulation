package com.tutorial.beans.factory.support;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;

import com.tutorial.beans.BeanInstantiationException;
import com.tutorial.beans.BeansException;
import com.tutorial.beans.factory.BeanDefinitionStoreException;
import com.tutorial.beans.factory.BeanFactory;
import com.tutorial.util.BeanUtils;
import com.tutorial.util.ReflectionUtils;
import com.tutorial.util.StringUtils;

/**
 * Simple object instantiation strategy for use in a BeanFactory.
 *
 * <p>Does not support Method Injection, although it provides hooks for subclasses
 * to override to add Method Injection support, for example by overriding methods.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 1.1
 */
public abstract class SimpleInstantiationStrategy implements InstantiationStrategy {
	
	private static final ThreadLocal<Method> currentlyInvokedFactoryMethod = new ThreadLocal<Method>();

	public Object instantiate(RootBeanDefinition beanDefinition, String beanName, BeanFactory owner)
			throws BeansException {
		// Don't override the class with CGLIB if no overrides.
		if(beanDefinition.getMethodOverrides().isEmpty()) {
			Constructor<?> constructorToUse = null;
			synchronized(beanDefinition.constructorArgumentLock) {
				constructorToUse = (Constructor<?>) beanDefinition.resolvedConstructorOrFactoryMethod;
				if(constructorToUse == null) {
					final Class<?> clazz = beanDefinition.getBeanClass();
					if(clazz.isInterface()) {
						throw new BeanInstantiationException(clazz, "Specified class is an interface");
					}
					try {
						if(System.getSecurityManager() != null) {
							constructorToUse = AccessController.doPrivileged(new PrivilegedExceptionAction<Constructor<?>>() {
								public Constructor<?> run() throws Exception {
									return clazz.getDeclaredConstructor((Class<?>[]) null);
								}
							});
						} else {
							constructorToUse = clazz.getDeclaredConstructor((Class<?>[]) null);
						}
						beanDefinition.resolvedConstructorOrFactoryMethod = constructorToUse;
					} catch (Exception ex) {
						throw new BeanInstantiationException(clazz, "No default constructor found", ex);
					}
				}
			} 
			return BeanUtils.instantiateClass(constructorToUse);
		} else {
			// must generate CGLIB subclass.
			return instantiateWithMethodInjection(beanDefinition, beanName, owner);
		}
	}
	
	public Object instantiate(RootBeanDefinition beanDefinition, String beanName, BeanFactory owner, Object factoryBean,
			final Method factoryMethod, Object[] args) throws BeansException {
		try {
			if(System.getSecurityManager() != null) {
				AccessController.doPrivileged(new PrivilegedAction<Object>() {
					public Object run() {
						ReflectionUtils.makeAccessible(factoryMethod);
						return null;
					}
				});
			} else {
				ReflectionUtils.makeAccessible(factoryMethod);
			}
			
			Method priorInvokedFactoryMethod = currentlyInvokedFactoryMethod.get();
			try {
				currentlyInvokedFactoryMethod.set(factoryMethod);
				return factoryMethod.invoke(factoryBean, args);
			} finally {
				if(priorInvokedFactoryMethod != null) {
					currentlyInvokedFactoryMethod.set(priorInvokedFactoryMethod);
				} else {
					currentlyInvokedFactoryMethod.remove();
				}
			}
		} catch (IllegalArgumentException ex) {
			throw new BeanDefinitionStoreException(
					"Illegal arguments to factory method [" + factoryMethod + "]; " +
					"args: " + StringUtils.arrayToCommaDelimitedString(args));
		} catch (IllegalAccessException ex) {
			throw new BeanDefinitionStoreException(
					"Cannot access factory method [" + factoryMethod + "]; is it public?");
		} catch (InvocationTargetException ex) {
			throw new BeanDefinitionStoreException(
					"Factory method [" + factoryMethod + "] threw exception", ex.getTargetException());
		}
	}

	public Object instantiate(RootBeanDefinition beanDefinition, String beanName, BeanFactory owner,
			final Constructor<?> ctor, Object[] args) throws BeansException {
		if(beanDefinition.getMethodOverrides().isEmpty()) {
			if(System.getSecurityManager() != null) {
				// use own privileged to change accessibility (when security is on)
				AccessController.doPrivileged(new PrivilegedAction<Object>() {
					public Object run() {
						ReflectionUtils.makeAccessible(ctor);
						return null;
					}
				});
			}
			return BeanUtils.instantiateClass(ctor, args);
		} else {
			return instantiateWithMethodInjection(beanDefinition, beanName, owner, ctor, args);
		}
	}
	
	/**
	 * Return the factory method currently being invoked or {@code null} if none.
	 * Allows factory method implementations to determine whether the current
	 * caller is the container itself as opposed to user code.
	 */
	public static Method getCurrentlyInvokedFactoryMethod() {
		return currentlyInvokedFactoryMethod.get();
	}

	/**
	 * Subclasses can override this method, which is implemented to throw
	 * UnsupportedOperationException, if they can instantiate an object with
	 * the Method Injection specified in the given RootBeanDefinition.
	 * Instantiation should use a no-arg constructor.
	 */
	protected abstract Object instantiateWithMethodInjection(RootBeanDefinition beanDefinition, String beanName,
			BeanFactory owner);

	/**
	 * Subclasses can override this method, which is implemented to throw
	 * UnsupportedOperationException, if they can instantiate an object with
	 * the Method Injection specified in the given RootBeanDefinition.
	 * Instantiation should use the given constructor and parameters.
	 */
	protected abstract Object instantiateWithMethodInjection(RootBeanDefinition beanDefinition, String beanName, BeanFactory owner,
			Constructor<?> ctor, Object[] args);

}
