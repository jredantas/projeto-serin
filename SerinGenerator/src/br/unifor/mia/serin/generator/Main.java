package br.unifor.mia.serin.generator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

import com.hp.hpl.jena.ontology.AnnotationProperty;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.Ontology;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.OWL2;
import com.hp.hpl.jena.vocabulary.RDF;

public class Main {

	//private static final String ONTOLOGY_PATH = "/home/renato/Dropbox/Doutorado/tese/MassaDeDados/hRESTS-TC3/ontology/teste/";
	private static final String ONTOLOGY_PATH = "/home/09959295800/Dropbox/Doutorado/tese/MassaDeDados/hRESTS-TC3/ontology/teste/";

	public static void main(String[] args)  {
		// TODO Auto-generated method stub
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
	    //serin.setNsPrefix("serin", "http://www.activeontology.com.br/serin.owl#");
	    ResIterator annotations = serin.listSubjectsWithProperty(RDF.type, OWL.AnnotationProperty);
       	/*while(annotations.hasNext()) {
       		Resource r3 = serin.getResource(annotations.next().toString());
       		System.out.print("Anotação SERIN:");
	      	System.out.println(r3.toString());
         }
       	System.out.println("===============================================");*/
	    	    
       	Date d1 = new Date();
	    System.out.println(d1);
	    File directory = new File(Main.ONTOLOGY_PATH);
	    File[] files = directory.listFiles();
	    //for ( int i = 0; i < files.length; i++ ){
	    	int i = 0;
	       	System.out.println(Main.ONTOLOGY_PATH+files[i].getName());
	       	//Model model = FileManager.get().loadModel( Main.ONTOLOGY_PATH+files[i].getName() );
	       	
	       	InputStream in2 = FileManager.get().open(Main.ONTOLOGY_PATH+files[i].getName());
		    if (in2 == null) {
				throw new IllegalArgumentException("File: " + in2 + " not found");
			}
		    OntModel model = ModelFactory.createOntologyModel();
		    model.read(in2, null);
		    
		    //model.setNsPrefix("serin", "http://www.activeontology.com.br/serin.owl#");

		    //model.add(serin);
		    
		    //OntModel m2 = ModelFactory.createOntologyModel();
		    //Ontology ont = m2.createOntology("");
		    //ont.addImport(m2.createResource("http://www.activeontology.com.br/serin.owl"));
		    
		    Ontology ont = model.createOntology();
		    //ont.addImport(model.createResource("http://www.activeontology.com.br/serin.owl"));	

	       	
		    /*OntModel m2 = model.getImportedModel("http://www.activeontology.com.br/serin.owl");
	       	ResIterator it3 = m2.listSubjectsWithProperty(RDF.type, OWL.Class);
	       	while(it3.hasNext()) {
	       		Resource r3 = model.getResource(it3.next().toString());
		       	System.out.println(r3.toString());
	       		//r3.addProperty(annotation, ""); 
	         }
	       	System.out.println("===============================================");
		    */
		    
		    AnnotationProperty annotation = model.createAnnotationProperty( "http://www.activeontology.com.br/serin.owl#get" );

	       	ResIterator resources = model.listSubjectsWithProperty(RDF.type, OWL.Class);
	       	int cont = 1;
	       	while(resources.hasNext()) {
	       		Resource r3 = model.getResource(resources.next().toString());
	       		//System.out.print("Recurso "+cont+":");
		       	//System.out.println(r3.toString());
	       		
	       		//r3.addProperty(annotation, ""); 
	       		cont++;
	         }
	       	//System.out.println("===============================================");
	       	String base = "http://www.example.com/ont";
	    	model.write(System.out, "RDF/XML-ABBREV", base);
			try {
	       		FileOutputStream output = new FileOutputStream(Main.ONTOLOGY_PATH+files[i].getName());
				//model.write(output);
				output.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	       	//String a = m.getResource("http://127.0.0.1/ontology/ApothecaryOntology.owl#Physician_hasID").getNameSpace();
	       	//System.out.println(a);
	       	
	       	
			//ModelMaker modelMaker = ModelFactory.createFileModelMaker(Main.ONTOLOGY_PATH+files[i].getName());
			//Model model = modelMaker.createDefaultModel();
			
			
			//FileManager.get().readModel(model, Main.ONTOLOGY_PATH+files[i].getName());
			
			//System.out.println(model.toString());
	        //Model baseOntology = FileManager.get().loadModel( SOURCE_URL );
	        //m.addSubModel( baseOntology );
			//dataset.begin(ReadWrite.READ);

			//model.add(dataset.getDefaultModel());
			
			//dataset.end();

        //fim-do-for}
	    Date d2 = new Date();
	    System.out.println(d2);
	    System.out.println(d2.getTime()-d1.getTime());

        
	    	
        
        System.out.println("Encerrou!!!");

	
	
	}
	
}
