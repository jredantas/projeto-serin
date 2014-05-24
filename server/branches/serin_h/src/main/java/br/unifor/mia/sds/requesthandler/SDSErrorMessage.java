package br.unifor.mia.sds.requesthandler;

public interface SDSErrorMessage {
	
	/**
	 * Usado na classe 'AnnotationlessException'
	 */
	public static final String SERIN_ANNOTATION_NOT_AVAILABLE = "<SDS>SERIN ANNOTATION NOT AVAILABLE</SDS>";

	/**
	 * Usado na classe 'SERINMalFormedException'
	 */
	public static final String CONCEPT_NOT_DEFINED = "<SDS>CONCEPT NOT DEFINED IN ABSTRACT INTERFACE</SDS>";
	
	/**
	 * Usado na classe 'MembershipException'
	 */
	public static final String NOT_MEMBERSHIP = "<SDS>RESOURCE NOT MEMBERSHIP OF CLASS</SDS>";

	/**
	 * Usado na classe 'SERINMalFormedException'
	 */
	public static final String PROPERTY_DOMAIN_INVALID = "<SDS>THE PROPERTY´S DOMAIN IS NOT DEFINED FOR DECLARED CLASS</SDS>";
}
