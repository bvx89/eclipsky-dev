package no.hal.eclipsky.services.workspace.http;

import java.io.PrintWriter;

public class JsonResponseFormatter extends ResponseFormatter {
	
	public JsonResponseFormatter(PrintWriter printWriter) {
		super(printWriter);
	}
	
	// attributes

	@Override
	protected ResponseFormatter startAttributes() {
		pushDelimiter(", ");
		return this;
	}

	@Override
	protected ResponseFormatter endAttributes() {
		pop(1);
		return this;
	}

	private void attribute(String name, String s, boolean quote) {
		printWriter.write("\"" + name + "\": ");
		if (s.equals("\"\"")) {
			quote = false;
		}
		if (quote) {
			printWriter.write("\"");
		}
		printWriter.write(s);
		if (quote) {
			printWriter.write("\"");
		}
	}
	
	@Override
	public ResponseFormatter attributeUnescaped(String name, CharSequence cs) {
		attribute(name, cs.toString(), true);
		return this;
	}
	
	protected boolean noQuotes(Object o) {
		return o instanceof Number || o instanceof Boolean;
	}

	public ResponseFormatter attribute(String name, Object o) {
		writeDelimiter();
		if (noQuotes(o)) {
			attribute(name, o.toString(), false);
		} else {
			super.attribute(name, o);
		}
		return this;
	}

	@Override
	public CharSequence escape(CharSequence cs) {
		return escapeJson(cs);
	}

	public static CharSequence escapeJson(CharSequence cs) {
		if (cs == null || cs.length() == 0) {
			return "\"\"";
		}

		char         c = 0;
		int          i;
		int          len = cs.length();
		StringBuilder sb = new StringBuilder(len + 4);
		String       t;

		for (i = 0; i < len; i += 1) {
		    c = cs.charAt(i);
		    switch (c) {
		    case '\\':
		    case '"':
				sb.append('\\');
				sb.append(c);
				break;
	    	case '/':
	    		// if (b == '<') {
	    		sb.append('\\');
    			// }
	    		sb.append(c);
	    		break;
	    	case '\b':
    			sb.append("\\b");
    			break;
	    	case '\t':
	    		sb.append("\\t");
	    		break;
	    	case '\n':
	    		sb.append("\\n");
	    		break;
	    	case '\f':
	    		sb.append("\\f");
	    		break;
	    	case '\r':
	    		sb.append("\\r");
	    		break;
	    	default:
	    		if (c < ' ') {
	    			t = "000" + Integer.toHexString(c);
	    			sb.append("\\u" + t.substring(t.length() - 4));
	    		} else {
	    			sb.append(c);
	    		}
		    }
		}
		return sb;
	}
	
	// entities
	
	@Override
	public ResponseFormatter startEntity(String name) {
		writeDelimiter(popDelimiter());
		pushDelimiter(", \n");
		printWriter.write("{ ");
		attributeUnescaped("type", name);
		return this;
	}

	@Override
	public ResponseFormatter endEntity() {
		printWriter.write(" }");
		return this;
	}

	@Override
	protected ResponseFormatter startEntities(String name, boolean multi) {
		if (! contextStack.isEmpty()) {
			printWriter.write("\"" + name + "\": ");
		}
		if (multi) {
			printWriter.write("[");
			pushDelimiter("]");
		} else {
			pushDelimiter(null);
		}
		indent(1);
		pushDelimiter("\n");
		return this;
	}

	@Override
	protected ResponseFormatter endEntities() {
		pop(1);
		newline(-1);
		writeDelimiter(popDelimiter());
		return this;
	}
}
