package br.unifor.mia.serin.server;

import java.util.ArrayList;
import java.util.Collection;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.jboss.resteasy.annotations.providers.jaxb.Wrapped;

import br.unifor.mia.serin.Ontology;
import br.unifor.mia.serin.VeiculoOntology;
import br.unifor.mia.serin.util.OntologyConverter;
import br.unifor.mia.serin.util.Triple;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

@Path("/veiculos")
@Consumes(MediaType.TEXT_XML)
public class VeiculosResource {

	private static Ontology ontology = new VeiculoOntology();
	
	public VeiculosResource() {
		ontology.createIndividual("http://www.unifor.br/veiculo.owl#Logan", "Renault", "Logan");
	}
	
    @POST
	public Response postVeiculo(Collection<Triple> triples) throws JAXBException {
    	
    	printXML(triples);
    	
    	OntologyConverter.toOntology(ontology.getModel(), triples);
    	
    	return Response.status(Status.CREATED).build();
	}
    
    @DELETE
    public Response deleteVeiculo(Collection<Triple> triples) {
    	
    	Collection<Individual> individuals = OntologyConverter.toOntology(ontology.getModel(), triples);
    	
    	for (Individual individual : individuals) {
    		individual.remove();
    	}
    	
    	return Response.status(Status.OK).build();
    }
	
	@GET
	@Wrapped(element = "triples")
	@Produces(MediaType.TEXT_XML)
	public Collection<Triple> listVeiculo() {
		
		Collection<Triple> result = new ArrayList<Triple>();
		
		ExtendedIterator<Individual> itr = ontology.getModel().listIndividuals();
		
		while (itr.hasNext()) {
			Individual individual = itr.next();
			result.addAll(OntologyConverter.getRepresentation(individual));
		}
		
		return result;
	}

	private void printXML(Collection<Triple> triples)  throws JAXBException {
		JAXBContext context = JAXBContext.newInstance(Triple.class);
		Marshaller m = context.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		for (Triple triple : triples) {
			m.marshal(triple, System.out);
		}
	}
}