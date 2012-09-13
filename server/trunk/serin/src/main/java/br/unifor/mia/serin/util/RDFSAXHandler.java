package br.unifor.mia.serin.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class RDFSAXHandler extends DefaultHandler {

	private Map<String, String> namespaceMap = new HashMap<String, String>();

	private List<Description> descriptions = new ArrayList<Description>();

	private Description description = null;

	private String propertyQName = null;

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (qName.equals("rdf:RDF")) {
			for (int i = 0; i < attributes.getLength(); i++) {
				namespaceMap.put(attributes.getQName(i).split(":")[1], attributes.getValue(i));
			}
		} else if (qName.equals("rdf:Description")) {
			for (int i = 0; i < attributes.getLength(); i++) {
				description = attributes.getQName(i).equals("rdf:about") ? new Description(attributes.getValue(i)) : null;
			}
		} else {
			propertyQName = namespaceMap.get(qName.split(":")[0]) + qName.split(":")[1];
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (qName.equals("rdf:Description")) {
			descriptions.add(description);
			description = null;
		}
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		String value = new String(Arrays.copyOfRange(ch, start, start + length));
		if (propertyQName != null) {
			description.setPropertyValue(propertyQName, value);
			propertyQName = null;
		}
	}

	public RDF getRDF() {
		return new RDF(namespaceMap, descriptions);
	}
}
