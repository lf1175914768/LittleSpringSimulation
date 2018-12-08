package com.tutorial.expression.spel;

import static org.junit.Assert.fail;

import org.junit.Test;

import com.tutorial.expression.spel.standard.SpelExpressionParser;

public class ParsingTests {
	
	private SpelExpressionParser parser = new SpelExpressionParser();
	
	@Test
	public void testLiteralBoolean01() {
		parseCheck("false");
	}

	private void parseCheck(String expression) {
		parseCheck(expression, expression);
	}
	
	public void parseCheck(String expression, String expectedStringFormatOfAST) {
		
	}

}
