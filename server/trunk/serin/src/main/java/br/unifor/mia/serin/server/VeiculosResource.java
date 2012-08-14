package br.unifor.mia.serin.server;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.jboss.resteasy.annotations.providers.jaxb.Wrapped;

import br.unifor.mia.Veiculo;

@Path("/veiculos")
public class VeiculosResource {

	private final static ConcurrentMap<String, Veiculo> veiculoMap = new ConcurrentHashMap<String, Veiculo>();
	
	public ConcurrentMap<String, Veiculo> getVeiculoMap() {
		return veiculoMap;
	}

    @POST
    @Consumes(MediaType.TEXT_XML)
	public Response setVeiculo(Veiculo veiculo) {
   		// Register the new item if one is not already registered.
   		getVeiculoMap().putIfAbsent(veiculo.getMarca(), veiculo);
    	return Response.status(Status.CREATED).build();
	}
	
	@GET
	@Wrapped(element = "veiculos")
	@Produces(MediaType.TEXT_XML)
	public Collection<Veiculo> getVeiculos() {
		return getVeiculoMap().values();
	}
}
