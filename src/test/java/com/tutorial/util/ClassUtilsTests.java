package com.tutorial.util;

import com.tutorial.util.ClassUtils;

import junit.framework.TestCase;

public class ClassUtilsTests extends TestCase {
	
	public void setUp() {
		InnerClass.argCalled = false;
		InnerClass.noArgCalled = false;
		InnerClass.overloadedCalled= false;
	}
	
	public void testGetQualifiedName() {
		String className =  ClassUtils.getQualifiedName(getClass());
		assertEquals("Class name didnot match", "com.tutorial.utils.ClassUtilsTests", className);
	}
	
	public static class InnerClass {
		static boolean noArgCalled;
		static boolean argCalled;
		static boolean overloadedCalled;
		
		public static void staticMethod() {
			noArgCalled = true;
		}
		public static void staticMethod(String arg) {
			overloadedCalled = true;
		}
		public static void argsStaticMethod(String arg) {
			argCalled = true;
		}		
	}
	private static class OverloadedMethodsClass {
		public void print(String messages) {
			/* no-op */
		}
		public void print(String[] messages) {
			/* no-op */
		}
	}
	private static class SubOverloadedMethodsClass extends OverloadedMethodsClass{
		public void print(String header, String[] messages) {
			/* no-op */
		}
		void print(String header, String[] messages, String footer) {
			/* no-op */
		}
	}
	
}
