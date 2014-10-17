package br.unifor.mia.sds.interfacemanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.unifor.mia.sds.interfacemanager.integrityconstraint.DBHandler;
import br.unifor.mia.sds.interfacemanager.integrityconstraint.SDSIntegrityConstraintHandler;
import br.unifor.mia.sds.util.RDFXMLException;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class SERINManager {
	
	/**
	 * Mapa onde são guardados todas as interfaces SERIN.
	 * Essa Classe pode gerenciar várias interfaces SERIN ao mesmo tempo.
	 */
	private static Map<String, OntModel> ontologyMap = new HashMap<String, OntModel>();
	
	private String urlOfInterface;

	private String annotation;
	
	public SERINManager(String annotation, String urlOfInterface) {
		this.annotation = annotation;
		this.urlOfInterface = urlOfInterface;
	}
	
	private String getNamespace() {
		// Exemplo: http://www.unifor.br/clinic.owl#
		List<Statement> listStmt = getOntModelOfInterface().listStatements(null, RDF.type , OWL.Ontology).toList();
		
		return listStmt.get(0).getSubject().getURI();// + "#";
	}
	
	
	private boolean hasSerinAnnotation(String resourceName, String annotationURI) throws SERINException {
	
		Property serinAnot = getOntModelOfInterface().getProperty(annotationURI);
	
		if (lookup(resourceName).getProperty(serinAnot) != null) {
			return true;
		}
	
		return false;
	}
	
	/*
	 * Busca todas as propriedades de uma determinada classe.
	 */
	private List<Property> getPropertiesDomainedBy(String className) throws SERINException {
		
		List<Statement> statements =
				getOntModelOfInterface().listStatements(null, RDFS.domain, lookup(className)).toList();
		
		List<Property> properties = new ArrayList<Property>();
		
		for (Statement statement : statements) {
			properties.add(statement.getSubject().as(Property.class));
		}
		
		return properties;
	}

	/**
	 * Gera um objeto 'OntModel' com a ontologia descrita na interface SERIN.
	 * 
	 * @param urlOfInterface O endereço (a URL) onde a interface SERIN está publicada na Web.
	 * 
	 * @return
	 */
	public OntModel getOntModelOfInterface() {

		if (ontologyMap.get(urlOfInterface) == null) {

			ontologyMap.put(urlOfInterface, ModelFactory.createOntologyModel());

			//ontologyMap.get(urlOfInterface).read("file:////home/renato/Dados/serin.owl");
			ontologyMap.get(urlOfInterface).read(urlOfInterface);
			
		}
		
		return ontologyMap.get(urlOfInterface);
	}
	
	
	/**
	 * 
	 * 
	 * @param resourceName
	 * @return
	 * @throws SERINException 
	 */
	public OntResource lookup(String resourceName) throws SERINException {
	
		OntModel model = getOntModelOfInterface();
		
		// Tenta recuperar conceitos da ontologia
		OntResource resource = model.getOntResource(getNamespace()+"#" + resourceName);
		
		// Senão localizar é por que esse recurso não está definido na interface SERIN
		if (resource == null) {
			throw new SERINException(SERINWarningMessage.CONCEPT_NOT_DEFINED);
		}
		
		return resource;	
	}

	public void checkPermission(String className) throws AnnotationlessException, SERINException {
		
		if (!hasSerinAnnotation(className, annotation)) {
			// Se a classe não possui a anotação indicada
			throw new AnnotationlessException();
		}
	}

	public void checkDomain(String propertyName, String className) throws SERINException {

		OntModel model = getOntModelOfInterface();

		OntResource classResource = lookup(className);
		
		OntResource propertyResource = lookup(propertyName);

		// Senão contem a relação de dominio é por que esse recurso não está definido na interface SERIN
		if (!model.contains(propertyResource, RDFS.domain, classResource)) {
			throw new SERINException(SERINWarningMessage.PROPERTY_DOMAIN_INVALID);
		}
	}
	
	public void checkIntegrityConstraint(String className, DBHandler dbHandler, String rdfXml)
			throws SERINException, RDFXMLException {
		
		// Localiza todas as propriedade associadas à classe 'className'.
		List<Property> properties = getPropertiesDomainedBy(className);

		SDSIntegrityConstraintHandler icHandler = new SDSIntegrityConstraintHandler(dbHandler);

		// Verificar as anotações de propriedades
		icHandler.checkProperties(getOntModelOfInterface(), properties, rdfXml);
		
		// TODO Verificar classe Internal
	}

	/**
	 * Busca apenas as instâncias embedded de PRIMEIRO nível, isso é, não faz busca em profundidade.  
	 * 
	 * @param className
	 * @param rdfID
	 * @param dbHandler
	 * @return
	 * @throws SERINException
	 */
	public String getIndividual(String className, String rdfID, DBHandler dbHandler) throws SERINException {

		// Localiza todas as propriedade associadas à classe 'className'.
		List<Property> properties = getPropertiesDomainedBy(className);

		SDSIntegrityConstraintHandler icHandler = new SDSIntegrityConstraintHandler(dbHandler);

		// Busca instância 'rdfID' e suas instâncias Embedded
		return icHandler.getIndividual(getOntModelOfInterface(), className, rdfID, properties);
	}

	public String getIndividuals(OntResource classResource, DBHandler dbHandler) throws SERINException {

		// Localiza todas as propriedade associadas à classe 'className'.
		List<Property> properties = getPropertiesDomainedBy(classResource.getLocalName());

		SDSIntegrityConstraintHandler icHandler = new SDSIntegrityConstraintHandler(dbHandler);

		// Busca instância 'rdfID' e suas instâncias Embedded
		return icHandler.getIndividuals(getOntModelOfInterface(), classResource, properties);
	}
	
	public String getHosts(OntResource classResource, DBHandler dbHandler) throws SERINException {

		// Localiza todas as propriedade associadas à classe 'className'.
		List<Property> properties = getPropertiesDomainedBy(classResource.getLocalName());

		SDSIntegrityConstraintHandler icHandler = new SDSIntegrityConstraintHandler(dbHandler);

		// Busca instância 'rdfID' e suas instâncias Embedded
		return icHandler.getIndividuals(getOntModelOfInterface(), classResource, properties);
	}

}
