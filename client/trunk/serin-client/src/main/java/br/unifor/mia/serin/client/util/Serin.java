package br.unifor.mia.serin.client.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.util.GenericType;

import br.unifor.mia.Veiculo;

import com.hp.hpl.jena.ontology.AnnotationProperty;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class Serin {

	public static final String SERIN_ONTOLOGY_URI = "http://www.unifor.br/serin.owl#";

	public final static String GET = SERIN_ONTOLOGY_URI + "get";
	public final static String PUT = SERIN_ONTOLOGY_URI + "put";
	public final static String POST = SERIN_ONTOLOGY_URI + "post";
	public final static String DELETE = SERIN_ONTOLOGY_URI + "delete";
	public final static String LIST = SERIN_ONTOLOGY_URI + "list";

	// TODO [REMOVER DEPENDENCIA]
	private final static String SERVICO_WEB = "http://localhost:8080/serin/ws/veiculos";

	private static boolean existActivelowlAnnotation(Individual individual, Property prop, String anon) {

		// validando se existe a anota��o no individuo...
		if (individual == null) {
			return false;
		}

		/*
		 * Cria referencias para as propriedades para saber se elas existem
		 * na propriedade corrente.
		 */
		AnnotationProperty anonProp = individual.getOntModel().getAnnotationProperty(anon);
		if (individual.getOntClass().getProperty(anonProp) != null) {
			String anotation = individual.getOntClass().getProperty(anonProp).getPredicate().getURI();
			/*
			 * Existe alguma anota��o??
			 */
			if (GET.equals(anotation) || PUT.equals(anotation) || POST.equals(anotation)
			 || DELETE.equals(anotation)|| LIST.equals(anotation) ) {
				return true;
			}
		}

		// validando se existe a anota��o na propriedade...
		if (prop != null
				&& ((OntProperty) prop.as(OntProperty.class))
						.isFunctionalProperty()) {

			OntProperty ontProp = (OntProperty) prop.as(OntProperty.class);

			/*
			 * Cria referencias para as propriedades para saber se elas existem
			 * na propriedade corrente.
			 */
			Property annotation = ontProp.getOntModel().getProperty(anon);

			/*
			 * Existe alguma anota��o?
			 */
			if (annotation != null) {
				return true;
			}
		}
		return false;
	}

	/**
	 * M�todo que recupera o valor de uma propriedade de um individuo de
	 * ontologia...
	 * @param model 
	 * 
	 * @param individual
	 * @param property
	 * @return
	 * @throws Exception
	 */
	public static List<Statement> list(OntModel m) throws Exception {

		// verificando se existe alguma anota��o na propriedade solicitada...
		if (existActivelowlAnnotation(null, null, GET)) {

			System.out.println("Invocando servi�o web de busca...");
			ClientRequest request = new ClientRequest(SERVICO_WEB);

			ClientResponse<GenericType<Collection<Veiculo>>> response
				= request.get(new GenericType<Collection<Veiculo>>() {});

			Collection<Veiculo> veiculos = (Collection<Veiculo>) response.getEntity();

			if (response.getResponseStatus().equals(Status.OK)) {
				
				List<Statement> result = new ArrayList<Statement>();
				
				StmtIterator iterator = m.listStatements(null, m.getAnnotationProperty(GET), (RDFNode) null);
				while (iterator.hasNext()) {
					result.add(iterator.nextStatement());
				}
	
				return result; //OntologyConverter.toOntology(veiculos, m, null);
			}
		}

		//TODO CODIGO TEMPORARIO REMOVE-LO DAKI
		List<Statement> result = new ArrayList<Statement>();
		
		StmtIterator iterator = m.listStatements(null, m.getAnnotationProperty(GET), (RDFNode) null);
		while (iterator.hasNext()) {
			result.add(iterator.nextStatement());
		}

		return result;
		//TODO CODIGO TEMPORARIO REMOVE-LO DAKI
		//return null;
	}

	/**
	 * M�todo que insere um individuo de ontologia ou altera uma propriedade...
	 * 
	 * @param individual
	 * @param property
	 * @return
	 * @throws Exception
	 */
	public static boolean post(Individual individual) throws Exception {

		// verificando se existe alguma anota��o na propriedade solicitada...
		if (existActivelowlAnnotation(individual, null, POST)) {

			// Existe...
			OntModel m = individual.getOntModel();

			System.out.println("Invocando servi�o web de inser��o...");

			ClientRequest request = new ClientRequest(SERVICO_WEB);

			// We're posting XML and a JAXB object
			request.body(MediaType.TEXT_XML, OntologyConverter.getRepresentation(individual));

			ClientResponse<Veiculo> response = request.post(Veiculo.class);

			return response.getResponseStatus().equals(Status.CREATED);
		}
		individual.remove();
		return false;
	}

}
