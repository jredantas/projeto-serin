import java.net.URL;
import java.util.List;

import br.unifor.mia.serin.client.SerinClient;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class ClinicSample {

	//private static final String URL_HOST = "http://semantic-interfaces.rhcloud.com";

	 private static final String URL_HOST = "http://localhost:8080/semantic";

	private static final String URI_ONTOLOGY = "www.unifor.br/clinic.owl";

	private static SerinClient serin;

	private static OntModel model;

	public static void main(String[] args) throws Exception {

		URL ontology = ClinicSample.class.getClassLoader().getResource(
				URI_ONTOLOGY.substring(URI_ONTOLOGY.indexOf('/') + 1));

		model = ModelFactory.createOntologyModel();
		model.read(ontology.openStream(), null);

		// Criando as especialidades...
		Individual specialty = model.createIndividual(Clinic.NS + "Audiology",
				Clinic.SPECIALTY);
		specialty.setPropertyValue(Clinic.DESCRIPTION,
				model.createTypedLiteral("Audiology"));

		// Criando os doutores...
		Individual doctor = model.createIndividual(Clinic.NS + "Hermano_Lira",
				Clinic.DOCTOR);
		doctor.setPropertyValue(Clinic.DOCTORNAME,
				model.createTypedLiteral("Hermano Lira"));
		doctor.setPropertyValue(Clinic.isSpecializedIn, specialty);

		// Criando as clinicas...
		Individual clinic = model.createIndividual(Clinic.NS + "AUDIO_CLINIC",
				Clinic.CLINIC);
		clinic.setPropertyValue(Clinic.CLINICNAME,
				model.createTypedLiteral("AUDIO CLINIC"));
		clinic.setPropertyValue(Clinic.attendedBy, doctor);
		clinic.setPropertyValue(Clinic.PHONENUMBER,
				model.createTypedLiteral("34773400"));

		// Criando o client...
		serin = new SerinClient(URL_HOST, URI_ONTOLOGY);

		// Adicionando um clinica...
		boolean isCreated = serin.post(clinic);

		// Informando ao usuário se o recurso foi adicionado com sucesso!
		if (isCreated) {
			System.out.println("Individuo criado: " + clinic);
			model.write(System.out, "RDF/XML-ABBREV");
		} else {
			System.out.println("Individuo NÃO criado: " + clinic);
		}

		// GET --> Obtém o veiculo Renault Logan
		Individual cardioClinic = serin.get(Clinic.CLINIC, "CARDIO_CLINIC");
		System.out.println("Individuo obtido: " + cardioClinic);

		// PUT --> Atualiza o veiculo Peugeot 207
		clinic.removeAll(Clinic.PHONENUMBER);
		clinic.setPropertyValue(Clinic.PHONENUMBER,
				model.createTypedLiteral("43773481"));
		serin.put(clinic);

		// LIST --> Obtém uma lista de todos os veiculos
		List<Individual> invidivuals = serin.list(Clinic.CLINIC);
		System.out.println("Lista de Individuos: " + invidivuals);

	}
}