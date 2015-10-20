import java.util.*;
import java.sql.*; // Java package for accessing Oracle
import java.io.*; // Java package includes Console for getting password from user and printing to screen

public class FlightBooker {
  
  static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
  static final String DB_URL = "jdbc:mysql://localhost/EMP";

  public static void main(String[] args)
  {
  	// before you start, write 'CLASSPATH = $CLASSPATH:' into the console.
  	// get username
	System.out.print("Username: ");
	Console co = System.console();
	String m_userName = co.readLine();
	
	// obtain password
	char[] passwordArray = co.readPassword("Password: ");
	String m_password = new String(passwordArray);

	// The URL we are connecting to
    String m_url = "jdbc:oracle:thin:@gwynne.cs.ualberta.ca:1521:CRS";

	// The driver to use for connection
    String m_driverName = "oracle.jdbc.driver.OracleDriver";

    Connection m_con;
    String createString;

    // final String db_username = System.getenv("DB_USERNAME"); // username and password is sent in by user at beginning...
    // final String db_password = System.getenv("DB_PASSWORD");
    /*
    Scanner scan = new Scanner(System.in);
    System.out.println("Login to database - Enter username:");
    db_username = scan.nextLine();
    System.out.println("Login to database - Enter password:");
    db_password = scan.nextLine();
    try {

      Class.forName("com.mysql.jdbc.Driver");
      System.out.println("Connecting to database...");
      Connection connect = DriverManager.getConnection(DB_URL,db_username,db_password);
      UI cli = new UI();
    
    } catch(SQLException se) {
      //Handle errors for JDBC
      se.printStackTrace();
    } catch(Exception e) {
      //Handle errors for Class.forName
      e.printStackTrace();
    } */ 

    try {
	  Class drvClass = Class.forName(m_driverName);  
    } catch (Exception e) {
      System.err.print("ClassNotFoundException: ");
      System.err.println(e.getMessage());
    }
	try {
	  // Establish a connection
   	  m_con = DriverManager.getConnection(m_url, m_userName, m_password);
	  // Changed to reflect changes made in the result set and to make these changes permanent to the database too
      Statement stmt = m_con.createStatement(
		ResultSet.TYPE_SCROLL_SENSITIVE, 
		ResultSet.CONCUR_UPDATABLE); 
      UI cli = new UI(); // no statement passed - i think the sql passed here...
	  // No more statements to compile/execute. So, close connection.
      stmt.close();
      m_con.close();
	  System.out.println("SQL success.");
   	} catch(SQLException ex) {
      System.err.println("SQLException: " + ex.getMessage());
    }
  }
}
