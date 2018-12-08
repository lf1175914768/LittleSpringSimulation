package com.tutorial.context;

/**
 * Strategy interface for processing Lifecycle beans within the ApplicationContext.
 * 
 * @author Liufeng
 * Created on 2018年12月2日 上午10:42:31
 */
public interface LifecycleProcessor extends Lifecycle {

	/**
	 * Notification of context refresh, e.g. for auto-starting components.
	 */
	void onRefresh();
	
	/**
	 * Notification of context close phase, e.g. for auto-stopping components.
	 */
	void onClose();
	
}
