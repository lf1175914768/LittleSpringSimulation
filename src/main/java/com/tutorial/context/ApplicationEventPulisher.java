package com.tutorial.context;

/**
 * Interface that encapsulates event publication functionality.
 * Serves as super-interface for ApplicationContext.
 *
 * @author Juergen Hoeller
 * @since 1.1.1
 * @see ApplicationContext
 * @see ApplicationEventPublisherAware
 * @see com.tutorial.context.ApplicationEvent
 * @see com.tutorial.context.event.EventPublicationInterceptor
 */
public interface ApplicationEventPulisher {
	
	/**
	 * Notify all listeners registered with this application of an application
	 * event. Events may be framework events (such as RequestHandledEvent)
	 * or application-specific events.
	 * @param event the event to publish
	 * @see com.tutorial.web.context.support.RequestHandledEvent
	 */
	void publishEvent(ApplicationEvent event);

}
