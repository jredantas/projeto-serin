package br.unifor.mia.sds.interfacemanager.serin;

import java.util.ArrayList;
import java.util.List;

import br.unifor.mia.sds.requesthandler.DBHandler;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDFS;

public class SerinHandler {

	private DBHandler dbHandler;
	


	public SerinHandler(DBHandler dbHandler) {
		this.dbHandler = dbHandler;
	}

	/**
	 * Método fixo.
	 * Esse método monta os indivíduos a partir da lista de hosts.
	 * @param modelOfInterface
	 * @param classResource
	 * @param properties
	 * @return
	 */
	public String getHosts(OntModel modelOfInterface, OntResource classResource, String interfaceName) {

		return this.dbHandler.getHosts(classResource, interfaceName);
	}

	/**
	 * Método fixo.
	 * Esse método monta a lista de interfaces atendidas pelo host
	 * @param modelOfInterface
	 * @param classResource
	 * @param properties
	 * @return
	 */
	public String getInterfaces(OntModel modelOfInterface, OntResource classResource) {

		return this.dbHandler.getInterfaces(classResource);
	}
	/**
	 * Busca todas as classes do range de uma determinada propriedade.
	 * 
	 * @param modelOfInterface
	 * @param property
	 */
	private List<Resource> getClassesRangedBy(OntModel modelOfInterface, Property property) {

		List<Statement> statements =
				modelOfInterface.listStatements(property, RDFS.range, (RDFNode) null).toList();
		
		List<Resource> classes = new ArrayList<Resource>();
		
		for (Statement statement : statements) {
			if (statement.getObject().isResource()) {
				classes.add(statement.getObject().asResource());
			}
		}
		
		return classes;		
	}
}
