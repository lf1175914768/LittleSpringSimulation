package com.tutorial.expression.spel.ast;

/**
 * @author Liufeng
 * Created on 2018年11月11日 下午10:41:09
 */
public enum TypeCode {
	
	OBJECT(Object.class), BOOLEAN(Boolean.TYPE),
	BYTE(Byte.TYPE), CHAR(Character.TYPE), 
	SHORT(Short.TYPE), INT(Integer.TYPE), 
	LONG(Long.TYPE), FLOAT(Float.TYPE), DOUBLE(Double.TYPE);
	
	private Class<?> type;
	
	TypeCode(Class<?> type) {
		this.type = type;
	}
	
	public Class<?> getType() {
		return type;
	} 
	
	public static TypeCode forName(String name) {
		String searchingFor = name.toUpperCase();
		TypeCode[] tcs = values();
		for(int i = 1; i < tcs.length; i++) {
			if(tcs[i].name().equals(searchingFor)) {
				return tcs[i];
			}
		}
		return TypeCode.OBJECT;
	}
	
	public static TypeCode forClass(Class<?> c) {
		TypeCode[] allValues = values();
		for(int i = 1; i < allValues.length; i++) {
			TypeCode typeCode = allValues[i];
			if(c == typeCode.getType()) {
				return typeCode;
			}
		}
		return TypeCode.OBJECT;
	}

}
