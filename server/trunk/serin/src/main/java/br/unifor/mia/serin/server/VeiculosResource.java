package br.unifor.mia.serin.server;

import static br.unifor.mia.serin.server.Serin.DELETE;
import static br.unifor.mia.serin.server.Serin.GET;
import static br.unifor.mia.serin.server.Serin.LIST;
import static br.unifor.mia.serin.server.Serin.POST;
import static br.unifor.mia.serin.server.Serin.PUT;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

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

import org.jboss.resteasy.annotations.providers.jaxb.Wrapped;

import br.unifor.mia.serin.util.Triple;

import com.hp.hpl.jena.ontology.AnnotationProperty;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.update.UpdateAction;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateRequest;
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
	
	private boolean hasSerinAnnotation(OntClass ontClass, AnnotationProperty anonProp) {
		
		if (ontClass == null) {
			return false;
		}

		if (anonProp == null) {
			return false;
		}

		if (ontClass.getProperty(anonProp) == null) {
			return false;
		}

		Property anotation = ontClass.getProperty(anonProp).getPredicate();
	
		if (GET.equals(anotation) || PUT.equals(anotation) ||
			POST.equals(anotation) || DELETE.equals(anotation)||
			LIST.equals(anotation) ) {
			return true;
		}

		return false;
	}
	
	@PUT
	@Wrapped(element = "triples")
	public Response putVeiculo(Collection<Triple> triples) {
		
		//TODO [Chamar método "hasSerinAnnotation"]
		/*if (!hasSerinAnnotation(individual.getOntClass(), PUT)) {
			individual.remove();
			return false;
		}*/
		
		String insertString = "INSERT DATA {";
		
		for (Triple triple : triples) {
			if (triple.getPred().equals(RDF.type.toString())) {
				insertString += "<"+triple.getSubj()+"> a <"+triple.getObj()+">.";
			} else {
				insertString += "<"+triple.getSubj()+"> <"+triple.getPred()+"> \""+triple.getObj()+"\".";	
			}
		}
		
		insertString += "}";
		
    	UpdateRequest request = UpdateFactory.create(insertString);
    	
    	UpdateAction.execute(request, model);

    	return Response.status(Status.CREATED).build();
	}
	
    @POST
    @Path("{rdfID}")
	public Response postVeiculo(@PathParam("ontClass") String ontClass,
			@PathParam("rdfID") String rdfID, Collection<Triple> triples) {

		//TODO [Chamar método "hasSerinAnnotation"]
    	
    	String uriID = Veiculo.NS + rdfID;   	
    	String deletePartString = "DELETE {<"+uriID+"> ?p ?o}";
    	
    	String wherePartString = "WHERE {<"+uriID+"> ?p ?o}";
    	
		String insertPartString = "INSERT {";

		for (Triple triple : triples) {
			if (triple.getPred().equals(RDF.type.toString())) {
				insertPartString += "<"+uriID+"> a <"+triple.getObj()+">.";
			} else {
				insertPartString += "<"+uriID+"> <"+triple.getPred()+"> \""+triple.getObj()+"\".";	
			}
		}
		
		insertPartString += "}";
		
    	UpdateRequest request = UpdateFactory.create(deletePartString + insertPartString + wherePartString);
    	
    	UpdateAction.execute(request, model);
    	
    	return Response.status(Status.CREATED).build();
	}
    
    @DELETE
    @Path("{rdfID}")
    public Response deleteVeiculo(@PathParam("ontClass") String ontClass, @PathParam("rdfID")String rdfID) {
    	
		//TODO [Chamar método "hasSerinAnnotation"]
		/*if (!hasSerinAnnotation(ontClass, DELETE)) {
			return false;
		}*/
		
    	String uriID = Veiculo.NS + rdfID;   	
    	String deleteString = "DELETE WHERE {<"+uriID+"> ?p ?o}";
    	
    	UpdateRequest request = UpdateFactory.create(deleteString);
    	UpdateAction.execute(request, model);

    	return Response.status(Status.OK).build();
    }
	
	@GET
	@Wrapped(element = "triples")
	@Produces(MediaType.TEXT_XML)
	public Collection<Triple> listVeiculo(@PathParam("ontClass") String ontClass) {

		//TODO [Chamar método "hasSerinAnnotation"]
		/*if (!hasSerinAnnotation(ontClass, LIST)) {
			return null;
		}*/
		
        String queryString = "SELECT * WHERE {?s a <"+ Veiculo.NS + ontClass +">. ?s ?p ?o.}";
        Query query = QueryFactory.create(queryString);
        
        // Create a single execution of this query, apply to a model
        // which is wrapped up as a Dataset
        QueryExecution qexec = QueryExecutionFactory.create(query, model);

        try {
        	Collection<Triple> result = new ArrayList<Triple>();
        	
        	ResultSet rs = qexec.execSelect();
        	
            // The order of results is undefined. 
            while (rs.hasNext()) {
            	QuerySolution rb = rs.nextSolution();
            	result.add(new Triple(rb.get("s").toString(), rb.get("p").toString(), rb.get("o").toString()));
            }
            return result;
        } finally {
            // QueryExecution objects should be closed to free any system resources
        	qexec.close();
        }
	}
	
	@GET
	@Wrapped(element = "triples")
	@Produces(MediaType.TEXT_XML)
	@Path("{rdfID}")
	public Collection<Triple> getVeiculo(@PathParam("ontClass") String ontClass, @PathParam("rdfID")String rdfID) {
		
		//TODO [Chamar método "hasSerinAnnotation"]
		/*if (!hasSerinAnnotation(ontClass, LIST)) {
			return null;
		}*/
		
		String uriID = Veiculo.NS + rdfID;
        String queryString = "SELECT * WHERE {<"+ uriID +"> ?pred ?obj}";
        Query query = QueryFactory.create(queryString);
        QueryExecution qexec = QueryExecutionFactory.create(query, model);

		try {
        	Collection<Triple> result = new ArrayList<Triple>();
        	ResultSet rs = qexec.execSelect();
            // The order of results is undefined. 
            while (rs.hasNext()) {
            	QuerySolution rb = rs.nextSolution();
    			result.add(new Triple(uriID, rb.get("pred").toString(), rb.get("obj").toString()));
            }
            return result;
        } finally {
            // QueryExecution objects should be closed to free any system resources
        	qexec.close();
        }
	}
}