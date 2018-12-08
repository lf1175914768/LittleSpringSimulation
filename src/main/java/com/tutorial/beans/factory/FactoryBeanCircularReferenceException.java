package com.tutorial.beans.factory;

public class FactoryBeanCircularReferenceException extends BeanCreationException {

	public FactoryBeanCircularReferenceException(String beanName, String msg) {
		super(beanName, msg);
	}

	private static final long serialVersionUID = 1L;

}
