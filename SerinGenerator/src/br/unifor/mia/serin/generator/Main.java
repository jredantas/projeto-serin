package br.unifor.mia.serin.generator;
import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

//import javax.xml.transform.Result;








import javax.net.ssl.SSLPeerUnverifiedException;
import javax.ws.rs.core.Response.Status;

import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.Element;

import com.google.gson.Gson;

import br.unifor.mia.serin.client.*;
import br.unifor.mia.serin.util.Db;

public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("Discovery agent started...");
		/**
		 * Variável para criar query de inserção dos registros na base de dados
		 */
		final String INSERT_HOST = "INSERT INTO host_service (host_address,interface_uri, host_protocol) VALUES (?,?,?)";
		
		/**
		 * Usa a API de busca da Google, Google Search, para criar uma lista de endereços de servidores
		 * que pertencem ao domínio gov.br.
		 * 
		 */
		List<GoogleResults.Result> listResults = new ArrayList<GoogleResults.Result>();
	    for(int i = 0; i < 1; i++) {
	    	try{
	  	      String address = "http://ajax.googleapis.com/ajax/services/search/web?v=1.0&start=" + i * 4 + "&q=";
		      String query = "gov.br";
		      String charset = "UTF-8";

		      URL url = new URL(address + URLEncoder.encode(query, charset));
		      Reader reader = new InputStreamReader(url.openStream(), charset);
		      GoogleResults results = new Gson().fromJson(reader, GoogleResults.class);
		      listResults.addAll(results.getResponseData().getResults());
	    		
	    	}
	    	catch(Exception e){
	    		System.out.println("Não foi possível recuperar mais endereços.");
	    		break;
	    	}
	    }
	    
	    listResults.add(new GoogleResults.Result("http://localhost:8080/","Servidor de teste local", "localhost:8080"));
	    
	    //Iterator iterator = listResults.iterator();

	    //int i = 0;
		
	    /*while (iterator.hasNext()){
		    //System.out.print(listResults.get(i).getTitle()+" - ");
		    System.out.println(listResults.get(i).getUrl());
	    	//System.out.println(iterator.toString());
	    	System.out.println(i);
		    iterator.next();
		    i++;
	    	
	    }*/
	    
	    /**
	     * Faz a consulta em um servidor, buscando o método /getInterfaceList, que devolve a lista de interfaces 
	     * semânticas atendidas por cada servidor de uma lista do tipo List<GoogleResults.Result>.
	     * 
	     * O padrão de endereçamento é 
	     * 
	     * http://<endereço do servidor>/serin/getInterfaceList
	     * 
	     */
		//System.out.println("Separando...");
		//iterator = listResults.iterator();
		//i = 0;
	    //while (iterator.hasNext()){
			//String[] temp = listResults.get(i).getUrl().split("/");
			//for(int x =0; x < temp.length ; x++)
			//    if (x==2) System.out.println("http://"+temp[x]+"/serin");
		    //System.out.println(listResults.get(i).getVisibleUrl());
		//    iterator.next();
		//    i++;
	    //}

		System.out.println("Invocando serviço web de busca...");

		String url = "";
		//i = 0;
		
	    //while (iterator.hasNext()){
		for (Iterator<GoogleResults.Result> iterator = listResults.iterator(); iterator.hasNext(); ) {
			GoogleResults.Result elemento = iterator.next();

	    	//SerinClient serinClient = new SerinClient(listResults.get(i).getUrl().split(":")[0]+"://"+listResults.get(i).getVisibleUrl()+"/Serin3");
			SerinClient serinClient = new SerinClient(elemento.getUrl().split(":")[0]+"://"+elemento.getVisibleUrl()+"/Serin2");
			url = serinClient.getUrlHost()+"/getInterfaceList";
		    System.out.println(url.toString());

	    	try {
		    

		    ClientRequest request = new ClientRequest(url);
		
		    ClientResponse<String> response = request.get(String.class);
		    

		    if (response.getResponseStatus().equals(Status.OK)) {
		    	
		    	//faz a leitura do XML chamado pela URL de descoberta em cada servidor
		    	String rdfXml = (String) response.getEntity();
		    	System.out.println(rdfXml);
		    	Document doc = null;
		    	SAXBuilder builder = new SAXBuilder();

		    		doc = builder.build(url);
   	   	        	Element indice = doc.getRootElement();
   	   	        	List<Element> lista = indice.getChildren("entry");
   	   	        	for (Element e: lista) {
   	   	        		System.out.println("Key: "+ e.getAttributeValue("key"));
   	   	        		System.out.println("Entry: " + e.getText());
   	   	        		
   	   	        		//faz a persistência no banco de dados, das URIs de interface listadas no servidor
   	   	      	Connection con = null;
   	   	      	String statement = INSERT_HOST;
   	 		try {
   	 			con = Db.getConnection();
   	 			PreparedStatement prepared = con.prepareStatement(statement);
   	 			prepared.setString(1, elemento.getVisibleUrl());
   	 			prepared.setString(2, e.getText());
   	 			prepared.setString(3, elemento.getUrl().split(":")[0]);
   	 			prepared.execute();

   	 		} catch (SQLException sql) {
   	 		System.out.println(sql.getMessage());
   	 		} finally {
   	 			Db.closeConnnection(con);
   	 		}


   	   	        	}
		    }
		    else System.out.println(response.getResponseStatus());
   	   	     	
   	          	} catch (SSLPeerUnverifiedException e) {
   	          		System.out.println(Status.FORBIDDEN);		      
		    	}	catch (IOException io) {
   	        		System.out.println(io.getMessage());
   	          	} catch (JDOMException jdomex) {
   	        	  	System.out.println(jdomex.getMessage());
   	          	} catch (Exception e) {
		            e.printStackTrace();		      
   	          	}
   	     	
		    System.out.println("==================================================================");
		    //iterator.next();
		    //i++;
    	}
		
		System.out.println("Discovery agent finished!");

	}

}

/* Código reserva-----------------------------------------------------------------------
String google = "http://ajax.googleapis.com/ajax/services/search/web?v=1.0&num=100&q=";
String search = "gov.br";
String charset = "UTF-8";

URL url = new URL(google + URLEncoder.encode(search, charset));
Reader reader = new InputStreamReader(url.openStream(), charset);
GoogleResults results = new Gson().fromJson(reader, GoogleResults.class);

// Show title and URL of 1st result.
int i = 0;
Iterator iterator = results.getResponseData().getResults().iterator();
System.out.println(results.getResponseData().getResults().size());
System.out.println(results.getResponseData().getResults().get(i).getUrl());
while (iterator.hasNext()){
    System.out.print(results.getResponseData().getResults().get(i).getTitle()+" - ");
    System.out.println(results.getResponseData().getResults().get(i).getUrl());
	//System.out.println(iterator.toString());
	System.out.println(i);
    iterator.next();
    i++;
	
}*/

