package br.unifor.mia.sds.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.vocabulary.RDFS;

public class OntologyUtil {

	private static final String FORMAT = "RDF/XML-ABBREV";
	
	/**
	 * Converte uma string rdfXml em uma lista de triplas.
	 * 
	 * @throws Exception
	 *             Lança um exceção SAXParserException, no caso de 'rdfXml' ser
	 *             um XML mal formado.
	 */
	private static List<Statement> getStatements(String rdfXml) {
		
		OntModel model = ModelFactory.createOntologyModel();
		ByteArrayInputStream stream = new ByteArrayInputStream(rdfXml.getBytes());
		
		model.read(stream, null);
		
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
	public static List<Statement> getStatements(Property property, String rdfXml) throws RDFXMLException {
		
		OntModel model = ModelFactory.createOntologyModel();
		ByteArrayInputStream stream = new ByteArrayInputStream(rdfXml.getBytes());
		
		try {
			model.read(stream, null);
		} catch (JenaException e) {
			throw new RDFXMLException("WARNING: Instância RDF/XML com erro de sintaxe. --> " + e.getMessage());
		}
		
		List<Statement> listStmt = model.listStatements(null, property, (RDFNode) null).toList();
		
		return listStmt.isEmpty() ? null : listStmt;
	}

	public static String listIndividualsToRDFXML(Individual... individuals) {

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

	public static String listStatementsToRDFXML(Resource classResource, Statement... listStmt) {
	
		OntModel model = ModelFactory.createOntologyModel();
		
		for (Statement stmt : listStmt) {
			
			model.setNsPrefixes(stmt.getModel().getNsPrefixMap());
			
			Individual newInd = model.createIndividual(stmt.getSubject().getURI(), classResource);
			
			newInd.addProperty(stmt.getPredicate(), stmt.getObject());
		}
		
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
	public static List<Statement> RDFXMLtoListStatements(String rdfXml, OntResource classResource, OntModel model) {
		
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
