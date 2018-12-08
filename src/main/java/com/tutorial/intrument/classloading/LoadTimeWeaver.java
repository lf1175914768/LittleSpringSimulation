package com.tutorial.intrument.classloading;

import java.lang.instrument.ClassFileTransformer;

/**
 * Defines the contract for adding one or more
 * {@link ClassFileTransformer ClassFileTransformers} to a {@link ClassLoader}.
 *
 * <p>Implementations may operate on the current context <code>ClassLoader</code>
 * or expose their own instrumentable <code>ClassLoader</code>.
 * 
 * @author Liufeng
 * Created on 2018年11月24日 下午11:55:53
 */
public interface LoadTimeWeaver {
	
	/**
	 * Add a <code>ClassFileTransformer</code> to be applied by this
	 * <code>LoadTimeWeaver</code>.
	 * @param transformer the <code>ClassFileTransformer</code> to add
	 */
	void addTransformer(ClassFileTransformer transformer);
	
	/**
	 * Return a <code>ClassLoader</code> that supports instrumentation
	 * through AspectJ-style load-time weaving based on user-defined
	 * {@link ClassFileTransformer ClassFileTransformers}.
	 * <p>May be the current <code>ClassLoader</code>, or a <code>ClassLoader</code>
	 * created by this {@link LoadTimeWeaver} instance.
	 * @return the <code>ClassLoader</code> which will expose
	 * instrumented classes according to the registered transformers
	 */
	ClassLoader getInstrumentableClassLoader();
	
	/**
	 * Return a throwaway <code>ClassLoader</code>, enabling classes to be
	 * loaded and inspected without affecting the parent <code>ClassLoader</code>.
	 * <p>Should <i>not</i> return the same instance of the {@link ClassLoader}
	 * returned from an invocation of {@link #getInstrumentableClassLoader()}.
	 * @return a temporary throwaway <code>ClassLoader</code>; should return
	 * a new instance for each call, with no existing state
	 */
	ClassLoader getThrowawayClassLoader();

}
