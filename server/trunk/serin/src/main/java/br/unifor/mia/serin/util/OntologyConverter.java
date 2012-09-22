package br.unifor.mia.serin.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

public class OntologyConverter {
	
	public static List<Individual> toObject(Resource resource, String rdfXml) {

		OntModel model = ModelFactory.createOntologyModel();
		ByteArrayInputStream stream = new ByteArrayInputStream(rdfXml.getBytes());
		model.read(stream, null);
		
		return model.listIndividuals(resource).toList();
	}

	public static String toRDFXML(Individual... individuals) throws IOException {

		OntModel model = ModelFactory.createOntologyModel();
		model.setNsPrefixes(individuals[0].getModel().getNsPrefixMap());
		
		for (Individual ind : individuals) {
		
			Individual newInd = model.createIndividual(ind.getURI(), ind.getRDFType());
			
			for (Resource cls : ind.listRDFTypes(true).toList()) {
				newInd.addOntClass(cls);
			}
			
			for (Statement stmt : ind.listProperties().toList()) {
				newInd.addProperty(stmt.getPredicate(), stmt.getObject());	
			}
		}
		
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		
		model.write(stream, RDF_SYNTAX.FORMAT);

		String rdfXml = new String(stream.toByteArray());

		return rdfXml;
	}
}
