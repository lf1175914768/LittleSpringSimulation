package com.tutorial.core;

/**
 * Interface to be implemented by a reloading-aware ClassLoader
 * (e.g. a Groovy-based ClassLoader). Detected for example by
 * Spring's CGLIB proxy factory for making a caching decision.
 *
 * <p>If a ClassLoader does <i>not</i> implement this interface,
 * then all of the classes obtained from it should be considered
 * as not reloadable (i.e. cacheable).
 *  
 * @author Liufeng
 * Created on 2018年11月25日 上午12:20:06
 */
public interface SmartClassLoader {
	
	/**
	 * Determine whether the given class is reloadable (in this ClassLoader).
	 * <p>Typically used to check whether the result may be cached (for this
	 * ClassLoader) or whether it should be reobtained every time.
	 * @param clazz the class to check (usually loaded from this ClassLoader)
	 * @return whether the class should be expected to appear in a reloaded
	 * version (with a different <code>Class</code> object) later on
	 */
	boolean isClassReloadable(Class clazz);

}
