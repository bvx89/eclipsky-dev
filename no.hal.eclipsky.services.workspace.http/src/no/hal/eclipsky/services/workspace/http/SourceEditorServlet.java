package no.hal.eclipsky.services.workspace.http;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import no.hal.eclipsky.services.common.Proposal;
import no.hal.eclipsky.services.common.SourceFileMarker;
import no.hal.eclipsky.services.editor.EditorService;
import no.hal.eclipsky.services.editor.RunResult;
import no.hal.eclipsky.services.editor.SourceEditor;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketServlet;
import org.osgi.service.http.HttpService;

@SuppressWarnings("serial")
public class SourceEditorServlet extends WebSocketServlet {

	private HttpService httpService;
	
	public synchronized void setHttpService(HttpService httpService) {
		this.httpService = httpService;
	}

	private EditorService editorService;

	public synchronized void setEditorService(EditorService editorService) {
		this.editorService = editorService;
	}

	protected void activate() {
		try {
			httpService.registerServlet("/sourceEditor", (HttpServlet) this, null, null);
			httpService.registerResources("/ace", "/web/ace-builds/src-noconflict", null);
			httpService.registerResources("/js", "/web/js", null);
		} catch (Exception e) {
		}
	}

	protected void deactivate() {
		try {
			httpService.unregister("/sourceEditor");
			httpService.unregister("/ace");
			httpService.unregister("/js");
		} catch (Exception e) {
		}
	}

	protected String getSourceFileContent(HttpServletRequest request) {
		String projectName = request.getParameter("projectName");
		String packageName = request.getParameter("packageName");
		String resourceName = request.getParameter("resourceName");
		return editorService.getSourceFile(projectName, packageName, resourceName);
	}

	private AceEditorHelper aceEditorHelper = new AceEditorHelper();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String resourceName = request.getParameter("resourceName");
		boolean embed = Boolean.valueOf(request.getParameter("embed"));
		String stringContent = getSourceFileContent(request);
		if (stringContent == null) {
			super.doGet(request, response);
		} else {
			response.setContentType("text/html");
			PrintWriter writer = response.getWriter();
			aceEditorHelper.writeEditorHtml(writer, resourceName, stringContent, null, embed);
		}
	}

	private Collection<SourceEditor> editors = new ArrayList<SourceEditor>();

	protected SourceEditor getSourceEditor(String projectName, String packageName, String resourceName) {
		for (SourceEditor editor : editors) {
			if (editor.edits(projectName, packageName, resourceName)) {
				return editor;
			}
		}
		SourceEditor editor = editorService.openEditor(projectName, packageName, resourceName);
		editors.add(editor);
		return editor;
	}

	protected SourceEditor getSourceEditor(HttpServletRequest request) {
		String projectName = request.getParameter("projectName");
		String packageName = request.getParameter("packageName");
		String resourceName = request.getParameter("resourceName");

		return getSourceEditor(projectName, packageName, resourceName);
	}

	private PrintWriter debugWriter = null; // new PrintWriter(System.out);
	
	protected void updateSourceFile(HttpServletRequest request, HttpServletResponse response, Boolean exists, Boolean markers) throws ServletException, IOException {
		StringBuilder body = new StringBuilder(request.getContentLength());
		BufferedReader reader = request.getReader();
		String line = null;
		while ((line = reader.readLine()) != null) {
			body.append(line);
			body.append("\n");
		}
		String stringContent = body.toString();
		if (stringContent == null) {
			super.doPost(request, response);
		} else {
			SourceEditor editor = getSourceEditor(request);
			if (editor == null) {
				super.doPost(request, response);
			} else {
				String responseFormat = AbstractWorkspaceServiceServlet.getResponseFormat(request);
				response.setContentType("text/" + ("html".equals(responseFormat) ? "html" : "plain"));
				SourceFileMarker[] sourceFileMarkers = editor.update(stringContent, markers, false);
				if (debugWriter != null) {
					SourceFileMarkersServlet.writeMarkersResponse(responseFormat, debugWriter, sourceFileMarkers);
					debugWriter.flush();
				}
				SourceFileMarkersServlet.writeMarkersResponse(responseFormat, response.getWriter(), sourceFileMarkers);
			}
		}
	}
	
	private String runResponse(RunResult result, String protocol) {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		PrintWriter output = new PrintWriter(buffer);
		SourceFileRunServlet.writeRunResponse(protocol, output, result);
		output.close();
		return buffer.toString();
	}

	private String markersResponse(SourceFileMarker[] sourceFileMarkers, String protocol) {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		PrintWriter output = new PrintWriter(buffer);
		SourceFileMarkersServlet.writeMarkersResponse(protocol, output, sourceFileMarkers);
		output.close();
		return buffer.toString();
	}
	
	private String completionsResponse(Proposal [] completions, String protocol) {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		PrintWriter output = new PrintWriter(buffer);
		SourceFileCompletionsServlet.writeCompletionsResponse(protocol, output, completions);
		output.close();
		return buffer.toString();
	}

	protected Boolean markersDefault = true;

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		updateSourceFile(request, response, true, markersDefault);
	}

	@Override
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		updateSourceFile(request, response, false, markersDefault);
	}

	@Override
	public WebSocket doWebSocketConnect(HttpServletRequest request, final String protocol) {

		final SourceEditor editor = getSourceEditor(request);

		return new WebSocket.OnTextMessage() {

			private Connection connection;

			@Override
			public void onClose(int closeCode, String message) {
				// TODO: Notify GIT exporter
				editor.close(null);
			}

			@Override
			public void onMessage(String message) {
				int pos = message.indexOf('\n');
				String command = message, contents = null;
				if (pos >= 0) {
					command = command.substring(0, pos);
					contents = message.substring(pos + 1);
				}
				
				switch(command) {
				case "update":
					doUpdate(contents);
					break;
				case "codeCompletion":
					sendComplete(contents);
					break;
				case "run":
					runCode(contents);
					break;
				}
			}
			
			private void runCode(String contents) {				
				editor.close(null);
				// second boolean, result, doesn't do anything
				RunResult result = editor.run(contents, true);
				if (debugWriter != null) {
					SourceFileRunServlet.writeRunResponse(protocol, debugWriter, result);
					debugWriter.flush();
				}
				try {
					connection.sendMessage(runResponse(result, protocol));
				} catch (IOException e) {
				}
			}

			private void doUpdate(String stringContent) {
				SourceFileMarker[] sourceFileMarkers = editor.update(stringContent, markersDefault, false);
				if (debugWriter != null) {
					SourceFileMarkersServlet.writeMarkersResponse(protocol, debugWriter, sourceFileMarkers);
					debugWriter.flush();
				}
				try {
					connection.sendMessage(markersResponse(sourceFileMarkers, protocol));
				} catch (IOException e) {
				}
			}
			
			private void sendComplete(String contents) {
				int position = Integer.parseInt(contents);
				Proposal[] completions = editor.complete(position);
				if (completions.length == 0) {
					return;
				}
				if (debugWriter != null) {
					SourceFileCompletionsServlet.writeCompletionsResponse(protocol, debugWriter, completions);
				}
				
				try {
					connection.sendMessage(completionsResponse(completions, protocol));
				} catch (IOException e) {
				}
			}
			
			@Override
			public void onOpen(Connection connection) {
				this.connection = connection;
				try {
					connection.sendMessage(markersResponse(null, protocol));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
	}
}
