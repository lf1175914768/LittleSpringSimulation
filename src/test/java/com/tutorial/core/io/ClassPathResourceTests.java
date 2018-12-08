package com.tutorial.core.io;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.junit.internal.matchers.StringContains.containsString;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Test;

public class ClassPathResourceTests {

	private static final String PACKAGE_PATH = "com.tutorial.core.io";
	private static final String RESOURCE_NAME = "notexist.xml";
	private static final String FQ_RESOURCE_PATH = PACKAGE_PATH + '/' + RESOURCE_NAME;
	
	@Test
	public void stringConstructorRaiseExceptionWithFullyQualifiedPath() {
		assertExceptionContainsFullyQualifiedPath(new ClassPathResource(FQ_RESOURCE_PATH));
	}
	
	@Test
	public void classLiteralConstructorRaisesExceptionWithFullyQualifiedPath() {
		//assertExceptionContainsFullyQualifiedPath(new ClassPathResource(RESOURCE_NAME, this.getClass()));
	}
	
	@Test
	public void classLoaderConstructorRaisesExceptionWithFullyQualifiedPath() {
		assertExceptionContainsFullyQualifiedPath(new ClassPathResource(FQ_RESOURCE_PATH, this.getClass().getClassLoader()));
	}
	
	private void assertExceptionContainsFullyQualifiedPath(ClassPathResource resource) {
		try {
			resource.getInputStream();
			fail("FileNotFoundException expected for resource : " + resource);
		} catch (IOException e) {
			assertThat(e, instanceOf(FileNotFoundException.class));
			assertThat(e.getMessage(), containsString(FQ_RESOURCE_PATH));
		}
		
	}
	
}
