package br.unifor.mia.serin.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * Classe que auxilia a conversão das classes de ontologia para objetos que
 * podem ser utilizados pelo serviço web
 * 
 * @author bruno muniz (brunoamuniz@gmail.com)
 * @since 06/06/2009
 */
public class OntologyConverter {
	
	/**
	 * Método que converte um objeto em uma instancia de uma ontologia....
	 * 
	 * @return
	 * @throws IOException
	 */
	public static Collection<Individual> toOntology(OntModel model, Collection<Triple> triples) {

		if (triples == null) {
			return null;
		}

		Map<String, Individual> individualMap = new HashMap<String, Individual>();
		List<Triple> propertiesTriples = new ArrayList<Triple>();
		
		for (Triple triple : triples) {
			if (triple.getPred().equals(RDF.type.toString())) {
				individualMap.put(triple.getSubj(),
						model.createIndividual(triple.getSubj(), model.getResource(triple.getObj())));
			} else {
				propertiesTriples.add(triple);
			}
		}
		
		for (Triple triple : propertiesTriples) {
			individualMap.get(triple.getSubj()).setPropertyValue(
					model.getProperty(triple.getPred()),
					model.createLiteral(triple.getObj()));
		}
		
		return individualMap.values();	
	}

	/**
	 * Returns the Representation of an item.
	 * 
	 * @param item
	 *            the item.
	 * 
	 * @return The Representation of the item.
	 */
	public static Collection<Triple> getRepresentation(Individual individual) {

		Collection<Triple> triples = new ArrayList<Triple>();
		
		StmtIterator itr = individual.listProperties();
		while (itr.hasNext()) {
			Statement statment = itr.next();
			triples.add(new Triple(
					statment.getSubject().getURI(),
					statment.getPredicate().getURI(),
					statment.getObject().toString()));
		}
		return triples;
	}
}