package com.tutorial.expression.spel.standard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.tutorial.expression.spel.InternalParseException;
import com.tutorial.expression.spel.SpelMessage;
import com.tutorial.expression.spel.SpelParseException;
import com.tutorial.util.Assert;

/**
 * Lex some input data into a stream of tokens that can then be parsed.
 *  
 * @author Andy Clement
 * @since 3.0
 */
class Tokenizer {
	
	String expressionString;
	char[] toProcess;
	int max;
	int pos;
	List<Token> tokens = new ArrayList<Token>();
	
	public Tokenizer(String inputdata) {
		this.expressionString = inputdata;
		this.toProcess = (inputdata + '\0').toCharArray();
		this.max = toProcess.length;
		this.pos = 0;
		process();
	}
	
	public List<Token> getTokens() {
		return this.tokens;
	}

	public void process() {
		while(pos < max) {
			char ch = toProcess[pos];
			if(isAlphabetic(ch)) {
				lexIdentifier();
			} else {
				switch(ch) {
				case '+': 
					pushCharToken(TokenKind.PLUS);
					break;
				case '_':
					lexIdentifier();
					break;
				case '-':
					pushCharToken(TokenKind.MINUS);
					break;
				case ':':
					pushCharToken(TokenKind.COLON);
					break;
				case '.':
					pushCharToken(TokenKind.DOT);
					break;
				case ',':
					pushCharToken(TokenKind.COMMA);
					break;
				case '*':
					pushCharToken(TokenKind.STAR);
					break;
				case '/':
					pushCharToken(TokenKind.DIV);
					break;
				case '%':
					pushCharToken(TokenKind.MOD);
					break;
				case '(':
					pushCharToken(TokenKind.LPAREN);
					break;
				case ')':
					pushCharToken(TokenKind.RPAREN);
					break;
				case '[':
					pushCharToken(TokenKind.LSQUARE);
					break;
				case '#':
					pushCharToken(TokenKind.HASH);
					break;
				case ']':
					pushCharToken(TokenKind.RSQUARE);
					break;
				case '{':
					pushCharToken(TokenKind.LCURLY);
					break;
				case '}':
					pushCharToken(TokenKind.RCURLY);
					break;
				case '@':
					pushCharToken(TokenKind.BEAN_REF);
					break;
				case '^':
					if (isTwoCharToken(TokenKind.SELECT_FIRST)) {
						pushPairToken(TokenKind.SELECT_FIRST);
					} else {
						pushCharToken(TokenKind.POWER);					
					}
					break;
				case '!':
					if (isTwoCharToken(TokenKind.NE)) {
						pushPairToken(TokenKind.NE);
					} else if (isTwoCharToken(TokenKind.PROJECT)) {
						pushPairToken(TokenKind.PROJECT);
					} else {
						pushCharToken(TokenKind.NOT);
					}
					break;
				case '=':
					if (isTwoCharToken(TokenKind.EQ)) {
						pushPairToken(TokenKind.EQ);
					} else {
						pushCharToken(TokenKind.ASSIGN);
					}
					break;
				case '?':
					if (isTwoCharToken(TokenKind.SELECT)) {
						pushPairToken(TokenKind.SELECT);
					} else if (isTwoCharToken(TokenKind.ELVIS)) {
						pushPairToken(TokenKind.ELVIS);
					} else if (isTwoCharToken(TokenKind.SAFE_NAVI)) {
						pushPairToken(TokenKind.SAFE_NAVI);
					} else {
						pushCharToken(TokenKind.QMARK);					
					}
					break;
				case '$':
					if (isTwoCharToken(TokenKind.SELECT_LAST)) {
						pushPairToken(TokenKind.SELECT_LAST);
					} else {
						lexIdentifier();
					}
					break;
				case '>':
					if (isTwoCharToken(TokenKind.GE)) {
						pushPairToken(TokenKind.GE);
					} else {
						pushCharToken(TokenKind.GT);
					}
					break;
				case '<':
					if (isTwoCharToken(TokenKind.LE)) {
						pushPairToken(TokenKind.LE);
					} else {
						pushCharToken(TokenKind.LT);
					}
					break;
				case '0':
				case '1':
				case '2':
				case '3':
				case '4':
				case '5':
				case '6':
				case '7':
				case '8':
				case '9':
					lexNumericLiteral(ch=='0');
					break;
				case ' ':
				case '\t':
				case '\r':
				case '\n':
					// drift over white space
					pos++;
					break;
				case '\'':
					lexQuotedStringLiteral();
					break;
				case '"':
					lexDoubleQuotedStringLiteral();
					break;
				case 0:
					// hit sentinel at end of value
					pos++; // will take us to the end
					break;
				default:
					throw new IllegalStateException("Cannot handle ("+Integer.valueOf(ch)+") '"+ch+"'");
				}
			}
		}
	}
	
	private void lexQuotedStringLiteral() {
		int start = pos;
		boolean terminated = false;
		while(!terminated) {
			pos++;
			char ch = toProcess[pos];
			if(ch == '\'') {
				// may not be the end if the char after is also a '
				if(toProcess[pos + 1] == '\'') {
					pos++;  // skip over that too, and continue
				} else {
					terminated = true;
				}
			}
			if(ch == 0) {
				throw new InternalParseException(new SpelParseException(
						expressionString,start,SpelMessage.NON_TERMINATING_QUOTED_STRING));
			}
		}
		pos++;
		tokens.add(new Token(TokenKind.LITERAL_STRING, subarray(start, pos), start, pos));
	}

	private void lexDoubleQuotedStringLiteral() {
		int start = pos;
		boolean terminated = false;
		while(!terminated) {
			pos++;
			char ch = toProcess[pos];
			if(ch == '"') {
				terminated = true;
			} 
			if(ch == 0) {
				throw new InternalParseException(new SpelParseException(
						expressionString,start,SpelMessage.NON_TERMINATING_DOUBLE_QUOTED_STRING));
			}
		}
		pos++;
		tokens.add(new Token(TokenKind.LITERAL_STRING, subarray(start, pos), start, pos));
	}

	private void lexNumericLiteral(boolean firstCharIsZero) {
		boolean isReal = false;
		int start = pos;
		char ch = toProcess[pos + 1];
		boolean isHex = ch == 'x' || ch == 'X';
		
		// deal with hexadecimal
		if(firstCharIsZero && isHex) {
			pos = pos + 1;
			do {
				pos ++;
			} while(isHexadecimalDigit(toProcess[pos]));
			if(isChar('L', 'l')) {
				pushHexIntToken(subarray(start + 2, pos), true, start, pos);
				pos++;
			} else {
				pushHexIntToken(subarray(start + 2, pos), false, start, pos);
			}
			return;
		}
		
		// real numbers must have leading digits.
		// Consume first part of number
		do {
			pos++;
		} while(isDigit(toProcess[pos]));
		
		// a '.' indicates this number is a real
		ch = toProcess[pos];
		if(ch == '.') {
			isReal = true;
			// carry on consuming digits.
			do {
				pos++;
			} while(isDigit(toProcess[pos]));
		}
		
		int endOfNumber = pos;
		// Now there may or may not be an exponent
		
		// is it a long ?
		if (isChar('L','l')) {
			if (isReal) { // 3.4L - not allowed
				throw new InternalParseException(new SpelParseException(expressionString,start,SpelMessage.REAL_CANNOT_BE_LONG));
			}
			pushIntToken(subarray(start, endOfNumber), true, start, endOfNumber);
			pos++;
		} else if (isExponentChar(toProcess[pos])) {
			isReal = true; // if it wasnt before, it is now
			pos++;
			char possibleSign = toProcess[pos];
			if (isSign(possibleSign)) {
				pos++;
			}
			
			// exponent digits
			do {
				pos++;
			} while (isDigit(toProcess[pos]));
			boolean isFloat = false;
			if (isFloatSuffix(toProcess[pos])) {
				isFloat = true;
				endOfNumber = ++pos;
			} else if (isDoubleSuffix(toProcess[pos])) {				
				endOfNumber = ++pos;
			}
			pushRealToken(subarray(start,pos), isFloat, start, pos);
		} else {
			ch = toProcess[pos];
			boolean isFloat = false;
			if (isFloatSuffix(ch)) {
				isReal = true;
				isFloat = true;
				endOfNumber = ++pos;
			} else if (isDoubleSuffix(ch)) {
				isReal = true;
				endOfNumber = ++pos;				
			}
			if (isReal) {
				pushRealToken(subarray(start,endOfNumber), isFloat, start, endOfNumber);
			} else {
				pushIntToken(subarray(start,endOfNumber), false, start, endOfNumber);
			}
		}
	}

	private void pushRealToken(char[] data, boolean isFloat, int start, int end) {
		if(isFloat) {
			tokens.add(new Token(TokenKind.LITERAL_REAL_FLOAT, data, start, end));
		} else {
			tokens.add(new Token(TokenKind.LITERAL_REAL, data, start, end));
		}
	}

	private boolean isExponentChar(char c) {
		return c == 'e' || c == 'E';
	}

	private boolean isFloatSuffix(char c) {
		return c == 'f' || c == 'F';
	}

	private boolean isDoubleSuffix(char ch) {
		return ch == 'd' || ch == 'D';
	}

	private boolean isSign(char ch) {
		return ch == '+' || ch == '-';
	}

	private void pushIntToken(char[] data, boolean isLong, int start, int end) {
		if (isLong) {
			tokens.add(new Token(TokenKind.LITERAL_LONG, data, start, end));
		} else {
			tokens.add(new Token(TokenKind.LITERAL_INT, data, start, end));
		} 
	}

	private void pushHexIntToken(char[] data, boolean isLong, int start, int end) {
		if(data.length == 0) {
			if(isLong) {
				throw new InternalParseException(new SpelParseException(
						expressionString,start,SpelMessage.NOT_A_LONG,expressionString.substring(start,end+1)));
			} else {
				throw new InternalParseException(new SpelParseException(
						expressionString,start,SpelMessage.NOT_AN_INTEGER,expressionString.substring(start,end)));
			}
		}
		if(isLong) {
			tokens.add(new Token(TokenKind.LITERAL_HEXLONG, data, start, end));
		} else {
			tokens.add(new Token(TokenKind.LITERAL_HEXINT, data, start, end));
		}
	}

	private static final String[] alternativeOperatorNames = {"DIV", "EQ", "GE", "GT", "LE", "LT", "MOD", "NE", "NOT"};
	

	private void lexIdentifier() {
		int start = pos;
		do {
			pos++;
		} while(isIdentifier(toProcess[pos]));
		char[] subarray = subarray(start, pos);
		
		// Check if this is the alternative (textual) representation of an operator 
		if((pos - start) == 2 || (pos - start) == 3) {
			String asString = new String(subarray).toUpperCase();
			int idx = Arrays.binarySearch(alternativeOperatorNames, asString);
			if(idx >= 0) {
				pushOneCharOrTwoCharToken(TokenKind.valueOf(asString), start, subarray);
				return;
			}
		}
		tokens.add(new Token(TokenKind.IDENTIFIER, subarray, start, pos));
	}

	private char[] subarray(int start, int end) {
		char[] result = new char[end - start];
		System.arraycopy(toProcess, start, result, 0, end - start);
		return result;
	}
	
	private boolean isTwoCharToken(TokenKind kind) {
		Assert.isTrue(kind.tokenChars.length == 2);
		Assert.isTrue(toProcess[pos] == kind.tokenChars[0]);
		return toProcess[pos + 1] == kind.tokenChars[1];
	}

	private void pushOneCharOrTwoCharToken(TokenKind kind, int pos, char[] data) {
		tokens.add(new Token(kind, data, pos, pos + kind.getLength()));
	}

	/**
	 * Push a token of just one character in length.
	 */
	private void pushCharToken(TokenKind kind) {
		tokens.add(new Token(kind, pos, pos + 1));
		pos++;
	}
	
	private void pushPairToken(TokenKind kind) {
		tokens.add(new Token(kind, pos, pos + 2));
		pos += 2;
	}

	private boolean isIdentifier(char ch) {
		return isAlphabetic(ch) || isDigit(ch) || ch == '_' || ch == '$';
	}

	private boolean isChar(char a, char b) {
		char ch = toProcess[pos];
		return ch == a || ch == b;
	}

	private boolean isDigit(char ch) {
		if(ch > 255) {
			return false;
		}
		return (flags[ch] & IS_DIGIT) != 0;
	}

	private boolean isAlphabetic(char ch) {
		if(ch > 255) {
			return false;
		}
		return (flags[ch] & IS_ALPHA) != 0;
	}	

	private boolean isHexadecimalDigit(char c) {
		if(c > 255) {
			return false;
		}
		return (flags[c] & IS_HEXDIGIT) != 0;
	}

	private static final byte flags[] = new byte[256];
	private static final byte IS_DIGIT = 0x01;
	private static final byte IS_HEXDIGIT = 0x02;
	private static final byte IS_ALPHA = 0x04;
	
	static {
		for(int ch = '0'; ch <= '9'; ch++) {
			flags[ch] |= IS_DIGIT | IS_HEXDIGIT;
		} 
		for(int ch = 'A'; ch <= 'F'; ch++) {
			flags[ch] |= IS_HEXDIGIT;
		}
		for(int ch = 'a'; ch <= 'f'; ch++) {
			flags[ch] |= IS_HEXDIGIT;
		}
		for(int ch = 'A'; ch <= 'Z'; ch++) {
			flags[ch] |= IS_ALPHA;
		}
		for(int ch = 'a'; ch <= 'z'; ch++) {
			flags[ch] |= IS_ALPHA;
		}
	}

}
