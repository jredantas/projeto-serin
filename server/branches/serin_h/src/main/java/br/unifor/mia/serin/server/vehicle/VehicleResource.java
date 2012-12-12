package br.unifor.mia.serin.server.vehicle;

import java.io.InputStream;

import javax.ws.rs.Path;

import br.unifor.mia.serin.server.SerinServer;

import com.hp.hpl.jena.ontology.Individual;
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
			
			Individual renault = getModel().createIndividual(NS+"Renault", Vehicle.MANUFACTURER);
			renault.setPropertyValue(Vehicle.NAME, getModel().createTypedLiteral("Renault Motors"));

			Individual logan = getModel().createIndividual(NS+"Logan", Vehicle.VEHICLE);
			logan.setPropertyValue(Vehicle.YEAR, getModel().createTypedLiteral(2012));
			logan.setPropertyValue(Vehicle.MODEL, getModel().createTypedLiteral("Logan"));
			logan.setPropertyValue(Vehicle.isMadeBy, renault);
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