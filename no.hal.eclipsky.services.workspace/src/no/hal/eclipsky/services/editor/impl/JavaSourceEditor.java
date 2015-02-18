package no.hal.eclipsky.services.editor.impl;

import java.util.Collection;

import no.hal.eclipsky.services.common.Proposal;
import no.hal.eclipsky.services.common.SourceFileMarker;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.CompletionRequestor;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IProblemRequestor;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;

public class JavaSourceEditor extends GenericSourceEditor {

	private ICompilationUnit workingCopy = null;

	public JavaSourceEditor(String projectName, String packageName, String resourceName) {
		super(projectName, packageName, resourceName);
		IFile file = getFile(true);
		workingCopy = JavaCore.createCompilationUnitFrom(file);
		if (workingCopy.exists()) {
			try {
				workingCopy.becomeWorkingCopy(null);
			} catch (JavaModelException e) {
				workingCopy = null;
			}
		}
	}

	private static String[] JAVA_SOURCE_FOLDERS = {"src", "resources"}; 
	
	protected String[] getSourceFolderNames() {
		return JAVA_SOURCE_FOLDERS;
	}

	@Override
	public SourceFileMarker[] update(String stringContent, boolean markers, boolean commit) {
		if (workingCopy != null) {
			IBuffer buffer;
			try {
				buffer = workingCopy.getBuffer();
				buffer.setContents(stringContent);
				final SourceFileMarkersProvider sourceFileMarkersProvider = new SourceFileMarkersProvider();
				workingCopy.reconcile(ICompilationUnit.NO_AST, true, new WorkingCopyOwner() {
					@Override
					public IProblemRequestor getProblemRequestor(ICompilationUnit workingCopy) {
						return sourceFileMarkersProvider;
					}
				}, null);
				try {
					SourceFileMarker[] sourceFileMarkers = sourceFileMarkersProvider.getSourceFileMarkersFuture().get();
					return sourceFileMarkers;
				} catch (Exception e) {
				}
			} catch (JavaModelException e) {
			}
			return null;
		}
		return super.update(stringContent, markers, commit);
	}

	@Override
	protected void addCompletionProposals(int pos, Collection<Proposal> completions) {
		try {
			workingCopy.codeComplete(pos, new CompletionRequestor() {
				@Override
				public void accept(CompletionProposal proposal) {
					Proposal p = Proposal.getProposal(proposal);
					completions.add(p);
					
				}
			});
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void close() {
		if (workingCopy != null) {
			try {
				workingCopy.commitWorkingCopy(true, null);
				workingCopy.discardWorkingCopy();
			} catch (JavaModelException e) {
			}
		}
		workingCopy = null;
		super.close();
	}

	// launching
		
	private static String javaApplicationLaunchId = IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION;
	private static String junitTestLaunchId = "org.eclipse.jdt.junit.launchconfig";

	protected ILaunchConfiguration getLaunchConfiguration(String key) throws Exception {
		if (MAIN_LAUNCH_KEY.equals(key)) {
			return createLaunchConfiguration(javaApplicationLaunchId, "Run Main", getQualifiedClassName(null));
		} else if (TESTS_LAUNCH_KEY.equals(key)) {
			return createLaunchConfiguration(junitTestLaunchId, "Run Tests", getQualifiedClassName("Test"));
		}
		return null;
	}
	
	private ILaunchConfiguration createLaunchConfiguration(String launchConfigurationTypeId, String configName, String qualifiedClassName) throws CoreException {
		ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfigurationType launchConfigurationType = launchManager.getLaunchConfigurationType(launchConfigurationTypeId);
		ILaunchConfigurationWorkingCopy workingCopy = launchConfigurationType.newInstance(null, configName);
		workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, projectName);
		workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, qualifiedClassName);
		return workingCopy.doSave();
	}
}
