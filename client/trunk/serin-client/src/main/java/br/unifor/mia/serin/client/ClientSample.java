package br.unifor.mia.serin.client;

import java.util.List;

import br.unifor.mia.serin.Ontology;
import br.unifor.mia.serin.VeiculoOntology;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;

public class ClientSample {

	/**
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		Ontology veiculoOntology = new VeiculoOntology();
		SerinClient serin = new SerinClient(veiculoOntology);
		
		// CRIA UM INDIVIDUO --> 207
		Individual peugeout207 = veiculoOntology.createIndividual(VeiculoOntology.URI + "207", "Peugeot", "207");
		
		serin.post(peugeout207);

		OntModel model = veiculoOntology.getModel();	
		//model.write(System.out, "RDF/XML-ABBREV");
		
		// RECUPERA TODOS OS VEICULOS (com anotação LIST)
		List<Individual> lista = serin.list(model.getOntResource(VeiculoOntology.VEICULO_CLASS_URI));
		
		System.out.println("Lista de Statments: " + lista);
		//model.write(System.out, "RDF/XML-ABBREV");
		
		// DELETA UM INDIVIDUO --> Logan
		Individual logan = veiculoOntology.getIndividual(VeiculoOntology.URI + "Logan");
		serin.delete(logan);
		//model.write(System.out, "RDF/XML-ABBREV");
	}
}