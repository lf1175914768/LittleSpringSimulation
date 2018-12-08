package com.tutorial.beans;

import com.tutorial.core.AttributeAccessorSupport;

/**
 * Extension of {@link org.springframework.core.AttributeAccessorSupport},
 * holding attributes as {@link BeanMetadataAttribute} objects in order
 * to keep track of the definition source.
 *
 * @author Juergen Hoeller
 * @since 2.5
 */
public class BeanMetadataAttributeAccessor extends AttributeAccessorSupport 
			implements BeanMetadataElement {

	private Object source;
	
	private static final long serialVersionUID = 1L;

	public Object getSource() {
		return source;
	}

	/**
	 * Set the configuration source <code>Object</code> for this metadata element.
	 * <p>The exact type of the object will depend on the configuration mechanism used.
	 */
	public void setSource(Object source) {
		this.source = source;
	}
	
	/**
	 * Add the given BeanMetadataAttribute to this accessor's set of attributes.
	 * @param attribute the BeanMetadataAttribute object to register
	 */
	public void addMetadataAttribute(BeanMetadataAttribute attribute) {
		super.setAttribute(attribute.getName(), attribute);
	}
	
	/**
	 * Look up the given BeanMetadataAttribute in this accessor's set of attributes.
	 * @param name the name of the attribute
	 * @return the corresponding BeanMetadataAttribute object,
	 * or <code>null</code> if no such attribute defined
	 */
	public BeanMetadataAttribute getMetadataAttribute(String name) {
		return (BeanMetadataAttribute) super.getAttribute(name);
	}
	
	@Override 
	public void setAttribute(String name, Object value) {
		super.setAttribute(name, new BeanMetadataAttribute(name, value));
	}
	
	@Override
	public Object getAttribute(String name) {
		BeanMetadataAttribute attr = (BeanMetadataAttribute) super.getAttribute(name);
		return (attr != null ? attr.getValue() : null);
	}
	
	@Override
	public Object removeAttribute(String name) {
		BeanMetadataAttribute attribute = (BeanMetadataAttribute) super.removeAttribute(name);
		return (attribute != null ? attribute.getValue() : null);
	}

}
