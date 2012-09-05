import java.net.URL;
import java.util.List;

import br.unifor.mia.serin.client.SerinClient;
import br.unifor.mia.serin.server.Serin;
import br.unifor.mia.serin.server.Veiculo;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class ClientSample {

	private static final String URL_HOST = "http://localhost:8080";
	
	private static final String URI_ONTOLOGY = "www.unifor.br/veiculo.owl";
	
	private static SerinClient serin;
	
	private static OntModel model;

	public static void main(String[] args) throws Exception {

		String urlSerin = ClientSample.class.getClassLoader().getResource("serin.owl").toString();
		
		URL ontology =
				ClientSample.class.getClassLoader().getResource(URI_ONTOLOGY.substring(URI_ONTOLOGY.indexOf('/')+1));
		
		model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		model.getDocumentManager().addAltEntry(Serin.NS, urlSerin);
		model.read(ontology.openStream(), null);

		serin = new SerinClient(URL_HOST, URI_ONTOLOGY, ontology.toString());

		// CRIA UM INDIVIDUO --> 207
		Individual peugeout207 = model.createIndividual(Veiculo.NS + "207", Veiculo.VEICULO);
		peugeout207.setPropertyValue(Veiculo.MARCA, model.createLiteral("Peugeot"));
		peugeout207.setPropertyValue(Veiculo.MODELO, model.createLiteral("207"));
		
		boolean isCreated = serin.put(peugeout207);

		if (isCreated) {
			System.out.println("Individuo criado: " + peugeout207);
			model.write(System.out, "RDF/XML-ABBREV");	
		} else {
			System.out.println("Individuo NÃO criado: " + peugeout207);
		}

		// CRIA UM INDIVIDUO --> Logan
		Individual logan = model.createIndividual(Veiculo.NS + "Logan", Veiculo.VEICULO);
		logan.setPropertyValue(Veiculo.MARCA, model.createLiteral("Renault"));
		logan.setPropertyValue(Veiculo.MODELO, model.createLiteral("Logan"));
		
		isCreated = serin.put(logan);

		if (isCreated) {
			System.out.println("Individuo criado: " + logan);
			model.write(System.out, "RDF/XML-ABBREV");	
		} else {
			System.out.println("Individuo NÃO criado: " + logan);
		}
		
		// RECUPERA TODOS OS VEICULOS (com anotação LIST)
		List<Individual> lista = serin.list(Veiculo.VEICULO);
		System.out.println("Lista de Individuos: " + lista);

		// DELETA UM INDIVIDUO --> Logan
		boolean isDeleted = serin.delete(Veiculo.VEICULO, "Logan");
		if (isDeleted) {
			System.out.println("Individuo deletado: "+ Veiculo.NS +"Logan");
		}
	}
}