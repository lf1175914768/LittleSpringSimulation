package com.tutorial.beans.factory.xml;

import java.io.Serializable;

import test.beans.IndexedTestBean;
import test.beans.TestBean;

@SuppressWarnings("serial")
public class ConstructorDependenciesBean implements Serializable {
	
	private int age;
	
	private String name;
	
	private TestBean spouse1;
	
	private TestBean spouse2;
	
	private IndexedTestBean other;
	
	public ConstructorDependenciesBean(int age) {
		this.age = age;
	}
	
	public ConstructorDependenciesBean(String name) {
		this.name = name;
	}

	public ConstructorDependenciesBean(TestBean spouse1) {
		this.spouse1 = spouse1;
	}

	public ConstructorDependenciesBean(TestBean spouse1, TestBean spouse2) {
		this.spouse1 = spouse1;
		this.spouse2 = spouse2;
	}

	public ConstructorDependenciesBean(TestBean spouse1, TestBean spouse2, int age) {
		this.spouse1 = spouse1;
		this.spouse2 = spouse2;
		this.age = age;
	}

	public ConstructorDependenciesBean(TestBean spouse1, TestBean spouse2, IndexedTestBean other) {
		this.spouse1 = spouse1;
		this.spouse2 = spouse2;
		this.other = other;
	}

	public int getAge() {
		return age;
	}

	public String getName() {
		return name;
	}

	public TestBean getSpouse1() {
		return spouse1;
	}

	public TestBean getSpouse2() {
		return spouse2;
	}

	public IndexedTestBean getOther() {
		return other;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public void setName(String name) {
		this.name = name;
	}
	
}
