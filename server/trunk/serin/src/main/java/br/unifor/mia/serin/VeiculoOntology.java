package br.unifor.mia.serin;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

public class VeiculoOntology implements Ontology {
	
	//SERIN
	public static final String SERIN_URL = "http://localhost:8080/serin/ontologia/serin.owl";
	public static final String SERIN_URI = "http://www.unifor.br/serin.owl#";
	
	// VEICULO
	private static final String VEICULO_ONTOLOGY_URL = "http://localhost:8080/serin/ontologia/veiculo.owl";
	
	public static final String URI                 = "http://www.unifor.br/veiculo.owl#";
	public static final String VEICULO_CLASS_URI   = URI + "Veiculo";
	public static final String MARCA_PROPERTY_URI  = URI + "marca";
	public static final String MODELO_PROPERTY_URI = URI + "modelo";

	// WEB SERVICE
	private final String WEB_SERVICE = "http://localhost:8080/serin/ws/veiculos";
	
	private OntModel model;
	
	public VeiculoOntology() {
		
		model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);

		getModel().getDocumentManager().addAltEntry(URI, VEICULO_ONTOLOGY_URL);
		getModel().getDocumentManager().addAltEntry(SERIN_URI, SERIN_URL);
		// read the source document
		getModel().read(VEICULO_ONTOLOGY_URL);

	}

	public String WEB_SERVICE() {
		return WEB_SERVICE;
	}

	/**
	 * @see br.unifor.mia.serin.Ontology#getModel()
	 */
	@Override
	public OntModel getModel() {
		return model;
	}

	/**
	 * @see br.unifor.mia.serin.Ontology#createIndividual(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public Individual createIndividual(String about, String marca, String modelo) {
		
		Individual individuo = model.createIndividual(about, model.getResource(VeiculoOntology.VEICULO_CLASS_URI));
		individuo.setPropertyValue(model.getProperty(VeiculoOntology.MARCA_PROPERTY_URI), model.createLiteral(marca));
		individuo.setPropertyValue(model.getProperty(VeiculoOntology.MODELO_PROPERTY_URI), model.createLiteral(modelo));
		
		return individuo;
		
	}

	@Override
	public Individual getIndividual(String about) {
		
		ExtendedIterator<Individual> itrInd = model.listIndividuals();
		
		while (itrInd.hasNext()) {
			Individual ind = itrInd.next();
			if (ind.getURI().equals(about)) {
				return ind;
			}
		}
		return null;
	}	
}
