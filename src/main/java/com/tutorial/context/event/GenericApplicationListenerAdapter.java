package com.tutorial.context.event;

import com.tutorial.aop.support.AopUtils;
import com.tutorial.context.ApplicationEvent;
import com.tutorial.context.ApplicationListener;
import com.tutorial.core.GenericTypeResolver;
import com.tutorial.core.Ordered;
import com.tutorial.util.Assert;

/**
 * {@link SmartApplicationListener} adapter that determines supported event types
 * through introspecting the generically declared type of the target listener.
 * 
 * @author Liufeng
 * Created on 2018年12月1日 下午4:45:57
 */
public class GenericApplicationListenerAdapter implements SmartApplicationListener {
	
	private final ApplicationListener delegate;
	
	public GenericApplicationListenerAdapter(ApplicationListener delegate) {
		Assert.notNull(delegate, "Delegate listener must not be null");
		this.delegate = delegate;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		this.delegate.onApplicationEvent(event);
	}

	@Override
	public int getOrder() {
		return (this.delegate instanceof Ordered ? 
				((Ordered) this.delegate).getOrder() : 
					Ordered.LOWEST_PRECEDENCE);
	}

	@Override
	public boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
		Class<?> typeArg = GenericTypeResolver.resolveTypeArgument(this.delegate.getClass(), ApplicationListener.class);
		if(typeArg == null || typeArg.equals(ApplicationEvent.class)) {
			Class<?> targetClass = AopUtils.getTargetClass(this.delegate);
			if(targetClass != this.delegate.getClass()) {
				typeArg = GenericTypeResolver.resolveTypeArgument(targetClass, ApplicationListener.class);
			}
		} 
		return (typeArg == null || typeArg.isAssignableFrom(eventType));
	}

	@Override
	public boolean supportsSourceType(Class<?> sourceType) {
		return true;
	}

}
