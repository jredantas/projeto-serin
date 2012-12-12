package br.unifor.mia.serin.server.clinic;

import java.io.IOException;

import javax.ws.rs.Path;

import com.hp.hpl.jena.update.UpdateAction;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateRequest;

import br.unifor.mia.serin.server.SerinServer;
import br.unifor.mia.serin.util.FileUtil;

@Path("/www.unifor.br/clinic.owl")
public final class ClinicResource extends SerinServer {

	/**
	 * Clinic Ontology URI.
	 */
	private final String NS = "http://www.unifor.br/clinic.owl#";

	public ClinicResource() throws IOException {

		String insertString = FileUtil.getContent("CLINIC_INSERT_DATA.txt");

		UpdateRequest request = UpdateFactory.create(insertString);
		UpdateAction.execute(request, getModel());
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