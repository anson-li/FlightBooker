import java.util.*;
import java.sql.*; // Java package for accessing Oracle
import java.io.Console; // Java package includes Console for getting password from user and printing to screen

public class FlightBooker {

  static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
  static final String DB_URL = "jdbc:mysql://localhost/EMP";

  public static void main(String[] args)
  {
  	// before you start, write 'CLASSPATH = $CLASSPATH:' into the console.

    Console co = System.console();
    if (co == null)
    {
      System.err.println("System.console() returned null.")
      // System.console() returns null if console doesn't exist
      // need error handling
    }

    String m_userName = "";
    String m_password = "";

    System.println("Login in to SQLPlus Database")
    // get username
    try {
    	m_userName = co.readLine("SQLPlus Username: ");
      char[] passwordArray = co.readPassword("SQLPlus Password: ");
    	m_password = String.valueOf(passwordArray);
    } catch (IOError ioe) {
      // System.err.println(ioe.toString());
    }

    try {
      SQLHandler sql_handler = SQLHandler(m_userName, m_password);
      UI cli = new UI();

      // No more statements to compile/execute. So, close connection.
      sql_handler.close();
      System.out.println("SQL success.");
   	} catch(SQLException sqle) {
      System.err.println("SQLException: " + sqle.getMessage());
    }
  }
}
