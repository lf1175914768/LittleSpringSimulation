package com.tutorial.beans.factory;

import static org.junit.Assert.fail;
import static test.util.TestResourceUtils.qualifiedResource;

import org.junit.Test;

import com.tutorial.core.io.Resource;

public class FactoryBeanTests {

	private static final Class<?> CLASS = FactoryBeanTests.class;
	private static final Resource RETURNS_NULL_CONTEXT = qualifiedResource(CLASS, "returnsNull.xml");
	
	@Test
	public void test() {
		//fail("Not yet implemented");
	}

}
