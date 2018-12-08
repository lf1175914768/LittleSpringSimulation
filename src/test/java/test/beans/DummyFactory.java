package test.beans;

import com.tutorial.beans.BeansException;
import com.tutorial.beans.factory.BeanFactory;
import com.tutorial.beans.factory.BeanNameAware;
import com.tutorial.beans.factory.DisposableBean;
import com.tutorial.beans.factory.FactoryBean;
import com.tutorial.beans.factory.InitializingBean;
import com.tutorial.beans.factory.config.AutowireCapableBeanFactory;

/**
 * Simple factory to allow testing of FactoryBean support in AbstractBeanFactory.
 * Depending on whether its singleton property is set, it will return a singleton
 * or a prototype instance.
 *
 * <p>Implements InitializingBean interface, so we can check that
 * factories get this lifecycle callback if they want.
 *
 * @author Rod Johnson
 * @author Chris Beams
 * @since 10.03.2003
 */
public class DummyFactory implements FactoryBean<Object>, BeanNameAware, InitializingBean, DisposableBean {

	public static final String SINGLETON_NAME = "Factory singleton";
	
	private static boolean prototypeCreated;
	
	/**
	 * Clear static state.
	 */
	public static void reset() {
		prototypeCreated = false;
	}

	/**
	 * Default is for factories to return a singleton instance.
	 */
	private boolean singleton = true;
	
	private TestBean testBean;
	
	private String beanName;
	
	private AutowireCapableBeanFactory beanFactory;
	
	private boolean postProcessed;
	
	private boolean initialized;
	
	private TestBean otherTestBean;
	
	public DummyFactory() {
		this.testBean = new TestBean();
		this.testBean.setName(SINGLETON_NAME);
		this.testBean.setAge(25);
	}
	
	public void destroy() throws Exception {
		if(this.testBean != null) {
			this.testBean.setName(null);
		}
	}
	
	public static boolean wasPrototypeCreated() {
		return prototypeCreated;
	}

	public void setOtherTestBean(TestBean otherTestBean) {
		this.otherTestBean = otherTestBean;
		this.testBean.setSpouse(otherTestBean);
	}

	public TestBean getOtherTestBean() {
		return otherTestBean;
	}

	public void setBeanName(String name) {
		this.beanName = name;
	}

	/**
	 * Return the managed object, supporting both singleton
	 * and prototype mode.
	 * @see FactoryBean#getObject()
	 */
	public Object getObject() throws BeansException {
		if(isSingleton()) {
			return this.testBean;
		} else {
			TestBean prototype = new TestBean("prototype create at " + System.currentTimeMillis(), 11);
			if(this.beanFactory != null) {
				this.beanFactory.applyBeanPostProcessorsBeforeInitialization(prototype, this.beanName);
			}
			prototypeCreated = true;
			return prototype;
		}
	}

	public Class<?> getObjectType() {
		return TestBean.class;
	}

	public boolean isSingleton() {
		return this.singleton;
	}

	public void setSingleton(boolean singleton) {
		this.singleton = singleton;
	}

	public String getBeanName() {
		return beanName;
	}

	public BeanFactory getBeanFactory() {
		return beanFactory;
	}

	public void setBeanFactory(BeanFactory beanFactory) {
		this.beanFactory = (AutowireCapableBeanFactory) beanFactory;
		this.beanFactory.applyBeanPostProcessorsBeforeInitialization(this.testBean, this.beanName);
	}

	public void setPostProcessed(boolean postProcessed) {
		this.postProcessed = postProcessed;
	}

	public boolean isPostProcessed() {
		return postProcessed;
	}
	
	public void afterPropertiesSet() {
		if (initialized) {
			throw new RuntimeException("Cannot call afterPropertiesSet twice on the one bean");
		}
		this.initialized = true;
	}

	/**
	 * Was this initialized by invocation of the
	 * afterPropertiesSet() method from the InitializingBean interface?
	 */
	public boolean wasInitialized() {
		return initialized;
	}
}
