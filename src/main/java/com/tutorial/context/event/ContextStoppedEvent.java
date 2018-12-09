package com.tutorial.context.event;

import com.tutorial.context.ApplicationContext;

/**
 * @author Liufeng
 * Created on 2018年12月9日 上午12:15:32
 */
public class ContextStoppedEvent extends ApplicationContextEvent {

	private static final long serialVersionUID = -5441937490898895721L;

	public ContextStoppedEvent(ApplicationContext source) {
		super(source);
	}

}
