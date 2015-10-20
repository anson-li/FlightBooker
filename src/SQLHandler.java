import java.sql.*;

public class SQLHandler
{
  private Connection con;
  private String url;
  private String username;
  private String password;
  private Statement statement;
  
  SQLHandler(String username, String password) throws SQLException
  {
    try {
      loadDriver();
    } catch (ClassNotFoundException cnfe) {
      System.err.println("Could not load driver.");
    }
    
    setURL();
    this.username = username;
    this.password = password;
    
    makeConnection();
  }
  
  private void loadDriver() throws ClassNotFoundException
  {
    String driver_name = "oracle.jdbc.driver.OracleDriver";
    /*Class driver_class =*/ Class.forName(driver_name);
  }
  
  private void setURL()
  {
    url = System.getenv("DB_URL");
  }
  
  private void makeConnection() throws SQLException
  {
    con = DriverManager.getConnection(url, username, password);
    this.statement = con.createStatement();
  }
  
  public void runSQLStatement(String s) throws SQLException
  {
    statement.executeUpdate(s);
  }
  
  public ResultSet runSQLQuery(String q) throws SQLException
  {
    return statement.executeQuery(q);
  }
}
