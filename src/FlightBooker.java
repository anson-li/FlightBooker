import java.sql.*; // Java package for accessing Oracle
import java.io.Console; // Java package includes Console for getting password from user and printing to screen
import java.io.IOError;

public class FlightBooker {
  
  public static void main(String[] args)
  {
  	Console co = System.console();
    try {
      co.flush();
    } catch (NullPointerException npe) {
      npe.printStackTrace();
      System.err.println("Unable to initialize System.console()\nExiting.");
      System.exit(0);
    }

    String m_userName = "";
    String m_password = "";
    System.out.println("Login in to SQLPlus Database");

    try {
    	m_userName = co.readLine("SQLPlus Username: ");
      char[] passwordArray = co.readPassword("SQLPlus Password: ");
    	m_password = String.valueOf(passwordArray);
    } catch (IOError ioe) {
      System.err.println("Error: Unable to read SQL username/password.");
    }
    
    SQLHandler sql_handler = null;
    try {
      sql_handler = new SQLHandler(m_userName, m_password);
    } catch (SQLException e) {
      System.out.println("Login Failed.");
      System.err.println("Error: SQLPlus connection rejected. Exiting.");
      System.err.println(e.getMessage());
      System.exit(0);
    }

    new UI(sql_handler, co);
    try { 
      sql_handler.close();
    } catch (SQLException e) {
      System.err.println("Error: Unable to close SQL connection.");
      System.err.println(e.getMessage());
    }
  }
}
