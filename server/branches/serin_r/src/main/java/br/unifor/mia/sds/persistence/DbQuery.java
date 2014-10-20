package br.unifor.mia.sds.persistence;

/**
 * Static final variables with fixed queries.
 * @author renato
 *
 */
public final class DbQuery {
	
	public static final String SELECT_HOST = "SELECT * FROM host_service WHERE interface_uri = ?";
	
	public static final String SELECT_INTERFACE = "SELECT * FROM interface";
	

}
