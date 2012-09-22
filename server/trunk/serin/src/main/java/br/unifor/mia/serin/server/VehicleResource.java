package br.unifor.mia.serin.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import br.unifor.mia.serin.util.OntologyConverter;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.sparql.util.NodeFactory;
import com.hp.hpl.jena.update.UpdateAction;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateRequest;

@Path("/www.unifor.br/vehicle.owl")
@Consumes(MediaType.APPLICATION_XML)
@Produces(MediaType.APPLICATION_XML)
public class VehicleResource {

	/**
	 * 
	 */
	private static OntModel model;
	
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
	public VehicleResource() {
		
		if (model == null) {
			InputStream inVeiculo = getClass().getClassLoader().getResourceAsStream("vehicle.owl");
			
			model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
			model.read(inVeiculo, null);
			
			Individual individuo = model.createIndividual("http://www.unifor.br/vehicle.owl#Logan", Vehicle.VEHICLE);
			individuo.setPropertyValue(Vehicle.BRAND, model.createTypedLiteral("Renault"));
			individuo.setPropertyValue(Vehicle.MODEL, model.createTypedLiteral("Logan"));
		}
	}
	
	/**
	 * 
	 * 
	 * @param resourceURI
	 * @param serinAnotationURI
	 * @return
	 */
	private boolean hasSerinAnnotation(String resourceURI, String serinAnotationURI) {
		
		if (resourceURI == null) {
			return false;
		}

		if (serinAnotationURI == null) {
			return false;
		}

		OntResource resource = model.getOntResource(resourceURI);
		Property serinAnot = model.getProperty(serinAnotationURI);
		
		if (resource == null) {
			return false;
		}
		
		if (resource.getProperty(serinAnot) == null) {
			return false;
		}

		Property anotation = resource.getProperty(serinAnot).getPredicate();
	
		if (anotation != null) {
			return true;
		}

		return false;
	}
	
	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	@GET
	public String getOntology() throws IOException {
		
		InputStream inVeiculo = getClass().getClassLoader().getResourceAsStream("vehicle.owl");

		BufferedReader stream = new BufferedReader(new InputStreamReader(inVeiculo));
		
		StringBuilder ontology = new StringBuilder();
		
		String currentLine = stream.readLine();
		
		while(currentLine != null) {
			ontology.append(currentLine);
			currentLine = stream.readLine();
		}
		
		return ontology.toString();
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
		
		Resource cls = model.getResource(Vehicle.NS + ontClass);
		
		Individual individual = OntologyConverter.toObject(cls, rdfXml).get(0);
		
		if (!hasSerinAnnotation(individual.getRDFType().getURI(), Serin.POST)) {
			return Response.status(Status.BAD_REQUEST).entity(SERIN_NOT_AVAILABLE).build();
		}
		
		for (Statement stmt : individual.listProperties().toList()) {
			model.add(stmt);
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

		if (!hasSerinAnnotation(Vehicle.NS + ontClass, Serin.PUT)) {
			return Response.status(Status.BAD_REQUEST).entity(SERIN_NOT_AVAILABLE).build();
		}
		
		Resource cls = model.getResource(Vehicle.NS + ontClass);
		
		List<Statement> properties = OntologyConverter.toObject(cls, rdfXml).get(0).listProperties().toList();
    	
		String uriID = Vehicle.NS + rdfID;
		
		Individual individual = model.getIndividual(uriID);
		
		if (individual == null) {
			individual = model.createIndividual(uriID, cls);
		}
		
		for (Statement stmt : properties) {
			individual.addProperty(stmt.getPredicate(), stmt.getObject());
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
    public Response deleteIndividual(@PathParam("ontClass") String ontClass, @PathParam("rdfID")String rdfID) {
    	
		if (!hasSerinAnnotation(Vehicle.NS + ontClass, Serin.DELETE)) {
			return Response.status(Status.BAD_REQUEST).entity(SERIN_NOT_AVAILABLE).build();
		}
		
    	String uriID = Vehicle.NS + rdfID;
    	String deleteString = "DELETE WHERE {<"+uriID+"> ?p ?o}";
    	
    	UpdateRequest request = UpdateFactory.create(deleteString);
    	UpdateAction.execute(request, model);

    	return Response.status(Status.OK).build();
    }

    /**
     * 
     * @param ontClass
     * @return
     */
	@GET
	@Path("{ontClass}")
	public Response listIndividual(@PathParam("ontClass") String ontClass) {

		if (!hasSerinAnnotation(Vehicle.NS + ontClass, Serin.LIST)) {
			return Response.status(Status.BAD_REQUEST).entity(SERIN_NOT_AVAILABLE).build();
		}

        Resource cls = ResourceFactory.createResource(Vehicle.NS + ontClass);

        List<Individual> individuals = model.listIndividuals(cls).toList();
        
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
	public Response getResource(@PathParam("ontClass") String ontClass, @PathParam("resourceID")String resourceID) {
	
		OntResource resource = model.getOntResource(Vehicle.NS + resourceID);
	
		if (resource == null) {
			return Response.status(Status.NOT_FOUND).entity(RESOURCE_NOT_FOUND).build();
		}
		
		if (resource.isProperty()) {
			return listProperty(ontClass, resource.asProperty());
		} else {
			return getIndividual(ontClass, resource.asIndividual());	
		}
	}

	/**
	 * 
	 * 
	 * @param ontClass
	 * @param individual
	 * @return
	 */
	private Response getIndividual(String ontClass, Individual individual) {
		
		if (!hasSerinAnnotation(Vehicle.NS + ontClass, Serin.GET)) {
			return Response.status(Status.BAD_REQUEST).entity(SERIN_NOT_AVAILABLE).build();
		}
		
		try {
			String result = OntologyConverter.toRDFXML(individual);
			return Response.ok(result).build();
		} catch (IOException e) {
			return Response.status(Status.NOT_FOUND).entity(RESOURCE_NOT_FOUND).build();
		}
	}

	/**
	 * 
	 * 
	 * @param ontClass
	 * @param ontProperty
	 * @return
	 */
	private Response listProperty(String ontClass, OntProperty ontProperty) {
		
		if (!hasSerinAnnotation(ontProperty.getURI(), Serin.GET)) {
			return Response.status(Status.BAD_REQUEST).entity(SERIN_NOT_AVAILABLE).build();
		}
		
		List<Statement> statements =
				model.listStatements((Resource)null, ontProperty, (RDFNode)null).toList();
		return Response.ok(new RDFOutput(model.getNsPrefixMap(), statements)).build();
	}

	/**
	 * 
	 * @param ontClass
	 * @param rdfID
	 * @param ontProperty
	 * @param value
	 * @return
	 */
	@POST
	@Path("{ontClass}/{rdfID}/{ontProperty}/{value}")
	public Response postProperty(@PathParam("ontClass") String ontClass,
			@PathParam("rdfID") String rdfID, @PathParam("ontProperty") String ontProperty,
			@PathParam("value") String value) {
		
		if (!hasSerinAnnotation(Vehicle.NS + ontProperty, Serin.POST)) {
			return Response.status(Status.BAD_REQUEST).entity(SERIN_NOT_AVAILABLE).build();
		}
		
		String insertString = "INSERT DATA {";
		insertString += "<"+Vehicle.NS + rdfID+"> <"+Vehicle.NS + ontProperty+"> \""+value+"\".}";	
		
    	UpdateRequest request = UpdateFactory.create(insertString);
    	
    	UpdateAction.execute(request, model);

    	return Response.status(Status.CREATED).build();
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
	@Path("{ontClass}/{rdfID}/{ontProperty}/{value}")
	public Response putProperty(@PathParam("ontClass") String ontClass,
			@PathParam("rdfID") String rdfID, @PathParam("ontProperty") String ontProperty,
			@PathParam("value") String oldValue, String newValue) {
		
		if (!hasSerinAnnotation(Vehicle.NS + ontProperty, Serin.PUT)) {
			return Response.status(Status.BAD_REQUEST).entity(SERIN_NOT_AVAILABLE).build();
		}
		
		List<Statement> statements = model.getIndividual(Vehicle.NS + rdfID)
				.listProperties(model.getProperty(Vehicle.NS + ontProperty))
				.toList();

		boolean find = false;
		for (Statement stmt : statements) {
			 if (stmt.getObject().asNode().getLiteralValue().equals(oldValue)) {
				stmt.changeObject(model.asRDFNode(NodeFactory
						.createLiteralNode(newValue, null, stmt.getObject()
								.asNode().getLiteralDatatypeURI())));
				 find = true;
				 break;
			 }
		}
		
		return find ? Response.ok().build() : Response.status(Status.NOT_FOUND).entity(RESOURCE_NOT_FOUND).build();
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
			@PathParam("ontProperty") String ontProperty,
			@PathParam("rdfID") String rdfID) {

		if (!hasSerinAnnotation(Vehicle.NS + ontProperty, Serin.GET)) {
			return Response.status(Status.BAD_REQUEST).entity(SERIN_NOT_AVAILABLE).build();
		}
		
		Individual individual = model.getIndividual(Vehicle.NS + rdfID);
	
		if (individual == null) {
			return Response.status(Status.NOT_FOUND).entity(RESOURCE_NOT_FOUND).build();
		}

		List<Statement> statements =
				individual.listProperties(model.getProperty(Vehicle.NS + ontProperty)).toList();

		return Response.ok(new RDFOutput(model.getNsPrefixMap(), statements)).build();
	}

	/**
	 * Método DELETE ALL
	 * 
	 * @param ontClass
	 * @param rdfID
	 * @param ontProperty
	 * @return
	 */
	@DELETE
	@Path("{ontClass}/{rdfID}/{ontProperty}")
	public Response deleteAllProperty(@PathParam("ontClass") String ontClass,
			@PathParam("rdfID") String rdfID, @PathParam("ontProperty") String ontProperty) {
		
		if (!hasSerinAnnotation(Vehicle.NS + ontProperty, Serin.DELETE)) {
			return Response.status(Status.BAD_REQUEST).entity(SERIN_NOT_AVAILABLE).build();
		}
		
    	String uriID = Vehicle.NS + rdfID;
    	String predURI = Vehicle.NS + ontProperty;
    	
    	String deleteString = "DELETE WHERE {<"+uriID+"> <"+ predURI +"> ?o}";
    	
    	UpdateRequest request = UpdateFactory.create(deleteString);
    	UpdateAction.execute(request, model);

    	return Response.status(Status.OK).build();
		
	}	
	
	/**
	 * Método DELETE ONE
	 * 
	 * @param ontClass
	 * @param rdfID
	 * @param ontProperty
	 * @return
	 */
	@DELETE
	@Path("{ontClass}/{rdfID}/{ontProperty}/{value}")
	public Response deleteOneProperty(@PathParam("ontClass") String ontClass,
			@PathParam("rdfID") String rdfID, @PathParam("ontProperty") String ontProperty,
			@PathParam("value") String value) {

		if (!hasSerinAnnotation(Vehicle.NS + ontProperty, Serin.DELETE)) {
			return Response.status(Status.BAD_REQUEST).entity(SERIN_NOT_AVAILABLE).build();
		}
		
    	String uriID = Vehicle.NS + rdfID;
    	String predURI = Vehicle.NS + ontProperty;
    	
    	String deleteString = "DELETE WHERE {<"+uriID+"> <"+ predURI +"> \""+ value +"\"}";
    	
    	UpdateRequest request = UpdateFactory.create(deleteString);
    	UpdateAction.execute(request, model);

    	return Response.status(Status.OK).build();
	}
}