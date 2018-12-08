package com.tutorial.context.expression;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.tutorial.beans.BeansException;
import com.tutorial.beans.factory.BeanExpressionException;
import com.tutorial.beans.factory.config.BeanExpressionContext;
import com.tutorial.beans.factory.config.BeanExpressionResolver;
import com.tutorial.core.convert.ConversionService;
import com.tutorial.expression.Expression;
import com.tutorial.expression.ExpressionParser;
import com.tutorial.expression.ParserContext;
import com.tutorial.expression.spel.standard.SpelExpressionParser;
import com.tutorial.expression.spel.support.StandardEvaluationContext;
import com.tutorial.expression.spel.support.StandardTypeConverter;
import com.tutorial.expression.spel.support.StandardTypeLocator;
import com.tutorial.util.Assert;
import com.tutorial.util.StringUtils;

/**
 * Standard implementation of the
 * {@link com.tutorial.beans.factory.config.BeanExpressionResolver}
 * interface, parsing and evaluating Spring EL using Spring's expression module.
 * 
 * @author Juergen Hoeller
 * @since 3.0
 * @see com.tutorial.expression.ExpressionParser
 * @see com.tutorial.expression.spel.standard.SpelExpressionParser
 * @see com.tutorial.expression.spel.support.StandardEvaluationContext
 */
public class StandardBeanExpressionResolver implements BeanExpressionResolver {
	
	/** Default expression prefix: "#{" */
	public static final String DEFAULT_EXPRESSION_PREFIX = "#{";
	
	/** Default expression suffix: "}" */
	public static final String DEFAULT_EXPRESSION_SUFFIX = "}";
	
	private String expressionPrefix = DEFAULT_EXPRESSION_PREFIX;
	
	private String expressionSuffix = DEFAULT_EXPRESSION_SUFFIX;
	
	private ExpressionParser expressionParser = new SpelExpressionParser();
	
	private final Map<String, Expression> expressionCache = new ConcurrentHashMap<String, Expression>();
	
	private final Map<BeanExpressionContext, StandardEvaluationContext> evaluationCache = 
			new ConcurrentHashMap<BeanExpressionContext, StandardEvaluationContext>();
	
	private final ParserContext beanExpressionParserContext = new ParserContext() {
		@Override
		public boolean isTemplate() {
			return true;
		}
		@Override
		public String getExpressionPrefix() {
			return expressionPrefix;
		}
		@Override
		public String getExpressionSuffix() {
			return expressionSuffix;
		}
	};
	
	/**
	 * Set the prefix that an expression string starts with.
	 * The default is "#{".
	 * @see #DEFAULT_EXPRESSION_PREFIX
	 */
	public void setExpressionPrefix(String expressionPrefix) {
		Assert.hasText(expressionPrefix, "Expression prefix must not be empty");
		this.expressionPrefix = expressionPrefix;
	}

	/**
	 * Set the suffix that an expression string ends with.
	 * The default is "}".
	 * @see #DEFAULT_EXPRESSION_SUFFIX
	 */
	public void setExpressionSuffix(String expressionSuffix) {
		Assert.hasText(expressionSuffix, "Expression suffix must not be empty");
		this.expressionSuffix = expressionSuffix;
	}
	
	public Object evaluate(String value, BeanExpressionContext evalContext) throws BeansException {
		if(!StringUtils.hasLength(value)) {
			return value;
		}
		try {
			Expression expr = this.expressionCache.get(value);
			if(expr == null) {
				expr = this.expressionParser.parseExpression(value, this.beanExpressionParserContext);
				this.expressionCache.put(value, expr);
			} 
			StandardEvaluationContext sec = this.evaluationCache.get(evalContext);
			if(sec == null) {
				sec = new StandardEvaluationContext();
				sec.setRootObject(evalContext);
				sec.addPropertyAccessor(new BeanExpressionContextAccessor());
				sec.addPropertyAccessor(new BeanFactoryAccessor());
				sec.addPropertyAccessor(new MapAccessor());
				sec.addPropertyAccessor(new EnvironmentAccessor());
				sec.setBeanResolver(new BeanFactoryResolver(evalContext.getBeanFactory()));
				sec.setTypeLocator(new StandardTypeLocator(evalContext.getBeanFactory().getBeanClassLoader()));
				ConversionService conversionService = evalContext.getBeanFactory().getConversionService();
				if(conversionService != null) {
					sec.setTypeConverter(new StandardTypeConverter(conversionService));
				}
				customizeEvaluationContext(sec);
				this.evaluationCache.put(evalContext, sec);
			}
			return expr.getValue(sec);
		} catch (Exception e) {
			throw new BeanExpressionException("Expression parsing failed", e);
		}
	}

	/**
	 * Template method for customizing the expression evaluation context.
	 * <p>The default implementation is empty.
	 */
	protected void customizeEvaluationContext(StandardEvaluationContext sec) {
	}

}
