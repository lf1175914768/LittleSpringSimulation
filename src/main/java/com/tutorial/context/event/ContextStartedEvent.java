package com.tutorial.context.event;

import com.tutorial.context.ApplicationContext;

/**
 * Event raised when an <code>ApplicationContext</code> gets started.
 * 
 * @author Liufeng
 * Created on 2018年12月9日 上午12:13:50
 */
public class ContextStartedEvent extends ApplicationContextEvent {

	private static final long serialVersionUID = -3974755697000584692L;

	public ContextStartedEvent(ApplicationContext source) {
		super(source);
	}

}
