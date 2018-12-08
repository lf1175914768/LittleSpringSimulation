package com.tutorial.context.event;

import com.tutorial.context.ApplicationContext;

/**
 * Event raised when an <code>ApplicationContext</code> gets initialized or refreshed.
 * 
 * @author Liufeng
 * Created on 2018年12月2日 下午4:40:12
 */
public class ContextRefreshEvent extends ApplicationContextEvent {
	
	private static final long serialVersionUID = 3001609835137589692L;

	/**
	 * Create a new ContextRefreshedEvent.
	 * @param source the <code>ApplicationContext</code> that has been initialized
	 * or refreshed (must not be <code>null</code>)
	 */
	public ContextRefreshEvent(ApplicationContext source) {
		super(source);
	}
}
