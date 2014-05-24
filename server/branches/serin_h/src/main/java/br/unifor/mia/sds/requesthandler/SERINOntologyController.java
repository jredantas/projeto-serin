package br.unifor.mia.sds.requesthandler;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.vocabulary.RDFS;

public class SERINOntologyController {
	
	/**
	 * Mapa onde são guardados todas as interfaces SERIN.
	 * Essa Classe pode gerenciar várias interfaces SERIN ao mesmo tempo.
	 */
	private static Map<String, OntModel> ontologyMap = new HashMap<String, OntModel>();
	
	private String urlOfInterface;
	
	private String[] urlElement = new String[5];

	
	public SERINOntologyController(String urlOfInterface) {
		this.urlOfInterface = urlOfInterface;
		
		StringTokenizer st = new StringTokenizer(urlOfInterface, "/");
		
		urlElement[0] = st.nextToken(); // http:
		urlElement[1] = st.nextToken(); // www.activeontology.com.br
		urlElement[2] = st.nextToken(); // serin
		urlElement[3] = st.nextToken(); // www.unifor.br
		urlElement[4] = st.nextToken(); // clinic.owl
	}
	
	private String getSchema() {
		return urlElement[0];
	}
	
	private String getHostOfInterface() {
		return urlElement[1];
	}
	
	private String getEndpointOfInterface() {
		return urlElement[2];
	}
	
	private String getBaseUriOfInterface() {
		return urlElement[3];
	}
	
	private String getInterfaceOWLFilename() {
		return urlElement[4];
	}
	
	private String getNamespace() {
		// Exemplo: http://www.unifor.br/clinic.owl#
		return getSchema() + "//" + getBaseUriOfInterface() + "/" + getInterfaceOWLFilename() + "#";
	}
	
	
	private boolean hasSerinAnnotation(String className, String annotationURI) throws SERINMalFormedException {
	
		Property serinAnot = getOntModelOfInterface().getProperty(annotationURI);
	
		if (lookup(className).getProperty(serinAnot) != null) {
			return true;
		}
	
		return false;
	}

	/**
	 * Gera um objeto 'OntModel' com a ontologia descrita na interface SERIN.
	 * 
	 * @param urlOfInterface O endereço (a URL) onde a interface SERIN está publicada na Web.
	 * 
	 * @return
	 */
	public OntModel getOntModelOfInterface() {

		if (ontologyMap.get(urlOfInterface) == null) {

			ontologyMap.put(urlOfInterface, ModelFactory.createOntologyModel());

			ontologyMap.get(urlOfInterface).read(urlOfInterface);
		}
		
		return ontologyMap.get(urlOfInterface);
	}
	
	
	/**
	 * 
	 * 
	 * @param resourceName
	 * @return
	 * @throws SERINMalFormedException 
	 */
	public OntResource lookup(String resourceName) throws SERINMalFormedException {
	
		OntModel model = getOntModelOfInterface();
		
		// Tenta recuperar conceitos da ontologia
		OntResource resource = model.getOntResource(getNamespace() + resourceName);
		
		// Senão localizar é por que esse recurso não está definido na interface SERIN
		if (resource == null) {
			throw new SERINMalFormedException(SDSErrorMessage.CONCEPT_NOT_DEFINED);
		}
		
		return resource;	
	}

	public void checkPermission(String className, String annotation)
			throws AnnotationlessException, SERINMalFormedException {
		
		if (!hasSerinAnnotation(className, annotation)) {
			// Se a classe não possui a anotação indicada
			throw new AnnotationlessException(SDSErrorMessage.SERIN_ANNOTATION_NOT_AVAILABLE);
		}
	}

	public void checkDomain(String propertyName, String className) throws SERINMalFormedException {

		OntModel model = getOntModelOfInterface();

		OntResource classResource = lookup(className);
		
		OntResource propertyResource = lookup(propertyName);

		// Senão contem a relação de dominio é por que esse recurso não está definido na interface SERIN
		if (!model.contains(propertyResource, RDFS.domain, classResource)) {
			throw new SERINMalFormedException(SDSErrorMessage.PROPERTY_DOMAIN_INVALID);
		}
	}	
}
