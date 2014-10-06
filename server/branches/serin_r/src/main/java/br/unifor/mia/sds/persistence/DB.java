package br.unifor.mia.sds.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import br.unifor.mia.sds.entity.Host;
import br.unifor.mia.sds.requesthandler.ConfigurationException;
import br.unifor.mia.sds.util.OntologyUtil;
import br.unifor.mia.sds.util.RDFXMLException;
import br.unifor.mia.sds.util.URLTemplate;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.vocabulary.RDF;

public final class DB {
	
	// Make a TDB-backed dataset
	private String DB_DIRECTORY = "/home/09959295800/Dropbox/Doutorado/ontologia/loa2014";
	public static final String SELECT_INTERFACE = "SELECT * FROM host_service WHERE interface_name = ?";
	
	private Dataset dataset = TDBFactory.createDataset(DB_DIRECTORY);
	
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
		/*try {
			dataset.begin(ReadWrite.WRITE);

			Model model = dataset.getDefaultModel();
			
			 Carregar alguns Dados de exemplo 
			String insertString = FileUtil.getContent("CLINIC_INSERT_DATA.txt");
			UpdateRequest request = UpdateFactory.create(insertString);
			UpdateAction.execute(request, model);
			
			dataset.commit();
			
			dataset.end();

		} catch (IOException e) {
			e.printStackTrace();
		}*/
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
	 * Inicia conexão com banco de dados.
	 * A string de conexão está configurada para banco MySql.
	 * @return
	 * @throws SQLException
	 */
	public Connection getConnection() throws SQLException {
		Connection con = null;
	    String username = "root";     
	    String password = ""; 
		con = DriverManager.getConnection("jdbc:mysql://localhost:3306/serin_discovery",username,password);

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
	 * Cria lista com os dados obtidos do banco.
	 */
	public List<Host> getHostList(String interfaceName){
		Connection con = null;
  	    String statement = db.SELECT_INTERFACE;
		List<Host> lista = new ArrayList<Host>();
		try {
	 			con = db.getConnection();
	 			PreparedStatement prepared = con.prepareStatement(statement);
	 			prepared.setString(1, interfaceName);
	 			
	 			ResultSet reader = prepared.executeQuery();
	 							reader.first();
	 			                while (!reader.isLast())
	 			                {
	 			                    Host host = new Host();
	 			                    host.setAddress(reader.getString("host_address"));
	 			                    lista.add(host);
	 			                    reader.next();
	 			                }
		} catch (SQLException sql) {
 			throw new ConfigurationException(sql.getMessage());
 		} finally {
	 			db.closeConnnection(con);
	 		 	return lista;
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
}
