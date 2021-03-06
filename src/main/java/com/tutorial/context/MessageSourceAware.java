package com.tutorial.context;

import com.tutorial.beans.factory.Aware;

/**
 * Interface to be implemented by any object that wishes to be notified
 * of the MessageSource (typically the ApplicationContext) that it runs in.
 *
 * <p>Note that the MessageSource can usually also be passed on as bean
 * reference (to arbitrary bean properties or constructor arguments), because
 * it is defined as bean with name "messageSource" in the application context.
 * 
 * @author Liufeng
 * Created on 2018年11月24日 上午11:07:13
 */
public interface MessageSourceAware extends Aware {
	
	/**
	 * Set the MessageSource that this object runs in.
	 * <p>Invoked after population of normal bean properties but before an init
	 * callback like InitializingBean's afterPropertiesSet or a custom init-method.
	 * Invoked before ApplicationContextAware's setApplicationContext.
	 * @param messageSource message sourceto be used by this object
	 */
	void setMessageSource(MessageSource messageSource);
}
