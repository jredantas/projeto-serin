package br.unifor.mia.serin.util;

import java.util.List;
import java.util.Map;

public class RDF {

	private Map<String, String> namespaceMap;

	private List<Description> descriptions;

	public RDF(Map<String, String> namespaceMap, List<Description> descriptions) {
		this.namespaceMap = namespaceMap;
		this.descriptions = descriptions;
	}

	public Map<String, String> getNamespaceMap() {
		return namespaceMap;
	}

	public List<Description> getDescriptions() {
		return descriptions;
	}
}
