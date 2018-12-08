package com.tutorial.beans.factory.support;

import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.tutorial.beans.BeansException;
import com.tutorial.beans.factory.BeanCreationException;
import com.tutorial.beans.factory.BeanCurrentlyInCreationException;
import com.tutorial.beans.factory.FactoryBean;
import com.tutorial.beans.factory.FactoryBeanNotInitializedException;

/**
 * Support base class for singleton registries which need to handle
 * {@link com.tutorial.beans.factory.FactoryBean} instances,
 * integrated with {@link DefaultSingletonBeanRegistry}'s singleton management.
 *
 * <p>Serves as base class for {@link AbstractBeanFactory}.
 *
 * @author Juergen Hoeller
 * @since 2.5.1
 */
public class FactoryBeanRegistrySupport extends DefaultSingletonBeanRegistry {
	
	private final Map<String, Object> factoryBeanObjectCache = new ConcurrentHashMap<String, Object>();
	
	/**
	 * Determine the type for the given FactoryBean.
	 * @param factoryBean the FactoryBean instance to check
	 * @return the FactoryBean's object type,
	 * or <code>null</code> if the type cannot be determined yet
	 */
	protected Class<?> getTypeForFactoryBean(final FactoryBean<?> factoryBean) {
		try {
			if(System.getSecurityManager() != null) {
				return AccessController.doPrivileged(new PrivilegedAction<Class<?>>() {
					public Class<?> run() {
						return factoryBean.getObjectType();
					}
				}, getAccessControlContext());
			} else {
				return factoryBean.getObjectType();
			}
		} catch (Throwable e) {
			// Thrown from the FactoryBean's getObjectType implementation.
			logger.warn("FactoryBean threw exception from getObjectType, despite the contract saying " +
					"that it should return null if the type of its object cannot be determined yet", e);
			return null;
		} 
	}
	
	/**
	 * Obtain an object to expose from the given FactoryBean, if available
	 * in cached form. Quick check for minimal synchronization.
	 * @param beanName the name of the bean
	 * @return the object obtained from the FactoryBean,
	 * or <code>null</code> if not available
	 */
	protected Object getCachedObjectForFactoryBean(String beanName) {
		Object object = this.factoryBeanObjectCache.get(beanName);
		return object != NULL_OBJECT ? object : null;
	}
	
	/**
	 * Obtain an object to expose from the given FactoryBean.
	 * @param factory the FactoryBean instance
	 * @param beanName the name of the bean
	 * @param shouldPostProcess whether the bean is subject for post-processing
	 * @return the object obtained from the FactoryBean
	 * @throws BeanCreationException if FactoryBean object creation failed
	 * @see com.tutorial.beans.factory.FactoryBean#getObject()
	 */
	protected Object getObjectFromFactoryBean(FactoryBean<?> factory, String beanName, boolean shouldPostProcess) {
		if(factory.isSingleton() && containsSingleton(beanName)) {
			synchronized(getSingletonMutex()) {
				Object object = this.factoryBeanObjectCache.get(beanName);
				if(object == null) {
					object = doGetObjectFromFactoryBean(factory, beanName, shouldPostProcess);
					this.factoryBeanObjectCache.put(beanName, (object != null ? object : NULL_OBJECT));
				}
				return (object != null ? object : NULL_OBJECT);
			} 
		} else {
			return doGetObjectFromFactoryBean(factory, beanName, shouldPostProcess);
		}
	}
	
	/**
	 * Get a FactoryBean for the given bean if possible.
	 * @param beanName the name of the bean
	 * @param beanInstance the corresponding bean instance
	 * @return the bean instance as FactoryBean
	 * @throws BeansException if the given bean cannot be exposed as a FactoryBean
	 */
	protected FactoryBean<?> getFactoryBean(String beanName, Object beanInstance) throws BeansException {
		if(!(beanInstance instanceof FactoryBean)) {
			throw new BeanCreationException(beanName, 
					"Bean instance of type [" + beanInstance.getClass() + "] is not a FactoryBean");
		}
		return (FactoryBean<?>) beanInstance;
	}

	/**
	 * Obtain an object to expose from the given FactoryBean.
	 * @param factory the FactoryBean instance
	 * @param beanName the name of the bean
	 * @param shouldPostProcess whether the bean is subject for post-processing
	 * @return the object obtained from the FactoryBean
	 * @throws BeanCreationException if FactoryBean object creation failed
	 * @see com.tutorial.beans.factory.FactoryBean#getObject()
	 */
	private Object doGetObjectFromFactoryBean(final FactoryBean<?> factory, 
			final String beanName, final boolean shouldPostProcess) throws BeanCreationException {
		Object object;
		try {
			if(System.getSecurityManager() != null) {
				AccessControlContext acc = getAccessControlContext();
				try {
					object = AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {
						public Object run() throws Exception {
							return factory.getObject();
						}
					}, acc);
				} catch (PrivilegedActionException pae) {
					throw pae.getException();
				}
			} else {
				object = factory.getObject();
			}
		} catch (FactoryBeanNotInitializedException e) {
			throw new BeanCurrentlyInCreationException(beanName, e.toString());
		} catch(Throwable ex) {
			throw new BeanCreationException(beanName, "FactoryBean threw exception on object creation", ex);
		}
		
		// Do not accept a null value for a FactoryBean that's  not fully 
		// initialized yet : Many FactoryBean just return null then.
		if(object == null && isSingletonCurrentlyInCreation(beanName)) {
			throw new BeanCurrentlyInCreationException(
					beanName, "FactoryBean which is currently in creation returned null from getObject");
		}
		if(object != null && shouldPostProcess) {
			try {
				object = postProcessObjectFromFactoryBean(object, beanName);
			} catch (Throwable ex) {
				throw new BeanCreationException(beanName, "Post-processing of the FactoryBean's object failed", ex);
			}
		}
		return object;
	}

	/**
	 * Post-process the given object that has been obtained from the FactoryBean.
	 * The resulting object will get exposed for bean references.
	 * <p>The default implementation simply returns the given object as-is.
	 * Subclasses may override this, for example, to apply post-processors.
	 * @param object the object obtained from the FactoryBean.
	 * @param beanName the name of the bean
	 * @return the object to expose
	 * @throws com.tutorial.beans.BeansException if any post-processing failed
	 */
	protected Object postProcessObjectFromFactoryBean(Object object, String beanName) 
			throws BeansException {
		return object;
	}

	/**
	 * Returns the security context for this bean factory. If a security manager
	 * is set, interaction with the user code will be executed using the privileged
	 * of the security context returned by this method.
	 * @see AccessController#getContext()
	 */
	protected AccessControlContext getAccessControlContext() {
		return AccessController.getContext();
	}

}