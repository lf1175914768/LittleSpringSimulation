package com.tutorial.context.support;

import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;

import com.tutorial.beans.BeansException;
import com.tutorial.beans.factory.Aware;
import com.tutorial.beans.factory.config.BeanPostProcessor;
import com.tutorial.beans.factory.config.ConfigurableBeanFactory;
import com.tutorial.context.ApplicationEventPublisher;
import com.tutorial.context.ApplicationEventPublisherAware;
import com.tutorial.context.ConfigurableApplicationContext;
import com.tutorial.context.EmbeddedValueResolverAware;
import com.tutorial.context.EnvironmentAware;
import com.tutorial.context.MessageSourceAware;
import com.tutorial.context.ResourceLoaderAware;
import com.tutorial.util.StringValueResolver;

/**
 * @author Liufeng
 * Created on 2018年11月24日 上午10:43:37
 */
public class ApplicationContextAwareProcessor implements BeanPostProcessor {
	
	private final ConfigurableApplicationContext applicationContext;
	
	/**
	 * Create a new ApplicationContextAwareProcessor for the given context.
	 */
	public ApplicationContextAwareProcessor(ConfigurableApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	@Override
	public Object postProcessBeforeInitialization(final Object bean, String beanName) throws BeansException {
		AccessControlContext acc = null;
		if(System.getSecurityManager() != null && 
				(bean instanceof EnvironmentAware || bean instanceof EmbeddedValueResolverAware ||
						bean instanceof ResourceLoaderAware || bean instanceof ApplicationEventPublisherAware ||
						bean instanceof MessageSourceAware || bean instanceof ApplicationContextAware)) {
			acc = this.applicationContext.getBeanFactory().getAccessControlContext();
		} 
		if(acc != null) {
			AccessController.doPrivileged(new PrivilegedAction<Object>() {
				@Override
				public Object run() {
					invokeAwareInterfaces(bean);
					return null;
				}
			}, acc);
		} else {
			invokeAwareInterfaces(bean);
		} 
		return bean;
	}

	private void invokeAwareInterfaces(Object bean) {
		if(bean instanceof Aware) {
			if(bean instanceof EnvironmentAware) {
				((EnvironmentAware) bean).setEnvironment(this.applicationContext.getEnvironment());
			}
			if(bean instanceof EmbeddedValueResolverAware) {
				((EmbeddedValueResolverAware) bean).setEmbeddedValueResolver(
						new EmbeddedValueResolver(this.applicationContext.getBeanFactory()));
			}
			if (bean instanceof ResourceLoaderAware) {
				((ResourceLoaderAware) bean).setResourceLoader(this.applicationContext);
			}
			if (bean instanceof ApplicationEventPublisherAware) {
				((ApplicationEventPublisherAware) bean).setApplicationEventPublisher((ApplicationEventPublisher) this.applicationContext);
			}
			if (bean instanceof MessageSourceAware) {
				((MessageSourceAware) bean).setMessageSource(this.applicationContext);
			}
			if (bean instanceof ApplicationContextAware) {
				((ApplicationContextAware) bean).setApplicationContext(this.applicationContext);
			}
		}
	}
	
	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}
	
	private static class EmbeddedValueResolver implements StringValueResolver {
		private final ConfigurableBeanFactory beanFactory;
		
		public EmbeddedValueResolver(ConfigurableBeanFactory beanFactory) {
			this.beanFactory = beanFactory;
		}

		@Override
		public String resolveStringValue(String strVal) {
			return this.beanFactory.resolveEmbeddedValue(strVal);
		}
		
	}

}
