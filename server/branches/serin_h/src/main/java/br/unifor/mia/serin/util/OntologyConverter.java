package br.unifor.mia.serin.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDFS;

public class OntologyConverter {
	
	/*public static List<Individual> toObject(Resource resource, String rdfXml) {

		OntModel model = ModelFactory.createOntologyModel();
		ByteArrayInputStream stream = new ByteArrayInputStream(rdfXml.getBytes());
		model.read(stream, null);
		
		return model.listIndividuals(resource).toList();
	}*/

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

	public static Statement getStatement(Property property, String rdfXml) {
		
		OntModel model = ModelFactory.createOntologyModel();
		ByteArrayInputStream stream = new ByteArrayInputStream(rdfXml.getBytes());
		model.read(stream, null);
		
		List<Statement> listStmt = model.listStatements(null, property, (RDFNode) null).toList();
		
		return !listStmt.isEmpty() ? listStmt.get(0) : null;
	}
	
	public static List<Statement> getStatements(String rdfXml) {
		
		OntModel model = ModelFactory.createOntologyModel();
		ByteArrayInputStream stream = new ByteArrayInputStream(rdfXml.getBytes());
		model.read(stream, null);
		
		return model.listStatements().toList();
	}
	
	public static List<Property> getPropertiesDomainedBy(Resource cls, OntModel model) {
		
		List<Statement> statements = model.listStatements(null, RDFS.domain, cls).toList();
		
		List<Property> properties = new ArrayList<Property>();
		
		for (Statement statement : statements) {
			properties.add(statement.getSubject().as(Property.class));
		}
		
		return properties;
	}
}
