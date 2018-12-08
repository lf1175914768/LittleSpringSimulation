package test.beans;

import java.io.Serializable;

import com.tutorial.beans.factory.BeanNameAware;
import com.tutorial.beans.factory.DisposableBean;

@SuppressWarnings("serial")
public class DerivedTestBean extends TestBean implements Serializable, BeanNameAware, DisposableBean {

	private String beanName;
	
	private boolean initialized;
	
	private boolean destroy;
	
	public DerivedTestBean() {}
	
	public DerivedTestBean(String[] names) {
		if(names == null || names.length < 2) {
			throw new IllegalArgumentException("Invalid names array");
		}
		setName(names[0]);
		setBeanName(names[1]);
	}
	
	public static DerivedTestBean create(String[] names) {
		return new DerivedTestBean(names);
	}

	public void setBeanName(String beanName) {
		if (this.beanName == null || beanName == null) {
			this.beanName = beanName;
		}
	}

	public String getBeanName() {
		return beanName;
	}

	public void setActualSpouse(TestBean spouse) {
		setSpouse(spouse);
	}

	public void setSpouseRef(String name) {
		setSpouse(new TestBean(name));
	}
	
	public void initialize() {
		this.initialized = true;
	}
	
	public boolean wasInitialized() {
		return this.initialized;
	}

	public void destroy() throws Exception {
		this.destroy = true;
	}
	
	public boolean wasDestroy() {
		return this.destroy;
	}

}
