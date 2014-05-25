package br.unifor.mia.sds.interfacemanager.integrityconstraint;

import java.util.ArrayList;
import java.util.List;

import br.unifor.mia.sds.interfacemanager.DBHandler;
import br.unifor.mia.sds.interfacemanager.SerinAnnotations;
import br.unifor.mia.sds.util.OntologyUtil;
import br.unifor.mia.sds.util.RDFXMLException;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDFS;

public class SDSIntegrityConstraintHandler {

	private DBHandler dbHandler;

	public SDSIntegrityConstraintHandler(DBHandler dbHandler) {
		this.dbHandler = dbHandler;
	}

	public void checkProperties(OntModel modelOfInterface, List<Property> properties, String rdfXml)
			throws IntegrityConstraintException, RDFXMLException {

		for (Property property : properties) {

			// Verifica Not Null
			checkNotNullConstraint(modelOfInterface, property, rdfXml);

			// Verifica Unique
			checkUniqueConstraint(modelOfInterface, property, rdfXml);

			// Verifica Id
			checkIdConstraint(modelOfInterface, property, rdfXml);

			// Verifica ForeignURI
			checkForeignURIConstraint(modelOfInterface, property, rdfXml);

			// Verifica Embedded
			checkEmbeddedConstraint(modelOfInterface, property, rdfXml);
		}
	}

	/**
	 * Verifica Not Null
	 * 
	 * @param modelOfInterface
	 * 
	 * @param property
	 * @param rdfXml
	 * @throws NotNullException
	 * @throws RDFXMLException 
	 */
	private void checkNotNullConstraint(OntModel modelOfInterface, Property property, String rdfXml)
			throws NotNullException, RDFXMLException {

		Property serinAnot = modelOfInterface.getProperty(SerinAnnotations.NOT_NULL);

		if (property.getProperty(serinAnot) != null) {
			// verificar se regras do not null estão satisfeitas na instância 'rdfXml'
			List<Statement> stmts = OntologyUtil.getStatements(property, rdfXml);
			if (stmts == null || stmts.isEmpty()) {
				throw new NotNullException(property.getLocalName());
			}
		}
	}

	private void checkUniqueConstraint(OntModel modelOfInterface, Property property, String rdfXml)
			throws UniqueException, RDFXMLException {
		
		Property serinAnot = modelOfInterface.getProperty(SerinAnnotations.UNIQUE);
		
		if (property.getProperty(serinAnot) != null) {
			// verificar se regras do Unique estão satisfeitas na instância 'rdfXml'
			List<Statement> stmts = OntologyUtil.getStatements(property, rdfXml);
			if (stmts.size() > 1) {
				throw new UniqueException(property.getLocalName());
			}
		}
	}

	private void checkIdConstraint(OntModel modelOfInterface, Property property, String rdfXml)
			throws IdException, RDFXMLException {
		
		Property serinAnot = modelOfInterface.getProperty(SerinAnnotations.ID);
		
		if (property.getProperty(serinAnot) != null) {
			// verificar se regras do ID estão satisfeitas na instância 'rdfXml'
			List<Statement> stmts = OntologyUtil.getStatements(property, rdfXml);
			if (stmts == null || stmts.isEmpty() || stmts.size() > 1) {
				throw new IdException(property.getLocalName());
			}
		}
	}

	private void checkForeignURIConstraint(OntModel modelOfInterface, Property property, String rdfXml)
			throws RDFXMLException, ForeignURIException {
		
		Property serinAnot = modelOfInterface.getProperty(SerinAnnotations.FOREIGN_URI);
		
		if (property.getProperty(serinAnot) == null) {
			return;
		}

		// verificar se regras do FOREIGN URI estão satisfeitas na instância 'rdfXml'
		List<Statement> stmts = OntologyUtil.getStatements(property, rdfXml);
		
		for (Statement stmt : stmts) {
			// Verifica se não é literal (literais não são FOREIGN URIs)
			if (stmt.getObject().isResource()) {
				if (!dbHandler.contains(stmt.getObject().asResource().getURI())) {
					throw new ForeignURIException(property.getLocalName(), stmt.getObject().asResource().getURI());		
				}
			}
			
			// Busca todas as classes do range de uma determinada propriedade.
			List<Resource> classes = getClassesRangedBy(modelOfInterface, property);

			// Verifica se o recurso 'stmt.getObject()' pertence ao range da propriedade 'property'
			for (Resource cls : classes) {
				if (!dbHandler.isMembership(stmt.getObject().asResource(), cls)) {
					throw new ForeignURIException(
							"Atribuição inconsistente! Instância <" + stmt.getObject().asResource().getURI() +
							"> precisa pertencer a classe <" + cls.getURI() + ">");
				}
			}
		}
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

	private void checkEmbeddedConstraint(OntModel modelOfInterface,
			Property property, String rdfXml) {
		// TODO checkEmbeddedConstraint

	}
}
