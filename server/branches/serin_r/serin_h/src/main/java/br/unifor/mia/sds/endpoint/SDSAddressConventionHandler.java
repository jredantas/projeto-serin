package br.unifor.mia.sds.endpoint;

import java.io.IOException;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import br.unifor.mia.sds.requesthandler.ConfigurationException;
import br.unifor.mia.sds.requesthandler.SDSException;
import br.unifor.mia.sds.requesthandler.SDSRequestHandler;
import br.unifor.mia.sds.util.RDFXMLException;
import br.unifor.mia.sds.util.URLTemplate;


/**
 * A responsabilidade dessa classes é tratar a convenção de endereçamento.
 * 
 * @author Hermano
 *
 */
@Consumes("application/xml") //application/rdf+xml
@Produces("application/xml") //application/rdf+xml
@Path("/")
public class SDSAddressConventionHandler {

	private static final String CONFIG_FILE = "sds.properties";
	
	private SDSRequestHandler requestHandler = new SDSRequestHandler(CONFIG_FILE);
	
	@Context
	private UriInfo info;

	/**
	 * Método GET.
	 * 
	 * @return
	 * Retorna A lista de interfaces SERIN disponíveis no Servidor.
	 * 
	 * @throws IOException 
	 */
	@GET
	@Path("/getInterfaceList")
	public Response get_interface_list() {
		try {
			return Response.ok(requestHandler.getInterfaceList()).build();
		} catch (ConfigurationException e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		}
	}
	
	/**
	 * Método GET.
	 * 
	 * @param iHostName Nome do Host do namespace da interface SERIN. (Ex. www.unifor.br)
	 * @param interfaceName Nome do arquivo .owl da interface SERIN. (Ex. clinic.owl)
	 * @return
	 * Retorna uma interface SERIN em particular.
	 * 
	 * @throws IOException
	 */
	@GET
	@Path("{iHostName}/{interfaceName}")
	public Response get_interface(@PathParam("iHostName") String iHostName,
			@PathParam("interfaceName") String interfaceName) {
		
		String interfaceKey = iHostName + "_" + interfaceName;

		try {
			return Response.ok(requestHandler.get_interface(interfaceKey)).build();
		} catch (ConfigurationException e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		}
	}

	/**
	 * Método GET
	 * 
	 * @param iHostName Nome do Host do namespace da interface SERIN. (Ex. www.unifor.br)
	 * @param interfaceName Nome do arquivo .owl da interface SERIN. (Ex. clinic.owl)
	 * @param className Nome de uma classe definida na interface SERIN. (Ex. Clinic)
	 * @return
	 * Retorna um lista de individuos de uma Classe.
	 */
	@GET
	@Path("{iHostName}/{interfaceName}/{className}")
	public Response get_individual_list(@PathParam("iHostName") String iHostName,
			@PathParam("interfaceName") String interfaceName, @PathParam("className") String className) {
		
		String interfaceKey = iHostName + "_" + interfaceName;
		
		try {
			String individuals = requestHandler.get_individual_list(interfaceKey, className);
			
			if (individuals == null) {
				return Response.status(Status.NOT_FOUND).entity(HTTPErrorMessage.RESOURCE_NOT_FOUND).build();
			}
		
			String urlOfInterfaceInSDSServer = info.getAbsolutePath().toString().substring(0, 
					info.getAbsolutePath().toString().indexOf(className)-1);
			
			return Response.ok(URLTemplate.decode(individuals, urlOfInterfaceInSDSServer)).build();
			
		} catch (SDSException e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		} catch (ConfigurationException e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		}
	}
	
	/**
	 * Método GET
	 * 
	 * @param iHostName
	 *            Nome do Host do namespace da interface SERIN. (Ex.
	 *            www.unifor.br)
	 * @param interfaceName
	 *            Nome do arquivo .owl da interface SERIN. (Ex. clinic.owl)
	 * @param className
	 *            Nome de uma classe definida na interface SERIN. (Ex. Clinic)
	 * @param rdfID
	 *            URL identificadora de um individuo mantido na base do servidor
	 *            SDS (Ex.
	 *            http://localhost:8080/serinsds/www.unifor.br/clinic.owl
	 *            /Clinic/3)
	 * 
	 * @return Retorna um individuo em particular.
	 */
	@GET
	@Path("{iHostName}/{interfaceName}/{className}/{rdfID}")
	public Response get_individual(@PathParam("iHostName") String iHostName,
			@PathParam("interfaceName") String interfaceName, @PathParam("className") String className,
			@PathParam("rdfID") String rdfID) {
	
		String interfaceKey = iHostName + "_" + interfaceName;
		
		try {
			
			String individual = requestHandler.get_individual(interfaceKey, className, rdfID);
			
			if (individual == null) {
				return Response.status(Status.NOT_FOUND).entity(HTTPErrorMessage.RESOURCE_NOT_FOUND).build();
			}
		
			String urlOfInterfaceInSDSServer = info.getAbsolutePath().toString().substring(0, 
					info.getAbsolutePath().toString().indexOf(className)-1);
			
			return Response.ok(URLTemplate.decode(individual, urlOfInterfaceInSDSServer)).build();
			
		} catch (SDSException e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		} catch (ConfigurationException e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		}
	}
	
	/**
	 * Método GET
	 * 
	 * @param iHostName
	 *            Nome do Host do namespace da interface SERIN. (Ex.
	 *            www.unifor.br)
	 * @param interfaceName
	 *            Nome do arquivo .owl da interface SERIN. (Ex. clinic.owl)
	 * @param className
	 *            Nome de uma classe definida na interface SERIN. (Ex. Clinic)
	 * @param rdfID
	 *            URL identificadora de um individuo mantido na base do servidor
	 *            SDS (Ex.
	 *            http://localhost:8080/serinsds/www.unifor.br/clinic.owl
	 *            /Clinic/3)
	 * @param propertyName
	 *            URL identificadora do valor de uma propriedade de um individuo mantido na base do servidor
	 *            SDS (Ex. http://localhost:8080/serinsds/www.unifor.br/clinic.owl/Clinic/3/attendedBy)
	 * 
	 * @return Retorna o valor de uma propriedade de um individuo.
	 * @throws RDFXMLException 
	 */
	@GET
	@Path("{iHostName}/{interfaceName}/{className}/{rdfID}/{propertyName}")
	public Response get_property_value(@PathParam("iHostName") String iHostName,
			@PathParam("interfaceName") String interfaceName, @PathParam("className") String className,
			@PathParam("rdfID") String rdfID, @PathParam("propertyName") String propertyName) {
		
		
		String interfaceKey = iHostName + "_" + interfaceName;
		
		try {
			
			String individual = requestHandler.get_property_value(interfaceKey, className, rdfID, propertyName);
			
			if (individual == null) {
				return Response.status(Status.NOT_FOUND).entity(HTTPErrorMessage.RESOURCE_NOT_FOUND).build();
			}
		
			String urlOfInterfaceInSDSServer = info.getAbsolutePath().toString().substring(0, 
					info.getAbsolutePath().toString().indexOf(className)-1);
			
			return Response.ok(URLTemplate.decode(individual, urlOfInterfaceInSDSServer)).build();
			
		} catch (SDSException e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		} catch (ConfigurationException e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		} 
	}
	
	@POST
	/**
	 * Método POST
	 * 
	 * POST de um individuo (e apenas UM)
	 * 
	 * @return
	 */
	@Path("{iHostName}/{interfaceName}/{className}")
	public Response post_individual(@PathParam("iHostName") String iHostName,
			@PathParam("interfaceName") String interfaceName, @PathParam("className") String className, String rdfXml) {
		
		// Verifica se o corpo da requisição não é nulo.
		if (rdfXml == null || "".equals(rdfXml)) {
			return Response.status(Status.BAD_REQUEST).entity("A RDF instance must be present in the request boby.").build();
		}
		
		String interfaceKey = iHostName + "_" + interfaceName;
		
		String urlOfInterfaceInSDSServer = info.getAbsolutePath().toString().substring(0, 
				info.getAbsolutePath().toString().indexOf(className)-1);

		try {
			rdfXml = URLTemplate.encode(rdfXml, urlOfInterfaceInSDSServer);
			
			String individual = requestHandler.post_individual(interfaceKey, className, rdfXml);

			return Response.status(Status.CREATED).entity(URLTemplate.decode(individual, urlOfInterfaceInSDSServer)).build();
			
		} catch (SDSException e) {
			String msg = URLTemplate.decode(e.getMessage(), urlOfInterfaceInSDSServer);
			return Response.status(Status.BAD_REQUEST).entity(msg).build();
		} catch (ConfigurationException e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		} catch (RDFXMLException e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}
	
	/**
	 * Método POST
	 * 
	 * Eu posso dá um POST de uma propriedade? (Sim, mas apenas se a propriedade for não funcional)
	 * 
	 * @param iHostName
	 *            Nome do Host do namespace da interface SERIN. (Ex.
	 *            www.unifor.br)
	 * @param interfaceName
	 *            Nome do arquivo .owl da interface SERIN. (Ex. clinic.owl)
	 * @param className
	 *            Nome de uma classe definida na interface SERIN. (Ex. Clinic)
	 * @param rdfID
	 *            URL identificadora de um individuo mantido na base do servidor
	 *            SDS (Ex.
	 *            http://localhost:8080/serinsds/www.unifor.br/clinic.owl
	 *            /Clinic/3)
	 * @param propertyName
	 *            URL identificadora do valor de uma propriedade de um individuo mantido na base do servidor
	 *            SDS (Ex. http://localhost:8080/serinsds/www.unifor.br/clinic.owl/Clinic/3/attendedBy)
	 * 
	 * @return Retorna o individuo atualizado como a nova propriedade.
	 * @throws RDFXMLException 
	 */
	@POST
	@Path("{iHostName}/{interfaceName}/{className}/{rdfID}/{propertyName}")
	public Response post_property_value(@PathParam("iHostName") String iHostName,
			@PathParam("interfaceName") String interfaceName, @PathParam("className") String className,
			@PathParam("rdfID") String rdfID, @PathParam("propertyName") String propertyName, String rdfXml) {

		// Verifica se o corpo da requisição não é nulo.
		if (rdfXml == null || "".equals(rdfXml)) {
			return Response.status(Status.BAD_REQUEST).entity("A RDF instance must be present in the request boby.").build();
		}
		
		String interfaceKey = iHostName + "_" + interfaceName;
		
		try {
			String urlOfInterfaceInSDSServer = info.getAbsolutePath().toString().substring(0, 
					info.getAbsolutePath().toString().indexOf(className)-1);
			
			rdfXml = URLTemplate.encode(rdfXml, urlOfInterfaceInSDSServer);
			
			String individual = requestHandler.post_property_value(interfaceKey, className, rdfID, propertyName, rdfXml);

			return Response.status(Status.CREATED).entity(URLTemplate.decode(individual, urlOfInterfaceInSDSServer)).build();
			
		} catch (SDSException e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		} catch (ConfigurationException e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		} catch (RDFXMLException e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}
	
	@PUT
	public String put() {
		return "método PUT não implementado";
	}
	
	@DELETE
	public String delete() {
		return "método DELETE não implementado";
	}
}
