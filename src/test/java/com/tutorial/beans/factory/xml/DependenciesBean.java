package com.tutorial.beans.factory.xml;

import com.tutorial.beans.BeansException;
import com.tutorial.beans.factory.BeanFactory;
import com.tutorial.beans.factory.BeanFactoryAware;

import test.beans.TestBean;

public class DependenciesBean implements BeanFactoryAware {

	private int age;
	
	private String name; 
	
	private TestBean spouse;
	
	private BeanFactory beanFactory;
	
	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public TestBean getSpouse() {
		return spouse;
	}

	public void setSpouse(TestBean spouse) {
		this.spouse = spouse;
	}

	public BeanFactory getBeanFactory() {
		return beanFactory;
	}

	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

}
