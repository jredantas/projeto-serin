package br.unifor.mia.serin.server.clinic;

import java.io.IOException;

import javax.ws.rs.Path;

import br.unifor.mia.serin.server.SerinServer;
import br.unifor.mia.serin.util.FileUtil;

import com.hp.hpl.jena.update.UpdateAction;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateRequest;

@Path("www.unifor.br/clinic.owl")
public class ClinicResource extends SerinServer {

	/**
	 * Clinic Ontology URI.
	 */
	private final String NS = "http://www.unifor.br/clinic.owl#";
	
	public ClinicResource() throws IOException {
		if (modelEmpty) {
			String insertString = FileUtil.getContent("CLINIC_INSERT_DATA.txt");
			UpdateRequest request = UpdateFactory.create(insertString);
			UpdateAction.execute(request, getModel());
			ontologyMap.get(getOntologyFileName()).sequence = 4;
			modelEmpty = false;
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