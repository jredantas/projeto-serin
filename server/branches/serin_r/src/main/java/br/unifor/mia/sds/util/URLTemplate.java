package br.unifor.mia.sds.util;

public class URLTemplate {

	public static final String URL_TEMPLATE = "http://URI";

	public static String decode(String rdfXml, String urlOfInterfaceInSDSServer) {
		return rdfXml.replace(URL_TEMPLATE, urlOfInterfaceInSDSServer);
	}

	public static String encode(String rdfXml, String urlOfInterfaceInSDSServer) {
		return rdfXml.replace(urlOfInterfaceInSDSServer, URL_TEMPLATE);
	}
}
