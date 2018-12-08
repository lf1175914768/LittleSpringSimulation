package com.tutorial.context;

/**
 * Interface for objects that may participate in a phased
 * process such as lifecycle management.
 * 
 * @author Liufeng
 * Created on 2018年12月2日 上午11:15:54
 */
public interface Phased {
	
	/**
	 * Return the phase value of this object.
	 */
	int getPhase();

}
