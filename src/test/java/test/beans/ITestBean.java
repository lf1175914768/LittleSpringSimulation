package test.beans;

import java.io.IOException;

public interface ITestBean {

	int getAge();
	
	void setAge(int age);
	
	String getName();
	
	void setName(String name);
	
	ITestBean getSpouse();
	
	void setSpouse(ITestBean spouse);
	
	ITestBean[] getSpouses();
	
	String[] getStringArray();
	
	void setStringArray(String[] stringArray);
	
	void exceptional(Throwable e) throws Throwable;
	
	Object returnThis();
	
	INestedTestBean getDoctor();
	
	INestedTestBean getLawyer();
	
	IndexedTestBean getNestedIndexedBean();
	
	int haveBirthday();
	
	void unreliableFileOperation() throws IOException;
	
}
