package br.unifor.mia.sds.requesthandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ByteArrayOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Properties;


import br.unifor.mia.sds.interfacemanager.AnnotationlessException;
import br.unifor.mia.sds.interfacemanager.SERINException;
import br.unifor.mia.sds.interfacemanager.SERINManager;
import br.unifor.mia.sds.interfacemanager.integrityconstraint.DBHandler;
import br.unifor.mia.sds.persistence.DB;
import br.unifor.mia.sds.persistence.DBInsertOperationException;
import br.unifor.mia.sds.persistence.DBQueryOperationException;
import br.unifor.mia.sds.util.FileUtil;
import br.unifor.mia.sds.util.RDFXMLException;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Responsabilidade da classe 'SDSRequestHandler' é tratar a requisição, isto é,
 * verificar se a URL mapeia uma entidade da base de dados e verificar se os
 * dados passados na requisição possuem uma estrutura válida. Por estrutura
 * válida entende-se:
 * 1 - o individuo pertence a uma classe descrita na interface
 * 2 - as propriedaes do individuo pertencem a uma propriedade descrita na interface
 * 3 - oq+?
 */
public class SDSRequestHandler {
	
	/**
	 * Arquivo de propriedades chave-valor.
	 * 
	 * chave: a chave é a interfaceKey, isso é, a concatenação do iHostName + "_" + interfaceName
	 * valor: o valor é a URL onde a interface SERIN está publicada. 
	 */
	private Properties sdsProperty;
	
	/**
	 * Nome do arquivo de propriedades chave-valor instanciado no atributo 'sdsProperty'.
	 */
	private String configFile;
	
	private DBHandler dbHandler = new DBHandler() {
		@Override
		public boolean contains(String uri) {
			return DB.getInstance().contains(uri);
		}

		@Override
		public boolean isMembership(Resource resource, Resource classResource) {
			return DB.getInstance().isMembership(resource, classResource);
		}

		@Override
		public String getIndividual(String className, String rdfID, List<Property> embeddedProperties) {
			return DB.getInstance().getIndividual(className, rdfID, embeddedProperties);
		}

		@Override
		public String getIndividuals(OntResource classResource, List<Property> embeddedProperties) {
			return DB.getInstance().getIndividuals(classResource, embeddedProperties);
		}
	};
	
	/**
	 * Construtor.
	 */
	public SDSRequestHandler(String configFile) {
		this.configFile = configFile;
	}

	/**
	 * Carrega o objeto 'sdspProperty' com a lista de URLs de interfaces SERIN disponíveis na Web para o servidor SDS.
	 * 
	 * @return 
	 * @throws ConfigurationException 
	 *  
	 * @throws IOException
	 */
	private Properties initialization() throws ConfigurationException {
		
		sdsProperty = new Properties();
		
		try {
			sdsProperty.load(getClass().getClassLoader().getResourceAsStream(configFile));
		} catch (IOException e) {
			throw new ConfigurationException("Arquivo de configuração do servidor SDS não localizado.");
		}
		
		return sdsProperty;
	}

	public String getHostList(String interfaceName) throws ConfigurationException {
		try {
			
			//List<Host> lista = DB.getInstance().getHostList(interfaceName);
			//XStream xStream = new XStream(new DomDriver());
			//xStream.alias("host", Host.class);
			//xStream.alias("lista", List.class);

			//String xmlString = xStream.toXML(lista);
			
			//Cria os dados em padrão RDF
			// Carrega a interface associada a essa requisição
			//SERINManager iManager = new SERINManager(RequestAnnotations.GET, initialization().get("localhost_serin.owl").toString());
			OntModel model = DB.getInstance().getHostList(interfaceName);
			
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			
			model.write(stream, "RDF/XML-ABBREV");

			String rdfXml = new String(stream.toByteArray());
			
		    return rdfXml;
			
		} catch(NullPointerException e) {
			throw new ConfigurationException("No host attends this SERIN interface.", e);
		} //catch (IOException e) {
			// TODO Auto-generated catch block
		//	throw new ConfigurationException("No host attends this SERIN interface.");
		//}
	}


	public String getInterfaceList() throws ConfigurationException {
		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
		    initialization().storeToXML(os, "Available SERIN interfaces", "UTF-8");
		    String xmlString = os.toString();

		    return xmlString;
		} catch(NullPointerException e) {
			throw new ConfigurationException("No SERIN interface found.");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			throw new ConfigurationException("No SERIN interface found.");
		}
	}

	public String get_interface_list() throws ConfigurationException {
		try {
			return FileUtil.getContent(configFile);
		} catch (IOException e) {
			throw new ConfigurationException("Arquivo de configuração do servidor SDS não localizado.");
		}
	}

	public String get_interface(String interfaceKey) throws ConfigurationException {

			try {
				URL urlInterface = new URL(initialization().get(interfaceKey).toString());
			
				InputStream inInsertStatement = urlInterface.openStream();
				
				BufferedReader stream = new BufferedReader(new InputStreamReader(inInsertStatement));
		
				StringBuilder fileContent = new StringBuilder();
		
				String currentLine = stream.readLine();
		
				while (currentLine != null) {
					fileContent.append(currentLine);
					currentLine = stream.readLine();
				}
		
				return fileContent.toString();
				
			} catch (MalformedURLException e) {
			throw new ConfigurationException(
					"Uma URL mal formada foi definida no arquivo de configuração (sds.properties) do servidor SDS.");
			} catch (IOException e) {
				throw new ConfigurationException("Interface SERIN não está online.");
			} catch(NullPointerException e) {
				throw new ConfigurationException("Interface SERIN não localizada.");
			}
	}
	

	public String get_individual_list(String interfaceKey, String className) throws ConfigurationException, SDSException {

		// Carrega a interface associada a essa requisição
		SERINManager iManager = new SERINManager(RequestAnnotations.GET, initialization().get(interfaceKey).toString());
		
		try {
			// Verifica se a classe 'className' pode ser lida, isto é, possui a anotação GET definida na interface
			iManager.checkPermission(className);

			/*
			 *  Método 'lookup' verifica se a classe 'className' está definida na interface.
			 *  Método 'getIndividuals' busca a lista de individuos da classe 'className'.
			 *  Método 'getIndividuals' também traz todos os individuos embedded associados aos
			 *  da classe 'className'. 
			 */ 
			return iManager.getIndividuals(iManager.lookup(className), dbHandler);
			
		} catch (AnnotationlessException e) {
			throw new SDSException(e.getMessage());
		} catch (SERINException e) {
			throw new SDSException(e.getMessage());
		}
	}

	public String get_individual(String interfaceKey, String className, String rdfID)
			throws ConfigurationException, SDSException {
	
		// Carrega a interface associada a essa requisição
		SERINManager iManager = new SERINManager(RequestAnnotations.GET, initialization().get(interfaceKey).toString());

		try {
			// Verifica se a classe 'className' pode ser lida, isto é, possui a anotação GET definida na interface
			iManager.checkPermission(className);
			
			/*
			 * Método 'lookup' verifica se a classe 'className' do individuo está definida na interface e
			 * 'isMembership' verifica se o individuo (rdfID) pertence a classe 'className'
			 */
			if (DB.getInstance().isMembership(rdfID, iManager.lookup(className))) {
				// Busca o objeto 'rdfID' e todos os individuos embedded na base de dados 
				return iManager.getIndividual(className, rdfID, dbHandler);
			} else {
				throw new MembershipException();
			}
			
		} catch (AnnotationlessException e) {
			throw new SDSException(e.getMessage());
		} catch (SERINException e) {
			throw new SDSException(e.getMessage());
		}
	}

	/**
	 * TODO falta implementar a busca pelo composite (embedded) individivuo
	 */
	public String get_property_value(String interfaceKey, String className, String rdfID, String propertyName)
			throws SDSException, ConfigurationException {

		// Carrega a interface associada a essa requisição
		SERINManager iManager = new SERINManager(RequestAnnotations.GET, initialization().get(interfaceKey).toString());

		try {
			// Verifica se a classe 'className' pode ser lida, isto é, possui a anotação GET definida na interface
			iManager.checkPermission(className);
	
			/*
			 * Verifica se a classe 'className' do individuo está definida na interface e
			 * Verifica se o individuo é realmente do tipo 'className'
			 */
			if (DB.getInstance().isMembership(rdfID, iManager.lookup(className))) {
				
				// Verifica se o domínio da propriedade 'propertyName' é realmente a 'className'.
				iManager.checkDomain(propertyName, className);
				
				// Busca o individuo na base de dados
				return DB.getInstance().getPropertyValueOf(iManager.lookup(className), rdfID, iManager.lookup(propertyName));
			} else {
				throw new MembershipException();
			}
		
		} catch (SERINException e) {
			throw new SDSException(e.getMessage());
		} catch (DBQueryOperationException e) {
			throw new SDSException(e.getMessage());
		}
		
	}

	public String post_individual(String interfaceKey, String className, String rdfXml)
			throws ConfigurationException, SDSException, RDFXMLException {

		// Carrega a interface associada a essa requisição
		SERINManager iManager = new SERINManager(RequestAnnotations.POST, initialization().get(interfaceKey).toString());

		// Verifica se a classe 'className' pode ser persistida, isto é, possui a anotação POST definida na interface
		try {
			iManager.checkPermission(className);

			// Verifica se todas as restrições de integridade da instancia estão satisfeitas
			iManager.checkIntegrityConstraint(className, dbHandler, rdfXml);

			// Insere uma instancia RDF na base de dados. 
			return DB.getInstance().postIndividual(iManager.getOntModelOfInterface(), iManager.lookup(className), rdfXml); 
			
		} catch (SERINException e) {
			throw new SDSException(e.getMessage());
		}
		
	}

	public String post_property_value(String interfaceKey, String className, String rdfID, String propertyName,
			String rdfXml) throws ConfigurationException, SDSException, RDFXMLException {

		// Carrega a interface associada a essa requisição
		SERINManager iManager = new SERINManager(RequestAnnotations.POST, initialization().get(interfaceKey).toString());

		// Verifica se a classe 'className' pode ser persistida, isto é, possui
		// a anotação POST definida na interface
		try {
			iManager.checkPermission(className);

			// Verifica se todas as restrições de integridade da instancia estão satisfeitas
			iManager.checkIntegrityConstraint(className, dbHandler, rdfXml);

			// Insere uma instancia RDF na base de dados.
			return DB.getInstance().postProperty(iManager.getOntModelOfInterface(), iManager.lookup(className), rdfID,
						iManager.lookup(propertyName), rdfXml);			

		} catch (SERINException e) {
			throw new SDSException(e.getMessage());
		} catch (DBInsertOperationException e) {
			throw new SDSException(e.getMessage());
		}		
	}
}
