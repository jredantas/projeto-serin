package br.unifor.mia.sds.interfacemanager;

/**
 * Exceções utilizadas quando a URL é montada incorretamente.
 * 
 * @author Hermano
 *
 */
public interface SERINWarningMessage {

	/**
	 * Usado na classe 'SERINException'
	 */
	public static final String CONCEPT_NOT_DEFINED = "<SERIN>CONCEPT NOT DEFINED IN ABSTRACT INTERFACE</SERIN>";

	/**
	 * Usado na classe 'SERINException'
	 */
	public static final String PROPERTY_DOMAIN_INVALID = "<SERIN>THE DECLARED CLASS IS NOT THE DOMAIN OF PROPERTY</SERIN>";
}
