package com.tutorial.context;

import com.tutorial.beans.factory.Aware;
import com.tutorial.util.StringValueResolver;

/**
 * Interface to be implemented by any object that wishes to be notified of a
 * <b>StringValueResolver</b> for the <b> resolution of embedded definition values.
 *
 * <p>This is an alternative to a full ConfigurableBeanFactory dependency via the
 * ApplicationContextAware/BeanFactoryAware interfaces.
 * 
 * @author Liufeng
 * Created on 2018年11月24日 上午11:09:40
 */
public interface EmbeddedValueResolverAware extends Aware {
	
	/**
	 * Set the StringValueResolver to use for resolving embedded definition values.
	 */
	void setEmbeddedValueResolver(StringValueResolver resolver);

}
