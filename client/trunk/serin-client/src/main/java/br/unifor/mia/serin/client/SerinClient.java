package br.unifor.mia.serin.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.util.GenericType;

import br.unifor.mia.serin.Ontology;
import br.unifor.mia.serin.util.OntologyConverter;
import br.unifor.mia.serin.util.Triple;

import com.hp.hpl.jena.ontology.AnnotationProperty;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

public class SerinClient {

	public static final String URI = "http://www.unifor.br/serin.owl#";
	
	public final static String GET = URI + "get";
	public final static String PUT = URI + "put";
	public final static String POST = URI + "post";
	public final static String DELETE = URI + "delete";
	public final static String LIST = URI + "list";
	
	private final GenericType<Collection<Triple>> TRIPLES_TYPE = new GenericType<Collection<Triple>>(){};

	private final Ontology ontology;
	
	public SerinClient(final Ontology ontology) {
		this.ontology = ontology;
	}
	
	
	private boolean hasSerinAnnotation(OntResource ontClass, String anon) {
		
		if (ontClass == null) {
			return false;
		}
		
		AnnotationProperty anonProp = ontClass.getOntModel().getAnnotationProperty(anon);

		if (anonProp == null) {
			return false;
		}

		if (ontClass.getProperty(anonProp) == null) {
			return false;
		}

		String anotation = ontClass.getProperty(anonProp).getPredicate().getURI();
	
		if (GET.equals(anotation) || PUT.equals(anotation) ||
			POST.equals(anotation) || DELETE.equals(anotation)||
			LIST.equals(anotation) ) {
			return true;
		}

		return false;
	}

	private boolean hasSerinAnnotation(Individual individual, String anon) {

		if (individual == null) {
			return false;
		}

		AnnotationProperty anonProp = individual.getOntModel().getAnnotationProperty(anon);

		if (anonProp == null) {
			return false;	
		}
		
		if (individual.getOntClass().getProperty(anonProp) != null) {
			
			String anotation = individual.getOntClass().getProperty(anonProp).getPredicate().getURI();
			
			if (GET.equals(anotation) || PUT.equals(anotation) ||
				POST.equals(anotation) || DELETE.equals(anotation)||
				LIST.equals(anotation) ) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Método que altera uma propriedade na ontologia.
	 * 
	 * @param individual
	 * @return
	 * @throws Exception
	 */
	public boolean put(OntProperty property) throws Exception {		
		return false;
	}
	
	/**
	 * Método que insere um individuo na ontologia.
	 * 
	 * @param individual
	 * @return
	 * @throws Exception
	 */
	public boolean post(Individual individual) throws Exception {
	
		if (!hasSerinAnnotation(individual, POST)) {
			individual.remove();
			return false;
		}
	
		System.out.println("Invocando serviço web de inserção...");
		ClientRequest request = new ClientRequest(ontology.WEB_SERVICE());

		request.body(MediaType.TEXT_XML_TYPE, OntologyConverter.getRepresentation(individual), TRIPLES_TYPE);

		ClientResponse<GenericType<Collection<Triple>>> response = request.post(TRIPLES_TYPE);

		return response.getResponseStatus().equals(Status.CREATED);
	}
	
	/**
	 * Delete um individuo da ontologia.
	 * 
	 * @param individual
	 * @return
	 * @throws Exception
	 */
	public boolean delete(Individual individual) throws Exception {

		if (!hasSerinAnnotation(individual, DELETE)) {
			return false;
		}
		
		System.out.println("Invocando serviço web de deleção...");
		ClientRequest request = new ClientRequest(ontology.WEB_SERVICE());

		request.body(MediaType.TEXT_XML_TYPE, OntologyConverter.getRepresentation(individual), TRIPLES_TYPE);

		ClientResponse<GenericType<Collection<Triple>>> response = request.delete(TRIPLES_TYPE);

		if (response.getResponseStatus().equals(Status.OK)) {
			individual.remove();
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Método que recupera um conjunto de individuos de uma classe da ontologia.
	 * 
	 * @param ontClass 
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public List<Individual> list(OntResource ontClass) throws Exception {

		if (!hasSerinAnnotation(ontClass, LIST)) {
			return null;
		}

		System.out.println("Invocando serviço web de busca...");
		ClientRequest request = new ClientRequest(ontology.WEB_SERVICE());
		
		ClientResponse<GenericType<Collection<Triple>>> response = request.get(TRIPLES_TYPE);

		if (response.getResponseStatus().equals(Status.OK)) {

			Collection<Triple> triples = (Collection<Triple>) response.getEntity();
			
			OntologyConverter.toOntology(ontology.getModel(), triples);
			
			List<Individual> result = new ArrayList<>();
			
			ExtendedIterator<Individual> iterator = ontology.getModel().listIndividuals();
					
			while (iterator.hasNext()) {
				result.add(iterator.next());
			}

			return result;
		}

		return null;
	}
}
