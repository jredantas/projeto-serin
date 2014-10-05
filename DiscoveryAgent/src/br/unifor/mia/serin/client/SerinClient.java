package br.unifor.mia.serin.client;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;

import br.unifor.mia.serin.util.OntologyConverter;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;

/**
 * Algumas restrições foram criadas quanto ao uso das anotações:
 * 
 * • A anotação get só pode ser definida para propriedades funcionais.
 * • Anotações não se aplicam a classes anônimas ou construídas a partir de expressões.
 * • Anotações não podem ser herdadas, ou seja, a classe-filha deve especificar os
 *   serviços web correspondentes.
 *   
 * @author Hermano A. Lira
 * @see http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html
 */
public class SerinClient {
	
	private String urlActiveOntology;

	private String urlHost;

	public String getUrlHost() {
		return urlHost;
	}

	public void setUrlHost(String urlHost) {
		this.urlHost = urlHost;
	}

	public String getUrlActiveOntology() {
		return urlActiveOntology;
	}

	public void setUrlActiveOntology(String urlActiveOntology) {
		this.urlActiveOntology = urlActiveOntology;
	}

	public SerinClient(String urlHost, String uriOntology) throws IOException {
		this.urlActiveOntology = urlHost + "/" + uriOntology;	
	}

	public SerinClient(String urlHost) throws IOException {
		this.urlHost = urlHost;	
	}


	/**
	 * Método que insere um individuo na ontologia.
	 * 
	 * @param individual
	 * @return
	 * @throws Exception
	 */
	public boolean post(Individual individual) throws Exception{
		
		String rdfXml = OntologyConverter.toRDFXML(individual);
		
		String urlSerinClass = urlActiveOntology +"/"+ individual.getOntClass().getLocalName();
	
		System.out.println("Invocando serviço web de inserção...");
		ClientRequest request = new ClientRequest(urlSerinClass);

		request.body(MediaType.APPLICATION_XML, rdfXml);

		ClientResponse<String> response = request.post(String.class);

		return response.getResponseStatus().equals(Status.CREATED);
	}

	/**
	 * Método que atualiza um individuo na ontologia.
	 * 
	 * @param individual
	 * @return
	 * @throws Exception
	 */
	public boolean put(Individual individual) throws Exception {

		String rdfXml = OntologyConverter.toRDFXML(individual);
		
		String urlSerinIndividual =
				urlActiveOntology +"/"+ individual.getOntClass().getLocalName() + "/" + individual.getURI().split("#")[1];
	
		System.out.println("Invocando serviço web de atualização...");
		ClientRequest request = new ClientRequest(urlSerinIndividual);

		request.body(MediaType.APPLICATION_XML, rdfXml);

		ClientResponse<String> response = request.put(String.class);

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
		
		String urlSerinIndividual = urlActiveOntology +"/"+ ontClass.getLocalName() + "/" + rdfID;
		
		System.out.println("Invocando serviço web de deleção...");
		ClientRequest request = new ClientRequest(urlSerinIndividual);

		ClientResponse<String> response = request.delete(String.class);

		if (response.getResponseStatus().equals(Status.OK)) {
			return true;
		} else {
			return false;
		}
	}

	public Individual get(OntClass ontClass, String rdfID) throws Exception {
		
		String urlSerinIndividual = urlActiveOntology +"/"+ ontClass.getLocalName() + "/" + rdfID;

		System.out.println("Invocando serviço web de busca...");
		ClientRequest request = new ClientRequest(urlSerinIndividual);
		
		ClientResponse<String> response = request.get(String.class);

		if (response.getResponseStatus().equals(Status.OK)) {

			String rdfXml = (String) response.getEntity();

			List<Individual> individuals = OntologyConverter.toObject(ontClass, rdfXml);

			return individuals.size() > 0 ? individuals.get(0) : null;
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
	public List<Individual> list(OntClass ontClass) throws Exception {

		String urlSerinClass = urlActiveOntology +"/"+ ontClass.getLocalName();
		
		System.out.println("Invocando serviço web de busca...");
		ClientRequest request = new ClientRequest(urlSerinClass);
		
		ClientResponse<String> response = request.get(String.class);

		if (response.getResponseStatus().equals(Status.OK)) {

			String rdfXml = (String) response.getEntity();

			return OntologyConverter.toObject(ontClass, rdfXml);
		}

		return null;
	}
}
