package com.tutorial.beans.factory.support;

import com.tutorial.beans.FatalBeanException;

public class BeanDefinitionValidationException extends FatalBeanException {

	public BeanDefinitionValidationException(String msg) {
		super(msg);
	}

	private static final long serialVersionUID = 1L;

	public BeanDefinitionValidationException(String msg, Throwable ex) {
		super(msg, ex);
	}
}
