package test.beans;

import com.tutorial.beans.BeansException;
import com.tutorial.beans.factory.BeanFactory;
import com.tutorial.beans.factory.BeanFactoryAware;
import com.tutorial.beans.factory.BeanNameAware;
import com.tutorial.beans.factory.DisposableBean;
import com.tutorial.beans.factory.InitializingBean;
import com.tutorial.beans.factory.config.BeanPostProcessor;

public class LifecycleBean implements BeanNameAware, BeanFactoryAware, InitializingBean, DisposableBean {
	
	protected boolean initMethodDeclared = false;
	
	protected String beanName;
	
	protected BeanFactory owningFactory;
	
	protected boolean postProcessedBeforeInit;
	
	protected boolean inited;
	
	protected boolean initedViaDeclaredInitMethod;
	
	protected boolean postProcessedAfterInit;
	
	protected boolean destroyed;

	public boolean isInitMethodDeclared() {
		return initMethodDeclared;
	}

	public void setInitMethodDeclared(boolean initMethodDeclared) {
		this.initMethodDeclared = initMethodDeclared;
	}

	public String getBeanName() {
		return beanName;
	}
	
	public void postProcessBeforeInit() {
		if(this.inited || this.initedViaDeclaredInitMethod) {
			throw new RuntimeException("Factory called postProcessBeforeInit after afterPropertiesSet");
		}
		if(this.postProcessedBeforeInit) {
			throw new RuntimeException("Factory called postProcessBeforeInit twice");
		}
		this.postProcessedBeforeInit = true;
	}

	public void afterPropertiesSet() {
		if (this.owningFactory == null) {
			throw new RuntimeException("Factory didn't call setBeanFactory before afterPropertiesSet on lifecycle bean");
		}
		if (!this.postProcessedBeforeInit) {
			throw new RuntimeException("Factory didn't call postProcessBeforeInit before afterPropertiesSet on lifecycle bean");
		}
		if (this.initedViaDeclaredInitMethod) {
			throw new RuntimeException("Factory initialized via declared init method before initializing via afterPropertiesSet");
		}
		if (this.inited) {
			throw new RuntimeException("Factory called afterPropertiesSet twice");
		}
		this.inited = true;
	}

	public void declaredInitMethod() {
		if (!this.inited) {
			throw new RuntimeException("Factory didn't call afterPropertiesSet before declared init method");
		}

		if (this.initedViaDeclaredInitMethod) {
			throw new RuntimeException("Factory called declared init method twice");
		}
		this.initedViaDeclaredInitMethod = true;
	}

	public void postProcessAfterInit() {
		if (!this.inited) {
			throw new RuntimeException("Factory called postProcessAfterInit before afterPropertiesSet");
		}
		if (this.initMethodDeclared && !this.initedViaDeclaredInitMethod) {
			throw new RuntimeException("Factory called postProcessAfterInit before calling declared init method");
		}
		if (this.postProcessedAfterInit) {
			throw new RuntimeException("Factory called postProcessAfterInit twice");
		}
		this.postProcessedAfterInit = true;
	}
	
	public void businessMethod() {
		if(!this.inited || (this.initMethodDeclared && !this.initedViaDeclaredInitMethod) ||
				!this.postProcessedAfterInit) {
			throw new RuntimeException("factory didn't initialize lifecycle object correctly");
		}
	}

	public void destroy() throws Exception {
		if(this.destroyed) {
			throw new IllegalStateException("Already destroyed");
		}
		this.destroyed = true;
	}
	
	public boolean isDestroyed() {
		return this.destroyed;
	}

	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.owningFactory = beanFactory;
	}

	public void setBeanName(String name) {
		this.beanName = name;
	}

	public static class PostProcessor implements BeanPostProcessor {

		public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
			if(bean instanceof LifecycleBean) {
				((LifecycleBean) bean).postProcessBeforeInit();
			}
			return bean;
		}

		public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
			if(bean instanceof LifecycleBean) {
				((LifecycleBean) bean).postProcessAfterInit();
			} 
			return bean;
		}
		
	}
	
}
