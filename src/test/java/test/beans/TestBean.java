package test.beans;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.tutorial.beans.BeansException;
import com.tutorial.beans.factory.BeanFactory;
import com.tutorial.beans.factory.BeanFactoryAware;
import com.tutorial.beans.factory.BeanNameAware;
import com.tutorial.util.ObjectUtils;

@SuppressWarnings("rawtypes")
public class TestBean implements BeanNameAware, BeanFactoryAware, ITestBean, IOther, Comparable {

	private String beanName;
	
	private BeanFactory beanFactory;
	
	private boolean postProcess;
	
	private boolean jedi;
	
	private boolean destroyed;
	
	private String name;
	
	private String sex;
	
	private int age;
	
	private String touchy;
	
	private String country;
	
	private Integer[] someIntegerArray;
	
	private String date;
	
	private Float myFloat;
	
	private Collection<?> friends;
	
	private Properties someProperties = new Properties();
	
	private List<?> someList = new ArrayList();
	
	private Set<?> someSet = new HashSet();
	
	private Map<?, ?> someMap = new HashMap();
	
	private ITestBean[] spouses;
	
	private String[] stringArray;
	
	private INestedTestBean doctor = new NestedTestBean();
	
	private INestedTestBean lawyer = new NestedTestBean();
	
	private Number someNumber;
	
	private Boolean someBoolean;
	
	private Color favouriteColour;
	
	private IndexedTestBean nestedIndexBean;
	
	private List<Color> otherColors;
	
	private List<?> pets;
	
	public TestBean() {}
	
	public TestBean(String name) {
		this.name = name;
	}
	
	public TestBean(ITestBean spouse) {
		this.spouses = new ITestBean[] {spouse};
	}
	
	public TestBean(String name, int age) {
		this.name = name;
		this.age = age;
	}
	
	public TestBean(ITestBean spouse, Properties someProperties) {
		this.spouses = new ITestBean[] {spouse};
		this.someProperties = someProperties;
	}
	
	public TestBean(List<?> someList) {
		this.someList = someList;
	}
	
	public TestBean(Set<?> someSet) {
		this.someSet = someSet;
	}
	
	public TestBean(Map<?, ?> someMap) {
		this.someMap = someMap;
	}
	
	public TestBean(Properties someProperties) {
		this.someProperties = someProperties;
	}
	
	public int compareTo(Object o) {
		if(this.name != null && o instanceof TestBean) {
			return this.name.compareTo(((TestBean) o).getName());
		} else {
			return 1;
		}
	}

	public void absquatulate() {
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ITestBean getSpouse() {
		return (spouses != null ? spouses[0] : null);
	}

	public void setSpouse(ITestBean spouse) {
		this.spouses = new ITestBean[] {spouse}; 
	}

	public ITestBean[] getSpouses() {
		return this.spouses;
	}

	public String[] getStringArray() {
		return this.stringArray;
	}

	public void setStringArray(String[] stringArray) {
		this.stringArray = stringArray;
	}

	public void exceptional(Throwable e) throws Throwable {
		if(e != null) {
			throw e;
		}
	}

	public Object returnThis() {
		return this;
	}

	public INestedTestBean getDoctor() {
		return this.doctor;
	}

	public INestedTestBean getLawyer() {
		return this.lawyer;
	}

	public IndexedTestBean getNestedIndexedBean() {
		return this.nestedIndexBean;
	}

	public int haveBirthday() {
		return age++;
	}

	public void unreliableFileOperation() throws IOException {
		throw new IOException();
	}

	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

	public BeanFactory getBeanFactory() {
		return beanFactory;
	}

	public void setBeanName(String name) {
		this.beanName = name;
	}

	public String getBeanName() {
		return beanName;
	}

	public boolean isPostProcess() {
		return postProcess;
	}

	public void setPostProcess(boolean postProcess) {
		this.postProcess = postProcess;
	}

	public String getSex() {
		return sex;
	}

	public void setSex(String sex) {
		this.sex = sex;
		if(this.name == null) {
			this.name = sex;
		}
	}

	public boolean isJedi() {
		return jedi;
	}

	public void setJedi(boolean jedi) {
		this.jedi = jedi;
	}

	public String getTouchy() {
		return touchy;
	}

	public void setTouchy(String touchy) throws Exception {
		if(touchy.indexOf('.') != -1) {
			throw new Exception("Can't contain a .");
		}
		if(touchy.indexOf(',') != -1) {
			throw new NumberFormatException("Number format exception: contains a ,");
		}
		this.touchy = touchy;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public Integer[] getSomeIntegerArray() {
		return someIntegerArray;
	}

	public void setSomeIntegerArray(Integer[] someIntegerArray) {
		this.someIntegerArray = someIntegerArray;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public Float getMyFloat() {
		return myFloat;
	}

	public void setMyFloat(Float myFloat) {
		this.myFloat = myFloat;
	}

	public Collection<?> getFriends() {
		return friends;
	}

	public void setFriends(Collection<?> friends) {
		this.friends = friends;
	}

	public Properties getSomeProperties() {
		return someProperties;
	}

	public void setSomeProperties(Properties someProperties) {
		this.someProperties = someProperties;
	}

	public List<?> getSomeList() {
		return someList;
	}

	public void setSomeList(List<?> someList) {
		this.someList = someList;
	}

	public Set<?> getSomeSet() {
		return someSet;
	}

	public void setSomeSet(Set<?> someSet) {
		this.someSet = someSet;
	}

	public Map<?, ?> getSomeMap() {
		return someMap;
	}

	public void setSomeMap(Map<?, ?> someMap) {
		this.someMap = someMap;
	}

	public void setDoctor(INestedTestBean doctor) {
		this.doctor = doctor;
	}

	public void setLawyer(INestedTestBean lawyer) {
		this.lawyer = lawyer;
	}

	public Number getSomeNumber() {
		return someNumber;
	}

	public void setSomeNumber(Number someNumber) {
		this.someNumber = someNumber;
	}

	public Color getFavouriteColour() {
		return favouriteColour;
	}

	public void setFavouriteColour(Color favouriteColour) {
		this.favouriteColour = favouriteColour;
	}

	public Boolean getSomeBoolean() {
		return someBoolean;
	}

	public void setSomeBoolean(Boolean someBoolean) {
		this.someBoolean = someBoolean;
	}

	public IndexedTestBean getNestedIndexBean() {
		return nestedIndexBean;
	}

	public void setNestedIndexBean(IndexedTestBean nestedIndexBean) {
		this.nestedIndexBean = nestedIndexBean;
	}

	public List<Color> getOtherColors() {
		return otherColors;
	}

	public void setOtherColors(List<Color> otherColors) {
		this.otherColors = otherColors;
	}

	public List<?> getPets() {
		return pets;
	}

	public void setPets(List<?> pets) {
		this.pets = pets;
	}

	public boolean isDestroyed() {
		return destroyed;
	}

	public void setDestroyed(boolean destroyed) {
		this.destroyed = destroyed;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		} 
		if(obj == null || !(obj instanceof TestBean)) {
			return false;
		} 
		TestBean other = (TestBean) obj;
		return (ObjectUtils.nullSafeEquals(this.name, other.name) && this.age == other.age);
	}
	
	@Override
	public int hashCode() {
		return this.age;
	}
	
	@Override
	public String toString() {
		return this.name;
	}

}
