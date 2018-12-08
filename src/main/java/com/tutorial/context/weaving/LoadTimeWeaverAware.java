package com.tutorial.context.weaving;

import com.tutorial.beans.factory.Aware;
import com.tutorial.intrument.classloading.LoadTimeWeaver;

/**
 * Interface to be implemented by any object that wishes to be notified
 * of the application context's default {@link LoadTimeWeaver}.
 * 
 * @author Liufeng
 * Created on 2018年11月24日 下午11:48:17
 */
public interface LoadTimeWeaverAware extends Aware {
	
	/**
	 * Set the {@link LoadTimeWeaver} of this object's containing
	 * {@link com.tutorial.context.ApplicationContext ApplicationContext}.
	 * <p>Invoked after the population of normal bean properties but before an
	 * initialization callback like
	 * {@link com.tutorial.beans.factory.InitializingBean InitializingBean's}
	 * {@link com.tutorial.beans.factory.InitializingBean#afterPropertiesSet() afterPropertiesSet()}
	 * or a custom init-method. Invoked after
	 * {@link com.tutorial.context.ApplicationContextAware ApplicationContextAware's}
	 * {@link com.tutorial.context.ApplicationContextAware#setApplicationContext setApplicationContext(..)}.
	 * <p><b>NOTE:</b> This method will only be called if there actually is a
	 * <code>LoadTimeWeaver</code> available in the application context. If
	 * there is none, the method will simply not get invoked, assuming that the
	 * implementing object is able to activate its weaving dependency accordingly.
	 * @param loadTimeWeaver the <code>LoadTimeWeaver</code> instance (never <code>null</code>)
	 * @see com.tutorial.beans.factory.InitializingBean#afterPropertiesSet
	 * @see com.tutorial.context.ApplicationContextAware#setApplicationContext
	 */
	void setLoadTimeWeaver(LoadTimeWeaver loadTimeWeaver);

}
