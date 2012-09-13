package br.unifor.mia.serin.util;

import java.util.HashMap;

import com.hp.hpl.jena.vocabulary.OWL2;
import com.hp.hpl.jena.vocabulary.RDF;

public class Description {

	private String rdfID;

	private HashMap<String, String> properties = new HashMap<String, String>();
	
	public Description(String rdfID) {
		this.rdfID = rdfID;
	}

	public String getRdfID() {
		return rdfID;
	}

	public void setPropertyValue(String propertyURI, String value) {
		properties.put(propertyURI, value);
	}

	public HashMap<String, String> getProperties() {
		return properties;
	}

	@Override
	public String toString() {
		return rdfID;
	}

	public String getType() {
		if (properties.keySet().contains(RDF.type.toString())) {
			return properties.get(RDF.type.toString());
		}
		return OWL2.Thing.toString();
	}
}
