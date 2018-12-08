package com.tutorial.beans.factory.config;

import java.io.IOException;
import java.util.Properties;

import com.tutorial.beans.factory.FactoryBean;
import com.tutorial.beans.factory.InitializingBean;
import com.tutorial.core.io.support.PropertiesLoaderSupport;

/**
 * Allows for making a properties file from a classpath location available
 * as Properties instance in a bean factory. Can be used to populate
 * any bean property of type Properties via a bean reference.
 *
 * <p>Supports loading from a properties file and/or setting local properties
 * on this FactoryBean. The created Properties instance will be merged from
 * loaded and local values. If neither a location nor local properties are set,
 * an exception will be thrown on initialization.
 *
 * <p>Can create a singleton or a new object on each request.
 * Default is a singleton.
 *
 * @author Juergen Hoeller
 * @see #setLocation
 * @see #setProperties
 * @see #setLocalOverride
 * @see java.util.Properties
 */
public class PropertiesFactoryBean extends PropertiesLoaderSupport
		implements FactoryBean<Properties>, InitializingBean {
	
	private boolean singleton = true;
	
	private Properties singletonInstance;

	public void afterPropertiesSet() throws Exception {
		if(this.singleton) {
			this.singletonInstance = createProperties();
		}
	}

	/**
	 * Template method that subclasses may override to construct the object
	 * returned by this factory. The default implementation returns the
	 * plain merged Properties instance.
	 * <p>Invoked on initialization of this FactoryBean in case of a
	 * shared singleton; else, on each {@link #getObject()} call.
	 * @return the object returned by this factory
	 * @throws IOException if an exception occured during properties loading
	 * @see #mergeProperties()
	 */
	protected Properties createProperties() throws IOException {
		return (Properties) createInstance();
	}

	/**
	 * Template method that subclasses may override to construct the object
	 * returned by this factory. The default implementation returns the
	 * plain merged Properties instance.
	 * <p>Invoked on initialization of this FactoryBean in case of a
	 * shared singleton; else, on each {@link #getObject()} call.
	 * @return the object returned by this factory
	 * @throws IOException if an exception occured during properties loading
	 */
	protected Properties createInstance() throws IOException{
		return mergeProperties();
	}

	public Properties getObject() throws Exception {
		if(this.singleton) {
			return this.singletonInstance;
		} else {
			return createProperties();
		}
	}

	public Class<?> getObjectType() {
		return Properties.class;
	}

	/**
	 * Set whether a shared 'singleton' Properties instance should be
	 * created, or rather a new Properties instance on each request.
	 * <p>Default is "true" (a shared singleton).
	 */
	public final void setSingleton(boolean singleton) {
		this.singleton = singleton;
	}
	
	public final boolean isSingleton() {
		return this.singleton;
	}

}
