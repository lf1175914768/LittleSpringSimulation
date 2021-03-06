package com.tutorial.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Utility methods for resolving resource locations to files in the
 * file system. Mainly for internal use within the framework.
 *
 * <p>Consider using Spring's Resource abstraction in the core package
 * for handling all kinds of file resources in a uniform manner.
 * {@link org.springframework.core.io.ResourceLoader}'s <code>getResource</code>
 * method can resolve any location to a {@link org.springframework.core.io.Resource}
 * object, which in turn allows to obtain a <code>java.io.File</code> in the
 * file system through its <code>getFile()</code> method.
 *
 * <p>The main reason for these utility methods for resource location handling
 * is to support {@link Log4jConfigurer}, which must be able to resolve
 * resource locations <i>before the logging system has been initialized</i>.
 * Spring' Resource abstraction in the core package, on the other hand,
 * already expects the logging system to be available.
 *
 * @author Juergen Hoeller
 * @since 1.1.5
 * @see org.springframework.core.io.Resource
 * @see org.springframework.core.io.ClassPathResource
 * @see org.springframework.core.io.FileSystemResource
 * @see org.springframework.core.io.UrlResource
 * @see org.springframework.core.io.ResourceLoader
 */
public abstract class ResourceUtils {
	
	public static final String CLASSPATH_URL_PREFIX = "classpath:";
	
	public static final String FILE_URL_PREFIX = "file:";
	
	public static final String URL_PROTOCOL_FILE = "file";
	
	public static final String URL_PROTOCOL_JAR = "jar";
	
	public static final String URL_PROTOCOL_ZIP = "zip";
	
	public static final String URL_PROTOCOL_VFSZIP = "vfszip"; 
	
	public static final String URL_PROTOCOL_VFS = "vfs";
	
	public static final String URL_PROTOCOL_WSJAR = "wsjar";

	public static final String URL_PROTOCOL_CODE_SOURCE = "code-source";
	
	public static final String JAR_URL_SEPARATOR = "!/";
	
	/**
	 * Create a URI instance for the given URL,
	 * replacing spaces with "%20" quotes first.
	 * <p>Furthermore, this method works on JDK 1.4 as well,
	 * in contrast to the <code>URL.toURI()</code> method.
	 * @param url the URL to convert into a URI instance
	 * @return the URI instance
	 * @throws URISyntaxException if the URL wasn't a valid URI
	 * @see java.net.URL#toURI()
	 */
	public static URI toURI(URL url) throws URISyntaxException {
		return toURI(url.toString());
	}

	/**
	 * Create a URI instance for the given location String,
	 * replacing spaces with "%20" quotes first.
	 * @param location the location String to convert into a URI instance
	 * @return the URI instance
	 * @throws URISyntaxException if the location wasn't a valid URI
	 */
	public static URI toURI(String location) throws URISyntaxException {
		return new URI(StringUtils.replace(location, " ", "%20"));
	}

	/**
	 * Resolve the given resource URL to a <code>java.io.File</code>,
	 * i.e. to a file in the file system.
	 * @param resourceUrl the resource URL to resolve
	 * @param description a description of the original resource that
	 * the URL was created for (for example, a class path location)
	 * @return a corresponding File object
	 * @throws FileNotFoundException if the URL cannot be resolved to
	 * a file in the file system
	 */
	public static File getFile(URL resourceUrl, String description) throws FileNotFoundException {
		Assert.notNull(resourceUrl, "Resource URL must not be null");
		if(!URL_PROTOCOL_FILE.equals(resourceUrl.getProtocol())) {
			throw new FileNotFoundException(
					description + " cannot be resolved to absolute file path " +
							"because it does not reside in the file system: " + resourceUrl);
		}
		try {
			return new File(toURI(resourceUrl).getSchemeSpecificPart());
		} catch (URISyntaxException e) {
			return new File(resourceUrl.getFile());
		} 
	}

	/**
	 * Determine whether the given URL points to a resource in a jar file,
	 * that is, has protocol "jar", "zip", "wsjar" or "code-source".
	 * <p>"zip" and "wsjar" are used by BEA WebLogic Server and IBM WebSphere, respectively,
	 * but can be treated like jar files. The same applies to "code-source" URLs on Oracle
	 * OC4J, provided that the path contains a jar separator.
	 * @param url the URL to check
	 * @return whether the URL has been identified as a JAR URL
	 */
	public static boolean isJarURL(URL url) {
		String protocol = url.getProtocol();
		return (URL_PROTOCOL_JAR.equals(protocol) ||
				URL_PROTOCOL_ZIP.equals(protocol) ||
				URL_PROTOCOL_WSJAR.equals(protocol) ||
				(URL_PROTOCOL_CODE_SOURCE.equals(protocol) && url.getPath().contains(JAR_URL_SEPARATOR)));
	}

	/**
	 * Extract the URL for the actual jar file from the given URL
	 * (which may point to a resource in a jar file or to a jar file itself).
	 * @param jarUrl the original URL
	 * @return the URL for the actual jar file
	 * @throws MalformedURLException if no valid jar file URL could be extracted
	 */
	public static URL extractJarFileURL(URL jarUrl) throws MalformedURLException {
		String urlFile = jarUrl.getFile();
		int separatorIndex = urlFile.indexOf(JAR_URL_SEPARATOR);
		if(separatorIndex != -1) {
			String jarFile = urlFile.substring(0, separatorIndex);
			try {
				return new URL(jarFile);
			} catch (MalformedURLException e) {
				// Probably no protocol in original jar URL, like "jar:C:/mypath/myjar.jar".
				// This usually indicates that the jar file resides in the file system.
				if(!jarFile.startsWith("/")) {
					jarFile = "/" + jarFile;
				}
				return new URL(FILE_URL_PREFIX + jarFile);
			}
		}
		return jarUrl;
	}

	/**
	 * Resolve the given resource URI to a <code>java.io.File</code>,
	 * i.e. to a file in the file system.
	 * @param resourceUri the resource URI to resolve
	 * @param description a description of the original resource that
	 * the URI was created for (for example, a class path location)
	 * @return a corresponding File object
	 * @throws FileNotFoundException if the URL cannot be resolved to
	 * a file in the file system
	 */
	public static File getFile(URI resourceUri, String description) throws FileNotFoundException {
		Assert.notNull(resourceUri, "Resource URI must not be null");
		if(!URL_PROTOCOL_FILE.equals(resourceUri.getScheme())) {
			throw new FileNotFoundException(
					description + " cannot be resolved to absolute file path " +
					"because it does not reside in the file system: " + resourceUri);
		}
		return new File(resourceUri.getSchemeSpecificPart());
	}

	/**
	 * Determine whether the given URL points to a resource in the file system,
	 * that is, has protocol "file" or "vfs".
	 * @param url the URL to check
	 * @return whether the URL has been identified as a file system URL
	 */
	public static boolean isFileURL(URL url) {
		String protocol = url.getProtocol();
		return URL_PROTOCOL_FILE.equals(protocol) || protocol.startsWith(URL_PROTOCOL_VFS);
	}

	/**
	 * Return whether the given resource location is a URL:
	 * either a special "classpath" pseudo URL or a standard URL.
	 * @param resourceLocation the location String to check
	 * @return whether the location qualifies as a URL
	 * @see #CLASSPATH_URL_PREFIX
	 * @see java.net.URL
	 */
	public static boolean isUrl(String resourceLocation) {
		if(resourceLocation == null) {
			return false;
		}
		if(resourceLocation.startsWith(CLASSPATH_URL_PREFIX)) {
			return true;
		}
		try {
			new URL(resourceLocation);
			return true;
		} catch (MalformedURLException e) {
			return false;
		}
	}
	
}
