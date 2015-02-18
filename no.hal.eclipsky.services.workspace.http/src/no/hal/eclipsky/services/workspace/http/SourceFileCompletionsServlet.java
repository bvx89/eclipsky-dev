package no.hal.eclipsky.services.workspace.http;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import no.hal.eclipsky.services.common.Proposal;

@SuppressWarnings("serial")
public class SourceFileCompletionsServlet extends AbstractWorkspaceServiceServlet {

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


	public static void writeCompletionsResponse(String protocol, PrintWriter output, Proposal[] completions) {
		ResponseFormatter formatter = getResponseFormatter(protocol, output);
		if (formatter != null) {
			formatter.startEntities("completions", true);
		} else {
			output.println("<html>\n"
					+ "\t<head><title>Completions</title></head>\n"
					+ "\t<body>");
			output.println("\t\t<h1>Completions</h1>\n\t\t<ul>");
		}
		int count = (completions != null ? completions.length : 0);
		for (int i = 0; i < count; i++) {
			Proposal completion = completions[i];
			if (formatter != null) {
				formatter.entity("completion", 
						"name", completion.getName(),
						"value", completion.getValue(),
						"score", completion.getScore()).endEntity();
			} else {
				output.println("\t\t\t<li>" + completion + "</li>");
			}
		}
		if (formatter != null) {
			formatter.endEntities();
		} else {
			output.println("\t\t</ul>");
			output.println("\t</body>\n"
					+ "</html>");
		}
	}
}
