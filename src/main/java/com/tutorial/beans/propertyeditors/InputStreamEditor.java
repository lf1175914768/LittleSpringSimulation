package com.tutorial.beans.propertyeditors;

import java.beans.PropertyEditorSupport;
import java.io.IOException;

import com.tutorial.core.io.Resource;
import com.tutorial.core.io.ResourceEditor;
import com.tutorial.util.Assert;

/**
 * One-way PropertyEditor which can convert from a text String to a
 * <code>java.io.InputStream</code>, interpreting the given String
 * as Spring resource location (e.g. a URL String).
 *
 * <p>Supports Spring-style URL notation: any fully qualified standard URL
 * ("file:", "http:", etc) and Spring's special "classpath:" pseudo-URL.
 *
 * <p>Note that in the default usage, the stream is not closed by Spring itself!
 *
 * @author Juergen Hoeller
 * @since 1.0.1
 * @see java.io.InputStream
 * @see org.springframework.core.io.ResourceEditor
 * @see org.springframework.core.io.ResourceLoader
 * @see URLEditor
 * @see FileEditor
 */
public class InputStreamEditor extends PropertyEditorSupport {
	
	private final ResourceEditor resourceEditor;
	
	/**
	 * Create a new InputStreamEditor,
	 * using the default ResourceEditor underneath.
	 */
	public InputStreamEditor() {
		this.resourceEditor = new ResourceEditor();
	}
	
	/**
	 * Create a new InputStreamEditor,
	 * using the given ResourceEditor underneath.
	 * @param resourceEditor the ResourceEditor to use
	 */
	public InputStreamEditor(ResourceEditor resourceEditor) {
		Assert.notNull(resourceEditor, "ResourceEditor must not be null");
		this.resourceEditor = resourceEditor;
	}
	
	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		this.resourceEditor.setAsText(text);
		Resource resource = (Resource) this.resourceEditor.getValue();
		try {
			setValue(resource != null ? resource.getInputStream() : null);
		} 
		catch(IOException e) {
			throw new IllegalArgumentException("Could not retrieve InputStream from " + 
						resource + ":" + e.getMessage());
		}
	}
	
	/**
	 * This implementation returns <code>null</code> to indicate that
	 * there is no appropriate text representation.
	 */
	@Override
	public String getAsText() {
		return null;
	}
}
