package br.unifor.mia.serin;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;

public interface Ontology {

	public abstract OntModel getModel();

	public abstract Individual createIndividual(String about, String marca,
			String modelo);
	
	public String WEB_SERVICE();

	public abstract Individual getIndividual(String string);

}