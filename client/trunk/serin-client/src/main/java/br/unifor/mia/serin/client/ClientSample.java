package br.unifor.mia.serin.client;

import java.util.List;

import br.unifor.mia.serin.client.util.Serin;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Statement;

public class ClientSample {

	private static final String SERIN_ONTOLOGY_URL   = "http://localhost:8080/serin/ontologia/serin.owl";
	private static final String VEICULO_ONTOLOGY_URL = "http://localhost:8080/serin/ontologia/veiculo.owl";
	
	private static final String VEICULO_ONTOLOGY_URI = "http://www.unifor.br/veiculo.owl#";

	private static final String VEICULO_CLASS_URI    = VEICULO_ONTOLOGY_URI + "Veiculo";
	private static final String MARCA_PROPERTY_URI   = VEICULO_ONTOLOGY_URI + "marca";
	private static final String MODELO_PROPERTY_URI  = VEICULO_ONTOLOGY_URI + "modelo";
	
	/**
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);

		model.getDocumentManager().addAltEntry(VEICULO_ONTOLOGY_URI, VEICULO_ONTOLOGY_URL);
		model.getDocumentManager().addAltEntry(Serin.SERIN_ONTOLOGY_URI, SERIN_ONTOLOGY_URL);
		// read the source document
		model.read(VEICULO_ONTOLOGY_URL);
		
		// CRIA UM INDIVIDUO --> 207 
		Individual peugeout207 = model.createIndividual(VEICULO_ONTOLOGY_URI+"207", model.getResource(VEICULO_CLASS_URI));
		peugeout207.setPropertyValue(model.getProperty(MARCA_PROPERTY_URI), model.createLiteral("Peugeot"));
		peugeout207.setPropertyValue(model.getProperty(MODELO_PROPERTY_URI), model.createLiteral("207"));
		
		// registrando novo individual...
		Serin.post(peugeout207);
		//[IMPRIME MODELO RDF]
		model.write(System.out, "RDF/XML-ABBREV");
		
		Individual ind = model.getIndividual(VEICULO_ONTOLOGY_URI + "Logan");
		Property prop = model.getProperty(MARCA_PROPERTY_URI);
		
		String marca = ind.getPropertyValue(prop).toString();
		System.out.println("Marca: " + marca);
		
		// recuperando o registro...
		List<Statement> lista = Serin.list(model);
		
		System.out.println("Lista de Statments: " + lista);
	}
}