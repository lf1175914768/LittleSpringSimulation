package com.tutorial.context;

import com.tutorial.beans.factory.HierarchicalBeanFactory;
import com.tutorial.beans.factory.ListableBeanFactory;
import com.tutorial.beans.factory.config.AutowireCapableBeanFactory;
import com.tutorial.core.env.EnvironmentCapable;
import com.tutorial.core.io.support.ResourcePatternResolver;

/** 
 * Central interface to provide configuration for an application.
 * This is read-only while the application is running, but may be
 * reloaded if the implementation supports this.
 *
 * <p>An ApplicationContext provides:
 * <ul>
 * <li>Bean factory methods for accessing application components.
 * Inherited from {@link com.tutorial.beans.factory.ListableBeanFactory}.
 * <li>The ability to load file resources in a generic fashion.
 * Inherited from the {@link com.tutorial.core.io.ResourceLoader} interface.
 * <li>The ability to publish events to registered listeners.
 * Inherited from the {@link ApplicationEventPublisher} interface.
 * <li>The ability to resolve messages, supporting internationalization.
 * Inherited from the {@link MessageSource} interface.
 * <li>Inheritance from a parent context. Definitions in a descendant context
 * will always take priority. This means, for example, that a single parent
 * context can be used by an entire web application, while each servlet has
 * its own child context that is independent of that of any other servlet.
 * </ul>
 *
 * <p>In addition to standard {@link com.tutorial.beans.factory.BeanFactory}
 * lifecycle capabilities, ApplicationContext implementations detect and invoke
 * {@link ApplicationContextAware} beans as well as {@link ResourceLoaderAware},
 * {@link ApplicationEventPublisherAware} and {@link MessageSourceAware} beans.
 *
 * @see ConfigurableApplicationContext
 * @see com.tutorial.beans.factory.BeanFactory
 * @see com.tutorial.core.io.ResourceLoader
 */
public interface ApplicationContext extends EnvironmentCapable, ListableBeanFactory, MessageSource, 
		HierarchicalBeanFactory, ApplicationEventPublisher, ResourcePatternResolver {
	
	/**
	 * Return the unique id of this application context.
	 * @return the unique id of the context, or <code>null</code> if none
	 */
	String getId();
	
	/**
	 * Return a friendly name for this context.
	 * @return a display name for this context (never <code>null</code>)
	*/
	String getDisplayName();
	
	/**
	 * Return the parent context, or <code>null</code> if there is no parent
	 * and this is the root of the context hierarchy.
	 * @return the parent context, or <code>null</code> if there is no parent
	 */
	ApplicationContext getParent();
	
	/**
	 * Return the timestamp when this context was first loaded.
	 * @return the timestamp (ms) when this context was first loaded
	 */
	long getStartupDate();
	
	/**
	 * Expose AutowireCapableBeanFactory functionality for this context.
	 * <p>This is not typically used by application code, except for the purpose
	 * of initializing bean instances that live outside the application context,
	 * applying the Spring bean lifecycle (fully or partly) to them.
	 * <p>Alternatively, the internal BeanFactory exposed by the
	 * {@link ConfigurableApplicationContext} interface offers access to the
	 * AutowireCapableBeanFactory interface too. The present method mainly
	 * serves as convenient, specific facility on the ApplicationContext
	 * interface itself.
	 * @return the AutowireCapableBeanFactory for this context
	 * @throws IllegalStateException if the context does not support
	 * the AutowireCapableBeanFactory interface or does not hold an autowire-capable
	 * bean factory yet (usually if <code>refresh()</code> has never been called)
	 * @see ConfigurableApplicationContext#refresh()
	 * @see ConfigurableApplicationContext#getBeanFactory()
	 */
	AutowireCapableBeanFactory getAutowireCapableBeanFactory() throws IllegalStateException;
}
