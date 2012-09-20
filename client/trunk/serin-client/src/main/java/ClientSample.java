import java.net.URL;

import br.unifor.mia.serin.client.SerinClient;
import br.unifor.mia.serin.server.Vehicle;
import br.unifor.mia.serin.util.Description;
import br.unifor.mia.serin.util.RDF;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class ClientSample {

	private static final String URL_HOST = "http://localhost:8080";
	
	private static final String URI_ONTOLOGY = "www.unifor.br/vehicle.owl";
	
	private static SerinClient serin;
	
	private static OntModel model;

	public static void main(String[] args) throws Exception {

		URL ontology =
				ClientSample.class.getClassLoader().getResource(URI_ONTOLOGY.substring(URI_ONTOLOGY.indexOf('/')+1));
		
		model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		model.read(ontology.openStream(), null);

		Individual peugeout207 = model.createIndividual(Vehicle.NS + "207", Vehicle.VEHICLE);
		peugeout207.setPropertyValue(Vehicle.BRAND, model.createLiteral("Peugeot"));
		peugeout207.setPropertyValue(Vehicle.MODEL, model.createLiteral("207"));

		// CRIA CLIENTE SERIN
		serin = new SerinClient(URL_HOST, URI_ONTOLOGY);
		
		// POST --> Adiciona o veiculo Peugeot 207
		boolean isCreated = serin.post(peugeout207);

		if (isCreated) {
			System.out.println("Individuo criado: " + peugeout207);
			model.write(System.out, "RDF/XML-ABBREV");	
		} else {
			System.out.println("Individuo NÃO criado: " + peugeout207);
		}

		// GET --> Obtém o veiculo Renault Logan
		Description logan = serin.get(Vehicle.VEHICLE, "Logan");
		System.out.println("Individuo obtido: " + logan);
		
		// PUT --> Atualiza o veiculo Peugeot 207
		peugeout207.removeAll(Vehicle.MODEL);
		peugeout207.setPropertyValue(Vehicle.MODEL, model.createLiteral("208"));
		serin.put(peugeout207);

		// LIST --> Obtém uma lista de todos os veiculos
		RDF rdf = serin.list(Vehicle.VEHICLE);
		System.out.println("Lista de Individuos: " + rdf.getDescriptions());

		// DELETE --> Deleta o veiculo Renault Logan
		boolean isDeleted = serin.delete(Vehicle.VEHICLE, "Logan");
		if (isDeleted) {
			System.out.println("Individuo deletado: "+ Vehicle.NS +"Logan");
		}
	}
}