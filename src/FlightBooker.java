//import sampleui?
public class FlightBooker {
  
  static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
  static final String DB_URL = "jdbc:mysql://localhost/EMP";

  public static void main(String[] args)
  {
    final String db_username = System.getenv("DB_USERNAME"); // username and password is sent in by user at beginning...
    final String db_password = System.getenv("DB_PASSWORD");
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
      UI cli = new UI();

  }


}
