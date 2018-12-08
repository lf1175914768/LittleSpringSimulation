package com.tutorial.expression.spel;

/**
 * Configuration object for the SpEL expression parser.
 *
 * @author Juergen Hoeller
 * @since 3.0
 * @see com.tutorial.expression.spel.standard.SpelExpressionParser#SpelExpressionParser(SpelParserConfiguration)
 */
public class SpelParserConfiguration {
	
	private final boolean autoGrowNullReferences;
	
	private final boolean autoGrowCollections;
	
	public SpelParserConfiguration(boolean autoGrowNullReferences, boolean autoGrowCollections) {
		this.autoGrowNullReferences = autoGrowNullReferences;
		this.autoGrowCollections = autoGrowCollections;
	}

	public boolean isAutoGrowNullReferences() {
		return this.autoGrowNullReferences;
	}

	public boolean isAutoGrowCollections() {
		return this.autoGrowCollections;
	}

}
