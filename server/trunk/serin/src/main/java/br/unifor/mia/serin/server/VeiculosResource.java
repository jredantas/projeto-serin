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
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.update.UpdateAction;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateRequest;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.RDF;

@Path("/www.unifor.br/veiculo.owl/{ontClass}")
@Consumes(MediaType.TEXT_XML)
public class VeiculosResource {

	private static OntModel model;
	
	public VeiculosResource() {
		
		if (model == null) {
			String urlSerin = getClass().getClassLoader().getResource("serin.owl").toString().replace("vfs:", "file://");
			InputStream inVeiculo = getClass().getClassLoader().getResourceAsStream("veiculo.owl");
			
			model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
			model.getDocumentManager().addAltEntry(Serin.NS, urlSerin);
			model.read(inVeiculo, null);
			
			Individual individuo = model.createIndividual("http://www.unifor.br/veiculo.owl#Logan", Veiculo.VEICULO);
			individuo.setPropertyValue(Veiculo.MARCA, model.createLiteral("Renault"));
			individuo.setPropertyValue(Veiculo.MODELO, model.createLiteral("Logan"));
		}
	}
	
	private boolean hasSerinAnnotation(String classURI, String serinAnotationURI) {
		
		if (classURI == null) {
			return false;
		}

		if (serinAnotationURI == null) {
			return false;
		}

		OntClass ontClass = model.getOntClass(classURI);
		Property serinAnot = model.getProperty(serinAnotationURI);
		
		if (ontClass.getProperty(serinAnot) == null) {
			return false;
		}

		Property anotation = ontClass.getProperty(serinAnot).getPredicate();
	
		if (GET.equals(anotation) || PUT.equals(anotation) ||
			POST.equals(anotation) || DELETE.equals(anotation)||
			LIST.equals(anotation) ) {
			return true;
		}

		return false;
	}
	
	@PUT
	@Consumes(MediaType.TEXT_XML)
	public Response putVeiculo(String rdfXml) {
		
		Description individual = OntologyConverter.toObject(rdfXml).getDescriptions().get(0);
		
		if (!hasSerinAnnotation(individual.getType(), PUT.getURI())) {
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
	
    @POST
    @Path("{rdfID}")
	@Consumes(MediaType.TEXT_XML)
	public Response postVeiculo(@PathParam("ontClass") String ontClass, @PathParam("rdfID") String rdfID, String rdfXml) {

		if (!hasSerinAnnotation(Veiculo.NS + ontClass, POST.toString())) {
			return Response.status(Status.NOT_ACCEPTABLE).build();
		}
		
		HashMap<String, String> properties = OntologyConverter.toObject(rdfXml).getDescriptions().get(0).getProperties();
    	
		String uriID = Veiculo.NS + rdfID;
    	
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
    public Response deleteVeiculo(@PathParam("ontClass") String ontClass, @PathParam("rdfID")String rdfID) {
    	
		if (!hasSerinAnnotation(Veiculo.NS + ontClass, DELETE.toString())) {
			return Response.status(Status.NOT_ACCEPTABLE).build();
		}
		
    	String uriID = Veiculo.NS + rdfID;
    	String deleteString = "DELETE WHERE {<"+uriID+"> ?p ?o}";
    	
    	UpdateRequest request = UpdateFactory.create(deleteString);
    	UpdateAction.execute(request, model);

    	return Response.status(Status.OK).build();
    }
	
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response listVeiculo(@PathParam("ontClass") String ontClass) {

		if (!hasSerinAnnotation(Veiculo.NS + ontClass, LIST.toString())) {
			return Response.status(Status.NOT_ACCEPTABLE).build();
		}

        Resource cls = ResourceFactory.createResource(Veiculo.NS + ontClass);

        ExtendedIterator<Individual> list = model.listIndividuals(cls);

        List<Individual> individuals = new ArrayList<Individual>();

        while (list.hasNext()) {
        	individuals.add(list.next());
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
	
	@GET
	@Produces(MediaType.TEXT_XML)
	@Path("{rdfID}")
	public Response getVeiculo(@PathParam("ontClass") String ontClass, @PathParam("rdfID")String rdfID) {
		
		if (!hasSerinAnnotation(Veiculo.NS + ontClass, GET.toString())) {
			return Response.status(Status.NOT_ACCEPTABLE).build();
		}
		
		Individual individual = model.getIndividual(Veiculo.NS + rdfID);

		try {
			
			String result = OntologyConverter.toRDFXML(individual);
			return Response.ok(result).build();
			
		} catch (XMLStreamException e) {
			e.printStackTrace();
			return Response.serverError().build();
		} catch (IOException e) {
			e.printStackTrace();
			return Response.serverError().build();
		}
	}
}