package com.tutorial.core.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import com.tutorial.core.NestedIOException;
import com.tutorial.util.ResourceUtils;

/**
 * Convenience base class for {@link Resource} implementations,
 * pre-implementing typical behavior.
 *
 * <p>The "exists" method will check whether a File or InputStream can
 * be opened; "isOpen" will always return false; "getURL" and "getFile"
 * throw an exception; and "toString" will return the description.
 *
 * @author Juergen Hoeller
 * @since 28.12.2003
 */
public abstract class AbstractResource implements Resource {

	/**
	 * This implementation checks whether a File can be opened,
	 * falling back to whether an InputStream can be opened.
	 * This will cover both directories and content resources.
	 */
	public boolean exists() {
		try {
			return getFile().exists();
		} catch (IOException e) {
			try {
				InputStream is = getInputStream();
				is.close();
				return true;
			} catch (IOException e1) {
				return false;
			}
		}
	}
	
	/**
	 * This implementation always returns <code>true</code>.
	 */
	public boolean isReadable() {
		return true;
	}

	/**
	 * This implementation always returns <code>false</code>.
	 */
	public boolean isOpen() {
		return false;
	}

	/**
	 * This implementation throws a FileNotFoundException, assuming
	 * that the resource cannot be resolved to a URL.
	 */
	public URL getURL() throws IOException {
		throw new FileNotFoundException(getDescription() + " cannot be resolved to URL");
	}
	
	/**
	 * This implementation builds a URI based on the URL returned
	 * by {@link #getURL()}.
	 */
	public URI getURI() throws IOException {
		URL url = getURL();
		try {
			return ResourceUtils.toURI(url);
		} catch(URISyntaxException e) {
			throw new NestedIOException("Invalid URI [" + url + "]", e);
		}
	}

	/**
	 * This implementation throws a FileNotFoundException, assuming
	 * that the resource cannot be resolved to an absolute file path.
	 */
	public File getFile() throws IOException {
		throw new FileNotFoundException(getDescription() + " cannot be resolved to absolute file path");
	}
	
	/**
	 * This implementation checks the length of the underlying File,
	 * if available.
	 * @see #getFile()
	 */
	public long contentLength() throws IOException {
		return getFile().length();
	}
	
	/**
	 * This implementation checks the timestamp of the underlying File,
	 * if available.
	 * @see #getFileForLastModifiedCheck()
	 */
	public long lastModified() throws IOException {
		long lastModified = getFileForLastModifiedCheck().lastModified();
		if(lastModified == 0L) {
			throw new FileNotFoundException(getDescription() +
					" cannot be resolved in the file system for resolving its last-modified timestamp");
		}
		return lastModified;
	}

	/**
	 * Determine the File to use for timestamp checking.
	 * <p>The default implementation delegates to {@link #getFile()}.
	 * @return the File to use for timestamp checking (never <code>null</code>)
	 * @throws IOException if the resource cannot be resolved as absolute
	 * file path, i.e. if the resource is not available in a file system
	 */
	protected File getFileForLastModifiedCheck() throws IOException {
		return getFile();
	}

	/**
	 * This implementation throws a FileNotFoundException, assuming
	 * that relative resources cannot be created for this resource.
	 */
	public Resource createRelative(String relativePath) throws IOException {
		throw new FileNotFoundException("Cannot create a relative resource for " + getDescription());
	}

	/**
	 * This implementation always throws IllegalStateException,
	 * assuming that the resource does not have a filename.
	 */
	public String getFileName() throws IllegalStateException {
		throw new IllegalStateException(getDescription() + " does not carry a filename");
	}
	
	@Override
	public String toString() {
		return getDescription();
	}
	
	@Override
	public boolean equals(Object obj) {
		return (obj == this || (obj instanceof Resource && ((Resource) obj).getDescription().equals(getDescription())));
	}
	
	@Override
	public int hashCode() {
		return getDescription().hashCode();
	}
}
