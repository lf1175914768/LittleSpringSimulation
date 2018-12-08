package com.tutorial.context.support;

import org.junit.Test;

import com.tutorial.beans.factory.support.DefaultListableBeanFactory;

public class GenericApplicationContextTests {

	@Test
	public void testNullBeanRegistration() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		lbf.registerSingleton("nullBean", null);
		new GenericApplicationContext(lbf).refresh();
	}

}
