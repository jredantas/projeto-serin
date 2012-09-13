import java.net.URL;

import br.unifor.mia.serin.client.SerinClient;
import br.unifor.mia.serin.server.Serin;
import br.unifor.mia.serin.server.Veiculo;
import br.unifor.mia.serin.util.Description;
import br.unifor.mia.serin.util.RDF;

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

		Individual peugeout207 = model.createIndividual(Veiculo.NS + "207", Veiculo.VEICULO);
		peugeout207.setPropertyValue(Veiculo.MARCA, model.createLiteral("Peugeot"));
		peugeout207.setPropertyValue(Veiculo.MODELO, model.createLiteral("207"));

		// CRIA CLIENTE SERIN
		serin = new SerinClient(URL_HOST, URI_ONTOLOGY);
		
		// PUT --> 207
		boolean isCreated = serin.put(peugeout207);

		if (isCreated) {
			System.out.println("Individuo criado: " + peugeout207);
			model.write(System.out, "RDF/XML-ABBREV");	
		} else {
			System.out.println("Individuo NÃO criado: " + peugeout207);
		}

		// GET --> Logan
		Description logan = serin.get(Veiculo.VEICULO, "Logan");
		System.out.println("Individuo obtido: " + logan);
		
		// POST --> 207
		peugeout207.removeAll(Veiculo.MODELO);
		peugeout207.setPropertyValue(Veiculo.MODELO, model.createLiteral("208"));
		serin.post(peugeout207);

		// LIST
		RDF rdf = serin.list(Veiculo.VEICULO);
		System.out.println("Lista de Individuos: " + rdf.getDescriptions());

		// DELETE --> Logan
		boolean isDeleted = serin.delete(Veiculo.VEICULO, "Logan");
		if (isDeleted) {
			System.out.println("Individuo deletado: "+ Veiculo.NS +"Logan");
		}
	}
}