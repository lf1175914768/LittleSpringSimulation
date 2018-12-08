package com.tutorial.aop;

/**
 * Minimal interface for exposing the target class behind a proxy.
 *
 * <p>Implemented by AOP proxy objects and proxy factories
 * (via {@link org.springframework.aop.framework.Advised}}
 * as well as by {@link TargetSource TargetSources}.
 * 
 * @author Liufeng
 * Created on 2018年12月1日 下午6:01:43
 */
public interface TargetClassAware {
	
	/**
	 * Return the target class behind the implementing object
	 * (typically a proxy configuration or an actual proxy).
	 * @return the target Class, or <code>null</code> if not known
	 */
	Class<?> getTargetClass();
}
