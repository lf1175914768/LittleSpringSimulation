package com.tutorial.beans.factory.support;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.tutorial.beans.factory.BeanDefinitionStoreException;
import com.tutorial.beans.factory.NoSuchBeanDefinitionException;
import com.tutorial.beans.factory.config.BeanDefinition;
import com.tutorial.core.SimpleAliasRegistry;
import com.tutorial.util.Assert;
import com.tutorial.util.StringUtils;

public class SimpleBeanDefinitionRegistry extends SimpleAliasRegistry implements BeanDefinitionRegistry {
	
	/** Map of bean definition objects, keyed by bean name */
	private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<String, BeanDefinition>();

	public int getBeanDefinitionCount() {
		return this.beanDefinitionMap.size();
	}

	public String[] getBeanDefinitionNames() {
		return StringUtils.toStringArray(this.beanDefinitionMap.keySet());
	}

	public boolean containsBeanDefinition(String name) {
		return this.beanDefinitionMap.containsKey(name);
	}

	public BeanDefinition getBeanDefinition(String name) throws NoSuchBeanDefinitionException {
		BeanDefinition bd = this.beanDefinitionMap.get(name);
		if(bd == null) {
			throw new NoSuchBeanDefinitionException(name);
		}
		return bd;
	}

	public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition)
			throws BeanDefinitionStoreException {
		
		Assert.hasText(beanName, "'beanName' must not be empty");
		Assert.notNull(beanDefinition, "BeanDefinition must not be null");
		this.beanDefinitionMap.put(beanName, beanDefinition);
	}

	public void removeBeanDefinition(String beanName) throws NoSuchBeanDefinitionException {
		if(this.beanDefinitionMap.remove(beanName) == null) {
			throw new NoSuchBeanDefinitionException(beanName);
		}
	}

	public boolean isBeanNameInUse(String beanName) {
		return isAlias(beanName) || containsBeanDefinition(beanName);
	}

}
