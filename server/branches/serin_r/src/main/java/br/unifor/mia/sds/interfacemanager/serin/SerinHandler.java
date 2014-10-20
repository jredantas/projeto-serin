package br.unifor.mia.sds.interfacemanager.serin;

import br.unifor.mia.sds.requesthandler.DBHandler;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntResource;

public class SerinHandler {

	private DBHandler dbHandler;
	


	public SerinHandler(DBHandler dbHandler) {
		this.dbHandler = dbHandler;
	}

	/**
	 * Método fixo.
	 * Esse método monta os indivíduos a partir da lista de hosts.
	 * @param modelOfInterface
	 * @param classResource
	 * @param properties
	 * @return
	 */
	public String getHosts(OntModel modelOfInterface, OntResource classResource, String interfaceName) {

		return this.dbHandler.getHosts(classResource, interfaceName);
	}

	/**
	 * Método fixo.
	 * Esse método monta a lista de interfaces atendidas pelo host
	 * @param modelOfInterface
	 * @param classResource
	 * @param properties
	 * @return
	 */
	public String getInterfaces(OntModel modelOfInterface, OntResource classResource) {

		return this.dbHandler.getInterfaces(classResource);
	}
}
