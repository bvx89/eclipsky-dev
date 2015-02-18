package no.hal.eclipsky.services.workspace.http;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import no.hal.eclipsky.services.editor.RunResult;

@SuppressWarnings("serial")
public class SourceFileRunServlet extends AbstractWorkspaceServiceServlet {

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO
		
		/*
		ResourceRef resourceRef = getResourceRef(request);
		
		SourceFileMarker[] sourceFileMarkers = getWorkspaceService().getSourceFile(resourceRef);
		if (sourceFileMarkers == null) {
			super.doPost(request, response);
		} else {
			String responseFormat = getResponseFormat(request);
			response.setContentType("text/" + ("html".equals(responseFormat) ? "html" : "plain"));
			writeCompletionsResponse(responseFormat, response.getWriter(), sourceFileMarkers);
		}
		*/
	}


	public static void writeRunResponse(String protocol, PrintWriter output, RunResult result) {
		ResponseFormatter formatter = getResponseFormatter(protocol, output);
		if (formatter != null) {
			formatter.startEntities("run", false);
		} else {
			output.println("<html>\n"
					+ "\t<head><title>Run</title></head>\n"
					+ "\t<body>");
			output.println("\t\t<h1>Run</h1>\n\t\t<ul>");
		}

		if (formatter != null) {
			formatter.entity("run", 
					"console", result.getConsoleOutput(),
					"error", result.getErrorOutput(),
					"exLocation", result.getExceptionLocation()).endEntity();
		} else {
			// TODO: Write a proper result display
			output.println("\t\t\t<li>" + result + "</li>");
		}
		
		if (formatter == null) {
			output.println("\t\t</ul>");
			output.println("\t</body>\n"
					+ "</html>");
		}
	}
}
