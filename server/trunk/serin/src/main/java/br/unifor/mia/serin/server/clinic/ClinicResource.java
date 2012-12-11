package br.unifor.mia.serin.server.clinic;

import java.io.InputStream;

import javax.ws.rs.Path;

import br.unifor.mia.serin.server.SerinServer;
import br.unifor.mia.serin.server.clinic.Clinic;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.rdf.model.ModelFactory;

@Path("/www.unifor.br/clinic.owl")
public final class ClinicResource extends SerinServer {

	/**
	 * Clinic Ontology URI.
	 */
	private final String NS = "http://www.unifor.br/clinic.owl#";

	/**
	 * 
	 */
	public ClinicResource() {

		if (getModel() == null) {
			InputStream inClinic = getClass().getClassLoader()
					.getResourceAsStream(getOntologyFileName());

			setModel(ModelFactory.createOntologyModel());
			getModel().read(inClinic, null);

			// Criando as especialidades...
			Individual specialty = getModel().createIndividual(
					NS + "Cardiology", Clinic.SPECIALTY);
			specialty.setPropertyValue(Clinic.DESCRIPTION, getModel()
					.createTypedLiteral("Cardiology"));

			// Criando os doutores...
			Individual doctor = getModel().createIndividual(
					NS + "Pedro_Porfirio", Clinic.DOCTOR);
			doctor.setPropertyValue(Clinic.DOCTORNAME, getModel()
					.createTypedLiteral("Pedro Porfirio"));
			doctor.setPropertyValue(Clinic.isSpecializedIn, specialty);

			// Criando as clinicas...
			Individual clinic = getModel().createIndividual(
					NS + "CARDIO_CLINIC", Clinic.CLINIC);
			clinic.setPropertyValue(Clinic.CLINICNAME, getModel()
					.createTypedLiteral("CARDIO CLINIC"));
			clinic.setPropertyValue(Clinic.attendedBy, doctor);
			clinic.setPropertyValue(Clinic.PHONENUMBER, getModel()
					.createTypedLiteral("34596600"));

		}
	}

	@Override
	protected String getOntologyFileName() {
		return "clinic.owl";
	}

	@Override
	protected String namespace() {
		return NS;
	}

}