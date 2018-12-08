package com.tutorial.beans.factory.xml;

import com.tutorial.beans.factory.config.BeanDefinition;
import com.tutorial.beans.factory.support.BeanDefinitionRegistry;

/**
 * Context that gets passed along a bean definition parsing process,
 * encapsulating all relevant configuration as well as state.
 * Nested inside an {@link XmlReaderContext}.
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 * @see XmlReaderContext
 * @see BeanDefinitionParserDelegate
 */
public final class ParserContext {
	
	private final XmlReaderContext readerContext;
	
	private final BeanDefinitionParserDelegate delegate;
	
	private BeanDefinition containingBeanDefinition;
	
	public ParserContext(XmlReaderContext readerContext, BeanDefinitionParserDelegate delegate) {
		this.readerContext = readerContext;
		this.delegate = delegate;
	}
	
	public ParserContext(XmlReaderContext readerContext, BeanDefinitionParserDelegate delegate, 
			BeanDefinition containingBeanDefinition) {
		this.readerContext = readerContext;
		this.delegate = delegate;
		this.containingBeanDefinition = containingBeanDefinition;
	}

	public final XmlReaderContext getReaderContext() {
		return this.readerContext;
	}

	public final BeanDefinitionRegistry getRegistry() {
		return this.readerContext.getRegistry();
	}

	public final BeanDefinitionParserDelegate getDelegate() {
		return this.delegate;
	}

	public final BeanDefinition getContainingBeanDefinition() {
		return this.containingBeanDefinition;
	}
	
	public final boolean isNested() {
		return (this.containingBeanDefinition != null);
	}

}
