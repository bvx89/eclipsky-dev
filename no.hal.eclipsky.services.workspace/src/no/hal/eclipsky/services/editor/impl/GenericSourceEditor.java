package no.hal.eclipsky.services.editor.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import no.hal.eclipsky.services.common.Proposal;
import no.hal.eclipsky.services.common.ResourceHelper;
import no.hal.eclipsky.services.common.SourceFileMarker;
import no.hal.eclipsky.services.editor.RunResult;
import no.hal.eclipsky.services.editor.SourceEditor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;

public class GenericSourceEditor implements SourceEditor {

	protected final String projectName, packageName, resourceName;

	protected ResourceHelper resourceHelper;
	
	public GenericSourceEditor(String projectName, String packageName, String resourceName) {
		this.projectName = projectName;
		this.packageName = packageName;
		this.resourceName = resourceName;
		resourceHelper = new ResourceHelper(projectName, packageName, resourceName, null, getSourceFolderNames());
	}

	@Override
	public boolean edits(String projectName, String packageName, String resourceName) {
		return this.projectName.equals(projectName) && this.packageName.equals(packageName) && this.resourceName.equals(resourceName);
	}

	private static String[] SOURCE_FOLDER_NAMES = {"src"};
	
	protected String[] getSourceFolderNames() {
		return SOURCE_FOLDER_NAMES;
	}
	
	protected IFile getFile(boolean exists) {
		return resourceHelper.getFile(exists);
	}
	
	@Override
	public SourceFileMarker[] update(String stringContent, boolean markers, boolean commit) {
		if (getFile(true) != null) {
			resourceHelper.setFileStringContent(stringContent);
		}
		return resourceHelper.createSourceFileMarkers();
	}

	@Override
	public Proposal[] complete(int pos) {
		final Collection<Proposal> completions = new ArrayList<>();
		addCompletionProposals(pos, completions);		
		return completions.toArray(new Proposal[completions.size()]);
	}

	protected void addCompletionProposals(int pos, Collection<Proposal> completions) {
	}

	//
	
	protected Map<String, ILaunchConfiguration> launchConfigs = new HashMap<String, ILaunchConfiguration>();

	protected RunResult launch(String key, String stringContent, boolean result) {
		RunResult runResult = null;
		try {
			ILaunchConfiguration launchConfig = launchConfigs.get(key);
			if (launchConfig == null) {
				launchConfig = getLaunchConfiguration(key);
				launchConfigs.put(key, launchConfig);
			}
			if (launchConfig != null) {
				runResult = launch(launchConfig);
			}
		} catch (Exception e) {
		}
		return runResult;
	}
	
	protected final static String MAIN_LAUNCH_KEY = "main", TESTS_LAUNCH_KEY = "tests";

	protected ILaunchConfiguration getLaunchConfiguration(String key) throws Exception {
		return null;
	}

	@Override
	public RunResult run(String stringContent, boolean result) {
		return launch(MAIN_LAUNCH_KEY, stringContent, result);
	}

	@Override
	public RunResult test(String stringContent, boolean result) {
		return launch(TESTS_LAUNCH_KEY, stringContent, result);
	}

	@Override
	public void close() {
	}

	// launching
	
	private ILaunch launch = null;
	
	private RunResult launch(ILaunchConfiguration launchConfiguration) throws CoreException {
		/*
		if (launch != null) {
			return null;
		}*/
		launch = launchConfiguration.launch(ILaunchManager.RUN_MODE, null);
		while (! launch.isTerminated()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException ie) {
			}
		}
		StringBuilder outputBuffer = new StringBuilder(), errorBuffer = new StringBuilder();
		IProcess[] processes = launch.getProcesses();
		for (IProcess process : processes) {
			appendStreamContents(outputBuffer, process.getStreamsProxy().getOutputStreamMonitor());
			appendStreamContents(errorBuffer, process.getStreamsProxy().getErrorStreamMonitor());
		}
		RunResult runResult = new RunResult(launchConfiguration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, getQualifiedClassName(null)));
		runResult.setConsoleOutput(outputBuffer.toString());
		runResult.setErrorOutput(errorBuffer.toString());
		return runResult;
	}

	private void appendStreamContents(StringBuilder buffer, IStreamMonitor stream) {
		String output = stream.getContents();
		if (output != null && output.length() > 0)
			buffer.append(output);
	}

	protected String getQualifiedClassName(String suffix) {
		String simpleName = resourceName;
		int pos = resourceName.lastIndexOf('.');
		if (pos > 0) {
			simpleName = simpleName.substring(0, pos);
		}
		return packageName + "." + simpleName + (suffix != null ? suffix : "");
	}
}
