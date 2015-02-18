package no.hal.eclipsky.services.workspace.http;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import no.hal.eclipsky.services.common.ResourceRef;
import no.hal.eclipsky.services.emfs.EmfsService;
import no.hal.emfs.EmfsPackage;
import no.hal.emfs.EmfsResource;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;

@SuppressWarnings("serial")
public class EnsureProjectServlet extends ProjectListServlet {
	
	private EmfsService emfsService;
	
	public synchronized void setEmfsService(EmfsService emfsService) {
		this.emfsService = emfsService;
	}
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		ResourceRef resourceRef = getResourceRef(request);
		String responseFormat = getResponseFormat(request);
		getWorkspaceService().ensureProject(resourceRef.getProjectName(), null);
		writeProjectListResponse(responseFormat, response.getWriter(), resourceRef.getProjectName());
	}

	protected EmfsResource getEmfsResource(String body) {
		ResourceSet resourceSet = new ResourceSetImpl();
		String ext = "emfs";
		if (! body.startsWith("<?xml")) {
			ext += "x";
		}
		Resource resource = resourceSet.createResource(URI.createURI("temp." + ext));
		byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
		try (InputStream stream = new ByteArrayInputStream(bytes);){
			resource.load(stream, null);
			Object emfsResource = EcoreUtil.getObjectByType(resource.getContents(), EmfsPackage.eINSTANCE.getEmfsContainer());
			return (EmfsResource) emfsResource;
		} catch (IOException e) {
		}
		return null;
	}

	@Override
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		ResourceRef resourceRef = getResourceRef(request);
		boolean overwrite = Boolean.valueOf(request.getParameter("overwrite"));
		String bodyContent = getRequestBodyContent(request);
		EmfsResource emfsResource = getEmfsResource(bodyContent);
		emfsService.importResources(Arrays.asList(emfsResource), resourceRef.getProjectName(), overwrite, null);
	}
}
