package br.unifor.mia.sds.interfacemanager.integrityconstraint;

import java.util.List;

import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

public interface DBHandler {
	
	public boolean contains(String uri);

	public boolean isMembership(Resource resource, Resource classResource);

	public String getIndividual(String className, String rdfID, List<Property> embeddedProperties);

	public String getIndividuals(OntResource classResource, List<Property> embeddedProperties);
}