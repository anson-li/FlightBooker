import java.sql.*;
import java.util.*;
import java.io.*;

public class SampleSQL {

public static void main(String args[]) {

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

	// SQL statement to execute
        createString = "create table TOFFEES " +
		 "(T_NAME VARCHAR(32) PRIMARY KEY, " +
                 "SUP_ID INTEGER, " +
                 "PRICE FLOAT, " +
                 "SALES INTEGER, " +
                 "TOTAL INTEGER)";

	// create a statement object
       // Statement stmt;

       try
       {
	      Class drvClass = Class.forName(m_driverName); 
              // DriverManager.registerDriver((Driver)drvClass.newInstance());- not needed. 
              // This is automatically done by Class.forName().
	      

       } catch(Exception e)
       {

              System.err.print("ClassNotFoundException: ");
              System.err.println(e.getMessage());

       }

       try
       {
	      // Establish a connection

              m_con = DriverManager.getConnection(m_url, m_userName,
              m_password);
	
	      // Changed to reflect changes made in the result set and to make these changes permanent to the database too
              Statement stmt = m_con.createStatement(
		ResultSet.TYPE_SCROLL_SENSITIVE, 
		ResultSet.CONCUR_UPDATABLE);
	      // Since it is a DML command, use executeUpdate. Automatically converts our string to an SQL command.
	      // Notice: There is no ; in the query
              stmt.executeUpdate("drop table toffees");
	      stmt.executeUpdate(createString);

	      // insert a row
	      createString = "insert into toffees values ('Quadbury', 101,7.99,0,0)";
	      stmt.executeUpdate(createString);
	      
	      // Suppose executing a query and printing the results
	      String query = "select T_NAME, SUP_ID, SALES, PRICE, TOTAL from toffees";
	      // when you want to use an updatable result set, you cannot use * for select all:
	      // all column names should be specified.
	      ResultSet rs = stmt.executeQuery(query);
	      while (rs.next())
    	      {
      		String s = rs.getString("T_NAME");
		int supid = rs.getInt("SUP_ID");
      		float n = rs.getFloat("PRICE");
		int sales = rs.getInt("SALES");
		int total = rs.getInt("TOTAL");
      		System.out.println(s+"," + supid+"," +sales+"," +n+"," +total);
    		}
	      System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	      // Updating one of the rows
	      // Suppose we want to update 'Quadbury' to 'Cadbury'
	      rs.first(); // it the first row we want to update
	      rs.updateString(1,"Cadbury"); // currently only in the result set: Indexing from 1.
	      rs.updateRow(); // makes the changes permanent

	      // Now let's add a new row without using the insert command
	      rs.moveToInsertRow(); // move to the end
	      // for every column use updateX() method: indexing from 1. You may use the column name instead of index.
	      rs.updateString(1,"Jewel");
	      rs.updateInt(2,105);
	      rs.updateDouble(3,4.99);
	      rs.updateInt(4,3);
	      rs.updateInt(5,2);
	      rs.insertRow(); // make it permanent.
	
	      // See what we did!
	      rs = stmt.executeQuery(query);
              while (rs.next())
              {
                String s = rs.getString("T_NAME");
                int supid = rs.getInt("SUP_ID");
                int sales = rs.getInt("SALES");
                float n = rs.getFloat("PRICE");
                int total = rs.getInt("TOTAL");
                System.out.println(s + "," + supid+"," +sales+"," +n+"," +total);
        	}

	      // Using metadata to do the same
	      rs = stmt.executeQuery(query);
	      ResultSetMetaData rsetMD = rs.getMetaData();
	      int columnCount = rsetMD.getColumnCount();
	        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	      while (rs.next()){
	      		for (int c=1; c<=columnCount; c++)
       	      		{
				String name = rsetMD.getColumnLabel(c); // get column name
				Object o = rs.getObject(c); // get content at that index
				String value="null";
				if (o!=null)
					value = o.toString();
				System.out.println(name+" - "+value);
			
	     		 }
		}

	      // No more statements to compile/execute. So, close connection.
              stmt.close();
              m_con.close();

	      System.out.println("Successful!");

       } catch(SQLException ex) {

              System.err.println("SQLException: " +
              ex.getMessage());

       }

}
}