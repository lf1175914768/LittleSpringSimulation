package com.tutorial.core.io.support;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.tutorial.core.io.Resource;

public class PathMatchingResourcePatternResolverTests {
	
	private PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
	
	private static final String[] CLASSES_IN_COMMONSLOGGING =
			new String[] {"Log.class", "LogConfigurationException.class", "LogFactory.class",
										"LogFactory$1.class", "LogFactory$2.class", "LogFactory$3.class",
										"LogFactory$4.class", "LogFactory$5.class", "LogFactory$6.class",
										"LogSource.class"};

	@Test
	public void test() {
		//fail("Not yet implemented");
	}

	@Test
	public void testInvalidPrefixWithPatternElementInit() throws IOException {
		try {
			resolver.getResources("xx**:**/*.xy");
			fail("Should have thrown FileNotFoundException ");
		} catch (FileNotFoundException e) {
		}
	}
	
	@Test
	public void testSingleResourceOnFileSystem() throws IOException {
		Resource[] resources = resolver.getResources("com/tutorial/core/io/support/PathMatchingResourcePatternResolverTests.class");
		assertEquals(1, resources.length);
		assertProtocolAndFilename(resources[0], "file", "PathMatchingResourcePatternResolverTests.class");
	}
	
	@Test
	public void testSingleResourceInJar() throws IOException {
		Resource[] resources = resolver.getResources("java/net/URL.class");
		assertEquals(1, resources.length);
		assertProtocolAndFilename(resources[0], "jar", "URL.class");
	}
	
	@Test
	public void testClasspathStartWithPatternOnFileSystem() throws IOException {
		Resource[] resources = resolver.getResources("classpath*:com/tutorial/core/io/sup*/*.class");
		//Have to exclude Clover-generated class file here
		//as we might to be running as part of a Clover test run.
		List noCloverResources = new ArrayList();
		for(int i = 0; i < resources.length; i++) {
			if(resources[i].getFileName().indexOf("$__CLOVER_") == -1) {
				noCloverResources.add(resources[i]);
			}
		}
		resources = ((Resource[]) noCloverResources.toArray(new Resource[noCloverResources.size()]));
//		assertProtocolAndFilename(resources, "file", );
	}
	
	@Test
	public void testClasspathWithPatternInJar() throws IOException {
		Resource[] resources = resolver.getResources("classpath:org/apache/commons/logging/*.class");
		assertProtocolAndFileNames(resources, "jar", CLASSES_IN_COMMONSLOGGING);
	}
	
	@Test
	public void testClasspathStartWithPatternInJar() throws IOException {
		Resource[] resources = resolver.getResources("classpath*:org/apache/commons/logging/*.class");
		assertProtocolAndFileNames(resources, "jar", CLASSES_IN_COMMONSLOGGING);
	}

	private void assertProtocolAndFilename(Resource resource, String urlProtocol, String filename) 
			throws IOException {
		assertProtocolAndFileNames(new Resource[] {resource}, urlProtocol, new String[] {filename});
	}

	private void assertProtocolAndFileNames(Resource[] resources, String urlProtocol, String[] fileNames) 
			throws IOException {
		assertEquals("Correct number of the files found", fileNames.length, resources.length);
		for(int i = 0; i < resources.length; i++) {
			Resource resource = resources[i];
			assertEquals(urlProtocol, resource.getURL().getProtocol());
			assertFilenameIn(resource, fileNames);
		}
	}

	private void assertFilenameIn(Resource resource, String[] fileNames) {
		for(int i = 0; i < fileNames.length; i++) {
			if(resource.getFileName().endsWith(fileNames[i])) {
				return ;
			}
		}
		fail("resource [" + resource + "] does not have a filename that matches and of the names in 'fileNames'");
	}
	
}
