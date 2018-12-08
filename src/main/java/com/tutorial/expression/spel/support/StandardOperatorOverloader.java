package com.tutorial.expression.spel.support;

import com.tutorial.expression.EvaluationException;
import com.tutorial.expression.Operation;
import com.tutorial.expression.OperatorOverloader;

public class StandardOperatorOverloader implements OperatorOverloader {

	public boolean overridesOperation(Operation operation, Object leftOperand, Object rightOperand)
			throws EvaluationException {
		return false;
	}

	public Object operate(Operation operation, Object leftOperand, Object rightOperand) throws EvaluationException {
		throw new EvaluationException("No operation overloaded by default");
	}

}
