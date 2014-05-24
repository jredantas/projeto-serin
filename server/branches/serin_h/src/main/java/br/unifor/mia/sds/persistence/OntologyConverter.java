package br.unifor.mia.sds.persistence;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntResource;
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

	private static final String FORMAT = "RDF/XML-ABBREV";
	
	/**
	 * Converte uma string rdfXml em uma lista de triplas.
	 * 
	 * @throws Exception
	 *             Lança um exceção SAXParserException, no caso de 'rdfXml' ser
	 *             um XML mal formado.
	 */
	private static List<Statement> getStatements(String rdfXml) throws Exception  {
		
		OntModel model = ModelFactory.createOntologyModel();
		ByteArrayInputStream stream = new ByteArrayInputStream(rdfXml.getBytes());
		
		try {
			model.read(stream, null);
		} catch (Exception e) {
			throw (Exception) e.getCause();
		}
		
		return model.listStatements().toList();
	}

	/*
	 * Busca todas as propriedades de uma determinada classe.
	 */
	private static List<Property> getPropertiesDomainedBy(Resource cls, OntModel model) {
		
		List<Statement> statements = model.listStatements(null, RDFS.domain, cls).toList();
		
		List<Property> properties = new ArrayList<Property>();
		
		for (Statement statement : statements) {
			properties.add(statement.getSubject().as(Property.class));
		}
		
		return properties;
	}

	/*
	 * Retorna todas as triplas de uma string rdfXml cuja propriedade seja 'property'
	 */
	public static Statement getStatement(Property property, String rdfXml) {
		
		OntModel model = ModelFactory.createOntologyModel();
		ByteArrayInputStream stream = new ByteArrayInputStream(rdfXml.getBytes());
		model.read(stream, null);
		
		List<Statement> listStmt = model.listStatements(null, property, (RDFNode) null).toList();
		
		return !listStmt.isEmpty() ? listStmt.get(0) : null;
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
		
		model.write(stream, FORMAT);

		String rdfXml = new String(stream.toByteArray());

		return rdfXml;
	}

	public static String toRDFXML(Statement stmt, Resource classResource) throws IOException {
	
		OntModel model = ModelFactory.createOntologyModel();
		model.setNsPrefixes(stmt.getModel().getNsPrefixMap());
		
		Individual newInd = model.createIndividual(stmt.getSubject().getURI(), classResource);
		
		newInd.addProperty(stmt.getPredicate(), stmt.getObject());	
		
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		
		model.write(stream, FORMAT);
	
		String rdfXml = new String(stream.toByteArray());
	
		return rdfXml;
	}

	/**
	 * Converte o XML para uma lista de triplas que representam uma instância
	 * RDF.
	 * 
	 * @param rdfXml
	 *            String XML representando uma instância RDF válida.
	 * @param classResource
	 *            A instância RDF gerada a partir da string XML será do tipo
	 *            'classResource'
	 * @param model
	 *            Recebe o modelo de representa a interface SERIN.
	 * 
	 * @return Retorna uma lista de triplas que compõe a instância RDF.
	 * 
	 * @throws Exception
	 *             Lança um exceção SAXParserException, no caso de 'rdfXml' ser
	 *             um XML mal formado.
	 */
	public static List<Statement> toStatements(String rdfXml, OntResource classResource, OntModel model)
			throws Exception {
		
		List<Property> listOfPropertiesDefinedAtInterface = getPropertiesDomainedBy(classResource, model);
			
		List<Statement> individualStatements = new ArrayList<Statement>();
	
		for (Statement stmt : getStatements(rdfXml)) {
			/* Filtro para selecionar apenas as propriedades definidas na interface SERIN
			 *
			 * OBS.: Qualquer propriedade presente no XML do corpo da requisição que não estiver definido
			 * na interface SERIN será ignorado por esse filtro.
			 */
			if (listOfPropertiesDefinedAtInterface.contains(stmt.getPredicate())) {
				individualStatements.add(stmt);
			}
		}
		
		return individualStatements;
	}
}
