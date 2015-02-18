package no.hal.eclipsky.services.editor;

import no.hal.eclipsky.services.common.SourceFileMarker;

public class RunResult {
	
	private final String qualifiedName;
	private SourceFileMarker exceptionLocation;
	private String consoleOutput = "", errorOutput = "";

	public RunResult(String qualifiedName) {
		this.qualifiedName = qualifiedName;
	}

	public String getQualifiedName() {
		return qualifiedName;
	}

	public SourceFileMarker getExceptionLocation() {
		return exceptionLocation;
	}
	public void setExceptionLocation(SourceFileMarker exceptionLocation) {
		this.exceptionLocation = exceptionLocation;
	}

	public String getConsoleOutput() {
		return consoleOutput;
	}
	public void setConsoleOutput(String consoleOutput) {
		this.consoleOutput = consoleOutput;
	}
	
	public String getErrorOutput() {
		return errorOutput;
	}
	public void setErrorOutput(String errorOutput) {
		this.errorOutput = errorOutput;
	}

	@Override
	public String toString() {
		return "RunResult [qualifiedName=" + qualifiedName
				+ ", exceptionLocation=" + exceptionLocation
				+ ", consoleOutput=" + consoleOutput + ", errorOutput="
				+ errorOutput + "]";
	}
	
	
}
