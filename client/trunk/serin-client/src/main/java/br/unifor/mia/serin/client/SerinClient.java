package br.unifor.mia.serin.client;

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
import com.hp.hpl.jena.ontology.OntResource;
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
	
	private String urlSemanticWebService;
	
	// TODO [Analisar se passar a classe Veiculo como parametro é uma boa ideia]
	public SerinClient(String urlSemanticWebService) {
		this.urlSemanticWebService = urlSemanticWebService;
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
	
	private String getWebService(OntResource ontResource, AnnotationProperty anonProp) {
		
		if (ontResource == null) {
			return null;
		}

		if (anonProp == null) {
			return null;	
		}
		
		if (ontResource.isIndividual() && ontResource.asIndividual().getOntClass().getProperty(anonProp) != null) {
			
			Property anotation = ontResource.asIndividual().getOntClass().getProperty(anonProp).getPredicate();
			
			if (GET.equals(anotation) || PUT.equals(anotation) ||
				POST.equals(anotation) || DELETE.equals(anotation)||
				LIST.equals(anotation) ) {
				return ontResource.asIndividual().getOntClass().getProperty(anonProp).getObject().toString();
			}
		}
		
		if (ontResource.isClass() && ontResource.asClass().getProperty(anonProp) != null) {
			
			Property anotation = ontResource.asClass().getProperty(anonProp).getPredicate();
			
			if (GET.equals(anotation) || PUT.equals(anotation) ||
				POST.equals(anotation) || DELETE.equals(anotation)||
				LIST.equals(anotation) ) {
				return ontResource.asClass().getProperty(anonProp).getObject().toString();
			}
		}
		

		return null;
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
		ClientRequest request = new ClientRequest(getWebService(individual, POST));

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
		ClientRequest request = new ClientRequest(getWebService(individual, DELETE));

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
	public List<Individual> list(OntClass ontClass) throws Exception {

		if (!hasSerinAnnotation(ontClass, LIST)) {
			return null;
		}

		System.out.println("Invocando serviço web de busca...");
		ClientRequest request = new ClientRequest(getWebService(ontClass, LIST));
		
		ClientResponse<GenericType<Collection<Triple>>> response = request.get(TRIPLES_TYPE);

		if (response.getResponseStatus().equals(Status.OK)) {

			Collection<Triple> triples = (Collection<Triple>) response.getEntity();

			OntModel model = OntologyConverter.toOntology(triples);
			model.read(urlSemanticWebService);
			
			List<Individual> result = new ArrayList<>();

			ExtendedIterator<Individual> iterator = model.listIndividuals();
					
			while (iterator.hasNext()) {
				result.add(iterator.next());
			}

			return result;
		}

		return null;
	}
}
