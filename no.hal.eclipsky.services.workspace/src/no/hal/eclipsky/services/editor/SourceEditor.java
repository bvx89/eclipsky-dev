package no.hal.eclipsky.services.editor;

import org.eclipse.core.runtime.IProgressMonitor;

import no.hal.eclipsky.services.common.Proposal;
import no.hal.eclipsky.services.common.SourceFileMarker;

public interface SourceEditor {
	public boolean edits(String projectName, String packageName, String resourceName);
	public SourceFileMarker[] update(String stringContent, boolean markers, boolean commit);
	public Proposal[] complete(int pos);
	public RunResult run(String stringContent, boolean result);
	public RunResult test(String stringContent, boolean result);
	public void close(IProgressMonitor monitor);
}
