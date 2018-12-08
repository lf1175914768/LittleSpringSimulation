package com.tutorial.beans.factory.xml;

import test.beans.DummyFactory;
import test.beans.TestBean;

public class DummyReferencer {
	
	private TestBean testBean1;
	
	private TestBean testBean2;
	
	private DummyFactory dummyFactory;

	public DummyReferencer() {
	}

	public DummyReferencer(DummyFactory dummyFactory) {
		this.dummyFactory = dummyFactory;
	}

	public void setDummyFactory(DummyFactory dummyFactory) {
		this.dummyFactory = dummyFactory;
	}

	public DummyFactory getDummyFactory() {
		return dummyFactory;
	}

	public void setTestBean1(TestBean testBean1) {
		this.testBean1 = testBean1;
	}

	public TestBean getTestBean1() {
		return testBean1;
	}

	public void setTestBean2(TestBean testBean2) {
		this.testBean2 = testBean2;
	}

	public TestBean getTestBean2() {
		return testBean2;
	}


}
