package br.unifor.mia.serin.server;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import br.unifor.mia.serin.util.FileUtil;
import br.unifor.mia.serin.util.OntologyConverter;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.update.UpdateAction;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateRequest;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

@Consumes("application/rdf+xml")
@Produces("application/rdf+xml")
public abstract class SerinServer {

	/**
	 * 
	 */
	private static final String SERIN_NOT_AVAILABLE = "<SERIN>SERIN ANNOTATION NOT AVAILABLE</SERIN>";

	/**
	 * 
	 */
	private static final String RESOURCE_NOT_FOUND = "<SERIN>RESOURCE NOT FOUND</SERIN>";

	/**
	 * 
	 */
	private static final Object NOT_MEMBERSHIP = "<SERIN>RESOURCE NOT MEMBERSHIP OF CLASS</SERIN>";

	protected abstract String newSubjectURI(OntResource cls);

	/**
	 * 
	 * @return
	 */
	protected abstract String getOntologyFileName();

	/**
	 * 
	 * @return
	 */
	protected abstract String namespace();

	/**
	 * 
	 */
	private static Map<String, OntModel> modelMap = new HashMap<String, OntModel>();

	/**
	 * 
	 */
	protected static boolean modelEmpty;
	
	/**
	 * 
	 */
	public SerinServer() {
		if (getModel() == null) {
			InputStream inOntology = getClass().getClassLoader().getResourceAsStream(getOntologyFileName());
			modelMap.put(getOntologyFileName(),	ModelFactory.createOntologyModel());
			getModel().read(inOntology, null);
			modelEmpty = true;
		}
	}

	/**
	 * 
	 * @return
	 */
	public OntModel getModel() {
		return modelMap.get(getOntologyFileName());
	}

	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	@GET
	@Produces("application/owl+xml")
	public String getOntology() throws IOException {		
		return FileUtil.getContent(getOntologyFileName());
	}

	/**
	 * Trabalha tanto como "LIST Property" como "GET Individual".
	 * 
	 * @param ontClass
	 *            Classe a que o resource pertence.
	 * @param resourceID
	 *            identificação do resource, pode ser um individuo como uma
	 *            propriedade.
	 * @return
	 */
	@GET
	@Path("{ontClass}/{resourceID}")
	public Response getResource(@PathParam("ontClass") String ontClass, @PathParam("resourceID") String resourceID,
		@QueryParam("fetch") String fetch) {

		OntResource resource = lookup(resourceID);

		if (resource == null) {
			return Response.status(Status.NOT_FOUND).entity(RESOURCE_NOT_FOUND).build();
		}

		if (resource.isProperty()) {
			return listProperty(ontClass, resource.asProperty());
		} else {
			return getIndividual(ontClass, resource.asIndividual(), fetch);
		}
	}

	/**
	 * 
	 * @param ontClass
	 * @return
	 */
	@GET
	@Path("{ontClass}")
	public Response listIndividual(@PathParam("ontClass") String ontClass) {

		if (!hasSerinAnnotation(lookup(ontClass), Serin.GET)) {
			return Response.status(Status.BAD_REQUEST).entity(SERIN_NOT_AVAILABLE).build();
		}

		Resource cls = ResourceFactory.createResource(namespace() + ontClass);

		List<Individual> individuals = getModel().listIndividuals(cls).toList();

		if (individuals.isEmpty()) {
			return Response.status(Status.NOT_FOUND).entity(RESOURCE_NOT_FOUND).build();
		}

		try {
			String result = OntologyConverter.toRDFXML(individuals.toArray(new Individual[individuals.size()]));
			return Response.ok(result).build();
		} catch (IOException e) {
			e.printStackTrace();
			return Response.serverError().build();
		}
	}

	/**
	 * 
	 * 
	 * @param ontClass
	 * @param individual
	 * @return
	 */
	private Response getIndividual(String ontClass, Individual individual, String fetch) {
	
		if (!getModel().contains(individual, RDF.type, lookup(ontClass))) {
			return Response.status(Status.BAD_REQUEST).entity(NOT_MEMBERSHIP).build();
		}
	
		if (!hasSerinAnnotation(lookup(ontClass), Serin.GET)) {
			return Response.status(Status.BAD_REQUEST).entity(SERIN_NOT_AVAILABLE).build();
		}
	
		List<Individual> individuals = new ArrayList<Individual>();
		if ("eager".equals(fetch)) {
			for (Statement prop : individual.listProperties().toList()) {
				if (prop.getPredicate().as(OntProperty.class).isObjectProperty()) {
					individuals.add(prop.getObject().as(Individual.class));
				}
			}
		}
	
		try {
			individuals.add(individual);
			Individual[] array = new Individual[individuals.size()];
			String result = OntologyConverter.toRDFXML(individuals.toArray(array));
			return Response.ok(result).build();
		} catch (IOException e) {
			return Response.status(Status.NOT_FOUND).entity(RESOURCE_NOT_FOUND).build();
		}
	}

	/**
	 * 
	 * @param ontClass
	 * @param rdfXml
	 * @return
	 */
	@POST
	@Path("{ontClass}")
	public Response postIndividual(@PathParam("ontClass") String ontClass, String rdfXml) {

		OntResource cls = lookup(ontClass);
		
		if (!hasSerinAnnotation(cls, Serin.POST)) {
			return Response.status(Status.BAD_REQUEST).entity(SERIN_NOT_AVAILABLE).build();
		}

		OntModel rdfXmlModel = ModelFactory.createOntologyModel();
		ByteArrayInputStream stream = new ByteArrayInputStream(rdfXml.getBytes());
		rdfXmlModel.read(stream, null);
		
		List<Property> properties = OntologyConverter.getPropertiesDomainedBy(cls, getModel());
		
		List<Statement> individualStatements = new ArrayList<Statement>();
		
		// Verifica se TODAS as propriedades foram preenchidas.
		for (Property property : properties) {
			List<Statement> stmt = rdfXmlModel.listStatements(null, property, (RDFNode) null).toList();
			if (!stmt.isEmpty()) {
				individualStatements.add(stmt.get(0));
			} else {
				return Response.status(Status.BAD_REQUEST).entity("Required fields not fulfill.").build();	
			}
		}

		// Gera ID do sujeito automático no HTTP POST.
		Resource subject = getModel().createResource(newSubjectURI(cls), cls);
		
		for (Statement statement : individualStatements) {
			getModel().add(subject, statement.getPredicate(), statement.getObject());
		}

		return Response.status(Status.CREATED).build();
	}

	/**
	 * 
	 * @param ontClass
	 * @param rdfID
	 * @param rdfXml
	 * @return
	 */
	@PUT
	@Path("{ontClass}/{rdfID}")
	public Response putIndividual(@PathParam("ontClass") String ontClass, @PathParam("rdfID") String rdfID, String rdfXml) {
	
		List<Statement> statements = OntologyConverter.getStatements(rdfXml);
		
		for (Statement statement : statements) {
			putProperty(ontClass, rdfID, statement.getPredicate().getLocalName(), rdfXml);
		}
	
		return Response.status(Status.CREATED).build();
	}

	/**
	 * 
	 * @param ontClass
	 * @param rdfID
	 * @return
	 */
	@DELETE
	@Path("{ontClass}/{rdfID}")
	public Response deleteIndividual(@PathParam("ontClass") String ontClass, @PathParam("rdfID") String rdfID) {
	
		if (!hasSerinAnnotation(lookup(ontClass), Serin.DELETE)) {
			return Response.status(Status.BAD_REQUEST).entity(SERIN_NOT_AVAILABLE).build();
		}
	
		String uriID = namespace() + rdfID;
		String deleteString = "DELETE WHERE {<" + uriID + "> ?p ?o}";
	
		UpdateRequest request = UpdateFactory.create(deleteString);
		UpdateAction.execute(request, getModel());
	
		return Response.status(Status.OK).build();
	}

	/**
	 * 
	 * 
	 * @param ontClass
	 * @param ontProperty
	 * @return
	 */
	private Response listProperty(String ontClass, OntProperty ontProperty) {
	
		if (!hasSerinAnnotation(ontProperty, Serin.GET)) {
			return Response.status(Status.BAD_REQUEST).entity(SERIN_NOT_AVAILABLE).build();
		}
	
		List<Statement> statements = getModel().listStatements((Resource) null, ontProperty, (RDFNode) null).toList();
		return Response.ok(new RDFOutput(getModel().getNsPrefixMap(), statements)).build();
	}

	/**
	 * 
	 * @param ontClass
	 * @param ontProperty
	 * @param rdfID
	 * @return
	 */
	@GET
	@Path("{ontClass}/{rdfID}/{ontProperty}")
	public Response getProperty(@PathParam("ontClass") String ontClass,
			@PathParam("ontProperty") String ontProperty, @PathParam("rdfID") String rdfID) {

		if (!checkRelationship(ontClass, rdfID)) {
			return Response.status(Status.BAD_REQUEST).entity("Individual is not from class " + ontClass).build();
		}
		
		if (!hasSerinAnnotation(lookup(ontProperty), Serin.GET)) {
			return Response.status(Status.BAD_REQUEST).entity(SERIN_NOT_AVAILABLE).build();
		}

		Individual individual = lookup(rdfID).asIndividual();

		if (individual == null) {
			return Response.status(Status.NOT_FOUND).entity(RESOURCE_NOT_FOUND).build();
		}

		List<Statement> statements = individual.listProperties(lookup(ontProperty).asProperty()).toList();

		return Response.ok(new RDFOutput(getModel().getNsPrefixMap(), statements)).build();
	}

	/**
	 * 
	 * @param ontClass
	 * @param rdfID
	 * @param ontProperty
	 * @param oldValue
	 * @param newValue
	 * @return
	 */
	@PUT
	@Path("{ontClass}/{rdfID}/{ontProperty}")
	public Response putProperty(@PathParam("ontClass") String ontClass, @PathParam("rdfID") String rdfID,
			@PathParam("ontProperty") String ontProperty, String rdfXml) {

		if (!checkRelationship(ontClass, rdfID, ontProperty)) {
			return Response.status(Status.BAD_REQUEST).entity("Individual is not from class " + ontClass +
					"or this class not domain the property " + ontProperty).build();
		}
		
		Property property = lookup(ontProperty).asProperty();
		
		Statement stmt = OntologyConverter.getStatement(property, rdfXml);
		
		// Se não existe a propriedade 'ontProperty' dentro de rdfXml		
		if (stmt == null) {
			return Response.status(Status.NOT_FOUND).build();
		}
		
		if (!hasSerinAnnotation(lookup(ontProperty), Serin.PUT)) {
			return Response.status(Status.BAD_REQUEST).entity(SERIN_NOT_AVAILABLE).build();
		}

		Individual ind = lookup(rdfID).asIndividual();
		Property prop  = lookup(ontProperty).asProperty();
		
		Statement indStmt = ind.getProperty(prop);
		
		if (indStmt != null) {
			indStmt.changeObject(stmt.getObject());	
		} else {
			getModel().add(ind, prop, stmt.getObject());
		}

		return Response.ok().build();
	}

	/**
	 * Método DELETE
	 * 
	 * @param ontClass
	 * @param rdfID
	 * @param ontProperty
	 * @return
	 */
	@DELETE
	@Path("{ontClass}/{rdfID}/{ontProperty}")
	public Response deleteProperty(@PathParam("ontClass") String ontClass,
			@PathParam("rdfID") String rdfID, @PathParam("ontProperty") String ontProperty) {

		if (!checkRelationship(ontClass, rdfID, ontProperty)) {
			return Response.status(Status.BAD_REQUEST).entity("Individual is not from class \"" + ontClass +
					"\" or this class not domain the property \"" + ontProperty + "\".").build();
		}
		
		if (!hasSerinAnnotation(lookup(ontProperty), Serin.DELETE)) {
			return Response.status(Status.BAD_REQUEST).entity(SERIN_NOT_AVAILABLE).build();
		}

		String uriID = namespace() + rdfID;
		String predURI = namespace() + ontProperty;

		String deleteString = "DELETE WHERE {<" + uriID + "> <" + predURI + "> ?o}";

		UpdateRequest request = UpdateFactory.create(deleteString);
		UpdateAction.execute(request, getModel());

		return Response.status(Status.OK).build();
	}

	/**
	 * 
	 * 
	 * @param resourceURI
	 * @param serinAnotationURI
	 * @return
	 */
	private boolean hasSerinAnnotation(OntResource resource, String serinAnotationURI) {
		
		if (resource == null) {
			return false;
		}
		
		if (serinAnotationURI == null) {
			return false;
		}

		Property serinAnot = getModel().getProperty(serinAnotationURI);

		if (resource.getProperty(serinAnot) != null) {
			return true;	
		}
	
		if (resource.isProperty()) {
			OntResource domain = resource.asProperty().getDomain();
			if (domain.getProperty(serinAnot) != null) {
				return true;
			}
		}
	
		return false;	
	}

	/**
	 * Verifica se a URL apresenta um par 'ontClass'/'rdfID' válido.
	 * 
	 * @param ontClass
	 * @param rdfID
	 * @return
	 */
	private boolean checkRelationship(String ontClass, String rdfID) {
		
		Individual individual = lookup(rdfID).asIndividual();
		Resource type = individual.getRDFType(true);
		return type.getLocalName().equals(ontClass);
	}

	private boolean checkRelationship(String ontClass, String rdfID, String ontProperty) {
		
		try {
			Individual individual = lookup(rdfID).asIndividual();
	    	Property property = lookup(ontProperty).asProperty();
	    	OntClass clazz = lookup(ontClass).asClass();
	    	
	    	Resource type = individual.getRDFType(true);
			
			boolean result = type.getLocalName().equals(ontClass);
			
			result &= !getModel().listStatements(property, RDFS.domain, clazz).toList().isEmpty();
			
			return result;
	    	
		} catch (NullPointerException e) {
			return false;
		}
	}

	private OntResource lookup(String ontResource) {
		// TODO [Hermano] Implementar para procurar 'ontProperty' em todos os namespaces.
		return getModel().getOntResource(namespace() + ontResource);
	}
}
