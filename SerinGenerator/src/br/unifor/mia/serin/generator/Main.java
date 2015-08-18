package br.unifor.mia.serin.generator;

import java.io.File;

import javax.management.ObjectName;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileManager;

public class Main {

	private static final String ONTOLOGY_PATH = "/home/renato/Dropbox/Doutorado/tese/MassaDeDados/hRESTS-TC3/ontology/";

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
	    for ( int i = 0; i < files.length; i++ ){
	       	System.out.println(files[i].getName());
			OntModel model = ModelFactory.createOntologyModel();
			FileManager.get().readModel(model, Main.ONTOLOGY_PATH+files[i].getName());
			//System.out.println(model.toString());
	        //Model baseOntology = FileManager.get().loadModel( SOURCE_URL );
	        //m.addSubModel( baseOntology );
			//dataset.begin(ReadWrite.READ);

			//model.add(dataset.getDefaultModel());
			
			//dataset.end();

        } 
        
	    	
        
        System.out.println("Encerrou!!!");

	
	
	}
	
}
