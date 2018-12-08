package com.tutorial.context.weaving;

import com.tutorial.beans.BeansException;
import com.tutorial.beans.factory.BeanFactory;
import com.tutorial.beans.factory.BeanFactoryAware;
import com.tutorial.beans.factory.config.BeanPostProcessor;
import com.tutorial.context.ConfigurableApplicationContext;
import com.tutorial.intrument.classloading.LoadTimeWeaver;
import com.tutorial.util.Assert;

/**
 * {@link com.tutorial.beans.factory.config.BeanPostProcessor}
 * implementation that passes the context's default {@link LoadTimeWeaver}
 * to beans that implement the {@link LoadTimeWeaverAware} interface.
 *
 * <p>{@link com.tutorial.context.ApplicationContext Application contexts}
 * will automatically register this with their underlying {@link BeanFactory bean factory},
 * provided that a default <code>LoadTimeWeaver</code> is actually available.
 *
 * <p>Applications should not use this class directly.
 * 
 * @author Liufeng
 * Created on 2018年11月25日 上午12:06:05
 */
public class LoadTimeWeaverAwareProcessor implements BeanPostProcessor, BeanFactoryAware {
	
	private LoadTimeWeaver loadTimeWeaver;
	
	private BeanFactory beanFactory;
	
	/**
	 * Create a new <code>LoadTimeWeaverAwareProcessor</code> that will
	 * auto-retrieve the {@link LoadTimeWeaver} from the containing
	 * {@link BeanFactory}, expecting a bean named
	 * {@link ConfigurableApplicationContext#LOAD_TIME_WEAVER_BEAN_NAME "loadTimeWeaver"}.
	 */
	public LoadTimeWeaverAwareProcessor() {
	}
	
	/**
	 * Create a new <code>LoadTimeWeaverAwareProcessor</code> for the given
	 * {@link LoadTimeWeaver}.
	 * <p>If the given <code>loadTimeWeaver</code> is <code>null</code>, then a
	 * <code>LoadTimeWeaver</code> will be auto-retrieved from the containing
	 * {@link BeanFactory}, expecting a bean named
	 * {@link ConfigurableApplicationContext#LOAD_TIME_WEAVER_BEAN_NAME "loadTimeWeaver"}.
	 * @param loadTimeWeaver the specific <code>LoadTimeWeaver</code> that is to be used
	 */
	public LoadTimeWeaverAwareProcessor(LoadTimeWeaver loadTimeWeaver) {
		this.loadTimeWeaver = loadTimeWeaver;
	}
	
	/**
	 * Create a new <code>LoadTimeWeaverAwareProcessor</code>.
	 * <p>The <code>LoadTimeWeaver</code> will be auto-retrieved from
	 * the given {@link BeanFactory}, expecting a bean named
	 * {@link ConfigurableApplicationContext#LOAD_TIME_WEAVER_BEAN_NAME "loadTimeWeaver"}.
	 * @param beanFactory the BeanFactory to retrieve the LoadTimeWeaver from
	 */
	public LoadTimeWeaverAwareProcessor(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		if(bean instanceof LoadTimeWeaverAware) {
			LoadTimeWeaver ltw = this.loadTimeWeaver;
			if(ltw == null) {
				Assert.state(this.beanFactory != null, 
						"BeanFactory required if no LoadTimeWeaver explicitly specified");
				ltw = this.beanFactory.getBean(
						ConfigurableApplicationContext.LOAD_TIME_WEAVER_BEAN_NAME, 
						LoadTimeWeaver.class);
			} 
			((LoadTimeWeaverAware) bean).setLoadTimeWeaver(ltw);
		}
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

}
