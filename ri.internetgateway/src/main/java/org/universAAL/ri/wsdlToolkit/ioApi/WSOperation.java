package org.universAAL.ri.wsdlToolkit.ioApi;

public class WSOperation {

	private String operationName;
	private WSOperationInput hasInput;
	private WSOperationOutput hasOutput;
	private String hasDocumentation;
	private String hasStyle; // rpc or document
	private ParsedWSDLDefinition belongsToDefinition; // mporei na einai
	private String hasBindingSoapAction;
	 private AlignmentInformation hasAlignmentInformation;
	// public String hasSoapVersion;

	public WSOperationInput getHasInput() {
		return hasInput;
	}

	public void setHasInput(WSOperationInput hasInput) {
		this.hasInput = hasInput;
	}

	public WSOperationOutput getHasOutput() {
		return hasOutput;
	}

	public void setHasOutput(WSOperationOutput hasOutput) {
		this.hasOutput = hasOutput;
	}

	public String getOperationName() {
		return operationName;
	}

	public void setOperationName(String operationName) {
		this.operationName = operationName;
	}

	public String getHasDocumentation() {
		return hasDocumentation;
	}

	public void setHasDocumentation(String hasDocumentation) {
		this.hasDocumentation = hasDocumentation;
	}

	public String getHasStyle() {
		return hasStyle;
	}

	public void setHasStyle(String hasStyle) {
		this.hasStyle = hasStyle;
	}

	public ParsedWSDLDefinition getBelongsToDefinition() {
		return belongsToDefinition;
	}

	public void setBelongsToDefinition(ParsedWSDLDefinition belongsToDefinition) {
		this.belongsToDefinition = belongsToDefinition;
	}

	public String getHasBindingSoapAction() {
		return hasBindingSoapAction;
	}

	public void setHasBindingSoapAction(String hasBindingSoapAction) {
		this.hasBindingSoapAction = hasBindingSoapAction;
	}
    public void setHasAlignmentInformation(AlignmentInformation hasAlignmentInformation) {
        this.hasAlignmentInformation = hasAlignmentInformation;
    }
}
