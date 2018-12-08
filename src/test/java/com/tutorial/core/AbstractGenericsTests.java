package com.tutorial.core;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

import junit.framework.TestCase;

public abstract class AbstractGenericsTests extends TestCase {
	
	protected Class<?> targetClass;
	
	protected String[] methods;
	
	protected Type expectedResults[];
	
	protected void executeTest() throws NoSuchMethodException {
		String methodName = getName().substring(4);
		methodName = methodName.substring(0, 1).toLowerCase() + methodName.substring(1);
		for(int i = 0; i < this.methods.length; i++) {
			if(methodName.equals(methods[i])) {
				Method method = this.targetClass.getMethod(methodName);
				Type type = getType(method);
				assertEquals(this.expectedResults[i], type);
				return ;
			}
		}
		throw new IllegalArgumentException("Bad test data");
	}
	
	protected abstract Type getType(Method method);
	
}
