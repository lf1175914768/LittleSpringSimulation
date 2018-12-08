package com.tutorial.beans.propertyeditors;

import java.beans.PropertyEditorSupport;
import java.io.IOException;
import java.net.URL;

import com.tutorial.core.io.Resource;
import com.tutorial.core.io.ResourceEditor;
import com.tutorial.util.Assert;

/**
 * Editor for <code>java.net.URL</code>, to directly populate a URL property
 * instead of using a String property as bridge.
 *
 * <p>Supports Spring-style URL notation: any fully qualified standard URL
 * ("file:", "http:", etc) and Spring's special "classpath:" pseudo-URL,
 * as well as Spring's context-specific relative file paths.
 *
 * <p>Note: A URL must specify a valid protocol, else it will be rejected
 * upfront. However, the target resource does not necessarily have to exist
 * at the time of URL creation; this depends on the specific resource type.
 *
 * @author Juergen Hoeller
 * @since 15.12.2003
 * @see java.net.URL
 * @see com.tutorial.core.io.ResourceEditor
 * @see com.tutorial.core.io.ResourceLoader
 * @see FileEditor
 * @see InputStreamEditor
 */
public class URLEditor extends PropertyEditorSupport {
	
	private final ResourceEditor resourceEditor;
	
	/**
	 * Create a new URLEditor, using the default ResourceEditor underneath.
	 */
	public URLEditor() {
		this.resourceEditor = new ResourceEditor();
	}
	
	/**
	 * Create a new URLEditor, using the given ResourceEditor underneath.
	 * @param resourceEditor the ResourceEditor to use
	 */
	public URLEditor(ResourceEditor resourceEditor) {
		Assert.notNull(resourceEditor, "ResourceEditor must not be null");
		this.resourceEditor = resourceEditor;
	}
	
	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		this.resourceEditor.setAsText(text);
		Resource resource = (Resource) this.resourceEditor.getValue();
		try {
			setValue(resource != null ? resource.getURL() : null);
		} catch (IOException ex) {
			throw new IllegalArgumentException("Could not retrieve URL for " + resource + ": " + ex.getMessage());
		}
	}
		
	@Override
	public String getAsText() {
		URL url = (URL) getValue();
		return (url != null ? url.toExternalForm() : "");
	}
	
}
