package com.tutorial.context.event;

import java.util.concurrent.Executor;

import com.tutorial.beans.factory.BeanFactory;
import com.tutorial.context.ApplicationEvent;
import com.tutorial.context.ApplicationListener;

/**
 * Simple implementation of the {@link ApplicationEventMulticaster} interface.
 *
 * <p>Multicasts all events to all registered listeners, leaving it up to
 * the listeners to ignore events that they are not interested in.
 * Listeners will usually perform corresponding <code>instanceof</code>
 * checks on the passed-in event object.
 *
 * <p>By default, all listeners are invoked in the calling thread.
 * This allows the danger of a rogue listener blocking the entire application,
 * but adds minimal overhead. Specify an alternative TaskExecutor to have
 * listeners executed in different threads, for example from a thread pool.
 * 
 * @author Liufeng
 * Created on 2018年11月25日 下午11:42:10
 */
public class SimpleApplicationEventMulticaster extends AbstractApplicationEventMulticaster {
	
	private Executor taskExecutor;
	
	public SimpleApplicationEventMulticaster(BeanFactory beanFactory) {
		setBeanFactory(beanFactory);
	}
	
	public SimpleApplicationEventMulticaster() {
	}
	
	/**
	 * Set the TaskExecutor to execute application listeners with.
	 * <p>Default is a SyncTaskExecutor, executing the listeners synchronously
	 * in the calling thread.
	 * <p>Consider specifying an asynchronous TaskExecutor here to not block the
	 * caller until all listeners have been executed. However, note that asynchronous
	 * execution will not participate in the caller's thread context (class loader,
	 * transaction association) unless the TaskExecutor explicitly supports this.
	 * @see org.springframework.core.task.SyncTaskExecutor
	 * @see org.springframework.core.task.SimpleAsyncTaskExecutor
	 */
	public void setTaskExecutor(Executor taskExecutor) {
		this.taskExecutor = taskExecutor;
	}
	
	/**
	 * Return the current TaskExecutor for this multicaster.
	 */
	protected Executor getTaskExecutor() {
		return this.taskExecutor;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void multicastEvent(final ApplicationEvent event) {
		for(final ApplicationListener listener : getApplicationListeners(event)) {
			Executor executor = getTaskExecutor();
			if(executor != null) {
				executor.execute(new Runnable() {
					@Override
					public void run() {
						listener.onApplicationEvent(event);
					}
				});
			} else {
				listener.onApplicationEvent(event);
			}
		}
	}

}
