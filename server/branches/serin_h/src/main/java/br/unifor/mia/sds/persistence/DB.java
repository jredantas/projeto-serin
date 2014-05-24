package br.unifor.mia.sds.persistence;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import br.unifor.mia.sds.util.FileUtil;
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


public class DB {
	
	/**
	 * Banco de dados em memória.
	 */
	private static OntModel model = ModelFactory.createOntologyModel();
	
	/**
	 * Contador iniciando em 100.
	 */
	private static int sequence = 100;
	
	static {
		try {
			/* Carregar alguns Dados de exemplo */
			String insertString = FileUtil.getContent("CLINIC_INSERT_DATA.txt");
			UpdateRequest request = UpdateFactory.create(insertString);
			UpdateAction.execute(request, model);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param cls
	 * @return
	 */
	private static String newSubjectURI(OntResource cls) {
		return URLTemplate.URL_TEMPLATE + "/" + cls.getLocalName() + "/" + sequence++;
	}

	public static boolean isMembership(String rdfID, Resource classNameResource) {
	
		Individual individuo = model.getIndividual(URLTemplate.URL_TEMPLATE + "/"+ classNameResource.getLocalName() + "/" + rdfID);
		
		return model.contains(individuo, RDF.type, classNameResource);
	}

	public static String listIndividuals(Resource cls) {
		
		List<Individual> individuals = model.listIndividuals(cls).toList();
		
		try {
			return OntologyConverter.toRDFXML(individuals.toArray(new Individual[individuals.size()]));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}

	public static String getIndividual(String rdfID, Resource classResource) {

		Individual individuo = null;
		
		if (rdfID.indexOf(URLTemplate.URL_TEMPLATE) != -1) {
			individuo = model.getIndividual(rdfID);
		} else {
			individuo = model.getIndividual(URLTemplate.URL_TEMPLATE + "/"+ classResource.getLocalName() + "/" + rdfID);
		}
		try {
			return OntologyConverter.toRDFXML(individuo);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}

	public static String getProperty(OntResource classResource, String rdfID, OntResource propertyResource)
			throws DBException {

		Statement stmt = OntologyConverter.getStatement(propertyResource.asProperty(), getIndividual(rdfID, classResource));
		
		if (stmt == null) {
			throw new DBException(DBErrorMessage.GET_PROPERTY_NOT_DEFINED_FOR_THIS_RDF_INSTANCE);
		}
		
		try {
			return OntologyConverter.toRDFXML(stmt, classResource);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;		
	}

	public static String postIndividual(OntModel modelOfInterface, OntResource classResource, String rdfXml)
			throws Exception {
		
		// Converte o XML para uma lista de triplas que representam uma instância RDF.
		List<Statement> individualStatements = OntologyConverter.toStatements(rdfXml, classResource, modelOfInterface);
		
		// Gera ID do sujeito automático no HTTP POST.
		String rdfID = newSubjectURI(classResource);
		
		Resource subject = model.createResource(rdfID, classResource);
		
		// Insere as triplas da instância no banco de dados.
		for (Statement statement : individualStatements) {
			model.add(subject, statement.getPredicate(), statement.getObject());
		}

		return getIndividual(rdfID, classResource);
	}

	public static String postProperty(OntModel modelOfInterface, OntResource classResource, String rdfID,
			OntResource propertyResource, String rdfXml) throws Exception {

		// Converte o XML para uma lista de triplas que representam uma instância RDF.
		List<Statement> individualStatements = OntologyConverter.toStatements(rdfXml, classResource, modelOfInterface);
		
		Resource subject = model.getIndividual(URLTemplate.URL_TEMPLATE + "/"+ classResource.getLocalName() + "/" + rdfID);
		
		// Verifica se a instância pertence a base de dados
		if (subject == null) {
			throw new DBException(DBErrorMessage.INVALID_RDF_INSTANCE);
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
			throw new DBException(DBErrorMessage.POST_PROPERTY_NOT_DEFINED_FOR_THIS_RDF_INSTANCE);
		}
		
		// Insere as triplas da instância no banco de dados.
		for (Statement statement : propertyToInsert) {
			model.add(subject, statement.getPredicate(), statement.getObject());
		}

		return getIndividual(rdfID, classResource);
	}
}
