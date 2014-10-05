package br.unifor.mia.serin.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class Db {
	
	public static Connection getConnection() throws SQLException {
		Connection con = null;
	    String username = "root";     
	    String password = ""; 
		con = DriverManager.getConnection("jdbc:mysql://localhost:3306/serin_discovery",username,password);

		return con;
	}

	public static void closeConnnection(Connection con) {
		try {
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
