package br.unifor.mia.serin.client;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.util.GenericType;

import br.unifor.mia.serin.server.Serin;
import br.unifor.mia.serin.util.OntologyConverter;
import br.unifor.mia.serin.util.Triple;

import com.hp.hpl.jena.ontology.AnnotationProperty;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * Algumas restrições foram criadas quanto ao uso das anotações:
 * 
 * • A anotação get só pode ser definida para propriedades funcionais.
 * • Anotações não se aplicam a classes anônimas ou construídas a partir de expressões.
 * • Anotações não podem ser herdadas, ou seja, a classe-filha deve especificar os
 *   serviços web correspondentes.
 *   
 * @author Hermano
 */
public class SerinClient extends Serin {
	
	private final GenericType<Collection<Triple>> TRIPLES_TYPE = new GenericType<Collection<Triple>>(){};
	
	private String urlActiveOntology;
	
	private String urlOntology;
	
	private OntModel model;

	public SerinClient(String urlHost, String uriOntology) throws IOException {
		
		this.urlActiveOntology = urlHost + "/serin/" + uriOntology;
		
		String urlSerin = getClass().getClassLoader().getResource("serin.owl").toString();
		
		URL ontology = getClass().getClassLoader().getResource(uriOntology.substring(uriOntology.indexOf('/')+1));
		
		urlOntology = ontology.toString();
		
		model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		model.getDocumentManager().addAltEntry(Serin.NS, urlSerin);
		model.read(ontology.openStream(), null);
		
	}

	private boolean hasSerinAnnotation(OntResource ontClass, AnnotationProperty anonProp) {
		
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

	private boolean hasSerinAnnotation(Individual individual, AnnotationProperty anonProp) {

		if (individual == null) {
			return false;
		}

		if (anonProp == null) {
			return false;	
		}
		
		if (individual.getOntClass().getProperty(anonProp) != null) {
			
			Property anotation = individual.getOntClass().getProperty(anonProp).getPredicate();
			
			if (GET.equals(anotation) || PUT.equals(anotation) ||
				POST.equals(anotation) || DELETE.equals(anotation)||
				LIST.equals(anotation) ) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Método que insere um individuo na ontologia.
	 * 
	 * @param individual
	 * @return
	 * @throws Exception
	 */
	public boolean put(Individual individual) throws Exception {
	
		String urlSerinClass = urlActiveOntology +"/"+ individual.getOntClass().getLocalName();
		
		if (!hasSerinAnnotation(individual, POST)) {
			individual.remove();
			return false;
		}
	
		System.out.println("Invocando serviço web de inserção...");
		ClientRequest request = new ClientRequest(urlSerinClass);

		request.body(MediaType.TEXT_XML_TYPE, OntologyConverter.getRepresentation(individual), TRIPLES_TYPE);

		ClientResponse<GenericType<Collection<Triple>>> response = request.put(TRIPLES_TYPE);

		return response.getResponseStatus().equals(Status.CREATED);
	}
	
	/**
	 * Delete um individuo da ontologia.
	 * 
	 * @param individual
	 * @return
	 * @throws Exception
	 */
	public boolean delete(OntClass ontClass, String rdfID) throws Exception {

		if (!hasSerinAnnotation(ontClass, DELETE)) {
			return false;
		}
		
		String urlSerinIndividual = urlActiveOntology +"/"+ ontClass.getLocalName() + "/" + rdfID;
		
		System.out.println("Invocando serviço web de deleção...");
		ClientRequest request = new ClientRequest(urlSerinIndividual);

		ClientResponse<GenericType<Collection<Triple>>> response = request.delete(TRIPLES_TYPE);

		if (response.getResponseStatus().equals(Status.OK)) {
			return true;
		} else {
			return false;
		}
	}

	@SuppressWarnings("unchecked")
	public List<Individual> get(OntClass ontClass, String rdfID) throws Exception {

		if (!hasSerinAnnotation(ontClass, LIST)) {
			return null;
		}
		
		String urlSerinIndividual = urlActiveOntology +"/"+ ontClass.getLocalName() + "/" + rdfID;

		System.out.println("Invocando serviço web de busca...");
		ClientRequest request = new ClientRequest(urlSerinIndividual);
		
		ClientResponse<GenericType<Collection<Triple>>> response = request.get(TRIPLES_TYPE);

		if (response.getResponseStatus().equals(Status.OK)) {

			Collection<Triple> triples = (Collection<Triple>) response.getEntity();

			OntModel model = OntologyConverter.toOntology(triples);
			model.read(urlActiveOntology);
			
			List<Individual> result = new ArrayList<>();

			ExtendedIterator<Individual> iterator = model.listIndividuals();
					
			while (iterator.hasNext()) {
				result.add(iterator.next());
			}

			return result;
		}

		return null;
	}
	
	/**
	 * Método que recupera um conjunto de individuos de uma classe da ontologia.
	 * 
	 * @param ontClass 
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public List<Individual> list(OntClass ontClass) throws Exception {

		if (!hasSerinAnnotation(ontClass, LIST)) {
			return null;
		}

		String urlSerinClass = urlActiveOntology +"/"+ ontClass.getLocalName();
		
		System.out.println("Invocando serviço web de busca...");
		ClientRequest request = new ClientRequest(urlSerinClass);
		
		ClientResponse<GenericType<Collection<Triple>>> response = request.get(TRIPLES_TYPE);

		if (response.getResponseStatus().equals(Status.OK)) {

			Collection<Triple> triples = (Collection<Triple>) response.getEntity();

			OntModel model = OntologyConverter.toOntology(triples);
			model.read(urlOntology);
			
			List<Individual> result = new ArrayList<>();

			ExtendedIterator<Individual> iterator = model.listIndividuals();
					
			while (iterator.hasNext()) {
				result.add(iterator.next());
			}

			return result;
		}

		return null;
	}

	public OntModel getOntModel() {
		return model;
	}
}
