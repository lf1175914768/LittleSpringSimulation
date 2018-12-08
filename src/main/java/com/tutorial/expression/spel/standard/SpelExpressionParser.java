package com.tutorial.expression.spel.standard;

import com.tutorial.expression.Expression;
import com.tutorial.expression.ParseException;
import com.tutorial.expression.ParserContext;
import com.tutorial.expression.common.TemplateAwareExpressionParser;
import com.tutorial.expression.spel.SpelParserConfiguration;
import com.tutorial.util.Assert;

/**
 * SpEL parser. Instances are reusable and thread-safe.
 * 
 * @author Andy Clement
 * @author Juergen Hoeller
 * @since 3.0
 */
public class SpelExpressionParser extends TemplateAwareExpressionParser {
	
	private final SpelParserConfiguration configuration;
	
	/**
	 * Create a parser with standard configuration.
	 */
	public SpelExpressionParser() {
		this.configuration = new SpelParserConfiguration(false, false);
	}

	/**
	 * Create a parser with some configured behavior.
	 * @param configuration custom configuration options
	 */
	public SpelExpressionParser(SpelParserConfiguration configuration) {
		Assert.notNull(configuration, "SpelParserConfiguration must not be null");
		this.configuration = configuration;
	}

	@Override
	protected SpelExpression doParseExpression(String expressionString, ParserContext context) throws ParseException {
		return new InternalSpelExpressionParser(this.configuration).doParseExpression(expressionString, context);
	}
	
	public SpelExpression parseRaw(String expressionString) throws ParseException {
		return doParseExpression(expressionString, null);
	}

}
