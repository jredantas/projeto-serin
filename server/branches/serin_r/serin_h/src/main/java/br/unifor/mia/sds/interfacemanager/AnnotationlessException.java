package br.unifor.mia.sds.interfacemanager;

public class AnnotationlessException extends SERINException {

	private static final long serialVersionUID = -5478383635354263934L;

	public AnnotationlessException() {
		super("<SERIN>ANNOTATION NOT AVAILABLE</SERIN>");
	}

}
