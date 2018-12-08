package com.tutorial.context.event;

import com.tutorial.context.ApplicationContext;
import com.tutorial.context.ApplicationEvent;

/**
 * Base class for events raised for an <code>ApplicationContext</code>.
 * 
 * @author Liufeng
 * Created on 2018年12月2日 下午4:41:13
 */
public abstract class ApplicationContextEvent extends ApplicationEvent {
	
	/**
	 * Create a new ContextStartedEvent.
	 * @param source the <code>ApplicationContext</code> that the event is raised for
	 * (must not be <code>null</code>)
	 */
	public ApplicationContextEvent(ApplicationContext source) {
		super(source);
	}
	
	/**
	 * Get the <code>ApplicationContext</code> that the event was raised for.
	 */
	public final ApplicationContext getApplicationContext() {
		return (ApplicationContext) getSource();
	}
}
