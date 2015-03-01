package no.hal.eclipsky.services.workspace.http;

import java.io.IOException;
import java.io.PrintWriter;

public class AceEditorHelper {

	/*
	<html lang="en">
	<head>
	<title>ACE in Action</title>
	<style type="text/css" media="screen">
	    #editor { 
	        position: absolute;
	        top: 0;
	        right: 0;
	        bottom: 0;
	        left: 0;
	    }
	</style>
	</head>
	<body>

	<div id="editor">function foo(items) {
	    var x = "All this is syntax highlighted";
	    return x;
	}</div>
	    
	<script src="/ace-builds/src-noconflict/ace.js" type="text/javascript" charset="utf-8"></script>
	<script>
	    var editor = ace.edit("editor");
	    editor.setTheme("ace/theme/monokai");
	    editor.getSession().setMode("ace/mode/javascript");
	</script>
	</body>
	</html>
	*/

	public void writeEditorHtml(PrintWriter writer, String editorName, String stringContent, String mode, boolean embed) throws IOException {
		String editorId = editorName.replace(".", "_");
		if (mode == null) {
			mode = getEditorMode(editorName);
		}
		if (! embed) {
			writer.print("<html lang=\"en\">\n" +
					"\t<head><title>" + editorName + "</title>\n" +
					"\t\t<style type=\"text/css\" media=\"screen\">#" + editorId + " { position: absolute; top: 0; right: 0; bottom: 0; left: 0;}</style>\n" +
					"\t</head>\n" +
					"\t<body>\n");
		}
		writer.print("\t\t<div id=\"" + editorId + "\">");
		writer.print(XmlResponseFormatter.escapeHtml(stringContent));
		writer.println("</div>\n" +
				"\t\t\t<script src=\"/ace/ace.js\" type=\"text/javascript\" charset=\"utf-8\"></script>\n" +
				"\t\t\t<script src=\"/ace/ext-language_tools.js\" type=\"text/javascript\" charset=\"utf-8\"></script>\n" +
				"\t\t\t<script src=\"/js/connector.js\" type=\"text/javascript\" charset=\"utf-8\"></script>\n" +
				"\t\t\t<script src=\"/js/editor.js\" type=\"text/javascript\" charset=\"utf-8\"></script>\n");
		writer.println("\t\t\t<script>\n" +
				"\t\t\tvar editors = [{id: window.location}];\n" +
				"\t\t\teditor.init('" + editorId + "', 'java', editors);\n" +
				"\t\t\t</script>");
		if (! embed) {
			writer.println("\t</body>\n</html>\n");
		}
		
	}

	public String getEditorMode(String resourceName) {
		int pos = resourceName.lastIndexOf('.');
		String ext = (pos < 0 ? resourceName : resourceName.substring(pos + 1));
		switch (ext) {
		case "py": return "python";
		case "js": return "javascript";
		default: return ext;
		}
	}
}
