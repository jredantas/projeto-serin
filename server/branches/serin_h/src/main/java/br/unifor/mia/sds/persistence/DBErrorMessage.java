package br.unifor.mia.sds.persistence;

public interface DBErrorMessage {
	
	/**
	 * Usado na classe ''
	 */
	public static final String INVALID_RDF_INSTANCE = "<DB>INVALID RDF INSTANCE IN BODY REQUEST</DB>";
	
	/**
	 * Usado na classe 'DBInsertOperationException'
	 */
	public static final String RDF_INSTANCE_OF_PROPERTY_WAS_NOT_LOCATED = "<DB>RDF INSTANCE ASSOCIATED WITH THIS PROPERTY DOES NOT EXIST IN THE DATABASE</DB>";
	
	
	/**
	 * Usado na classe ''
	 */
	public static final String GET_PROPERTY_NOT_DEFINED_IN_INSTANCE = "<DB>ASKED PROPERTY IS NOT DEFINED IN THE RDF INSTANCE</DB>";
	
	/**
	 * Usado na classe 'DBInsertOperationException'
	 */
	public static final String POST_PROPERTY_NOT_DEFINED_IN_INSTANCE = "<DB>POSTED PROPERTY NOT DEFINED IN REQUEST BODY</DB>";
}
