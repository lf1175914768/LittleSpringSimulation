package com.tutorial.core.io.support;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import com.tutorial.core.io.Resource;
import com.tutorial.util.Assert;
import com.tutorial.util.ObjectUtils;

/**
 * Holder that combines a {@link org.springframework.core.io.Resource}
 * with a specific encoding to be used for reading from the resource.
 *
 * <p>Used as argument for operations that support to read content with
 * a specific encoding (usually through a <code>java.io.Reader</code>.
 *
 * @author Juergen Hoeller
 * @since 1.2.6
 * @see java.io.Reader
 */
public class EncodedResource {
	
	private final Resource resource;
	
	private final String encoding;
	
	/**
	 * Create a new EncodedResource for the given Resource,
	 * not specifying a specific encoding.
	 * @param resource the Resource to hold
	 */
	public EncodedResource(Resource resource) {
		this(resource, null);
	}
	
	/**
	 * Create a new EncodedResource for the given Resource,
	 * using the specified encoding.
	 * @param resource the Resource to hold
	 * @param encoding the encoding to use for reading from the resource
	 */
	public EncodedResource(Resource resource, String encoding) {
		Assert.notNull(resource, "Resource must not be null");
		this.resource = resource;
		this.encoding = encoding;
	}
	
	/**
	 * Return the Resource held.
	 */
	public final Resource getResource() {
		return this.resource;
	}

	/**
	 * Return the encoding to use for reading from the resource,
	 * or <code>null</code> if none specified.
	 */
	public final String getEncoding() {
		return this.encoding;
	}
	
	/**
	 * Open a <code>java.io.Reader</code> for the specified resource,
	 * using the specified encoding (if any).
	 * @throws IOException if opening the Reader failed
	 */
	public Reader getReader() throws IOException {
		if(this.encoding != null) {
			return new InputStreamReader(this.resource.getInputStream(), this.encoding);
		} else {
			return new InputStreamReader(this.resource.getInputStream());
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof EncodedResource) {
			EncodedResource other = (EncodedResource) obj;
			return (this.resource.equals(other.getResource()) && 
					ObjectUtils.nullSafeEquals(this.encoding, other.getEncoding()));
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return this.resource.hashCode();
	}
	
	@Override
	public String toString() {
		return this.resource.toString();
	}
}
