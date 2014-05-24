package br.unifor.mia.sds.requesthandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Properties;

import br.unifor.mia.sds.persistence.DB;
import br.unifor.mia.sds.persistence.DBException;
import br.unifor.mia.sds.util.FileUtil;

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
	 *  
	 * @throws IOException
	 */
	private Properties initialization() throws IOException {
		sdsProperty = new Properties();
		sdsProperty.load(getClass().getClassLoader().getResourceAsStream(configFile));
		
		return sdsProperty;
	}

	 

	public String get_interface_list() throws IOException {
		return FileUtil.getContent(configFile);
	}
	

	public String get_interface(String interfaceKey) {
		
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
		
		} catch (IOException ioException) {
			ioException.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		
		return "Interface não está online.";
	}
	

	public String get_individual_list(String interfaceKey, String className)
			throws IOException, SERINMalFormedException, AnnotationlessException {

		// Carrega a interface associada a essa requisição
		SERINOntologyController controller =
				new SERINOntologyController(initialization().get(interfaceKey).toString());
		
		// Verifica se a classe 'className' pode ser lida, isto é, possui a anotação GET definida na interface
		controller.checkPermission(className, SerinAnnotations.GET);
		
		/*
		 *  Verifica se a classe 'className' está definida na interface e busca
		 * a lista de individuos da classe 'className'.
		 */ 
		String individuals = DB.listIndividuals(controller.lookup(className));

		return individuals;
	}

	public String get_individual(String interfaceKey, String className, String rdfID)
			throws IOException, MembershipException, SERINMalFormedException, AnnotationlessException {
	
		// Carrega a interface associada a essa requisição
		SERINOntologyController controller =
				new SERINOntologyController(initialization().get(interfaceKey).toString());

		// Verifica se a classe 'className' pode ser lida, isto é, possui a anotação GET definida na interface
		controller.checkPermission(className, SerinAnnotations.GET);

		/*
		 * Método 'lookup' verifica se a classe 'className' do individuo está definida na interface e
		 * 'isMembership' verifica se o individuo pertence a classe 'className'
		 */
		if (DB.isMembership(rdfID, controller.lookup(className))) {
			// Busca o individuo na base de dados 
			return DB.getIndividual(rdfID, controller.lookup(className));
		} else {
			throw new MembershipException(SDSErrorMessage.NOT_MEMBERSHIP);
		}
	}

	public String get_property_value(String interfaceKey, String className, String rdfID, String propertyName)
			throws IOException, MembershipException, AnnotationlessException, SERINMalFormedException {

		// Carrega a interface associada a essa requisição
		SERINOntologyController controller =
				new SERINOntologyController(initialization().get(interfaceKey).toString());

		// Verifica se a classe 'className' pode ser lida, isto é, possui a anotação GET definida na interface
		controller.checkPermission(className, SerinAnnotations.GET);

		/*
		 * Verifica se a classe 'className' do individuo está definida na interface e
		 * Verifica se o individuo é realmente do tipo 'className'
		 */
		if (DB.isMembership(rdfID, controller.lookup(className))) {
			
			// Verifica se a propriedade 'propertyName' tem realmente a 'className' em seu domínio.
			controller.checkDomain(propertyName, className);
			
			// Busca o individuo na base de dados 
			try {
				return DB.getProperty(controller.lookup(className), rdfID, controller.lookup(propertyName));
			} catch (DBException e) {
				throw new SERINMalFormedException(e.getMessage());	
			}
		} else {
			throw new MembershipException(SDSErrorMessage.NOT_MEMBERSHIP);
		}
	}

	public String post_individual(String interfaceKey, String className,	String rdfXml)
			throws IOException, AnnotationlessException, SERINMalFormedException {

		// Carrega a interface associada a essa requisição
		SERINOntologyController controller =
				new SERINOntologyController(initialization().get(interfaceKey).toString());

		// Verifica se a classe 'className' pode ser persistida, isto é, possui a anotação POST definida na interface
		controller.checkPermission(className, SerinAnnotations.POST);

		try{
			// TODO [integrity_constraint] Verifica se todas as restrições de integridade da instancia estão satisfeitas

			
			// Insere uma instancia RDF na base de dados. 
			return DB.postIndividual(controller.getOntModelOfInterface(), controller.lookup(className), rdfXml); 
		} catch (Exception e) {
			throw new SERINMalFormedException(e.getMessage());
		}		
	}

	public String post_property_value(String interfaceKey, String className, String rdfID, String propertyName,
			String rdfXml) throws IOException, AnnotationlessException, SERINMalFormedException {

		// Carrega a interface associada a essa requisição
		SERINOntologyController controller = new SERINOntologyController(initialization().get(interfaceKey).toString());

		// Verifica se a classe 'className' pode ser persistida, isto é, possui
		// a anotação POST definida na interface
		controller.checkPermission(className, SerinAnnotations.POST);

		try {
			// TODO [integrity_constraint] Verifica se todas as restrições de integridade da instancia estão satisfeitas
			

			// Insere uma instancia RDF na base de dados.
			return DB.postProperty(controller.getOntModelOfInterface(), controller.lookup(className), rdfID,
					controller.lookup(propertyName), rdfXml);
		} catch (Exception e) {
			throw new SERINMalFormedException(e.getMessage());
		}
	}
}
