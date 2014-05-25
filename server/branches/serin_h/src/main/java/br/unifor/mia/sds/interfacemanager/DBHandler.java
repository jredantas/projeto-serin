package br.unifor.mia.sds.interfacemanager;

import com.hp.hpl.jena.rdf.model.Resource;

public interface DBHandler {
	
	public boolean contains(String uri);

	public boolean isMembership(Resource resource, Resource classResource);
}