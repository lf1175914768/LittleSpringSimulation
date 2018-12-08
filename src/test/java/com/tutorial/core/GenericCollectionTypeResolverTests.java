package com.tutorial.core;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Date;
import java.util.Map;
import java.util.Set;

public class GenericCollectionTypeResolverTests extends AbstractGenericsTests {

	protected void setUp() throws Exception {
		this.targetClass = Foo.class;
		this.methods = new String[] {"a", "b", "b2", "b3", "c", "d", "d2", "d3", "e", "e2", "e3"};
		this.expectedResults = new Class[] {
			Integer.class, null, Set.class, Set.class, null, Integer.class,
			Integer.class, Integer.class, Integer.class, Integer.class, Integer.class};
	}
	
	@Override
	protected Type getType(Method method) {
		
		return null;
	}
	
	public void testClassResolution() {
		//assertEquals(String.class, GenericCollectionTypeResolver.getcoll)
	}
	
	public void testProgrammaticListIntrospection() throws Exception {
		
	}
	
	private interface Foo {

		Map<String, Integer> a();

		Map<?, ?> b();

		Map<?, ? extends Set> b2();

		Map<?, ? super Set> b3();

		Map c();

		CustomMap<Date> d();

		CustomMap<?> d2();

		CustomMap d3();

		OtherCustomMap<Date> e();

		OtherCustomMap<?> e2();

		OtherCustomMap e3();
	}
	
	private abstract class CustomSet<T> extends AbstractSet<String> {
	}


	private abstract class CustomMap<T> extends AbstractMap<String, Integer> {
	}


	private abstract class OtherCustomMap<T> implements Map<String, Integer> {
	}

}
