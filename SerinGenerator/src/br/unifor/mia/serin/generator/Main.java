package br.unifor.mia.serin.generator;

import java.io.File;
import java.util.Date;
import java.util.Iterator;

import javax.management.ObjectName;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ModelMaker;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.FileManager;

public class Main {

	//private static final String ONTOLOGY_PATH = "/home/renato/Dropbox/Doutorado/tese/MassaDeDados/hRESTS-TC3/ontology/";
	private static final String ONTOLOGY_PATH = "/home/09959295800/Dropbox/Doutorado/tese/MassaDeDados/hRESTS-TC3/ontology/";

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("Discovery agent started...");
		
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
		File directory = new File(Main.ONTOLOGY_PATH);
	    File[] files = directory.listFiles();
	    Model serin = FileManager.get().loadModel("http://www.activeontology.com.br/serin.owl");
	    /*Resource r = serin.getResource("http://www.w3.org/2002/07/owl#AnnotationProperty");
       	//System.out.println("1===============================================");
       	StmtIterator rdf = r.listProperties();
       	while(rdf.hasNext()) {
       		Statement n = rdf.next();
       		String s = n.toString();
	      // 	System.out.println(s);
         }
       	//System.out.println("===============================================");
       	//System.out.println("===============================================");
       	//System.out.println("===============================================");
       	NodeIterator serinNodes = serin.listObjects();
       	while(serinNodes.hasNext()) {
       		RDFNode n = serinNodes.next();
       		String s = n.toString();
	      // 	System.out.println(s);
         }
       	//System.out.println("===============================================");
       	ResIterator serinresources = serin.listSubjects();
       	while(serinresources.hasNext()) {
       		RDFNode n = serinresources.next();
       		String s = n.toString();
	       	System.out.println(s);
         }
       	//System.out.println("===============================================");
	   */
       	Date d1 = new Date();
	    System.out.println(d1);
	    //for ( int i = 0; i < files.length; i++ ){
	    	int i = 0;
	       	System.out.println(Main.ONTOLOGY_PATH+files[i].getName());
	       	Model m = FileManager.get().loadModel( Main.ONTOLOGY_PATH+files[i].getName() );
	       	//OntModel m = ModelFactory.createOntologyModel();
/*
		    Resource r2 = m.getResource("http://127.0.0.1/ontology/ApothecaryOntology.owl#PhysicianID");
	       	//System.out.println("2===============================================");
	       	StmtIterator rdf2 = r2.listProperties();
	       	while(rdf2.hasNext()) {
	       		Statement n = rdf2.next();
	       		String s = n.toString();
		      // 	System.out.println(s);
	         }
	       	//System.out.println("===============================================");
	       	//System.out.println("===============================================");
	       	//System.out.println("===============================================");
*/
	       	
	       	
	       	ResIterator resources = m.listSubjects();
	       	//StmtIterator resources = m.listStatements();
	       	//NodeIterator resources = m.listObjects();
	       	int cont = 1;
	       	while(resources.hasNext()) {
	       		//RDFNode n = resources.next();
	       		//String s = n.toString();
	       		Resource r3 = m.getResource(resources.nextResource().toString());
	       		//Resource r3 = resources.nextStatement().getSubject().as();
		       		System.out.println("Recurso "+cont+":");
			       	System.out.println(r3.toString());
		       		cont++;
		       	//StmtIterator rdf3 = r3.listProperties();
		       	//while(rdf3.hasNext()) {
		       	//	Statement n = rdf3.nextStatement();
			     //  	System.out.println(n.toString());
		         //}
	       		System.out.println("--------------------------------");
		       	
	         }
	       	System.out.println("===============================================");
	       	
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
