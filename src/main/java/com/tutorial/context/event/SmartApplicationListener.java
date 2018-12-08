package com.tutorial.context.event;

import com.tutorial.context.ApplicationEvent;
import com.tutorial.context.ApplicationListener;
import com.tutorial.core.Ordered;

/**
 * Extended variant of the standard {@link ApplicationListener} interface,
 * exposing further metadata such as the supported event type.
 * 
 * @author Liufeng
 * Created on 2018年12月1日 下午12:36:24
 */
public interface SmartApplicationListener extends ApplicationListener<ApplicationEvent>, Ordered {

	/**
	 * Determine whether this listener actually supports the given event type.
	 */
	boolean supportsEventType(Class<? extends ApplicationEvent> eventType);
	
	/**
	 * Determine whether this listener actually supports the given source type.
	 */
	boolean supportsSourceType(Class<?> sourceType);
}
