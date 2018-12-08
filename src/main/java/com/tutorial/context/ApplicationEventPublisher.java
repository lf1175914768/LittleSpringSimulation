package com.tutorial.context;

/**
 * Interface that encapsulates event publication functionality.
 * Serves as super-interface for ApplicationContext.
 * 
 * @author Liufeng
 * Created on 2018年11月24日 上午11:15:44
 */
public interface ApplicationEventPublisher {

	/**
	 * Notify all listeners registered with this application of an application
	 * event. Events may be framework events (such as RequestHandledEvent)
	 * or application-specific events.
	 * @param event the event to publish
	 * @see com.tutorial.web.context.support.RequestHandledEvent
	 */
	void publishEvent(ApplicationEvent event);
}
