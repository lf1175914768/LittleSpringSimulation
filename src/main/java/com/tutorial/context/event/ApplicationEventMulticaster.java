package com.tutorial.context.event;

import com.tutorial.context.ApplicationEvent;
import com.tutorial.context.ApplicationListener;

/**
 * Interface to be implemented by objects that can manage a number of
 * {@link ApplicationListener} objects, and publish events to them.
 *
 * <p>An {@link com.tutorial.context.ApplicationEventPublisher}, typically
 * a Spring {@link com.tutorial.context.ApplicationContext}, can use an
 * ApplicationEventMulticaster as a delegate for actually publishing events.
 * 
 * @author Liufeng
 * Created on 2018年11月25日 下午11:34:23
 */
public interface ApplicationEventMulticaster {
	
	/**
	 * Add a listener to be notified of all events.
	 * @param listener the listener to add
	 */
	void addApplicationListener(ApplicationListener listener);
	
	/**
	 * Add a listener bean to be notified of all events.
	 * @param listenerBeanName the name of the listener bean to add
	 */
	void addApplicationListenerBean(String listenerBeanName);
	
	/**
	 * Remove a listener from the notification list.
	 * @param listener the listener to remove
	 */
	void removeApplicationListener(ApplicationListener listener);
	
	/**
	 * Remove a listener bean from the notification list.
	 * @param listenerBeanName the name of the listener bean to add
	 */
	void removeApplicationListenerBean(String listenerBeanName);
	
	/**
	 * Remove all listeners registered with this multicaster.
	 * <p>After a remove call, the multicaster will perform no action
	 * on event notification until new listeners are being registered.
	 */
	void removeAllListeners();
	
	/**
	 * Multicast the given application event to appropriate listeners.
	 * @param event the event to multicast
	 */
	void multicastEvent(ApplicationEvent event);
}
