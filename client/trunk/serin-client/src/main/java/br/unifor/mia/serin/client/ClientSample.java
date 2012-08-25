package br.unifor.mia.serin.client;

import java.util.List;

import com.hp.hpl.jena.ontology.Individual;

public class ClientSample {

	/**
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		Client veiculoClient = new Client();

		// CRIA UM INDIVIDUO --> 207
		Individual peugeout207 = veiculoClient.post(Client.NS + "207", "Peugeot", "207");
		System.out.println("Individuo criado: " + peugeout207);
		//model.write(System.out, "RDF/XML-ABBREV");

		// RECUPERA TODOS OS VEICULOS (com anotação LIST)
		List<Individual> lista = veiculoClient.list(Client.VEICULO);
		System.out.println("Lista de Statments: " + lista);
		
		// DELETA UM INDIVIDUO --> Logan
		veiculoClient.delete(Client.NS + "Logan");
		//veiculoOntology.getModel().write(System.out, "RDF/XML-ABBREV");
	}
}