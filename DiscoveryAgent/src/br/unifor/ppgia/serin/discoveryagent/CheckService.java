package br.unifor.ppgia.serin.discoveryagent;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.ws.rs.core.Response.Status;

import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jdom.JDOMException;

import br.unifor.mia.serin.util.Db;


public class CheckService {

	public static void main(String[] args) throws SQLException {
		System.out.println("Discovery agent check service started...");
       	Date d1 = new Date();
	    System.out.println(d1);

		String url = "";

		int countFound = 0;
		int countTotal = 0;
		
     	Connection con = null;
     	ResultSet result = null;
		try {
			con = Db.getConnection();
			PreparedStatement prepared = con.prepareStatement("select * from request limit 2000");
			result = prepared.executeQuery();
			while (result.next()){
				
				url = result.getString(2);
				System.out.println(url);
				TimeUnit.MILLISECONDS.sleep(500);
	    		ClientRequest request = new ClientRequest(url);
	    		ClientResponse<String> response = request.get(String.class);
	    		if (response.getResponseStatus().equals(Status.OK)) {
	    			countFound++;
   	   	        	}
			}
				
		} catch (SQLException sql) {
			System.out.println(sql.getMessage());
         	} 
		catch (SSLPeerUnverifiedException e) {
	          		//System.out.println(Status.FORBIDDEN);		      
	    	}	
		catch (IOException io) {
	        		//System.out.println(io.getMessage());
	          	} 
		catch (JDOMException jdomex) {
	        	  	//System.out.println(jdomex.getMessage());
	          	} 
		catch (Exception e) {
	            e.printStackTrace();		      
	          	}
		finally {
			Db.closeConnnection(con);
		}
		

	    Date d2 = new Date();
	    System.out.println(d2);
	    System.out.println("Total execution time: "+((d2.getTime()-d1.getTime())/1000.0)+" seconds.");
	    
	    System.out.println("Total tested requests "+countTotal);
	    System.out.println("Total found requests "+countFound);

	    System.out.println("Discovery agent checks finished!");


	}

}
