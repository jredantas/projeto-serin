package br.unifor.mia.serin.server;

import com.hp.hpl.jena.ontology.OntModel;

public class OntologySession {

	private OntModel ontModel;
	
	public int sequence = 1;

	public OntologySession(OntModel createOntModel) {
		ontModel = createOntModel;
	}

	public OntModel getOntModel() {
		return ontModel;
	}

}
