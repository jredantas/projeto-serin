package br.unifor.mia.serin.generator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import br.unifor.mia.serin.util.Db;

import com.hp.hpl.jena.ontology.AnnotationProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.Ontology;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;

public class Main {

	//private static final String ONTOLOGY_PATH = "/home/renato/Dropbox/Doutorado/tese/MassaDeDados/hRESTS-TC3/ontology/teste/";
	private static final String ONTOLOGY_PATH = "/home/09959295800/Dropbox/Doutorado/tese/MassaDeDados/hRESTS-TC3/ontology/teste/";
	
	//private static final String INSERT_HOST = "INSERT INTO host_service (host_address,interface_uri, host_protocol) VALUES (?,?,?)";
	private static final String INSERT_SERVICE = "INSERT INTO interface (chave,uri) VALUES (?,?);";
	


	public static void main(String[] args)  {
		System.out.println("SERIN Generator started...");
		
		/**
		 * Construir lista de ontologias para anotar:
		 * 1- Obter ontologias de um diretório;
		 * 2- Percorrer árvore da ontologia
		 * 3- Anotar aleatoriamente cada classe
		 *  3.1- Selecionar aleatoriamente (sortear) a classe inicial;
		 *  3.2- Sortear tamanho do passo para as próximas classes;
		 *  3.3- Sortear anotações para a classe (representadas por números de 1 a 4)
		 * 4-  
		 */

		//InputStream in = FileManager.get().open("http://www.activeontology.com.br/serin.owl");
	    InputStream in = FileManager.get().open("file:///home/09959295800/Dropbox/Doutorado/ontologia/serin.owl");
	    if (in == null) {
			throw new IllegalArgumentException("File: " + in + " not found");
		}
	    OntModel serin = ModelFactory.createOntologyModel();
	    serin.read(in, null);
	    List<AnnotationProperty> annotations =new ArrayList<AnnotationProperty>();
	    ResIterator annotationIterator = serin.listSubjectsWithProperty(RDF.type, OWL.AnnotationProperty);
	    while (annotationIterator.hasNext()) {
	      final AnnotationProperty annotation= serin.getAnnotationProperty(annotationIterator.next().getURI() );
	      if (annotation != null) {
	        annotations.add(annotation);
	      }
	    }
	    	    
       	Date d1 = new Date();
	    System.out.println(d1);
	    File directory = new File(Main.ONTOLOGY_PATH);
	    File[] files = directory.listFiles();
       	int cont = 1;
       	int interfaceCount = 0;
       	int classCount = 0;
	    for ( int i = 0; i < files.length; i++ ){
	       	System.out.println(Main.ONTOLOGY_PATH+files[i].getName());
	       	
	       	InputStream in2 = FileManager.get().open(Main.ONTOLOGY_PATH+files[i].getName());
		    if (in2 == null) {
				throw new IllegalArgumentException("File: " + in2 + " not found");
			}
		    OntModel model = ModelFactory.createOntologyModel();
		    model.read(in2, null);
		    
		    ExtendedIterator<Ontology> ontol = model.listOntologies();
	       	while(ontol.hasNext()) {
	       		Ontology ont = model.getOntology(ontol.next().toString());
	       		if (ont.getLocalName().equals(files[i].getName())){
		       		System.out.print("Ontology "+cont+":");
			       	System.out.println(ont.getURI());
				    ont.addImport(model.createResource("http://www.activeontology.com.br/serin.owl"));	
		       		cont++;
		       		break;
	       		}
	         }

		    model.setNsPrefix("serin", "http://www.activeontology.com.br/serin.owl#");
		    System.out.println("passo do prefixo");

		    AnnotationProperty annotationGet = serin.getAnnotationProperty("http://www.activeontology.com.br/serin.owl#get" );
		    AnnotationProperty annotationPut = serin.getAnnotationProperty("http://www.activeontology.com.br/serin.owl#put" );
		    AnnotationProperty annotationPost = serin.getAnnotationProperty("http://www.activeontology.com.br/serin.owl#post" );
		    AnnotationProperty annotationDelete = serin.getAnnotationProperty("http://www.activeontology.com.br/serin.owl#delete" );

	       	ExtendedIterator<OntClass> cl = model.listNamedClasses();
	       	List<Resource> resources = new ArrayList<Resource>(); 
	       	while(cl.hasNext()) {
	       		Resource r3 = model.getResource(cl.next().getURI());
	       		if (!(r3.getLocalName()==null)){
				    System.out.println("Classe: "+r3.toString());
				    System.out.println("Classe local name: "+r3.getLocalName());
				    System.out.println("Classe URI: "+r3.getURI());
				    resources.add(r3);
	       		}
	         }

		    
		    //ResIterator resources = model.listSubjectsWithProperty(RDF.type, OWL.Class);
		    ListIterator listResources = resources.listIterator();
		    System.out.println("===============passo da lista de sujeitos===================");
	       	while(listResources.hasNext()) {
	       		//Resource r3 = model.getResource(resources.next().toString());
	       		Resource r3 = model.getResource(listResources.next().toString());
	       		if (!(r3.getClass().getSimpleName()==null)){
		       		classCount++;
				    System.out.println(r3.toString());
				    System.out.println("R3 local name: "+r3.getLocalName());
				    System.out.println("R3 URI: "+r3.getURI());
		       		r3.addProperty(annotationGet, "");
		       		interfaceCount++;
		       		r3.addProperty(annotationPut, "");
		       		interfaceCount++;
		       		r3.addProperty(annotationPost, "");
		       		interfaceCount++;
		       		r3.addProperty(annotationDelete, "");
		       		interfaceCount++;
	       		}
	         }
	       	String base = "http://www.example.com/ont";
		    //System.out.println("passo da base");
	    	//model.write(System.out, "RDF/XML-ABBREV", base);
			try {
	       		FileOutputStream output = new FileOutputStream(Main.ONTOLOGY_PATH+files[i].getName());
				model.write(output);
				output.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
	       	System.out.println("===============================================");


        }
	    Date d2 = new Date();
	    System.out.println(d2);
	    System.out.println("Total execution time: "+(d2.getTime()-d1.getTime())+" miliseconds.");
        System.out.println("End!!!");
        
        /**
         * Segunda parte: armazenar as URIs das ontologias em banco de dados
         *                para leitura pelo serviço de busca
         */
        
       	d1 = new Date();
	    System.out.println(d1);
	    directory = new File(Main.ONTOLOGY_PATH);
	    files = directory.listFiles();
       	cont = 1;
	    for ( int i = 0; i < files.length; i++ ){
	       	//System.out.println(Main.ONTOLOGY_PATH+files[i].getName());
	       	
	       	InputStream in2 = FileManager.get().open(Main.ONTOLOGY_PATH+files[i].getName());
		    if (in2 == null) {
				throw new IllegalArgumentException("File: " + in2 + " not found");
			}
		    OntModel model = ModelFactory.createOntologyModel();
		    model.read(in2, null);
		    
		    ExtendedIterator<Ontology> ontol = model.listOntologies();
	       	while(ontol.hasNext()) {
	       		Ontology ont = model.getOntology(ontol.next().toString());
	       		if (ont.getLocalName().equals(files[i].getName())){
		       		//System.out.print("Ontology "+cont+":");
			       	//System.out.println(ont.getURI());
				    ont.addImport(model.createResource("http://www.activeontology.com.br/serin.owl"));	
		       		cont++;
		 	   	     //faz a persistência no banco de dados, das URIs de interface listadas no servidor
		   	      	Connection con = null;
		   	      	String statement = INSERT_SERVICE;
		 		try {
		 			con = Db.getConnection();
		 			PreparedStatement prepared = con.prepareStatement(statement);
		 			prepared.setString(1, "example.com_"+ont.getLocalName());
		 			prepared.setString(2, ont.getURI());
		 			prepared.execute();

		 		} catch (SQLException sql) {
		 		System.out.println(sql.getMessage());
		 		} finally {
		 			Db.closeConnnection(con);
		 		}
		       		break;
	       		}
	         }
	    

	    }
	    d2 = new Date();
	    System.out.println(d2);
	    System.out.println("Total execution time: "+(d2.getTime()-d1.getTime())+" miliseconds.");
	    System.out.println("Number of annotated classes: "+classCount);
	    System.out.println("Number of generated interfaces: "+interfaceCount);
        System.out.println("End!!!");
        


        
        
	}
}
