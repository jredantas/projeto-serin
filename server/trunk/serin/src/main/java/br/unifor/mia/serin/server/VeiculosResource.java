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

import br.unifor.mia.serin.util.OntologyConverter;
import br.unifor.mia.serin.util.Triple;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

@Path("/veiculos")
@Consumes(MediaType.TEXT_XML)
public class VeiculosResource {

	private static OntModel model;
	
	static {
		model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		//model.getDocumentManager().addAltEntry(Veiculo.NS, URL.VEICULO);
		model.getDocumentManager().addAltEntry(Serin.NS, URL.SERIN);
		model.read(URL.VEICULO);
		
		Individual individuo = model.createIndividual("http://www.unifor.br/veiculo.owl#Logan", Veiculo.VEICULO);
		individuo.setPropertyValue(Veiculo.MARCA, model.createLiteral("Renault"));
		individuo.setPropertyValue(Veiculo.MODELO, model.createLiteral("Logan"));
	}
	
    @POST
	public Response postVeiculo(Collection<Triple> triples) throws JAXBException {
    	
    	printXML(triples);
    	
    	model.add(OntologyConverter.toOntology(triples));
    	
    	return Response.status(Status.CREATED).build();
	}
    
    @DELETE
    public Response deleteVeiculo(Collection<Triple> triples) {
    	
    	model.add(OntologyConverter.toOntology(triples));

    	ExtendedIterator<Individual> individuals = model.listIndividuals();
    	
    	while (individuals.hasNext())
    		individuals.next().remove();
    	
    	return Response.status(Status.OK).build();
    }
	
	@GET
	@Wrapped(element = "triples")
	@Produces(MediaType.TEXT_XML)
	public Collection<Triple> listVeiculo() {
		
		Collection<Triple> result = new ArrayList<Triple>();
		
		ExtendedIterator<Individual> itr = model.listIndividuals();
		
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