package com.tutorial.context.expression;

import java.util.Map;

import com.tutorial.expression.AccessException;
import com.tutorial.expression.EvaluationContext;
import com.tutorial.expression.PropertyAccessor;
import com.tutorial.expression.TypedValue;

/**
 * @author Liufeng
 * Created on 2018年11月17日 下午4:50:31
 */
public class MapAccessor implements PropertyAccessor {

	@Override
	public Class<?>[] getSpecificTargetClasses() {
		return new Class[] {Map.class};
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canRead(EvaluationContext context, Object target, String name) throws AccessException {
		Map map = (Map) target;
		return map.containsKey(name);
	}

	@Override
	public TypedValue read(EvaluationContext context, Object target, String name) throws AccessException {
		Map map = (Map) target;
		Object value = map.get(name);
		if(value == null && !map.containsKey(name)) {
			throw new MapAccessException(name);
		}
		return new TypedValue(value);
	}

	@Override
	public boolean canWrite(EvaluationContext context, Object target, String name) throws AccessException {
		return true;
	}

	@Override
	public void write(EvaluationContext context, Object target, String name, Object newValue) throws AccessException {
		Map map = (Map) target;
		map.put(name, newValue);
	}
	
	private static final class MapAccessException extends AccessException {
		private static final long serialVersionUID = 1750281192903122988L;
		private final String key;
		public MapAccessException(String key) {
			super(null);
			this.key = key;
		}
		@Override
		public String getMessage() {
			return "Map does not contain a value for key '" + this.key + "'";
		}
	}

}
