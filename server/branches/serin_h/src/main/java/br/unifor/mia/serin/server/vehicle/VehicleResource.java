package br.unifor.mia.serin.server.vehicle;

import java.io.IOException;

import javax.ws.rs.Path;

import br.unifor.mia.serin.server.SerinServer;
import br.unifor.mia.serin.util.FileUtil;

import com.hp.hpl.jena.update.UpdateAction;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateRequest;

@Path("/vehicle")
public final class VehicleResource extends SerinServer {
	
	/**
	 * Vehicle Ontology URI.
	 */
	private final String NS = "http://www.unifor.br/vehicle.owl#";
	
	public VehicleResource() throws IOException {
		if (modelEmpty) {
			String insertString = FileUtil.getContent("VEHICLE_INSERT_DATA.txt");
			UpdateRequest request = UpdateFactory.create(insertString);
			UpdateAction.execute(request, getModel());
			modelEmpty = false;
		}
	}

	@Override
	protected String getOntologyFileName() {
		return "vehicle.owl";
	}

	@Override
	protected String namespace() {
		return NS;
	}
}