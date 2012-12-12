package br.unifor.mia.serin.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

import br.unifor.mia.serin.util.RDF_SYNTAX;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;

public class RDFOutput implements StreamingOutput {

	private List<Statement> statements;
	private Map<String, String> nsPrefixMap;

	public RDFOutput(Map<String, String> nsPrefixMap, List<Statement> statements) {
		this.statements = statements;
		this.nsPrefixMap = nsPrefixMap;
	}
	
	@Override
	public void write(OutputStream output) throws IOException, WebApplicationException {

		OntModel m = ModelFactory.createOntologyModel();
		m.setNsPrefixes(nsPrefixMap);
		
		for (Statement stmt : statements) {
			m.add(stmt);
		}
		
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		m.write(stream, RDF_SYNTAX.FORMAT);
		
		output.write(stream.toByteArray());
	}
}
