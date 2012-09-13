package br.unifor.mia.serin.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class OntologyConverter {

	public static RDF toObject(String rdfXml) {

		SAXParserFactory spf = SAXParserFactory.newInstance();
		RDFSAXHandler handler = new RDFSAXHandler();
		
		try {
			spf.newSAXParser().parse(new InputSource(new StringReader(rdfXml)), handler);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return handler.getRDF();
	}

	public static String toRDFXML(Individual... individuals) throws XMLStreamException, IOException {

		Model model = null;
		
		try {
			model = individuals[0].getModel();
		} catch (NullPointerException e) {
			return "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" />";
		}
		
		String fileName = "yourXML.xml";

		String RDF_NS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

		XMLOutputFactory xof = XMLOutputFactory.newInstance();
		XMLStreamWriter writer = xof.createXMLStreamWriter(new FileWriter(fileName));

		writer.writeStartDocument("UTF-8", "1.0");
		setPrefixes(model.getNsPrefixMap(), writer);
		writer.writeStartElement(RDF_NS, "RDF");
		writeNamespace(model.getNsPrefixMap(), writer);

		for (Individual individual : individuals) {
			// rdf:Description
			writer.writeStartElement(RDF_NS, "Description");
			writer.writeAttribute(RDF_NS, "about", individual.getURI());
			writeProperties(individual.listProperties(), writer);
			writer.writeEndElement();	
		}
		
		writer.writeEndElement();
		writer.writeEndDocument();

		writer.flush();
		writer.close();

		File rdf = new File(fileName);
		FileReader reader = new FileReader(rdf);
		BufferedReader bufferedReader = new BufferedReader(reader);

		StringBuilder strXml = new StringBuilder();
		while (bufferedReader.ready()) {
			strXml.append(bufferedReader.readLine());
		}

		bufferedReader.close();

		return strXml.toString();
	}

	private static void writeProperties(StmtIterator properties, XMLStreamWriter writer) throws XMLStreamException {

		while (properties.hasNext()) {

			Statement statement = properties.next();
			String[] parts = splitPredicate(statement.getPredicate().getURI());
			writer.writeStartElement(parts[0], parts[1]);
			writer.writeCharacters(statement.getObject().toString());
			writer.writeEndElement();
		}
	}

	private static String[] splitPredicate(String uri) {
		
		String[] parts = uri.split("#");
		
		if (parts.length < 2) {
			parts = new String[2];
			parts[0] = uri.substring(0, uri.lastIndexOf("/")+1);
			parts[1] = uri.substring(uri.lastIndexOf("/")+1, uri.length());
		} else {
			parts[0] += "#";
		}
		
		return parts;
	}

	private static void writeNamespace(Map<String, String> nsPrefixMap,
			XMLStreamWriter writer) throws XMLStreamException {
		for (String prefix : nsPrefixMap.keySet()) {
			String namespace = nsPrefixMap.get(prefix);
			writer.writeNamespace(prefix, namespace);
		}
	}

	private static void setPrefixes(Map<String, String> nsPrefixMap,
			XMLStreamWriter writer) throws XMLStreamException {
		for (String prefix : nsPrefixMap.keySet()) {
			String namespace = nsPrefixMap.get(prefix);
			writer.setPrefix(prefix, namespace);
		}
	}
}
