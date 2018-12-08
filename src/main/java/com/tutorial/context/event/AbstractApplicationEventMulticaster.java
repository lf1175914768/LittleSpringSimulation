package com.tutorial.context.event;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.tutorial.beans.BeansException;
import com.tutorial.beans.factory.BeanFactory;
import com.tutorial.beans.factory.BeanFactoryAware;
import com.tutorial.context.ApplicationEvent;
import com.tutorial.context.ApplicationListener;
import com.tutorial.core.OrderComparator;

/**
 * Abstract implementation of the {@link ApplicationEventMulticaster} interface,
 * providing the basic listener registration facility.
 *
 * <p>Doesn't permit multiple instances of the same listener by default,
 * as it keeps listeners in a linked Set. The collection class used to hold
 * ApplicationListener objects can be overridden through the "collectionClass"
 * bean property.
 *
 * <p>Implementing ApplicationEventMulticaster's actual {@link #multicastEvent} method
 * is left to subclasses. {@link SimpleApplicationEventMulticaster} simply multicasts
 * all events to all registered listeners, invoking them in the calling thread.
 * Alternative implementations could be more sophisticated in those respects.
 * 
 * @author Liufeng
 * Created on 2018年11月25日 下午11:41:55
 */
public abstract class AbstractApplicationEventMulticaster implements ApplicationEventMulticaster, BeanFactoryAware {

	private final ListenerRetriever defaultRetriever = new ListenerRetriever(false);
	
	private BeanFactory beanFactory;
	
	private final Map<ListenerCacheKey, ListenerRetriever> retrieverCache = 
			new ConcurrentHashMap<ListenerCacheKey, ListenerRetriever>();
	
	@Override
	public final void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

	@Override
	public void addApplicationListener(ApplicationListener listener) {
		synchronized(this.defaultRetriever) {
			this.defaultRetriever.applicationListeners.add(listener);
			this.retrieverCache.clear();
		}
	}

	@Override
	public void addApplicationListenerBean(String listenerBeanName) {
		synchronized(this.defaultRetriever) {
			this.defaultRetriever.applicationListenerBeans.add(listenerBeanName);
			this.retrieverCache.clear();
		}
	}

	@Override
	public void removeApplicationListener(ApplicationListener listener) {
		synchronized (this.defaultRetriever) {
			this.defaultRetriever.applicationListeners.remove(listener);
			this.retrieverCache.clear();
		}
	}

	@Override
	public void removeApplicationListenerBean(String listenerBeanName) {
		synchronized (this.defaultRetriever) {
			this.defaultRetriever.applicationListenerBeans.remove(listenerBeanName);
			this.retrieverCache.clear();
		}		
	}

	@Override
	public void removeAllListeners() {
		synchronized (this.defaultRetriever) {
			this.defaultRetriever.applicationListeners.clear();
			this.defaultRetriever.applicationListenerBeans.clear();
			this.retrieverCache.clear();
		}
	}

	/**
	 * Return a Collection containing all ApplicationListeners.
	 * @return a Collection of ApplicationListeners
	 * @see org.springframework.context.ApplicationListener
	 */
	protected Collection<ApplicationListener<?>> getApplicationListeners() {
		return this.defaultRetriever.getApplicationListeners();
	}
	
	/**
	 * Return a Collection of ApplicationListeners matching the given
	 * event type. Non-matching listeners get excluded early.
	 * @param event the event to be propagated. Allows for excluding
	 * non-matching listeners early, based on cached matching information.
	 * @return a Collection of ApplicationListeners
	 * @see org.springframework.context.ApplicationListener
	 */
	protected Collection<ApplicationListener<?>> getApplicationListeners(ApplicationEvent event) {
		Class<? extends ApplicationEvent> eventType = event.getClass();
		Class<?> sourceType = event.getSource().getClass();
		ListenerCacheKey cacheKey = new ListenerCacheKey(eventType, sourceType);
		ListenerRetriever retriever = this.retrieverCache.get(cacheKey);
		if(retriever != null) {
			return retriever.getApplicationListeners();
		} else {
			retriever = new ListenerRetriever(true);
			LinkedList<ApplicationListener<?>> allListeners = new LinkedList<ApplicationListener<?>>();
			synchronized(this.defaultRetriever) {
				for(ApplicationListener<?> listener : this.defaultRetriever.applicationListeners) {
					if(supportsEvent(listener, eventType, sourceType)) {
						retriever.applicationListeners.add(listener);
						allListeners.add(listener);
					}
				}
				if(!this.defaultRetriever.applicationListenerBeans.isEmpty()) {
					BeanFactory beanFactory = getBeanFactory();
					for(String listenerName : this.defaultRetriever.applicationListenerBeans) {
						ApplicationListener<?> listener = beanFactory.getBean(listenerName, ApplicationListener.class);
						if(!allListeners.contains(listener) && supportsEvent(listener, eventType, sourceType)) {
							retriever.applicationListenerBeans.add(listenerName);
							allListeners.add(listener);
						}
					}
				}
				OrderComparator.sort(allListeners);
				this.retrieverCache.put(cacheKey, retriever);
			}
			return allListeners;
		}
	}
	
	/**
	 * Determine whether the given listener supports the given event.
	 * <p>The default implementation detects the {@link SmartApplicationListener}
	 * interface. In case of a standard {@link ApplicationListener}, a
	 * {@link GenericApplicationListenerAdapter} will be used to introspect
	 * the generically declared type of the target listener.
	 * @param listener the target listener to check
	 * @param eventType the event type to check against
	 * @param sourceType the source type to check against
	 * @return whether the given listener should be included in the
	 * candidates for the given event type
	 */
	protected boolean supportsEvent(ApplicationListener<?> listener, 
			Class<? extends ApplicationEvent> eventType, Class<?> sourceType) {
		SmartApplicationListener smartListener = (listener instanceof SmartApplicationListener ? 
				(SmartApplicationListener) listener : new GenericApplicationListenerAdapter(listener));
		return smartListener.supportsEventType(eventType) && smartListener.supportsSourceType(sourceType);
	}
	
	private BeanFactory getBeanFactory() {
		if(this.beanFactory == null) {
			throw new IllegalStateException("ApplicationEventMulticaster cannot retrieve listener beans " +
					"because it is not associated with a BeanFactory");
		} 
		return this.beanFactory;
	}
	
	/**
	 * Cache key for ListenerRetrievers, based on event type and source type.
	 */
	private static class ListenerCacheKey {
		private final Class<?> eventType;
		private final Class<?> sourceType;
		public ListenerCacheKey(Class<?> eventType, Class<?> sourceType) {
			this.eventType = eventType;
			this.sourceType = sourceType;
		}
		
		@Override
		public boolean equals(Object obj) {
			if(this == obj) {
				return true;
			}
			ListenerCacheKey other = (ListenerCacheKey) obj;
			return (this.eventType.equals(other.eventType) && 
					this.sourceType.equals(other.sourceType));
		}
		
		@Override
		public int hashCode() {
			return this.eventType.hashCode() * 29 + this.sourceType.hashCode();
		}
	}
	
	/**
	 * Helper class that encapsulates a specific set of target listeners,
	 * allowing for efficient retrieval of pre-filtered listeners.
	 * <p>An instance of this helper gets cached per event type and source type.
	 */
	private class ListenerRetriever {
		public final Set<ApplicationListener<?>> applicationListeners;
		
		public final Set<String> applicationListenerBeans;
		
		private final boolean preFiltered;
		
		public ListenerRetriever(boolean preFiltered) {
			this.applicationListeners = new LinkedHashSet<ApplicationListener<?>>();
			this.applicationListenerBeans = new LinkedHashSet<String>();
			this.preFiltered = preFiltered;
		}
		
		public Collection<ApplicationListener<?>> getApplicationListeners() {
			LinkedList<ApplicationListener<?>> allListeners = new LinkedList<ApplicationListener<?>>();
			for(ApplicationListener<?> listener : this.applicationListeners) {
				allListeners.add(listener);
			} 
			if(!this.applicationListenerBeans.isEmpty()) {
				BeanFactory beanFactory = getBeanFactory();
				for(String listenerBeanName : this.applicationListenerBeans) {
					ApplicationListener<?> listener = beanFactory.getBean(listenerBeanName, ApplicationListener.class);
					if(this.preFiltered || !allListeners.contains(listener)) {
						allListeners.add(listener);
					}
				}
			}
			OrderComparator.sort(allListeners);
			return allListeners;
		}
	}

}
