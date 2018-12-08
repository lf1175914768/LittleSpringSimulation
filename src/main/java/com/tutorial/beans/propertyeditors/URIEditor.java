package com.tutorial.beans.propertyeditors;

import java.beans.PropertyEditorSupport;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.tutorial.core.io.ClassPathResource;
import com.tutorial.util.ClassUtils;
import com.tutorial.util.ResourceUtils;
import com.tutorial.util.StringUtils;

/**
 * Editor for <code>java.net.URI</code>, to directly populate a URI property
 * instead of using a String property as bridge.
 *
 * <p>Supports Spring-style URI notation: any fully qualified standard URI
 * ("file:", "http:", etc) and Spring's special "classpath:" pseudo-URL,
 * which will be resolved to a corresponding URI.
 *
 * <p>By default, this editor will encode Strings into URIs. For instance,
 * a space will be encoded into {@code %20}. This behavior can be changed
 * by calling the {@link #URIEditor(boolean)} constructor.
 *
 * <p>Note: A URI is more relaxed than a URL in that it does not require
 * a valid protocol to be specified. Any scheme within a valid URI syntax
 * is allowed, even without a matching protocol handler being registered.
 *
 * @author Juergen Hoeller
 * @since 2.0.2
 * @see java.net.URI
 * @see URLEditor
 */
public class URIEditor extends PropertyEditorSupport {

	private final ClassLoader classLoader;
	
	private final boolean encode;
	
	/**
	 * Create a new, encoding URIEditor, converting "classpath:" locations into
	 * standard URIs (not trying to resolve them into physical resources).
	 */
	public URIEditor() {
		this.classLoader = null;
		this.encode = true;
	}
	
	/**
	 * Create a new URIEditor, converting "classpath:" locations into
	 * standard URIs (not trying to resolve them into physical resources).
	 * @param encode indicates whether Strings will be encoded or not
	 */
	public URIEditor(boolean encode) {
		this.classLoader = null;
		this.encode = encode;
	}
	
	/**
	 * Create a new URIEditor, using the given ClassLoader to resolve
	 * "classpath:" locations into physical resource URLs.
	 * @param classLoader the ClassLoader to use for resolving "classpath:" locations
	 * (may be <code>null</code> to indicate the default ClassLoader)
	 */
	public URIEditor(ClassLoader classLoader) {
		this.classLoader = (classLoader != null ? classLoader : ClassUtils.getDefaultClassLoader());
		this.encode = true;
	}
	
	/**
	 * Create a new URIEditor, using the given ClassLoader to resolve
	 * "classpath:" locations into physical resource URLs.
	 * @param classLoader the ClassLoader to use for resolving "classpath:" locations
	 * (may be <code>null</code> to indicate the default ClassLoader)
	 * @param encode indicates whether Strings will be encoded or not
	 */
	public URIEditor(ClassLoader classLoader, boolean encode) {
		this.classLoader = (classLoader != null ? classLoader : ClassUtils.getDefaultClassLoader());
		this.encode = encode;
	}
	
	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		if(StringUtils.hasText(text)) {
			String uri = text.trim();
			if(this.classLoader != null && uri.startsWith(ResourceUtils.CLASSPATH_URL_PREFIX)) {
				ClassPathResource resource = 
						new ClassPathResource(uri.substring(ResourceUtils.CLASSPATH_URL_PREFIX.length()), this.classLoader);
				try {
					String url = resource.getURL().toString();
					setValue(createURI(url));
				} catch (IOException ex) {
					throw new IllegalArgumentException("Could not retrieve URI for " + resource + ": " + ex.getMessage());
				} catch (URISyntaxException ex) {
					throw new IllegalArgumentException("Invalid URI syntax: " + ex);
				} 
			} else {
				try {
					setValue(createURI(uri));
				} catch (URISyntaxException ex) {
					throw new IllegalArgumentException("Invalid URI syntax: " + ex);
				}
			}
		} else {
			setValue(null);
		}
	}

	/**
	 * Create a URI instance for the given (resolved) String value.
	 * <p>The default implementation encodes the value into a RFC
	 * 2396 compliant URI.
	 * @param value the value to convert into a URI instance
	 * @return the URI instance
	 * @throws java.net.URISyntaxException if URI conversion failed
	 */
	protected URI createURI(String url) throws URISyntaxException {
		int colonIndex = url.indexOf(":");
		if(this.encode && colonIndex != -1) {
			int fragmentIndex = url.indexOf('#', colonIndex + 1);
			String schema = url.substring(0, colonIndex);
			String ssp = url.substring(colonIndex + 1, (fragmentIndex > 0 ? fragmentIndex : url.length()));
			String fragment = fragmentIndex > 0 ? url.substring(fragmentIndex + 1) : null;
			return new URI(schema, ssp, fragment);
		} else {
			// Not encoding or the value contains no schema - fallback to default
			return new URI(url);
		}
	}
	
	@Override
	public String getAsText() {
		URI value = (URI) getValue();
		return value != null ? value.toString() : null;
	}
	
}
