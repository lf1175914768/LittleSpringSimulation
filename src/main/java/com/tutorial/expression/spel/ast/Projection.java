package com.tutorial.expression.spel.ast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.tutorial.expression.EvaluationException;
import com.tutorial.expression.TypedValue;
import com.tutorial.expression.spel.ExpressionState;
import com.tutorial.expression.spel.SpelEvaluationException;
import com.tutorial.expression.spel.SpelMessage;
import com.tutorial.util.ClassUtils;
import com.tutorial.util.ObjectUtils;

/**
 * @author Liufeng
 * Created on 2018年11月12日 下午9:01:59
 */
public class Projection extends SpelNodeImpl {
	
	private final boolean nullSafe;

	public Projection(boolean nullSafe, int pos, SpelNodeImpl expression) {
		super(pos, expression);
		this.nullSafe = nullSafe;
	}

	@Override
	public TypedValue getValueInternal(ExpressionState expressionState) throws EvaluationException {
		TypedValue op = expressionState.getActiveContextObject();
		Object operand = op.getValue();
		boolean operandIsArray = ObjectUtils.isArray(operand);
		if(operand instanceof Map) {
			Map<?, ?> mapData = (Map<?, ?>) operand;
			List<Object> result = new ArrayList<Object>();
			for(Map.Entry entry : mapData.entrySet()) {
				try {
					expressionState.pushActiveContextObject(new TypedValue(entry));
					result.add(this.children[0].getValueInternal(expressionState).getValue());
				} finally {
					expressionState.popActiveContextObject();
				}
			}
			return new TypedValue(result);
		} else if(operand instanceof Collection || operandIsArray) {
			Collection<?> data = (operand instanceof Collection ? (Collection<?>) operand :
				Arrays.asList(ObjectUtils.toObjectArray(operand)));
			List<Object> result = new ArrayList<Object>();
			int idx = 0;
			Class<?> arrayElementType = null;
			for(Object element : data) {
				try {
					expressionState.pushActiveContextObject(new TypedValue(element));
					expressionState.enterScope("index", idx);
					Object value = children[0].getValueInternal(expressionState).getValue();
					if(value != null && operandIsArray) {
						arrayElementType = determineCommonType(arrayElementType, value.getClass());
					}
					result.add(value);
				} finally {
					expressionState.exitScope();
					expressionState.popActiveContextObject();
				}
				idx++;
			}
			if(operandIsArray) {
				if(arrayElementType == null) {
					arrayElementType = Object.class;
				} 
				Object resultArray = Array.newInstance(arrayElementType, result.size());
				System.arraycopy(result.toArray(), 0, resultArray, 0, result.size());
				return new TypedValue(resultArray);
			}
			return new TypedValue(result);
		} else {
			if(operand == null) {
				if(this.nullSafe) {
					return TypedValue.NULL;
				} else {
					throw new SpelEvaluationException(getStartPosition(),
							SpelMessage.PROJECTION_NOT_SUPPORTED_ON_TYPE, "null");
				}
			} else {
				throw new SpelEvaluationException(getStartPosition(),
						SpelMessage.PROJECTION_NOT_SUPPORTED_ON_TYPE, operand.getClass().getName());
			}
		}
	}

	private Class<?> determineCommonType(Class<?> oldType, Class<?> newType) {
		if(oldType == null) 
			return newType;
		if(oldType.isAssignableFrom(newType)) {
			return oldType;
		}
		Class<?> nextType = newType;
		while(nextType != Object.class) {
			if(nextType.isAssignableFrom(oldType)) 
				return nextType;
			nextType = nextType.getSuperclass();
		}
		Class<?>[] interfaces = ClassUtils.getAllInterfacesForClass(newType);
		for(Class<?> nextInterface : interfaces) {
			if(nextInterface.isAssignableFrom(oldType)) 
				return nextInterface;
		}
		return Object.class;
	}

	@Override
	public String toStringAST() {
		StringBuilder sb = new StringBuilder();
		return sb.append("![").append(getChild(0).toStringAST()).append("]").toString();
	}

}
 