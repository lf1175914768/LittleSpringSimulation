package com.tutorial.context.event;

import com.tutorial.context.ApplicationContext;

/**
 * Event raised when an <code>ApplicationContext</code> gets closed.
 * 
 * @author Liufeng
 * Created on 2018年12月8日 下午10:52:23
 */
public class ContextClosedEvent extends ApplicationContextEvent {

	/**
	 * Creates a new ContextClosedEvent.
	 * @param source the <code>ApplicationContext</code> that has been closed
	 * (must not be <code>null</code>)
	 */
	public ContextClosedEvent(ApplicationContext source) {
		super(source);
	}

	private static final long serialVersionUID = 1459152847114653033L;

}
