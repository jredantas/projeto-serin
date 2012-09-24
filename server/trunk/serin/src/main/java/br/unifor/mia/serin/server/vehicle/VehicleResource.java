package br.unifor.mia.serin.server.vehicle;

import java.io.InputStream;

import javax.ws.rs.Path;

import br.unifor.mia.serin.server.SerinServer;

import com.hp.hpl.jena.rdf.model.ModelFactory;

@Path("/www.unifor.br/vehicle.owl")
public final class VehicleResource extends SerinServer {
	
	/**
	 * Vehicle Ontology URI.
	 */
	private final String NS = "http://www.unifor.br/vehicle.owl#";
	
	/**
	 * 
	 */
	public VehicleResource() {
		
		if (getModel() == null) {
			InputStream inVeiculo = getClass().getClassLoader().getResourceAsStream(getOntologyFileName());
			
			setModel(ModelFactory.createOntologyModel());
			getModel().read(inVeiculo, null);
			
			/*Individual individuo = getModel().createIndividual(NS+"Logan", Vehicle.VEHICLE);
			individuo.setPropertyValue(Vehicle.BRAND, getModel().createTypedLiteral("Renault"));
			individuo.setPropertyValue(Vehicle.MODEL, getModel().createTypedLiteral("Logan"));*/
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