package br.unifor.mia.serin.generator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.hp.hpl.jena.ontology.AnnotationProperty;
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
		//Model serin = FileManager.get().loadModel("http://www.activeontology.com.br/serin.owl");
	    InputStream in = FileManager.get().open("http://www.activeontology.com.br/serin.owl");
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
       	/*while(annotations.iterator().hasNext()) {
       		AnnotationProperty a = annotations.iterator().next();
       		Resource r3 = serin.getResource(a.getURI());
       		System.out.print("Anotação SERIN:");
	      	System.out.println(r3.toString());
         }*/
	    //for (int i=0;i<annotations.size();i++){
	    //	System.out.print("Anotação SERIN:");
	    //  	System.out.println(annotations.get(i).toString());
	    //}
	    
       	//System.out.println("===============================================");
	    	    
       	Date d1 = new Date();
	    System.out.println(d1);
	    File directory = new File(Main.ONTOLOGY_PATH);
	    File[] files = directory.listFiles();
       	int cont = 1;
	    for ( int i = 0; i < files.length; i++ ){
	    	//int i = 0;
	       	System.out.println(Main.ONTOLOGY_PATH+files[i].getName());
	       	
	       	InputStream in2 = FileManager.get().open(Main.ONTOLOGY_PATH+files[i].getName());
		    if (in2 == null) {
				throw new IllegalArgumentException("File: " + in2 + " not found");
			}
		    OntModel model = ModelFactory.createOntologyModel();
		    model.read(in2, null);
		    
		    ExtendedIterator<Ontology> ontol = model.listOntologies();
	       	while(ontol.hasNext()) {
	       		//Model baseModel = model.getBaseModel();
	       		Ontology ont = model.getOntology(ontol.next().toString());
	       		//Resource r3 = model.getResource(ontol.next().toString());
	       		if (ont.getLocalName().equals(files[i].getName())){
			       	//System.out.println(baseModel.get);
		       		System.out.print("Ontology "+cont+":");
			       	System.out.println(ont.getURI());
				    ont.addImport(model.createResource("http://www.activeontology.com.br/serin.owl"));	
		       		cont++;
		       		break;
	       		}
	         }

	       	System.out.println("===============================================");
		    model.setNsPrefix("serin", "http://www.activeontology.com.br/serin.owl#");

		    //Ontology ont = model.getOntology(model.listOntologies());
		    //ontol.addImport(model.createResource("http://www.activeontology.com.br/serin.owl"));	

		    AnnotationProperty annotationGet = serin.getAnnotationProperty("http://www.activeontology.com.br/serin.owl#get" );
		    AnnotationProperty annotationPut = serin.getAnnotationProperty("http://www.activeontology.com.br/serin.owl#put" );
		    AnnotationProperty annotationPost = serin.getAnnotationProperty("http://www.activeontology.com.br/serin.owl#post" );
		    AnnotationProperty annotationDelete = serin.getAnnotationProperty("http://www.activeontology.com.br/serin.owl#delete" );

	       	ResIterator resources = model.listSubjectsWithProperty(RDF.type, OWL.Class);
	       	//int cont = 1;
	       	while(resources.hasNext()) {
	       		Resource r3 = model.getResource(resources.next().toString());
	       		//System.out.print("Recurso "+cont+":");
		       	//System.out.println(r3.toString());
	       		r3.addProperty(annotationGet, "");
	       		r3.addProperty(annotationPut, "");
	       		r3.addProperty(annotationPost, "");
	       		r3.addProperty(annotationDelete, "");
	       		//cont++;
	         }
	       	//System.out.println("===============================================");
	       	String base = "http://www.example.com/ont";
	    	model.write(System.out, "RDF/XML-ABBREV", base);
			try {
	       		FileOutputStream output = new FileOutputStream(Main.ONTOLOGY_PATH+files[i].getName());
				model.write(output);
				output.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

        }
	    Date d2 = new Date();
	    System.out.println(d2);
	    System.out.println("Total execution time: "+(d2.getTime()-d1.getTime())+" miliseconds.");
        System.out.println("End!!!");

	}
}
