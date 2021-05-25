package fr.wonder.commons.streams.serialization;

import java.util.Scanner;
import java.util.regex.Matcher;

import fr.wonder.commons.utils.StringUtils;

/**
 * Utility class for text reading/parsing.<br>
 * Similar to {@link Scanner} but way more lightweight.
 */
public class SerializedText {
	
	private final String text;
	private final Matcher stringMatcher;
	private int pos;
	
	public SerializedText(String text) {
		this.text = StringUtils.stripContent(text);
		this.stringMatcher = StringUtils.STRING_PATTERN.matcher(this.text);
	}
	
	public void prev() {
		this.pos--;
	}
	
	public char currentChar() throws IndexOutOfBoundsException {
		return text.charAt(pos);
	}
	
	public char nextChar() throws IndexOutOfBoundsException {
		return text.charAt(pos++);
	}
	
	public String remaining() {
		return text.substring(pos);
	}
	
	public void skip(char c) throws IndexOutOfBoundsException, IllegalStateException {
		if(nextChar() != c)
			throw new IllegalStateException();
	}
	
	public void skip(int count) {
		pos += count;
	}
	
	public boolean nextIs(String next) {
		return text.startsWith(next, pos);
	}
	
	public boolean nextIsNull() {
		return nextIs("null");
	}

	public boolean nextIsNaN() {
		return nextIs("NaN");
	}
	
	public <T> T skipNull() throws IllegalStateException {
		if(!nextIsNull())
			throw new IllegalStateException();
		pos += 4;
		return null;
	}
	
	public String nextString() throws IllegalStateException {
		if(nextIsNull())
			return (String) skipNull();
		stringMatcher.find(pos);
		if(stringMatcher.start() != pos)
			throw new IllegalStateException();
		String s = stringMatcher.group();
		s = s.substring(1, s.length()-1);
		pos = stringMatcher.end();
		return s;
	}
	
	/** Beware that reading 'NaN' returns 0, to be able to convert to floats */
	public double nextDouble() throws IllegalStateException, NumberFormatException {
		String s = "";
		if(nextIsNaN()) {
			skip(3);
			return 0; // return Double.NaN
		}
		char c = nextChar();
		if(c != '-' && !Character.isDigit(c))
			prev();
		else
			s += c;
		while(Character.isDigit(c = nextChar()))
			s += c;
		if(c == '.') {
			s += c;
			while(Character.isDigit(c = nextChar()))
				s += c;
		}
		prev();
		return Double.parseDouble(s);
	}
	
	public long nextLong() throws IllegalStateException, NumberFormatException {
		String s = "";
		char c = nextChar();
		if(c != '-' && !Character.isDigit(c))
			prev();
		else
			s += c;
		while(Character.isDigit(c = nextChar()))
			s += c;
		prev();
		return Long.parseLong(s);
	}

	public boolean nextBool() throws IllegalStateException {
		if(nextIs("true")) {
			skip(4);
			return true;
		} else if(nextIs("false")) {
			skip(5);
			return false;
		}
		throw new IllegalStateException("Next is not a boolean");
	}
	
	@Override
	public String toString() {
		return text.substring(pos);
	}
	
}