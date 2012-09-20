package br.unifor.mia.serin.server;

import static br.unifor.mia.serin.server.Serin.DELETE;
import static br.unifor.mia.serin.server.Serin.GET;
import static br.unifor.mia.serin.server.Serin.LIST;
import static br.unifor.mia.serin.server.Serin.POST;
import static br.unifor.mia.serin.server.Serin.PUT;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
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
import javax.xml.stream.XMLStreamException;

import br.unifor.mia.serin.util.Description;
import br.unifor.mia.serin.util.OntologyConverter;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.update.UpdateAction;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateRequest;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.RDF;

@Path("/www.unifor.br/vehicle.owl/{ontClass}")
@Consumes(MediaType.TEXT_XML)
public class VehicleResource {

	private static OntModel model;
	
	public VehicleResource() {
		
		if (model == null) {
			InputStream inVeiculo = getClass().getClassLoader().getResourceAsStream("vehicle.owl");
			
			model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
			model.read(inVeiculo, null);
			
			Individual individuo = model.createIndividual("http://www.unifor.br/vehicle.owl#Logan", Vehicle.VEHICLE);
			individuo.setPropertyValue(Vehicle.BRAND, model.createLiteral("Renault"));
			individuo.setPropertyValue(Vehicle.MODEL, model.createLiteral("Logan"));
		}
	}
	
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
	
		if (GET.equals(anotation) || PUT.equals(anotation) ||
			POST.equals(anotation) || DELETE.equals(anotation)||
			LIST.equals(anotation) ) {
			return true;
		}

		return false;
	}
	
	@POST
	@Consumes(MediaType.TEXT_XML)
	public Response postIndividual(String rdfXml) {
		
		Description individual = OntologyConverter.toObject(rdfXml).getDescriptions().get(0);
		
		if (!hasSerinAnnotation(individual.getType(), POST.getURI())) {
			return Response.status(Status.NOT_ACCEPTABLE).build();
		}
		
		String insertString = "INSERT DATA {";
		
		for (String propertyURI : individual.getProperties().keySet()) {
			if (propertyURI.equals(RDF.type.toString())) {
				insertString += "<"+individual.getRdfID()+"> a <"+individual.getProperties().get(RDF.type.toString())+">.";
			} else {
				insertString += "<"+individual.getRdfID()+"> <"+propertyURI+"> \""+individual.getProperties().get(propertyURI)+"\".";	
			}
		}
		
		insertString += "}";
		
    	UpdateRequest request = UpdateFactory.create(insertString);
    	
    	UpdateAction.execute(request, model);

    	return Response.status(Status.CREATED).build();
	}
	
	@PUT
    @Path("{rdfID}")
	@Consumes(MediaType.TEXT_XML)
	public Response putIndividual(@PathParam("ontClass") String ontClass, @PathParam("rdfID") String rdfID, String rdfXml) {

		if (!hasSerinAnnotation(Vehicle.NS + ontClass, PUT.toString())) {
			return Response.status(Status.NOT_ACCEPTABLE).build();
		}
		
		HashMap<String, String> properties = OntologyConverter.toObject(rdfXml).getDescriptions().get(0).getProperties();
    	
		String uriID = Vehicle.NS + rdfID;
    	
		for (String property : properties.keySet()) {
			String deletePartString = "DELETE {<"+uriID+"> <"+ property +"> ?o}";
	    	String insertPartString = "INSERT {";
	    	
	    	if (property.equals(RDF.type.toString())) {
				insertPartString += "<"+uriID+"> a <"+properties.get(property)+">.";
			} else {
				insertPartString += "<"+uriID+"> <"+property+"> \""+properties.get(property)+"\".";	
			}
	    	
	    	insertPartString += "}";
	    	String wherePartString = "WHERE {<"+uriID+"> <"+ property +"> ?o}";
	    	
	    	UpdateRequest request = UpdateFactory.create(deletePartString + insertPartString + wherePartString);
	    	UpdateAction.execute(request, model);
		}
    	
    	return Response.status(Status.CREATED).build();
	}
    
    @DELETE
    @Path("{rdfID}")
    public Response deleteIndividual(@PathParam("ontClass") String ontClass, @PathParam("rdfID")String rdfID) {
    	
		if (!hasSerinAnnotation(Vehicle.NS + ontClass, DELETE.toString())) {
			return Response.status(Status.NOT_ACCEPTABLE).build();
		}
		
    	String uriID = Vehicle.NS + rdfID;
    	String deleteString = "DELETE WHERE {<"+uriID+"> ?p ?o}";
    	
    	UpdateRequest request = UpdateFactory.create(deleteString);
    	UpdateAction.execute(request, model);

    	return Response.status(Status.OK).build();
    }

	@GET
	@Produces(MediaType.TEXT_XML)
	public Response listIndividual(@PathParam("ontClass") String ontClass) {

		if (!hasSerinAnnotation(Vehicle.NS + ontClass, LIST.toString())) {
			return Response.status(Status.NOT_ACCEPTABLE).build();
		}

        Resource cls = ResourceFactory.createResource(Vehicle.NS + ontClass);

        ExtendedIterator<Individual> list = model.listIndividuals(cls);

        List<Individual> individuals = new ArrayList<Individual>();

        while (list.hasNext()) {
        	individuals.add(list.next());
        }
        
		if (individuals.isEmpty()) {
			return Response.status(Status.NOT_FOUND).build();
		}
        
		try {
			
			String result = OntologyConverter.toRDFXML(individuals.toArray(new Individual[individuals.size()]));
			return Response.ok(result).build();
			
		} catch (XMLStreamException e) {
			e.printStackTrace();
			return Response.serverError().build();
		} catch (IOException e) {
			e.printStackTrace();
			return Response.serverError().build();
		}
	}
	
	/**
	 * Trabalha tanto como "lIST Property" como "GET Individual"
	 * 
	 * @param ontClass
	 *            Classe a que o resource pertence.
	 * @param resourceID
	 *            identificação do resource, pode ser um individuo como uma
	 *            propriedade.
	 * @return
	 */
	@GET
	@Produces(MediaType.TEXT_XML)
	@Path("{resourceID}")
	public Response getResource(@PathParam("ontClass") String ontClass, @PathParam("resourceID")String resourceID) {
	
		OntResource resource = model.getOntResource(Vehicle.NS + resourceID);
	
		if (resource == null) {
			return Response.status(Status.NOT_FOUND).build();
		}
		
		if (resource.isProperty()) {
			if (!hasSerinAnnotation(resource.getURI(), GET.toString())) {
				return Response.status(Status.NOT_FOUND).build();
			}
		} else {
			if (!hasSerinAnnotation(Vehicle.NS + ontClass, GET.toString())) {
				return Response.status(Status.NOT_FOUND).build();
			}	
		}
	
		try {
			if (resource.isIndividual()) {
				String result = OntologyConverter.toRDFXML(resource.as(Individual.class));
				return Response.ok(result).build();				
			} else {
				
				String sparql = "select * where {?subject <"+resource.getURI()+"> ?object}";				
				QueryExecution qexec = QueryExecutionFactory.create(sparql, model);
				
				try {
		        	ResultSet rs = qexec.execSelect();
		        	return Response.ok(ResultSetFormatter.asXMLString(rs)).build();
		        } finally {
		        	qexec.close();
		        }
			}
	
		} catch (XMLStreamException e) {
			e.printStackTrace();
			return Response.serverError().build();
		} catch (IOException e) {
			e.printStackTrace();
			return Response.serverError().build();
		}
	}

	@POST
	@Path("{rdfID}/{ontProperty}/{value}")
	@Consumes(MediaType.TEXT_XML)
	public Response postProperty(@PathParam("ontClass") String ontClass,
			@PathParam("rdfID") String rdfID, @PathParam("ontProperty") String ontProperty,
			@PathParam("value") String value) {
		
		if (!hasSerinAnnotation(Vehicle.NS + ontProperty, POST.getURI())) {
			return Response.status(Status.NOT_FOUND).build();
		}
		
		String insertString = "INSERT DATA {";
		insertString += "<"+Vehicle.NS + rdfID+"> <"+Vehicle.NS + ontProperty+"> \""+value+"\".}";	
		
    	UpdateRequest request = UpdateFactory.create(insertString);
    	
    	UpdateAction.execute(request, model);

    	return Response.status(Status.CREATED).build();
	}

	@PUT
	@Path("{rdfID}/{ontProperty}/{value}")
	@Consumes(MediaType.TEXT_XML)
	public Response putProperty(@PathParam("ontClass") String ontClass,
			@PathParam("rdfID") String rdfID, @PathParam("ontProperty") String ontProperty,
			@PathParam("value") String oldValue, String newValue) {
		
		if (!hasSerinAnnotation(Vehicle.NS + ontProperty, PUT.getURI())) {
			return Response.status(Status.NOT_FOUND).build();
		}
		
		StmtIterator stmtIterator = model.getIndividual(Vehicle.NS + rdfID)
				.listProperties(model.getProperty(Vehicle.NS + ontProperty));
		
		Statement statement = null;
		boolean find = false;
		while (stmtIterator.hasNext()) {
			 statement = stmtIterator.next();
			 if (statement.getObject().toString().equals(oldValue)) {
				 find = true;
				 break;
			 }
		}
		if (statement != null & find) {
			statement.changeObject(newValue);
		} else {
			Response.status(Status.NOT_FOUND).build();
		}
		
    	return Response.ok().build();
	}

	@GET
	@Path("{rdfID}/{ontProperty}")
	@Produces(MediaType.TEXT_PLAIN)
	public Response getProperty(@PathParam("ontClass") String ontClass,
			@PathParam("ontProperty") String ontProperty,
			@PathParam("rdfID") String rdfID) {

		if (!hasSerinAnnotation(Vehicle.NS + ontProperty, GET.toString())) {
			return Response.status(Status.NOT_FOUND).build();
		}
		
		Individual individual = model.getIndividual(Vehicle.NS + rdfID);
	
		if (individual == null) {
			return Response.status(Status.NOT_FOUND).build();
		}

		StmtIterator stmtIterator = individual.listProperties(model.getProperty(Vehicle.NS + ontProperty));
		String result = "";
		
		while (stmtIterator.hasNext()) {
			 Statement statement = stmtIterator.next();
			 result += statement.getObject().toString() + ";";
		}

		return Response.ok(result).build();
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
	@Path("{rdfID}/{ontProperty}")
	public Response deleteAllProperty(@PathParam("ontClass") String ontClass,
			@PathParam("rdfID") String rdfID, @PathParam("ontProperty") String ontProperty) {
		
		if (!hasSerinAnnotation(Vehicle.NS + ontProperty, DELETE.toString())) {
			return Response.status(Status.NOT_FOUND).build();
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
	@Path("{rdfID}/{ontProperty}/{value}")
	public Response deleteOneProperty(@PathParam("ontClass") String ontClass,
			@PathParam("rdfID") String rdfID, @PathParam("ontProperty") String ontProperty,
			@PathParam("value") String value) {

		if (!hasSerinAnnotation(Vehicle.NS + ontProperty, DELETE.toString())) {
			return Response.status(Status.NOT_FOUND).build();
		}
		
    	String uriID = Vehicle.NS + rdfID;
    	String predURI = Vehicle.NS + ontProperty;
    	
    	String deleteString = "DELETE WHERE {<"+uriID+"> <"+ predURI +"> \""+ value +"\"}";
    	
    	UpdateRequest request = UpdateFactory.create(deleteString);
    	UpdateAction.execute(request, model);

    	return Response.status(Status.OK).build();
	}
}