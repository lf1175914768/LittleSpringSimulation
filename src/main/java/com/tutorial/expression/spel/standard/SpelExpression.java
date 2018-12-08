package com.tutorial.expression.spel.standard;

import com.tutorial.core.convert.TypeDescriptor;
import com.tutorial.expression.EvaluationContext;
import com.tutorial.expression.EvaluationException;
import com.tutorial.expression.Expression;
import com.tutorial.expression.TypedValue;
import com.tutorial.expression.common.ExpressionUtils;
import com.tutorial.expression.spel.ExpressionState;
import com.tutorial.expression.spel.SpelNode;
import com.tutorial.expression.spel.SpelParserConfiguration;
import com.tutorial.expression.spel.ast.SpelNodeImpl;
import com.tutorial.expression.spel.support.StandardEvaluationContext;
import com.tutorial.util.Assert;

/**
 * A SpelExpressions represents a parsed (valid) expression that is ready to be evaluated in a specified context. An
 * expression can be evaluated standalone or in a specified context. During expression evaluation the context may be
 * asked to resolve references to types, beans, properties, methods.
 * 
 * @author Andy Clement
 * @since 3.0
 */
public class SpelExpression implements Expression {
	
	private final String expression;
	
	private final SpelNodeImpl ast;
	
	private final SpelParserConfiguration configuration;
	
	// the default context is used if no override is supplied by the user
	private EvaluationContext defaultContext;
	
	/**
	 * Construct an expression, only used by the parser.
	 */
	public SpelExpression(String expression, SpelNodeImpl ast, SpelParserConfiguration configuration) {
		this.expression = expression;
		this.ast = ast;
		this.configuration = configuration;
	}

	public Object getValue() throws EvaluationException {
		ExpressionState expressionState = new ExpressionState(getEvaluationContext(), configuration);
		return ast.getValue(expressionState);
	}

	public Object getValue(Object rootObject) throws EvaluationException {
		ExpressionState expressionState = new ExpressionState(getEvaluationContext(), 
				toTypedValue(rootObject), configuration);
		return ast.getValue(expressionState);
	}

	public <T> T getValue(Class<T> expectedResultType) throws EvaluationException {
		ExpressionState expressionState = new ExpressionState(getEvaluationContext(), configuration);
		TypedValue typedResultType = ast.getTypedValue(expressionState);
		return ExpressionUtils.convertTypedValue(expressionState.getEvaluationContext(),
				typedResultType, expectedResultType);
	}

	public <T> T getValue(Object rootObject, Class<T> expectedResultType) throws EvaluationException {
		ExpressionState expressionState = new ExpressionState(getEvaluationContext(), 
				toTypedValue(rootObject), configuration);
		TypedValue typedResultValue = ast.getTypedValue(expressionState);
		return ExpressionUtils.convertTypedValue(expressionState.getEvaluationContext(), 
				typedResultValue, expectedResultType);
	}

	public Object getValue(EvaluationContext context) throws EvaluationException {
		Assert.notNull(context, "The EvaluationContext is required");
		return ast.getValue(new ExpressionState(context, configuration));
	}

	public Object getValue(EvaluationContext context, Object rootObject) throws EvaluationException {
		Assert.notNull(context, "The EvaluationContext is required");
		return ast.getValue(new ExpressionState(context, toTypedValue(rootObject), configuration));
	}

	public <T> T getValue(EvaluationContext context, Class<T> expectedResultType) throws EvaluationException {
		TypedValue typedResultValue = ast.getTypedValue(new ExpressionState(context, configuration));
		return ExpressionUtils.convertTypedValue(context, typedResultValue, expectedResultType);
	}

	public <T> T getValue(EvaluationContext context, Object rootObject, Class<T> desiredResultType)
			throws EvaluationException {
		TypedValue typedResultValue = ast.getTypedValue(new ExpressionState(context, 
				toTypedValue(rootObject), configuration));
		return ExpressionUtils.convertTypedValue(context, typedResultValue, desiredResultType);
	}

	public Class<?> getValueType() throws EvaluationException {
		return getValueType(getEvaluationContext());
	}

	public Class<?> getValueType(Object rootObject) throws EvaluationException {
		return getValueType(getEvaluationContext(), rootObject);
	}

	public Class<?> getValueType(EvaluationContext context) throws EvaluationException {
		Assert.notNull(context, "The EvaluationContext is required");
		ExpressionState eState = new ExpressionState(context, configuration);
		TypeDescriptor typeDescriptor = ast.getValueInternal(eState).getTypeDescriptor();
		return typeDescriptor != null ? typeDescriptor.getType() : null;
	}

	public Class<?> getValueType(EvaluationContext context, Object rootObject) throws EvaluationException {
		ExpressionState eState = new ExpressionState(context, toTypedValue(rootObject), configuration);
		TypeDescriptor typeDescriptor = ast.getValueInternal(eState).getTypeDescriptor();
		return typeDescriptor != null ? typeDescriptor.getType() : null;
	}

	public TypeDescriptor getValueTypeDescriptor() throws EvaluationException {
		return getValueTypeDescriptor(getEvaluationContext());
	}

	public TypeDescriptor getValueTypeDescriptor(Object rootObject) throws EvaluationException {
		ExpressionState eState = new ExpressionState(getEvaluationContext(),
				toTypedValue(rootObject), configuration);
		return ast.getValueInternal(eState).getTypeDescriptor();
	}

	public TypeDescriptor getValueTypeDescriptor(EvaluationContext context) throws EvaluationException {
		Assert.notNull(context, "The EvaluationContext is required");
		ExpressionState expressionState = new ExpressionState(context, configuration);
		return ast.getValueInternal(expressionState).getTypeDescriptor();
	}

	public TypeDescriptor getValueTypeDescriptor(EvaluationContext context, Object rootObject)
			throws EvaluationException {
		Assert.notNull(context, "The EvaluationContext is required");
		ExpressionState eState = new ExpressionState(context,
				toTypedValue(rootObject), configuration);
		return ast.getValueInternal(eState).getTypeDescriptor();
	}

	public boolean isWritable(EvaluationContext context) throws EvaluationException {
		Assert.notNull(context, "The EvaluationContext is required");
		return ast.isWritable(new ExpressionState(context, configuration));
	}

	public boolean isWritable(EvaluationContext context, Object rootObject) throws EvaluationException {
		Assert.notNull(context, "The EvaluationContext is required");		
		return ast.isWritable(new ExpressionState(context, toTypedValue(rootObject), configuration));
	}

	public boolean isWritable(Object rootObject) throws EvaluationException {
		return ast.isWritable(new ExpressionState(getEvaluationContext(), 
				toTypedValue(rootObject), configuration));
	}

	public void setValue(EvaluationContext context, Object value) throws EvaluationException {
		Assert.notNull(context, "The EvaluationContext is required");
		ast.setValue(new ExpressionState(context, configuration), value);
	}

	public void setValue(Object rootObject, Object value) throws EvaluationException {
		ast.setValue(new ExpressionState(getEvaluationContext(),
				toTypedValue(rootObject), configuration), value);
	}

	public void setValue(EvaluationContext context, Object rootObject, Object value) throws EvaluationException {
		Assert.notNull(context, "The EvaluationContext is required");
		ast.setValue(new ExpressionState(context, toTypedValue(rootObject), configuration), value);
	}

	public String getExpressionString() {
		return this.expression;
	}
	
	/**
	 * @return return the Abstract Syntax Tree for the expression
	 */
	public SpelNode getAST() {
		return ast;
	}
	
	/**
	 * Produce a string representation of the Abstract Syntax Tree for the expression, this should ideally look like the
	 * input expression, but properly formatted since any unnecessary whitespace will have been discarded during the
	 * parse of the expression.
	 * @return the string representation of the AST
	 */
	public String toStringAST() {
		return ast.toStringAST();
	}

	/**
     * Return the default evaluation context that will be used if none is supplied on an evaluation call
     * @return the default evaluation context
     */
	public EvaluationContext getEvaluationContext() {
		if(defaultContext == null) {
			defaultContext = new StandardEvaluationContext();
		}
		return this.defaultContext;
	}
	
	/**
     * Set the evaluation context that will be used if none is specified on an evaluation call.
     * @param context an evaluation context
     */
	public void setEvaluationContext(EvaluationContext context) {
		this.defaultContext = context;
	}

	private TypedValue toTypedValue(Object object) {
		if(object == null) {
			return TypedValue.NULL;
		} else {
			return new TypedValue(object);
		}
	}

}
