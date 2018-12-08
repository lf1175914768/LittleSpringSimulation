package com.tutorial.context;

import com.tutorial.beans.factory.Aware;
import com.tutorial.core.env.Environment;

/**
 * Interface to be implemented by any bean that wishes to be notified
 * of the {@link Environment} that it runs in.
 * 
 * @author Liufeng
 * Created on 2018年11月24日 上午11:05:24
 */
public interface EnvironmentAware extends Aware {
	
	/**
	 * Set the {@code Environment} that this object runs in.
	 */
	void setEnvironment(Environment environment);
}
