package com.tutorial.core;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import com.tutorial.util.Assert;

/**
 * Support class for {@link AttributeAccessor AttributeAccessors}, providing
 * a base implementation of all methods. To be extended by subclasses.
 *
 * <p>{@link Serializable} if subclasses and all attribute values are {@link Serializable}.
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 */
public abstract class AttributeAccessorSupport implements AttributeAccessor, Serializable {

	private static final long serialVersionUID = 1L;
	
	private final Map<String, Object> attributes = new LinkedHashMap<String, Object>();
	

	public void setAttribute(String name, Object value) {
		Assert.notNull(name, "Name must not be null");
		if(value != null){
			this.attributes.put(name, value);
		}
		else {
			this.attributes.remove(name);
		}
	}

	public Object getAttribute(String name) {
		Assert.notNull(name, "Name must not be null");
		return this.attributes.get(name);
	}

	public Object removeAttribute(String name) {
		Assert.notNull(name, "Name must not be null");
		return this.attributes.remove(name);
	}

	public boolean hasAttribute(String name) {
		Assert.notNull(name, "Name must not be null");
		return this.attributes.containsKey(name);
	}

	public String[] attributeNames() {
		return this.attributes.keySet().toArray(new String[attributes.size()]);
	}
	
	/**
	 * Copy the attributes from the supplied AttributeAccessor to this accessor.
	 * @param source the AttributeAccessor to copy from
	 */
	protected void copyAttributesFrom(AttributeAccessor source) {
		Assert.notNull(source, "Source must not be null");
		String[] attributeNames = source.attributeNames();
		for(String attribute : attributeNames) {
			setAttribute(attribute, source.getAttribute(attribute));
		}
	}
	
	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof AttributeAccessorSupport)) {
			return false;
		}
		AttributeAccessorSupport that = (AttributeAccessorSupport) other;
		return this.attributes.equals(that.attributes);
	}

	@Override
	public int hashCode() {
		return this.attributes.hashCode();
	}

}
