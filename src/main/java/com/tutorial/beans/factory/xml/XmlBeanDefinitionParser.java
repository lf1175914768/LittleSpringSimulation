package com.tutorial.beans.factory.xml;

import org.w3c.dom.Document;

import com.tutorial.beans.BeansException;
import com.tutorial.beans.factory.support.BeanDefinitionReader;
import com.tutorial.core.io.Resource;

public interface XmlBeanDefinitionParser {
	
	/**
	 * Parse bean definitions from the given DOM document,
	 * and register them with the given bean factory.
	 * @param reader the bean definition reader, containing the bean factory
	 * to work on and the bean class loader to use. Can also be used to load
	 * further bean definition files referenced by the given document.
	 * @param doc the DOM document
	 * @param resource descriptor of the original XML resource
	 * (useful for displaying parse errors)
	 * @throws BeansException in case of parsing errors
	 */
	int registerBeanDefinitions(BeanDefinitionReader reader, Document doc, Resource resource)
		throws BeansException;
}
