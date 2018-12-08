package test.util;

import com.tutorial.core.io.ClassPathResource;

public class TestResourceUtils {
	
	 /**
     * Loads a {@link ClassPathResource} qualified by the simple name of clazz,
     * and relative to the package for clazz.
     * 
     * <p>Example: given a clazz 'com.foo.BarTests' and a resourceSuffix of 'context.xml',
     * this method will return a ClassPathResource representing com/foo/BarTests-context.xml
     * 
     * <p>Intended for use loading context configuration XML files within JUnit tests.
     * 
     * @param clazz
     * @param resourceSuffix
     */
	public static ClassPathResource qualifiedResource(Class<?> clazz, String resourceSuffix) {
		return new ClassPathResource(String.format("%s-%s", clazz.getSimpleName(), resourceSuffix), clazz);
	}

}
