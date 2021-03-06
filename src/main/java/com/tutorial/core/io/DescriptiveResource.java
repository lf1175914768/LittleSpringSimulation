package com.tutorial.core.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Simple {@link Resource} implementation that holds a resource description
 * but does not point to an actually readable resource.
 *
 * <p>To be used as placeholder if a <code>Resource</code> argument is
 * expected by an API but not necessarily used for actual reading.
 *
 * @author Juergen Hoeller
 * @since 1.2.6
 */
public class DescriptiveResource extends AbstractResource {
	
	private final String description;
	
	public DescriptiveResource(String description) {
		this.description = (description == null ? "" : description);
	}
	
	@Override
	public boolean exists() {
		return false;
	}
	
	@Override
	public boolean isReadable() {
		return false;
	}

	public String getDescription() {
		return this.description;
	}

	public InputStream getInputStream() throws IOException {
		throw new FileNotFoundException(
				getDescription() + " cannot be opened because it does not point to a readable resource");
	}
	
	public boolean equals(Object obj) {
		return obj == this || 
				(obj instanceof DescriptiveResource && ((DescriptiveResource) obj).getDescription().equals(this.description));
	}
	
	/**
	 * This implementation returns the hash code of the underlying description String.
	 */
	@Override
	public int hashCode() {
		return this.description.hashCode();
	}

}
