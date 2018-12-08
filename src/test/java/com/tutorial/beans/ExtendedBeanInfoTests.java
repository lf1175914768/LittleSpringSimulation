package com.tutorial.beans;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.beans.BeanInfo;
import java.beans.IndexedPropertyDescriptor;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;

import org.junit.Test;

public class ExtendedBeanInfoTests {
	
	@Test
	public void standardReadMethodOnly() throws IntrospectionException {
		class C {
			@SuppressWarnings("unused")
			public String getFoo() { return null; }
		}
		BeanInfo bi = Introspector.getBeanInfo(C.class);
		ExtendedBeanInfo ebi = new ExtendedBeanInfo(bi);
		assertThat(hasReadMethodForProperty(bi, "foo"), is(true));
		assertThat(hasWriteMethodForProperty(bi, "foo"), is(false));
		
		assertThat(hasReadMethodForProperty(ebi, "foo"), is(true));
		assertThat(hasWriteMethodForProperty(ebi, "foo"), is(false));
	}
	
	@Test
	public void standardWriteMethodOnly() throws IntrospectionException {
		class C {
			@SuppressWarnings("unused")
			public void setFoo(String f) {}
		}
		BeanInfo bi = Introspector.getBeanInfo(C.class);
		ExtendedBeanInfo ebi = new ExtendedBeanInfo(bi);
		assertThat(hasReadMethodForProperty(bi, "foo"), is(false));
		assertThat(hasWriteMethodForProperty(bi, "foo"), is(true));
		assertThat(hasReadMethodForProperty(ebi, "foo"), is(false));
		assertThat(hasWriteMethodForProperty(ebi, "foo"), is(true));
	}
	
	@Test
	public void standardReadAndWriteMethods() throws IntrospectionException {
		class C {
			@SuppressWarnings("unused")
			public void setFoo(String f) {}
			@SuppressWarnings("unused")
			public String getFoo() {return null;}
		}
		
		BeanInfo bi = Introspector.getBeanInfo(C.class);
		ExtendedBeanInfo ebi = new ExtendedBeanInfo(bi);
		assertThat(hasReadMethodForProperty(bi, "foo"), is(true));
		assertThat(hasWriteMethodForProperty(bi, "foo"), is(true));
		assertThat(hasReadMethodForProperty(ebi, "foo"), is(true));
		assertThat(hasWriteMethodForProperty(ebi, "foo"), is(true));
	}
	
	@Test
	public void nonStandardWriteMethodOnly() throws IntrospectionException {
		@SuppressWarnings("unused") class C {
			public C setFoo(String foo) { return this; }
		}
		BeanInfo bi = Introspector.getBeanInfo(C.class);
		ExtendedBeanInfo ebi = new ExtendedBeanInfo(bi);
		
		assertThat(hasReadMethodForProperty(bi, "foo"), is(false));
		assertThat(hasWriteMethodForProperty(bi, "foo"), is(false));

		assertThat(hasReadMethodForProperty(ebi, "foo"), is(false));
		assertThat(hasWriteMethodForProperty(ebi, "foo"), is(true));
	}
	
	@Test
	public void standardReadAndNonStandardWriteMethods() throws IntrospectionException {
		@SuppressWarnings("unused") class C {
			public String getFoo() { return null; }
			public C setFoo(String foo) { return this; }
		}

		BeanInfo bi = Introspector.getBeanInfo(C.class);
		assertThat(hasReadMethodForProperty(bi, "foo"), is(true));
		assertThat(hasWriteMethodForProperty(bi, "foo"), is(false));
		
		ExtendedBeanInfo ebi = new ExtendedBeanInfo(bi);

		assertThat(hasReadMethodForProperty(bi, "foo"), is(true));
		assertThat(hasWriteMethodForProperty(bi, "foo"), is(true));

		assertThat(hasReadMethodForProperty(ebi, "foo"), is(true));
		assertThat(hasWriteMethodForProperty(ebi, "foo"), is(true));
	}
	
	@Test
	public void standardReadMethodsAndOverloadedNonStandardWriteMethods() throws IntrospectionException, NoSuchMethodException, SecurityException {
		@SuppressWarnings("unused") class C {
			public String getFoo() { return null; }
			public C setFoo(String foo) { return this; }
			public C setFoo(Number foo) { return this; }
		}
		
		BeanInfo bi = Introspector.getBeanInfo(C.class);

		assertThat(hasReadMethodForProperty(bi, "foo"), is(true));
		assertThat(hasWriteMethodForProperty(bi, "foo"), is(false));
		ExtendedBeanInfo ebi = new ExtendedBeanInfo(bi);

		assertThat(hasReadMethodForProperty(bi, "foo"), is(true));
		//assertThat(hasWriteMethodForProperty(bi, "foo"), is(true));

		assertThat(hasReadMethodForProperty(ebi, "foo"), is(true));
		assertThat(hasWriteMethodForProperty(ebi, "foo"), is(true));
		for (PropertyDescriptor pd : ebi.getPropertyDescriptors()) {
			if (pd.getName().equals("foo")) {
				assertThat(pd.getWriteMethod(), is(C.class.getMethod("setFoo", String.class)));
				return;
			}
		}
		fail("never matched write method");
	}
	
	@Test
	public void standardReadMethodInsupperclassAndNonStandardWriteMethodInSubclass() throws IntrospectionException {
		@SuppressWarnings("unused") class B {
			public String getFoo() { return null; }
		}
		@SuppressWarnings("unused") class C extends B {
			public C setFoo(String foo) { return this;}
		}
		
		BeanInfo bi = Introspector.getBeanInfo(C.class);

		assertThat(hasReadMethodForProperty(bi, "foo"), is(true));
		assertThat(hasWriteMethodForProperty(bi, "foo"), is(false));
		
		ExtendedBeanInfo ebi = new ExtendedBeanInfo(bi);

		assertThat(hasReadMethodForProperty(bi, "foo"), is(true));
		assertThat(hasWriteMethodForProperty(bi, "foo"), is(true));

		assertThat(hasReadMethodForProperty(ebi, "foo"), is(true));
		assertThat(hasWriteMethodForProperty(ebi, "foo"), is(true));
	}
	
	@Test
	public void standardReadMethodInSuperAndSubclassesAndGenericBuilderStyleNonStandardWriteMethodInSuperAndSubclasses() throws Exception {
		abstract class B<This extends B<This>> {
			@SuppressWarnings("unchecked")
			protected final This instance = (This) this;
			private String foo;
			public String getFoo() { return foo; }
			public This setFoo(String foo) {
				this.foo = foo;
				return this.instance;
			}
		}

		class C extends B<C> {
			private int bar = -1;
			public int getBar() { return bar; }
			public C setBar(int bar) {
				this.bar = bar;
				return this.instance;
			}
		}

		C c = new C()
			.setFoo("blue")
			.setBar(42);

		assertThat(c.getFoo(), is("blue"));
		assertThat(c.getBar(), is(42));

		BeanInfo bi = Introspector.getBeanInfo(C.class);

		assertThat(hasReadMethodForProperty(bi, "foo"), is(true));
		assertThat(hasWriteMethodForProperty(bi, "foo"), is(false));

		assertThat(hasReadMethodForProperty(bi, "bar"), is(true));
		assertThat(hasWriteMethodForProperty(bi, "bar"), is(false));

		ExtendedBeanInfo ebi = new ExtendedBeanInfo(bi);

		assertThat(hasReadMethodForProperty(bi, "foo"), is(true));
		assertThat(hasWriteMethodForProperty(bi, "foo"), is(true));

		assertThat(hasReadMethodForProperty(bi, "bar"), is(true));
		assertThat(hasWriteMethodForProperty(bi, "bar"), is(true));

		assertThat(hasReadMethodForProperty(ebi, "foo"), is(true));
		assertThat(hasWriteMethodForProperty(ebi, "foo"), is(true));

		assertThat(hasReadMethodForProperty(ebi, "bar"), is(true));
		assertThat(hasWriteMethodForProperty(ebi, "bar"), is(true));
	}
	
	@Test
	public void nonPublicStandardReadAndWriteMethods() throws Exception {
		@SuppressWarnings("unused") class C {
			String getFoo() { return null; }
			C setFoo(String foo) { return this; }
		}

		BeanInfo bi = Introspector.getBeanInfo(C.class);
		ExtendedBeanInfo ebi = new ExtendedBeanInfo(bi);

		assertThat(hasReadMethodForProperty(bi, "foo"), is(false));
		assertThat(hasWriteMethodForProperty(bi, "foo"), is(false));

		assertThat(hasReadMethodForProperty(ebi, "foo"), is(false));
		assertThat(hasWriteMethodForProperty(ebi, "foo"), is(false));
	}

	/**
	 * {@link ExtendedBeanInfo} should behave exactly like {@link BeanInfo}
	 * in strange edge cases.
	 */
	@Test
	public void readMethodReturnsSupertypeOfWriteMethodParameter() throws IntrospectionException {
		@SuppressWarnings("unused") class C {
			public Number getFoo() { return null; }
			public void setFoo(Integer foo) { }
		}

		BeanInfo bi = Introspector.getBeanInfo(C.class);
		ExtendedBeanInfo ebi = new ExtendedBeanInfo(bi);

		assertThat(hasReadMethodForProperty(bi, "foo"), is(true));
		assertThat(hasWriteMethodForProperty(bi, "foo"), is(false));

		assertThat(hasReadMethodForProperty(ebi, "foo"), is(true));
		assertThat(hasWriteMethodForProperty(ebi, "foo"), is(false));
	}

	@Test
	public void indexedReadMethodReturnsSupertypeOfIndexedWriteMethodParameter() throws IntrospectionException {
		@SuppressWarnings("unused") class C {
			public Number getFoos(int index) { return null; }
			public void setFoos(int index, Integer foo) { }
		}

		BeanInfo bi = Introspector.getBeanInfo(C.class);
		ExtendedBeanInfo ebi = new ExtendedBeanInfo(bi);

		assertThat(hasIndexedReadMethodForProperty(bi, "foos"), is(true));
		assertThat(hasIndexedWriteMethodForProperty(bi, "foos"), is(false));

		assertThat(hasIndexedReadMethodForProperty(ebi, "foos"), is(true));
		assertThat(hasIndexedWriteMethodForProperty(ebi, "foos"), is(false));
	}

	/**
	 * {@link ExtendedBeanInfo} should behave exactly like {@link BeanInfo}
	 * in strange edge cases.
	 */
	@Test
	public void readMethodReturnsSubtypeOfWriteMethodParameter() throws IntrospectionException {
		@SuppressWarnings("unused") class C {
			public Integer getFoo() { return null; }
			public void setFoo(Number foo) { }
		}

		BeanInfo bi = Introspector.getBeanInfo(C.class);
		ExtendedBeanInfo ebi = new ExtendedBeanInfo(bi);

		assertThat(hasReadMethodForProperty(bi, "foo"), is(true));
		assertThat(hasWriteMethodForProperty(bi, "foo"), is(false));

		assertThat(hasReadMethodForProperty(ebi, "foo"), is(true));
		assertThat(hasWriteMethodForProperty(ebi, "foo"), is(false));
	}

	@Test
	public void indexedReadMethodReturnsSubtypeOfIndexedWriteMethodParameter() throws IntrospectionException {
		@SuppressWarnings("unused") class C {
			public Integer getFoos(int index) { return null; }
			public void setFoo(int index, Number foo) { }
		}

		BeanInfo bi = Introspector.getBeanInfo(C.class);
		ExtendedBeanInfo ebi = new ExtendedBeanInfo(bi);

		assertThat(hasIndexedReadMethodForProperty(bi, "foos"), is(true));
		assertThat(hasIndexedWriteMethodForProperty(bi, "foos"), is(false));

		assertThat(hasIndexedReadMethodForProperty(ebi, "foos"), is(true));
		assertThat(hasIndexedWriteMethodForProperty(ebi, "foos"), is(false));
	}

	@Test
	public void indexedReadMethodOnly() throws IntrospectionException {
		@SuppressWarnings("unused")
		class C {
			// indexed read method
			public String getFoos(int i) { return null; }
		}

		BeanInfo bi = Introspector.getBeanInfo(C.class);
		BeanInfo ebi = new ExtendedBeanInfo(Introspector.getBeanInfo(C.class));

		assertThat(hasReadMethodForProperty(bi, "foos"), is(false));
		assertThat(hasIndexedReadMethodForProperty(bi, "foos"), is(true));

		assertThat(hasReadMethodForProperty(ebi, "foos"), is(false));
		assertThat(hasIndexedReadMethodForProperty(ebi, "foos"), is(true));
	}
	
	private boolean hasIndexedWriteMethodForProperty(BeanInfo beanInfo, String propertyName) {
		for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
			if (pd.getName().equals(propertyName)) {
				assertThat(propertyName + " property is not indexed", pd, instanceOf(IndexedPropertyDescriptor.class));
				return ((IndexedPropertyDescriptor)pd).getIndexedWriteMethod() != null;
			}
		}
		return false;
	}

	private boolean hasIndexedReadMethodForProperty(BeanInfo beanInfo, String propertyName) {
		for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
			if (pd.getName().equals(propertyName)) {
				assertThat(propertyName + " property is not indexed", pd, instanceOf(IndexedPropertyDescriptor.class));
				return ((IndexedPropertyDescriptor)pd).getIndexedReadMethod() != null;
			}
		}
		return false;
	}

	private Boolean hasWriteMethodForProperty(BeanInfo beanInfo, String name) {
		for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
			if (pd.getName().equals(name)) {
				return pd.getWriteMethod() != null;
			}
		}
		return false;
	}

	private boolean hasReadMethodForProperty(BeanInfo beanInfo, String propertyName) {
		for(PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
			if(pd.getName().equals(propertyName)) {
				return pd.getReadMethod() != null;
			}
		}
		return false;
	}
}
