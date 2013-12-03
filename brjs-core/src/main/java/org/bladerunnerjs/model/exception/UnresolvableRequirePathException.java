package org.bladerunnerjs.model.exception;


public class UnresolvableRequirePathException extends RequirePathException {
	private static final long serialVersionUID = 1L;
	
	public UnresolvableRequirePathException(String requirePath) {
		super("Source file '" + requirePath + "' could not be found.");
	}
	
	public UnresolvableRequirePathException(String sourceRequirePath, String dependencyRequirePath) {
		super("Source file '" + dependencyRequirePath + "' could not be found, it was needed by '" + sourceRequirePath + "'.");
	}
}
