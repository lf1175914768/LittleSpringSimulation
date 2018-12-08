package com.tutorial.beans.factory.xml;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.tutorial.beans.factory.BeanDefinitionStoreException;

/**
 * XML-specific BeanDefinitionStoreException subclass that wraps a
 * {@link org.xml.sax.SAXException}, typically a {@link org.xml.sax.SAXParseException}
 * which contains information about the error location.
 *
 * @author Juergen Hoeller
 * @since 2.0.2
 * @see #getLineNumber()
 * @see org.xml.sax.SAXParseException
 */
@SuppressWarnings("serial")
public class XmlBeanDefinitionStoreException extends BeanDefinitionStoreException {
	
	/**
	 * Create a new XmlBeanDefinitionStoreException.
	 * @param resourceDescription description of the resource that the bean definition came from
	 * @param msg the detail message (used as exception message as-is)
	 * @param cause the SAXException (typically a SAXParseException) root cause
	 * @see org.xml.sax.SAXParseException
	 */
	public XmlBeanDefinitionStoreException(String resourceDescription, String msg, SAXException cause) {
		super(resourceDescription, msg, cause);
	}
	
	public int getLineNumber() {
		Throwable cause = getCause();
		if(cause instanceof SAXParseException) {
			return ((SAXParseException) cause).getLineNumber();
		}
		return -1;
	}

}
