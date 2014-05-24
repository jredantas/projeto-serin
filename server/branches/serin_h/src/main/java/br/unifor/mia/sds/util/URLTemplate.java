package br.unifor.mia.sds.util;

public class URLTemplate {

	public static final String URL_TEMPLATE = "http://URI";
	
	/**
	 * 
	 * @param rdfXml
	 * @return
	 */
	public static Object decode(String rdfXml, String urlOfInterfaceInSDSServer) {
		return rdfXml.replace(URL_TEMPLATE, urlOfInterfaceInSDSServer);
	}
	
	/**
	 * 
	 * @param rdfXml
	 * @return
	 */
	@Deprecated
	private static String encode(String rdfXml) {
		return "";// rdfXml.replace(info.getBaseUri().toString() + ontology(), URL_TEMPLATE);
	}
	


}
