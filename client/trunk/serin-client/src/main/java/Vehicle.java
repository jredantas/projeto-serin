/* CVS $Id: $ */

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.ontology.*;

/**
 * Vocabulary definitions from owl/vehicle.owl
 * 
 * @author Auto-generated by schemagen on 23 Ago 2012 10:14
 */
public class Vehicle {
	/**
	 * <p>
	 * The ontology model that holds the vocabulary terms
	 * </p>
	 */
	private static OntModel m_model = ModelFactory.createOntologyModel();

	/**
	 * <p>
	 * The namespace of the vocabulary as a string
	 * </p>
	 */
	public static final String NS = "http://www.unifor.br/vehicle.owl#";

	/**
	 * <p>
	 * The namespace of the vocabulary as a string
	 * </p>
	 * 
	 * @see #NS
	 */
	public static String getURI() {
		return NS;
	}

	/**
	 * <p>
	 * The namespace of the vocabulary as a resource
	 * </p>
	 */
	public static final Resource NAMESPACE = m_model.createResource(NS);

	public static final DatatypeProperty isMadeBy = m_model.createDatatypeProperty(NS+"isMadeBy", true);

	public static final DatatypeProperty MODEL = m_model.createDatatypeProperty(NS+"model", true);
	
	public static final DatatypeProperty NAME = m_model.createDatatypeProperty(NS+"name", true);

	public static final DatatypeProperty YEAR = m_model.createDatatypeProperty(NS+"year", true);
	
	public static final OntClass VEHICLE = m_model.createClass(NS+"Vehicle");

	public static final OntClass MANUFACTURER = m_model.createClass(NS+"Manufacturer");
}
