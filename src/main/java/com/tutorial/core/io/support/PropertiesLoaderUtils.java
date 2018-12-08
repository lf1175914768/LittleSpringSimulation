package com.tutorial.core.io.support;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.Properties;

import com.tutorial.util.Assert;
import com.tutorial.util.ClassUtils;

/**
 * Convenient utility methods for loading of <code>java.util.Properties</code>,
 * performing standard handling of input streams.
 *
 * <p>For more configurable properties loading, including the option of a
 * customized encoding, consider using the PropertiesLoaderSupport class.
 *
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @since 2.0
 * @see PropertiesLoaderSupport
 */
public abstract class PropertiesLoaderUtils {

	/**
	 * Load all properties from the given class path resource,
	 * using the given class loader.
	 * <p>Merges properties if more than one resource of the same name
	 * found in the class path.
	 * @param resourceName the name of the class path resource
	 * @param classLoader the ClassLoader to use for loading
	 * (or <code>null</code> to use the default class loader)
	 * @return the populated Properties instance
	 * @throws IOException if loading failed
	 */
	public static Properties loadAllProperties(String resourceName, 
			ClassLoader classLoader) throws IOException{
		Assert.notNull(resourceName, "Resource name must not be null");
		ClassLoader clToUse = classLoader;
		if(clToUse == null) {
			clToUse = ClassUtils.getDefaultClassLoader();
		}
		Properties properties = new Properties();
		Enumeration<URL> urls = clToUse.getResources(resourceName);
		while(urls.hasMoreElements()) {
			URL url = urls.nextElement();
			InputStream is = null;
			try {
				URLConnection con = url.openConnection();
				con.setUseCaches(false);
				is = con.getInputStream();
				properties.load(is);
			} finally {
				if(is != null) {
					is.close();
				}
			}
		}
		return properties;
	}
	
}
