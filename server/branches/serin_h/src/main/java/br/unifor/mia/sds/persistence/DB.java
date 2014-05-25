package br.unifor.mia.sds.persistence;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import br.unifor.mia.sds.util.FileUtil;
import br.unifor.mia.sds.util.OntologyUtil;
import br.unifor.mia.sds.util.RDFXMLException;
import br.unifor.mia.sds.util.URLTemplate;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.update.UpdateAction;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateRequest;
import com.hp.hpl.jena.vocabulary.RDF;

public final class DB {
	
	/**
	 * Banco de dados em memória.
	 */
	private OntModel model = ModelFactory.createOntologyModel();
	
	/**
	 * Contador iniciando em 100.
	 */
	private int sequence = 100;
	
	private DB() {
		try {
			/* Carregar alguns Dados de exemplo */
			String insertString = FileUtil.getContent("CLINIC_INSERT_DATA.txt");
			UpdateRequest request = UpdateFactory.create(insertString);
			UpdateAction.execute(request, model);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static DB db;
	
	public static DB getInstance() {
		
		if (db == null) {
			db = new DB();
			return db;
		} else {
			return db;	
		}
	}

	/**
	 * 
	 * @param cls
	 * @return
	 */
	private String newSubjectURI(OntResource cls) {
		return URLTemplate.URL_TEMPLATE + "/" + cls.getLocalName() + "/" + sequence++;
	}

	public boolean isMembership(String rdfID, Resource classNameResource) {
	
		Individual individuo = model.getIndividual(URLTemplate.URL_TEMPLATE + "/"+ classNameResource.getLocalName() + "/" + rdfID);
		
		return model.contains(individuo, RDF.type, classNameResource);
	}

	public boolean isMembership(Resource resource, Resource classResource) {		
		return model.contains(resource, RDF.type, classResource);
	}

	public boolean contains(String uri) {
		Individual ind = model.getIndividual(uri);
		return ind == null ? false : true;
	}

	public String listIndividuals(Resource cls) {
		
		List<Individual> individuals = model.listIndividuals(cls).toList();

		if (individuals.isEmpty()) {
			return null;
		}
		
		return OntologyUtil.listIndividualsToRDFXML(individuals.toArray(new Individual[individuals.size()]));
	}

	public String getIndividual(String rdfID, Resource classResource) {

		if (rdfID == null) {
			return null;
		}
		
		Individual individual = null;
		
		if (rdfID.indexOf(URLTemplate.URL_TEMPLATE) != -1) {
			individual = model.getIndividual(rdfID);
		} else {
			individual = model.getIndividual(URLTemplate.URL_TEMPLATE + "/"+ classResource.getLocalName() + "/" + rdfID);
		}
		
		if (individual == null) {
			return null;
		}
		
		return OntologyUtil.listIndividualsToRDFXML(individual);
	}

	public String getProperty(OntResource classResource, String rdfID, OntResource propertyResource)
			throws DBQueryOperationException {

		if (classResource == null || rdfID == null || propertyResource == null) {
			return null;
		}

		try {
			List<Statement> listStmt =
					OntologyUtil.getStatements(propertyResource.asProperty(), getIndividual(rdfID, classResource));
			
			if (listStmt == null || listStmt.isEmpty()) {
				throw new DBQueryOperationException(DBErrorMessage.GET_PROPERTY_NOT_DEFINED_IN_INSTANCE);
			}
			
			return OntologyUtil.listStatementsToRDFXML(classResource, listStmt.toArray(new Statement[listStmt.size()]));
			
		} catch (RDFXMLException e) {
			return null;
		}
	}

	public String postIndividual(OntModel modelOfInterface, OntResource classResource, String rdfXml) {
		
		// Converte o XML para uma lista de triplas que representam uma instância RDF.
		List<Statement> individualStatements = OntologyUtil.RDFXMLtoListStatements(rdfXml, classResource, modelOfInterface);
		
		// Gera ID do sujeito automático no HTTP POST.
		String rdfID = newSubjectURI(classResource);
		
		Resource subject = model.createResource(rdfID, classResource);
		
		// Insere as triplas da instância no banco de dados.
		for (Statement statement : individualStatements) {
			model.add(subject, statement.getPredicate(), statement.getObject());
		}

		return getIndividual(rdfID, classResource);
	}

	public String postProperty(OntModel modelOfInterface, OntResource classResource, String rdfID,
			OntResource propertyResource, String rdfXml) throws DBInsertOperationException {

		// Converte o XML para uma lista de triplas que representam uma instância RDF.
		List<Statement> individualStatements = OntologyUtil.RDFXMLtoListStatements(rdfXml, classResource, modelOfInterface);
		
		Resource subject = model.getIndividual(URLTemplate.URL_TEMPLATE + "/"+ classResource.getLocalName() + "/" + rdfID);
		
		// Verifica se a instância pertence a base de dados
		if (subject == null) {
			throw new DBInsertOperationException(DBErrorMessage.RDF_INSTANCE_OF_PROPERTY_WAS_NOT_LOCATED);
		}
		
		List<Statement> propertyToInsert = new ArrayList<Statement>();
		
		// Extrai do conjunto 'individualStatements' a propriedade a ser inserida, i.e., 'propertyResource'.
		// Senão todas as propriedades de rdfXml serão inseridas. 
		for (Statement stmt : individualStatements) {
			if (stmt.getPredicate().equals(propertyResource)) {
				propertyToInsert.add(stmt);
			}
		}

		// Se 'propertyToInsert' está vazio, significa que no 'rdfXml' não há um nova propriedade 'propertyResource'
		// para inserir
		if (propertyToInsert.isEmpty()) {
			throw new DBInsertOperationException(DBErrorMessage.POST_PROPERTY_NOT_DEFINED_IN_INSTANCE);
		}
		
		// Insere as triplas da instância no banco de dados.
		for (Statement statement : propertyToInsert) {
			model.add(subject, statement.getPredicate(), statement.getObject());
		}

		return getIndividual(rdfID, classResource);
	}
}
