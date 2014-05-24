package br.unifor.mia.sds.persistence;

public interface DBErrorMessage {
	
	/**
	 * Usado na classe 'SERINMalFormedException'
	 */
	public static final String INVALID_RDF_INSTANCE = "<SDS>INVALID RDF INSTANCE IN BODY REQUEST</SDS>";
	
	/**
	 * Usado na classe 'SERINMalFormedException'
	 */
	public static final String GET_PROPERTY_NOT_DEFINED_FOR_THIS_RDF_INSTANCE = "<SDS>PROPERTY NOT DEFINED IN REQUESTED INSTANCE</SDS>";
	
	/**
	 * Usado na classe 'SERINMalFormedException'
	 */
	public static final String POST_PROPERTY_NOT_DEFINED_FOR_THIS_RDF_INSTANCE = "<SDS>POST PROPERTY NOT DEFINED IN REQUEST BOBY</SDS>";
}
