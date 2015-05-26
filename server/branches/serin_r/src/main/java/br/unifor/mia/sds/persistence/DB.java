package br.unifor.mia.sds.persistence;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import br.unifor.mia.sds.requesthandler.ConfigurationException;
import br.unifor.mia.sds.util.FileUtil;
import br.unifor.mia.sds.util.OntologyUtil;
import br.unifor.mia.sds.util.RDFXMLException;
import br.unifor.mia.sds.util.URLTemplate;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.update.UpdateAction;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateRequest;
import com.hp.hpl.jena.vocabulary.RDF;

public final class DB {
	
	// Make a TDB-backed dataset
	//private String DB_DIRECTORY = "MyDatabases/DatasetSDS";
	private String DB_DIRECTORY = "../app-root/data/clinic";
	
	private Dataset dataset = TDBFactory.createDataset(DB_DIRECTORY);

	private Properties sdsProperty = new Properties();

	/**
	 * Banco de dados em memória.
	 */
	//private OntModel model;// = ModelFactory.createOntologyModel();
	
	private OntModel getModel() {
		
		OntModel model = ModelFactory.createOntologyModel();
		
		dataset.begin(ReadWrite.READ);

		model.add(dataset.getDefaultModel());
		
		dataset.end();
		
		return model;
	}

	/**
	 * Contador iniciando em 100.
	 */
	private int sequence = 100;
	
	private DB() {
		try {
			dataset.begin(ReadWrite.WRITE);

			Model model = dataset.getDefaultModel();
			
			// Carregar alguns Dados de exemplo 
			String insertString = FileUtil.getContent("CLINIC_INSERT_DATA.txt");
			UpdateRequest request = UpdateFactory.create(insertString);
			UpdateAction.execute(request, model);
			
			dataset.commit();
			
			dataset.end();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static DB db;
	
	public static DB getInstance() {
		
		if (db == null) {
			db = new DB();
			return db;
		} else {
			return db;	
		}
	}

	/**
	 * 
	 * @param cls
	 * @return
	 */
	private String newSubjectURI(OntResource cls) {
		return URLTemplate.URL_TEMPLATE + "/" + cls.getLocalName() + "/" + sequence++;
	}

	private String getIndividual(String rdfID, Resource classResource) {
	
		if (rdfID == null) {
			return null;
		}
		
		Individual individual = null;
		
		if (rdfID.indexOf(URLTemplate.URL_TEMPLATE) != -1) {
			individual = getModel().getIndividual(rdfID);
		} else {
			individual = getModel().getIndividual(URLTemplate.URL_TEMPLATE + "/"+ classResource.getLocalName() + "/" + rdfID);
		}
		
		if (individual == null) {
			return null;
		}
		
		return OntologyUtil.listIndividualsToRDFXML(individual);
	}

	public boolean isMembership(String rdfID, Resource classNameResource) {
	
		Individual individuo = getModel().getIndividual(URLTemplate.URL_TEMPLATE + "/"+ classNameResource.getLocalName() + "/" + rdfID);
		
		return getModel().contains(individuo, RDF.type, classNameResource);
	}

	public boolean isMembership(Resource resource, Resource classResource) {		
		return getModel().contains(resource, RDF.type, classResource);
	}

	public boolean contains(String uri) {
		Individual ind = getModel().getIndividual(uri);
		return ind == null ? false : true;
	}

	public String getIndividual(String className, String rdfID, List<Property> embeddedProperties) {

		if (rdfID == null) {
			return null;
		}

		Individual individual = null;

		if (rdfID.indexOf(URLTemplate.URL_TEMPLATE) != -1) {
			individual = getModel().getIndividual(rdfID);
		} else {
			individual = getModel().getIndividual(URLTemplate.URL_TEMPLATE + "/" + className + "/" + rdfID);
		}

		if (individual == null) {
			return null;
		}

		List<Individual> individuals = new ArrayList<Individual>();
		
		individuals.add(individual);
		
		for (Property property : embeddedProperties) {
			List<Statement> stmts =
					getModel().listStatements(getModel().getResource(individual.getURI()), property, (RDFNode) null).toList();

			for (Statement stmt : stmts) {
				if (stmt.getObject().isResource()) {
					individuals.add(getModel().getIndividual(stmt.getObject().asResource().getURI()));
				}
			}
		}

		return OntologyUtil.listIndividualsToRDFXML(individuals.toArray(new Individual[individuals.size()]));
	}

	public String getIndividuals(OntResource classResource, List<Property> properties, List<Property> embeddedProperties, String namespace) {
		
		System.out.println("Entrou em DB.getIndividuals");
		//List<Individual> individuals = createModel().listIndividuals(classResource).toList();
		List<Individual> individuals = createIndividualsModel(classResource, properties, namespace).listIndividuals(classResource).toList();
		System.out.println("Retornou createIndividualsModel");
		List<Individual> embeddedIndividuals = new ArrayList<Individual>();

		if (individuals.isEmpty()) {
			return null;
		}
		
		for (Property property : embeddedProperties) {
			for (Individual individual : individuals) {
				
				List<Statement> stmts = getModel().listStatements(getModel().getResource(individual.getURI()), property, (RDFNode) null).toList();

				for (Statement stmt : stmts) {
					if (stmt.getObject().isResource()) {
						embeddedIndividuals.add(getModel().getIndividual(stmt.getObject().asResource().getURI()));
					}
				}
			}
		}

		individuals.addAll(embeddedIndividuals);
		
		return OntologyUtil.listIndividualsToRDFXML(individuals.toArray(new Individual[individuals.size()]));
	}


	public String getIndividuals(OntResource classResource, List<Property> embeddedProperties) {

		List<Individual> individuals = getModel().listIndividuals(classResource).toList();
		
		List<Individual> embeddedIndividuals = new ArrayList<Individual>();

		if (individuals.isEmpty()) {
			return null;
		}
		
		for (Property property : embeddedProperties) {
			for (Individual individual : individuals) {
				
				List<Statement> stmts =
						getModel().listStatements(getModel().getResource(individual.getURI()), property, (RDFNode) null).toList();

				for (Statement stmt : stmts) {
					if (stmt.getObject().isResource()) {
						embeddedIndividuals.add(getModel().getIndividual(stmt.getObject().asResource().getURI()));
					}
				}
			}
		}

		individuals.addAll(embeddedIndividuals);
		
		return OntologyUtil.listIndividualsToRDFXML(individuals.toArray(new Individual[individuals.size()]));
	}

	/**
	 * Apesar do nome do método, ele retorna uma instância RDF, contudo, a instância é retornada apenas com a
	 * propriedade solicitada.  
	 * 
	 * @param classResource
	 * @param rdfID
	 * @param propertyResource
	 * @return
	 * @throws DBQueryOperationException
	 */
	public String getPropertyValueOf(OntResource classResource, String rdfID, OntResource propertyResource)
			throws DBQueryOperationException {

		if (classResource == null || rdfID == null || propertyResource == null) {
			return null;
		}

		try {
			List<Statement> listStmt =
					OntologyUtil.getStatements(propertyResource.asProperty(), getIndividual(rdfID, classResource));
			
			if (listStmt == null || listStmt.isEmpty()) {
				throw new DBQueryOperationException(DBErrorMessage.GET_PROPERTY_NOT_DEFINED_IN_INSTANCE);
			}
			
			return OntologyUtil.listStatementsToRDFXML(classResource, listStmt.toArray(new Statement[listStmt.size()]));
			
		} catch (RDFXMLException e) {
			return null;
		}
	}

	public String postIndividual(OntModel modelOfInterface, OntResource classResource, String rdfXml) {
		
		// Converte o XML para uma lista de triplas que representam uma instância RDF.
		List<Statement> individualStatements = OntologyUtil.RDFXMLtoListStatements(rdfXml, classResource, modelOfInterface);
		
		// Gera ID do sujeito automático no HTTP POST.
		String rdfID = newSubjectURI(classResource);
		
		dataset.begin(ReadWrite.WRITE);
		Model model = dataset.getDefaultModel();
		
		Resource subject = model.createResource(rdfID, classResource);
		
		// Insere as triplas da instância no banco de dados.
		for (Statement statement : individualStatements) {
			model.add(subject, statement.getPredicate(), statement.getObject());
		}

		dataset.commit();
		dataset.end();
		
		return getIndividual(rdfID, classResource);
	}

	public String postProperty(OntModel modelOfInterface, OntResource classResource, String rdfID,
			OntResource propertyResource, String rdfXml) throws DBInsertOperationException {

		// Converte o XML para uma lista de triplas que representam uma instância RDF.
		List<Statement> individualStatements = OntologyUtil.RDFXMLtoListStatements(rdfXml, classResource, modelOfInterface);
		
		Resource subject = getModel().getIndividual(URLTemplate.URL_TEMPLATE + "/"+ classResource.getLocalName() + "/" + rdfID);
		
		// Verifica se a instância pertence a base de dados
		if (subject == null) {
			throw new DBInsertOperationException(DBErrorMessage.RDF_INSTANCE_OF_PROPERTY_WAS_NOT_LOCATED);
		}
		
		List<Statement> propertyToInsert = new ArrayList<Statement>();
		
		// Extrai do conjunto 'individualStatements' a propriedade a ser inserida, i.e., 'propertyResource'.
		// Senão todas as propriedades de rdfXml serão inseridas. 
		for (Statement stmt : individualStatements) {
			if (stmt.getPredicate().equals(propertyResource)) {
				propertyToInsert.add(stmt);
			}
		}

		// Se 'propertyToInsert' está vazio, significa que no 'rdfXml' não há um nova propriedade 'propertyResource'
		// para inserir
		if (propertyToInsert.isEmpty()) {
			throw new DBInsertOperationException(DBErrorMessage.POST_PROPERTY_NOT_DEFINED_IN_INSTANCE);
		}

		dataset.begin(ReadWrite.WRITE);
		Model model = dataset.getDefaultModel();

		// Insere as triplas da instância no banco de dados.
		for (Statement statement : propertyToInsert) {
			model.add(subject, statement.getPredicate(), statement.getObject());
		}

		dataset.commit();
		dataset.end();

		return getIndividual(rdfID, classResource);
	}
	
	/**
	 * Cria lista de hosts com os dados obtidos do banco.
	 * @throws ConfigurationException 
	 */
	public String getHostList(OntResource classResource, String interfaceName){
	
		Connection con = null;
  	    String statement = DbQuery.SELECT_HOST;
		List<Individual> individuals = new ArrayList<Individual>();

		try {
	 			con = db.getConnection();
	 			PreparedStatement prepared = con.prepareStatement(statement);
	 			prepared.setString(1, interfaceName);
	 			
	 			
	 			ResultSet reader = prepared.executeQuery();
	 							reader.first();
	 							while (!reader.isAfterLast())
	 			                {
	 			           		    Property predicado = getModel().getProperty("http://www.activeontology.com.br/serin.owl#address");
	 			           		    Individual individual = getModel().createIndividual("http://www.activeontology.com.br/serin.owl/Host/"+reader.getString("id_host_service"), classResource);
	 			           		    individual.addProperty(predicado, reader.getString("host_address"));
	 			                    individuals.add(individual);
	 			           		    reader.next();
	 			                }
	 							
	 							if (individuals.isEmpty()) {
	 								return null;
	 							}
	 							
	 			       		return OntologyUtil.listIndividualsToRDFXML(individuals.toArray(new Individual[individuals.size()]));
		} catch (SQLException sql) {
			System.out.println(sql.getMessage());
 		} finally {
	 			db.closeConnnection(con);
	 	}
		return null;

	}

	/**
	 * Cria lista de interfaces semânticas com os dados obtidos do banco.
	 * @throws ConfigurationException 
	 */
	public String getInterfaceList(OntResource classResource){
	
		Connection con = null;

		String statement = DbQuery.SELECT_INTERFACE;
		
		List<Individual> individuals = new ArrayList<Individual>();

		try {
	 			con = db.getConnection();
	 			PreparedStatement prepared = con.prepareStatement(statement);
	 			
	 			ResultSet reader = prepared.executeQuery();
	 							reader.first();
	 							while (!reader.isAfterLast())
	 			                {
	 			           		    Individual individual = getModel().createIndividual(reader.getString("uri"), classResource);
	 			                    individuals.add(individual);
	 			           		    reader.next();
	 			                }
	 							
	 							if (individuals.isEmpty()) {
	 								return null;
	 							}
	 							
	 			       		return OntologyUtil.listIndividualsToRDFXML(individuals.toArray(new Individual[individuals.size()]));
		} catch (SQLException sql) {
			System.out.println(sql.getMessage());
 		} finally {
	 			db.closeConnnection(con);
	 	}
		return null;

	}

	/**
	 * Inicia conexão com banco de dados.
	 * A string de conexão está configurada para banco MySql.
	 * @return
	 * @throws SQLException
	 */
	public Connection getConnection() throws SQLException {
		Connection con = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

	    String username = sdsProperty.get("username").toString();     
	    String password = sdsProperty.get("password").toString(); 
		con = DriverManager.getConnection(sdsProperty.get("connection").toString(),username,password);

		return con;
	}
	
	/**
	 * Fecha conexão com base de dados.
	 * @param con
	 */
	public void closeConnnection(Connection con) {
		try {
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Cria um modelo de ontologia contendo apenas individuos de uma base de dados TDB
	 * @param classResource
	 * @param namespace
	 * @return
	 */
		private OntModel createIndividualsModel(OntResource classResource, List<Property> properties, String namespace) {
			
			OntModel model = ModelFactory.createOntologyModel();
			
			
			String prefix = "loa";
			
			dataset.begin(ReadWrite.READ);
			
			//prefixo alterado quando se le indivíduos(hosts e interfaces) da própria ontologia SERIN
			model.setNsPrefix("serin", "http://www.activeontology.com.br/serin.owl#");
			model.setNsPrefix(prefix, namespace);
			
			/*querys usadas como exemplo durante os estudos.
			 *String searchQuery = "SELECT * WHERE { ?Orgao a <http://vocab.e.gov.br/2013/09/loa#Orgao>; <http://vocab.e.gov.br/2013/09/loa#codigo> ?codigo . } LIMIT 5";
			 *String searchQuery = "SELECT * WHERE { ?Orgao a <http://vocab.e.gov.br/2013/09/loa#Orgao>; <http://vocab.e.gov.br/2013/09/loa#codigo> ?codigo ;  <http://www.w3.org/1999/02/22-rdf-syntax-ns#label> ?label. } LIMIT 10";
			 *String searchQuery = "PREFIX loa: <http://vocab.e.gov.br/2013/09/loa#> SELECT ?UnidadeOrcamentaria ?codigo ?Orgao WHERE { ?UnidadeOrcamentaria a loa:UnidadeOrcamentaria; loa:codigo ?codigo; loa:temOrgao ?Orgao FILTER (?Orgao = <http://orcamento.dados.gov.br/2014/id/Orgao/26000>) .} LIMIT 50"; 

			String searchQuery = "PREFIX loa: <http://vocab.e.gov.br/2013/09/loa#>";
			searchQuery = searchQuery + "CONSTRUCT{"; 
			searchQuery = searchQuery + "?ItemDeDespesa a loa:ItemDeDespesa;";
			searchQuery = searchQuery + "loa:valorDotacaoInicial ?valorDotacaoInicial;";              
			searchQuery = searchQuery + "loa:valorPago ?valorPago;";
			searchQuery = searchQuery + "loa:temPrograma ?Programa;";
			searchQuery = searchQuery + "loa:temUnidadeOrcamentaria ?UnidadeOrcamentaria;";
			searchQuery = searchQuery + "loa:temAcao ?Acao ";
			searchQuery = searchQuery + ".}";

			//Alterando query SELECT para query CONSTRUCT
			//searchQuery = searchQuery + "	SELECT ?ItemDeDespesa ?UnidadeOrcamentaria ?Programa ?valorDotacaoInicial ?valorPago";
			searchQuery = searchQuery + "WHERE { ";
			searchQuery = searchQuery + "?ItemDeDespesa a loa:ItemDeDespesa;";
			searchQuery = searchQuery + "                         loa:valorDotacaoInicial ?valorDotacaoInicial;";
			searchQuery = searchQuery + "                         loa:valorPago ?valorPago;";
			searchQuery = searchQuery + "                         loa:temPrograma ?Programa;";
			searchQuery = searchQuery + "                         loa:temUnidadeOrcamentaria ?UnidadeOrcamentaria;";
			searchQuery = searchQuery + "                         loa:temAcao ?Acao .";
			searchQuery = searchQuery + "} LIMIT 50";
			 */
			
			//Desenvolvimento da query dinâmica a partir da lista de propriedades
			String searchQuery = "PREFIX "+prefix+": <"+model.getNsPrefixURI(prefix)+">";
			searchQuery = searchQuery + "CONSTRUCT{ "; 
			searchQuery = searchQuery + "?"+classResource.getLocalName()+" a "+prefix+":"+classResource.getLocalName()+". ";
			for (Property property : properties) {
				searchQuery = searchQuery + "?"+classResource.getLocalName()+" "+ prefix+":"+ property.getLocalName()+" ?"+property.getLocalName()+".";
			}
			searchQuery = searchQuery + " } ";
			searchQuery = searchQuery + " WHERE { ";
			searchQuery = searchQuery + "?"+classResource.getLocalName()+" a "+prefix+":"+classResource.getLocalName()+". ";
			for (Property property : properties) {
				searchQuery = searchQuery + "OPTIONAL { ?"+classResource.getLocalName()+" " +prefix+":"+ property.getLocalName()+" ?"+property.getLocalName()+".} ";
			}
			searchQuery = searchQuery + "} LIMIT 50";
			
			Query query = QueryFactory.create(searchQuery,Syntax.syntaxSPARQL_11);
			QueryExecution qexec = QueryExecutionFactory.create(query, dataset);
			try {
			 model.add(qexec.execConstruct());
			 /*
			  * Esse trecho de código foi uma tentativa de obter os resultados através de uma query SELECT.
			  * O SELECT traz os resultados em forma de tabela.
			  * A lógica desse trecho era converter os dados obtidos, na forma de tabela, em statements para serem inseridos no model
			  * em forma RDF.
			  * Posteriormente, descobri como gerar os resultados já em formato RDF usando a query CONSUL
			 com.hp.hpl.jena.query.ResultSet rs= qexec.execSelect();
			 while (rs.hasNext()) {
				    QuerySolution solution = rs.next();
				    rs.getResourceModel();
				    Iterator<String> iterator = solution.varNames();
				    Individual individual = null;
				    Property property = null;
				    RDFNode object = null;
				    Literal literal = null;
				    List<Statement> statements = new ArrayList<Statement>();
				    while (iterator.hasNext()){
				    	String varName = iterator.next();
				    	if (solution.get(varName) instanceof Literal){
				    		property = model.getProperty(namespace+varName);
				    		literal = solution.getLiteral(varName);
				    		Statement statement = ResourceFactory.createStatement(ResourceFactory.createResource(),property,literal);
				    		statements.add(statement);
				    	}
				    	if (solution.get(varName) instanceof Resource){
				    		if ((namespace+varName).equals(classResource.getURI())){
					    		OntClass c = model.createClass(namespace+varName);
						    	individual = c.createIndividual(solution.getResource(varName).toString());
				    		}
				    		else{
					    		property = model.getProperty(namespace+varName);
					    		object = solution.getResource(varName);
					    		Statement statement = ResourceFactory.createStatement(ResourceFactory.createResource(),property,object);
					    		statements.add(statement);
				    		}
				    	}
				    }
				    for (Statement statement: statements) {
						property = statement.getPredicate();
						literal = statement.getLiteral();
					    //individual.addProperty(property, literal);
						model.add(individual,property,literal);
					}
				    
				  }*/
			} catch (Exception e){
				e.getMessage();
			}
			finally {
			  qexec.close();
			}
			
			dataset.end();
			
			return model;
		}


}
