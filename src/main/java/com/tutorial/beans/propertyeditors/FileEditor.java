package com.tutorial.beans.propertyeditors;

import java.beans.PropertyEditorSupport;
import java.io.File;
import java.io.IOException;

import com.tutorial.core.io.Resource;
import com.tutorial.core.io.ResourceEditor;
import com.tutorial.util.Assert;
import com.tutorial.util.ResourceUtils;
import com.tutorial.util.StringUtils;

/**
 * Editor for <code>java.io.File</code>, to directly populate a File property
 * from a Spring resource location.
 *
 * <p>Supports Spring-style URL notation: any fully qualified standard URL
 * ("file:", "http:", etc) and Spring's special "classpath:" pseudo-URL.
 *
 * <p><b>NOTE:</b> The behavior of this editor has changed in Spring 2.0.
 * Previously, it created a File instance directly from a filename.
 * As of Spring 2.0, it takes a standard Spring resource location as input;
 * this is consistent with URLEditor and InputStreamEditor now.
 *
 * <p><b>NOTE:</b> In Spring 2.5 the following modification was made.
 * If a file name is specified without a URL prefix or without an absolute path
 * then we try to locate the file using standard ResourceLoader semantics.
 * If the file was not found, then a File instance is created assuming the file
 * name refers to a relative file location.
 *
 * @author Juergen Hoeller
 * @author Thomas Risberg
 * @since 09.12.2003
 * @see java.io.File
 * @see org.springframework.core.io.ResourceEditor
 * @see org.springframework.core.io.ResourceLoader
 * @see URLEditor
 * @see InputStreamEditor
 */
public class FileEditor extends PropertyEditorSupport {
	
	private final ResourceEditor resourceEditor;
	
	/**
	 * Create a new FileEditor,
	 * using the default ResourceEditor underneath.
	 */
	public FileEditor() {
		this.resourceEditor = new ResourceEditor();
	}
	
	/**
	 * Create a new FileEditor,
	 * using the given ResourceEditor underneath.
	 * @param resourceEditor the ResourceEditor to use
	 */
	public FileEditor(ResourceEditor resourceEditor) {
		Assert.notNull(resourceEditor, "ResourceEditor must not be null");
		this.resourceEditor = resourceEditor;
	}
	
	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		if(!StringUtils.hasText(text)) {
			setValue(null);
		}
		
		// check whether we got an absolute file path without "file:" prefix. 
		// for backwards compatibility, we'll consider those as straight file path.
		if(!ResourceUtils.isUrl(text)) {
			File file = new File(text);
			if(file.isAbsolute()) {
				setValue(file);
				return;
			}
		}
		
		// proceed with standard resource location parsing .
		this.resourceEditor.setAsText(text);
		Resource resource = (Resource) this.resourceEditor.getValue();
		
		// if it's a URL or a path pointing to an existing resource. use it as-is.
		if(ResourceUtils.isUrl(text) || resource.exists()) {
			try {
				setValue(resource.getFile());
			} catch (IOException ex) {
				throw new IllegalArgumentException(
						"Could not retrieve File for " + resource + ": " + ex.getMessage());
			}
		} else {
			setValue(new File(text));
		}
		
	}
	
	@Override
	public String getAsText() {
		File file = (File) getValue();
		return file != null ? file.getPath() : "";
	}
	
}
