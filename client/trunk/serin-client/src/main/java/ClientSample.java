import java.util.List;

import br.unifor.mia.serin.client.SerinClient;
import br.unifor.mia.serin.server.Veiculo;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;

public class ClientSample {

	private static SerinClient serin;

	public static void main(String[] args) throws Exception {

		String urlHost = "http://localhost:8080";
		String uriOntology = "www.unifor.br/veiculo.owl";

		serin = new SerinClient(urlHost, uriOntology);

		// CRIA UM INDIVIDUO --> 207
		OntModel m_model = serin.getOntModel();
		Individual peugeout207 = m_model.createIndividual(Veiculo.NS + "207", Veiculo.VEICULO);
		peugeout207.setPropertyValue(Veiculo.MARCA, m_model.createLiteral("Peugeot"));
		peugeout207.setPropertyValue(Veiculo.MODELO, m_model.createLiteral("207"));

		serin.put(peugeout207);

		System.out.println("Individuo criado: " + peugeout207);
		// model.write(System.out, "RDF/XML-ABBREV");

		// RECUPERA TODOS OS VEICULOS (com anotação LIST)
		List<Individual> lista = serin.list(Veiculo.VEICULO);
		System.out.println("Lista de Statments: " + lista);

		// DELETA UM INDIVIDUO --> Logan
		serin.delete(Veiculo.VEICULO, "Logan");
		// veiculoOntology.getModel().write(System.out, "RDF/XML-ABBREV");
	}
}