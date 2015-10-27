import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.Console;
import java.io.IOError;
import java.sql.*;
import java.text.*;

class UI
{

  private int num_uis;
  private SQLHandler sql_handler;
  private Scanner scan;
  private Console con;
  public String pub_email;
  public String pub_role;

  UI(SQLHandler sql_handler, Console con)
  {
    num_uis += 1;
    this.sql_handler = sql_handler;
    this.con = con;
    this.scan = new Scanner(System.in);
    try {
      GenerateViews();
      WelcomeScreen();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  };

  /**
   * Creates the views 'available_flights'
   * and 'good_connections' in the SQLPlus
   * database. Solution from assignment2
   * q7 and q9.
   * @throws SQLException
   */
  public void GenerateViews() throws SQLException {
    String dropAvailableFlights = "drop view available_flights";
    String createAvailableFlights = "create view available_flights(flightno,dep_date,src,dst,dep_time, " +
      "arr_time,fare,seats,price) as " +
      "select f.flightno, sf.dep_date, f.src, f.dst, f.dep_time+(trunc(sf.dep_date)-trunc(f.dep_time)), " +
      "f.dep_time+(trunc(sf.dep_date)-trunc(f.dep_time))+(f.est_dur/60+a2.tzone-a1.tzone)/24, " +
      "fa.fare, fa.limit-count(tno), fa.price " +
      "from flights f, flight_fares fa, sch_flights sf, bookings b, airports a1, airports a2 " +
      "where f.flightno=sf.flightno and f.flightno=fa.flightno and f.src=a1.acode and " +
      "f.dst=a2.acode and fa.flightno=b.flightno(+) and fa.fare=b.fare(+) and " +
      "sf.dep_date=b.dep_date(+) " +
      "group by f.flightno, sf.dep_date, f.src, f.dst, f.dep_time, f.est_dur,a2.tzone, " +
      "a1.tzone, fa.fare, fa.limit, fa.price " +
      "having fa.limit-count(tno) > 0";
    String dropGoodConnections = "drop view good_connections";
    String createGoodConnections = "create view good_connections (src,dst,dep_date,flightno1,flightno2, layover,price) as " +
      "select a1.src, a2.dst, a1.dep_date, a1.flightno, a2.flightno, a2.dep_time-a1.arr_time, " +
      "min(a1.price+a2.price) " +
      "from available_flights a1, available_flights a2 " +
      "where a1.dst=a2.src and a1.arr_time +1.5/24 <=a2.dep_time and a1.arr_time +5/24 >=a2.dep_time " +
      "group by a1.src, a2.dst, a1.dep_date, a1.flightno, a2.flightno, a2.dep_time, a1.arr_time ";

    sql_handler.runSQLStatement(dropAvailableFlights);
    sql_handler.runSQLStatement(createAvailableFlights);
    sql_handler.runSQLStatement(dropGoodConnections);
    sql_handler.runSQLStatement(createGoodConnections);
  }

  /**
   *
   * @throws SQLException
   */
  public void WelcomeScreen() throws SQLException {
    System.out.println("+-=-=-=-=-=-=-=-=-=-=-=-=-=-=-==-=-=-=-=-=-=-=-=-=-=-=-=-=-+");
    System.out.println("Welcome to Air Kappa!");
    while(true) {
      System.out.println("Please (L)ogin or (R)egister to use our services, "
                       + "\nor (E)xit the program.");
      System.out.println("+-=-=-=-=-=-=-=-=-=-=-=-=-=-=-==-=-=-=-=-=-=-=-=-=-=-=-=-=-+");
      String i = scan.nextLine();
      if (i.equals("l") || i.equals("L")) {
        Login();
      } else if (i.equals("r") || i.equals("R")) {
        Register();
      } else if (i.equals("e")  || i.equals("E")) {
        System.out.println("System is exiting; "
                         + "Thank you for visiting Air Kappa!");
        scan.close();
        System.exit(0);
      } else {
        System.out.println("Invalid entry - please try again.");
      }
    }
  }

  /**
   * FIXME: i want to be a complete method comment
   * @throws SQLException
   */
  /*
  Sample users table generation:
  create table users ( email varchar(20), password varchar(8), last_login date)
  create table airline_agents (email varchar(20), name varchar(20))
  insert into users values('dude@ggg.com', '1234', sysdate);
  insert into airline_agents values('dude@ggg.com', 'dude mcman');
  */
  public void Register() throws SQLException {

    String email = "";
    String password = "";

    System.out.println("Registration (password must be 4 (or less) alpha-numeric characters):");
    try {
      email = con.readLine("Email: ");
      char[] pwArray1 = con.readPassword("Password: ");
      char[] pwArray2 = con.readPassword("Confirm Password:");
      if (Arrays.equals(pwArray1, pwArray2))
      password = String.valueOf(pwArray1);
      else
      {
        System.out.println("Registration failed. Passwords do not match.");
        return;
      }
    } catch (IOError ioe){
      System.err.println(ioe.getMessage());
    }

    if (!validEmail(email))
    {
      System.out.println("Registration failed. Invalid Email.");
      return;
    }
    else
    {
      String query = "select * from users where email='"+email+"'";
      ResultSet rs = sql_handler.runSQLQuery(query);

      if (rs.next())
      {
        System.out.println("Registration failed. User exists.");
        return;
      }
    }

    if (!validPassword(password))
    {
      System.out.println("Registration failed. Invalid Password.");
      return;
    }
    else
    {
      String statement = "insert into users values('" + email +  "',"
                        + "'" + password +"',"
                        + "sysdate )";

      System.out.println(statement);
      sql_handler.runSQLStatement(statement);
    }

    // TODO: how is the user's role selected? are they predetermined?
    String role = "user";
    pub_email = email; // remove this once emailvalidation exists
    pub_role = role;
    MainHub();
    scan.close();
  }

  /**
   * FIXME: my dream is to be a complete comment
   * @throws SQLException
   */
  private void Login() throws SQLException {
    System.out.println("Login:");
    String email = "";
    String pword = "";
    String role = "";

    while(true) {
      email = con.readLine("Email: ");
      char[] pwordA = con.readPassword("Password:");
      pword = String.valueOf(pwordA);

      if (!validEmail(email))
      {
        System.out.println("Invalid email.");
        return;
      }

      if (!validPassword(pword))
      {
        System.out.println("Invalid password.");
        return;
      }

      String query = "select email, pass from users where email='"+email+"'";
      ResultSet rs = sql_handler.runSQLQuery(query);

      if (!rs.next())
      {
        System.out.println("Invalid email/password combination.");
        return;
      }

      query = "select * from airline_agents where email='"+email+"'";
      rs = sql_handler.runSQLQuery(query);
      if (rs.next())
      {
        role = "poweruser";
        String airline_email = rs.getString("EMAIL");
        String airline_name = rs.getString("NAME");
        System.out.println("Airline Agent: " + airline_name );
      }
      else
      {
        role = "user";
        System.out.println("Standard User.");
      }

      String statement = "update users "
          + "set last_login=sysdate "
          + "where email='"+pub_email+"'";

      sql_handler.runSQLStatement(statement);

      System.out.println("Login Successful. ");

      pub_email = email;
      pub_role = role;
      MainHub();
    }
  }

  /**
   * FIXME: im a incomplete comment
   * @param role
   * @throws SQLException
   */
  public void MainHub() throws SQLException {
    Scanner scan = new Scanner(System.in);
    while(true) {
      System.out.println("\n+-=-=-=-=-=-=-=-=-=-=-=-=-=-=-==-=-=-=-=-=-=-=-=-=-=-=-=-=-+");
      System.out.println("            Welcome to Air Kappa's Main Menu                ");
      System.out.println("         Please select from the following options:          ");
      System.out.println("+-=-=-=-=-=-=-=-=-=-=-=-=-=-=-==-=-=-=-=-=-=-=-=-=-=-=-=-=-+");
      System.out.println("(S)earch for flights & make a booking, \nView or cancel (E)xisting bookings, \nFind (R)ound trips, \n(L)og out.");
      if (pub_role.equals("poweruser")) {
        System.out.println("+-=-=-=-=-=-=-=-=-=-=-=-=-=-=-==-=-=-=-=-=-=-=-=-=-=-=-=-=-+");
        System.out.println("AIRLINE AGENT: Select from the following additional commands:");
        System.out.println("+-=-=-=-=-=-=-=-=-=-=-=-=-=-=-==-=-=-=-=-=-=-=-=-=-=-=-=-=-+");
        System.out.println("Record (D)eparture, \nRecord (A)rrival for a scheduled flight.");
      }
      System.out.println("+-=-=-=-=-=-=-=-=-=-=-=-=-=-=-==-=-=-=-=-=-=-=-=-=-=-=-=-=-+");
      String input = scan.nextLine();
      if (input.equals("S") || input.equals("s")) {
        SearchForFlights();
      } else if (input.equals("E") || input.equals("e")) {
        ExistingBookings();
      } else if (input.equals("R") || input.equals("r")) {
        RoundTrips();
      } else if (input.equals("L") || input.equals("l")) {
        Logout();
      } else if ((input.equals("D") || input.equals("d")) && pub_role.equals("poweruser")) {
        RecordDeparture();
      } else if ((input.equals("A") || input.equals("a")) && pub_role.equals("poweruser")) {
        RecordArrival();
      } else {
        System.out.println("Invalid entry - please try again.");
      }
    }

  }

  /*	Search for flights. A user should be able to search
  for flights. Your system should prompt the user for a
  source, a destination and a departure date. For source
  and destination, the user may enter an airport code or
  a text that can be used to find an airport code. If the
  entered text is not a valid airport code, your system
  should search for airports that have the entered text
  in their city or name fields (partial match is allowed)
  and display a list of candidates from which an airport
  can be selected by the user. Your search for source and
  destination must be case-insensitive. Your system should
  search for flights between the source and the destination
  on the given date(s) and return all those that have a
  seat available. The search result will include both direct
  flights and flights with one connection (i.e. two flights
  with a stop between). The result will include flight details
  (including flight number, source and destination airport
  codes, departure and arrival times), the number of stops,
  the layover time for non-direct flights, the price, and the
  number of seats at that price. The result should be sorted
  based on price (from the lowest to the highest); the user
  should also have the option to sort the result based on the
  number of connections (with direct flights listed first) as
  the primary sort criterion and the price as the
  secondary sort criterion.

  /**
   *
   * @param role
   * @throws SQLException
   */
  public void SearchForFlights() throws SQLException {
    String srcACode = "";
    String destACode = "";
    String depDate = "";
    
    GenerateViews();

    while (!validAcode(srcACode))
    {
      System.out.println("Please enter the airport code for your source:");
      srcACode = scan.nextLine();
    }

    while (!validAcode(destACode))
    {
      System.out.println("Please enter the airport code for your destination:");
      destACode = scan.nextLine();
    }

    // add departure date
    System.out.println("Please enter your departure date in format DD/MM/YYYY - eg: 01/10/2015 for October 10, 2015");
    depDate = scan.nextLine();
    /*DateFormat df = new SimpleDateFormat("dd-MMM-yy");
    java.util.Date depDate = new java.util.Date();
    try {
        depDate = df.parse(strDate);
    } catch (ParseException e) {
        e.printStackTrace();
    }*/

    String query =  "select flightno1, flightno2, layover, price " +
                    "from ( " +
                    "select flightno1, flightno2, layover, price, row_number() over (order by price asc) rn " +
                    "from " +
                    "(select flightno1, flightno2, layover, price " +
                    "from good_connections " +
                    "where to_char(dep_date,'DD/MM/YYYY')='"+ depDate +"' and src='"+srcACode.toUpperCase()+"' and dst='"+destACode.toUpperCase()+"' " +
                    "union " +
                    "select flightno flightno1, '' flightno2, 0 layover, price " +
                    "from available_flights " +
                    "where to_char(dep_date,'DD/MM/YYYY')='"+ depDate +"' and src='"+srcACode.toUpperCase()+"' and dst='"+destACode.toUpperCase()+"')) " +
                    "order by price";

    ResultSet rs = sql_handler.runSQLQuery(query);
    System.out.println("The flight plans that match your description are as follows:\n");
    ArrayList<String> flightnolist = new ArrayList<>();
    ArrayList<String> flightnolist2 = new ArrayList<>();
    int planId = 1;
    planId = printFlightPlans(rs, planId, flightnolist, flightnolist2);
    System.out.print("\nFlights are currently being sorted by price:"
        + "\n(S)ort the result based on number of connections, or ");

    while(true) {
      System.out.println("(R)eturn to main menu.");
      System.out.println("Or select a booking with the corresponding ID (eg. 1, 2, ...)");

      String i = scan.nextLine();
      if (i.equals("S") || i.equals("s")) {

        flightnolist = new ArrayList<>();
        flightnolist2 = new ArrayList<>();
        planId = 1;

        System.out.println("The flight plans that match your description are as follows:\n");

        String q =  "select flightno1, flightno2, layover, price " +
                    "from ( " +
                    "select flightno1, flightno2, layover, price, row_number() over (order by price asc) rn " +
                    "from " +
                    "(select flightno flightno1, '' flightno2, 0 layover, price " +
                    "from available_flights " +
                    "where to_char(dep_date,'DD/MM/YYYY')='"+ depDate +"' and src='"+srcACode.toUpperCase()+"' and dst='"+destACode.toUpperCase()+"')) " +
                    "order by price ";

        rs = sql_handler.runSQLQuery(q);
        planId = printFlightPlans(rs, planId, flightnolist, flightnolist2);

        q = "select flightno1, flightno2, layover, price " +
            "from ( " +
            "select flightno1, flightno2, layover, price, row_number() over (order by price asc) rn " +
            "from " +
            "(select flightno1, flightno2, layover, price " +
            "from good_connections " +
            "where to_char(dep_date,'DD/MM/YYYY')='"+ depDate +"' and src='"+srcACode.toUpperCase()+"' and dst='"+destACode.toUpperCase()+"')) " +
            "order by price ";

        rs = sql_handler.runSQLQuery(q);
        planId = printFlightPlans(rs, planId, flightnolist, flightnolist2);



      } else if (i.equals("R") || i.equals("r")) {
        MainHub();
      } else if (isInteger(i,10)) {
        Integer intIndex = Integer.parseInt(i);
        if (intIndex <= flightnolist.size() && intIndex > 0) {
          intIndex = intIndex - 1;
          MakeABooking(flightnolist.get(intIndex), flightnolist2.get(intIndex), null, null);
        } else {
          System.out.println("Invalid entry - please try again.");
        }
      } else {
        System.out.println("Invalid entry - please try again.");
      }
    }
  }

  /**
   * FIXME: add logic to return if no flights
   * @param rs
   * @param planId
   * @param flightnolist
   * @param flightnolist2
   * @return
   * @throws SQLException
   */
  private int printFlightPlans(ResultSet rs, int planId, ArrayList<String> flightnolist, ArrayList<String> flightnolist2) throws SQLException
  {
    int startId = planId;
    while(rs.next())
    {
      String flightno1 = rs.getString("FLIGHTNO1");
      String flightno2 = rs.getString("FLIGHTNO2");
      String layover = rs.getString("LAYOVER");
      String price = rs.getString("PRICE");
      boolean has_sec_flight = (flightno2 != null);
      String query = "";

      flightnolist.add(flightno1);
      flightnolist2.add(flightno2);

      query = "select src, dst, to_char(dep_time, 'hh24:mi') as dept, to_char(arr_time, 'hh24:mi') as arrt, seats " +
              "from available_flights where flightno='"+flightno1+"'";
      SQLHandler sqlh1 = new SQLHandler(sql_handler.get_uname(), sql_handler.get_pword());
      ResultSet rs1 = sqlh1.runSQLQuery(query);
      rs1.next();
      String src1 = rs1.getString("src");
      String dst1 = rs1.getString("dst");
      String dep1 = rs1.getString("dept");
      String arr1 = rs1.getString("arrt");
      int sea1 = Integer.parseInt(rs1.getString("seats"));

      String src2 = "-";
      String dst2 = "-";
      String dep2 = "-";
      String arr2 = "-";
      int sea2 = 0;

      if (has_sec_flight)
      {
        query = "select src, dst, to_char(dep_time, 'hh24:mi') as dept, to_char(arr_time, 'hh24:mi') as arrt, seats " +
            "from available_flights where flightno='"+flightno2+"'";
        ResultSet rs2 = sqlh1.runSQLQuery(query);
        rs2.next();
        src2 = rs2.getString("src");
        dst2 = rs2.getString("dst");
        dep2 = rs2.getString("dept");
        arr2 = rs2.getString("arrt");
        sea2 = Integer.parseInt(rs2.getString("seats"));
      }

      System.out.println("+-=-=-=-=-=-=-=-=-=-=-=-=-=-=-==-=-=-=-=-=-=-=-=-=-=-=-=-=-+");
      System.out.println  ("Flight Plan: " + planId);
      System.out.println  ("Price: " + price);
      System.out.print    ("Number of Stops: " );
      if (has_sec_flight)
      {
        System.out.println("1");
        System.out.println("Layover Time: " + (Integer.parseInt(layover)*1440) +" minutes.");
        System.out.println("Num Seats: " + Math.min(sea1, sea2));
        System.out.println("Flight Info:   Flight 1          Flight 2");
        System.out.println("               +=-=-=-+          +=-=-=-+");
        System.out.println("    Flight #:  "+flightno1+"          "+flightno2);
        System.out.println("    Source:    "+src1+"             "+src2);
        System.out.println("    Dest.:     "+dst1+"             "+dst2);
        System.out.println("    Dep. Time: "+dep1+"           "+dep2);
        System.out.println("    Arr. Time: "+arr1+"           "+arr2);
      }
      else
      {
        System.out.println("0");
        System.out.println("Num Seats: " + sea1);
        System.out.println("Flight Info:");
        System.out.println("    Flight #:  "+flightno1);
        System.out.println("    Source:    "+src1);
        System.out.println("    Dest.:     "+dst1);
        System.out.println("    Dep. Time: "+dep1);
        System.out.println("    Arr. Time: "+arr1);
      }

      sqlh1.close();
      planId++;
    }
    if (planId == 1)
      System.out.println("No available flights matching criteria.");

    if (startId != planId)
      System.out.println("+-=-=-=-=-=-=-=-=-=-=-=-=-=-=-==-=-=-=-=-=-=-=-=-=-=-=-=-=-+");

    return planId;
  }

  /* Make a booking. A user should be able to select a flight
  (or flights when there are connections) from those returned
  for a search and book it. The system should get the name
  of the passenger and check if the name is listed in the
  passenger table with the user email. If not, the name and
  the country of the passenger should be added to the passenger
  table with the user email. Your system should add rows to
  tables bookings and tickets to indicate that the booking is
  done (a unique ticket number should be generated by the system).
  Your system can be used by multiple users at the same time and
  overbooking is not allowed. Therefore, before your update
  statements, you probably want to check if the seat is still
  available and place this checking and your update statements
  within a transaction. Finally the system should return the
  ticket number and a confirmation message if a ticket is
  issued or a descriptive message if a ticket cannot be
  issued for any reason.*/


  // if seat is empty..
  // insert into passengers values ('EMAIL', 'NAME', 'COUNTRY')
  // insert into tickets values (tno (gen), 'EMAIL', 'PAID PRICE')
  // insert into bookings values (tno (gen), flight_no, fare, dep_date, seat)

  private void BookFlight(String flightno, String name) throws SQLException
  {
    int tno = 0;
    String findTno = "select max(tno) as tno from tickets";
    ResultSet tnoVal = sql_handler.runSQLQuery(findTno);
    tnoVal.next();
    String tmpTno = tnoVal.getString("tno");
    if (tmpTno != null)
      tno = Integer.parseInt(tmpTno);
    tno += 100;
      
    GenerateViews();
    String query = "select * from available_flights where flightno='"+flightno+"'";
    ResultSet rs = sql_handler.runSQLQuery(query);
    rs.next();
    String depdate = rs.getString("DEP_DATE"), convdate = "";
    String fare = rs.getString("FARE");
    int seat = rs.getInt("SEATS");
    
    
    String addToTickets = "insert into tickets values (" + tno + ", '" + name + "', '" + pub_email + "', " + rs.getFloat("PRICE") + ")";
    System.out.println(addToTickets);
    sql_handler.runSQLStatement(addToTickets);
    
    DateFormat df = new SimpleDateFormat("dd-MMM-yy");
    DateFormat initialdf = new SimpleDateFormat("yyyy-MM-dd");
    try {
      depdate = depdate.substring(0, 10);
      convdate = df.format(initialdf.parse(depdate));
    } catch (ParseException e) { System.out.println(e); }
    String addToBookings = "insert into bookings values ("+ tno + ", "
                                                          + "'" + flightno + "', "
                                                          + "'" + fare + "', "
                                                          + "'" + convdate + "', "
                                                          + seat +")";
    System.out.println(addToBookings);
    sql_handler.runSQLStatement(addToBookings);
    System.out.println("+-=-=-=-=-=-=-=-=-=-=-=-=-=-=-==-=-=-=-=-=-=-=-=-=-=-=-=-=-+");
    System.out.println("Success - you have booked your flight!");
    System.out.println("Your ticket number is: " + tno);
    System.out.println("+-=-=-=-=-=-=-=-=-=-=-=-=-=-=-==-=-=-=-=-=-=-=-=-=-=-=-=-=-+\n");
  }

  /**
   *
   * @param role
   * @param flightno1
   * @param flightno2
   * @throws SQLException
   */
  public void MakeABooking(String flightno1, String flightno2, String flightno3, String flightno4) throws SQLException {
    String name = "", country = "";
    System.out.println("Please enter the name of the passenger:");
    name = scan.nextLine();
    String passengerquery = "select * from passengers where email = '" + pub_email + "' and regexp_like(name, '"+name+"', 'i')"; 
    ResultSet passengerRs = sql_handler.runSQLQuery(passengerquery);
        
    if (!passengerRs.next()) {
      System.out.println("Please enter the country of the passenger:");
      country = scan.nextLine();
      String addToPassengers = "insert into passengers values ('" + pub_email + "', '" + name + "', '" + country + "')";
      sql_handler.runSQLStatement(addToPassengers);
    } else {
      ResultSet passengerRs1 = sql_handler.runSQLQuery(passengerquery);
      while (passengerRs1.next()) {
        name = passengerRs1.getString("name");
        country = passengerRs1.getString("country");
      }
    }
    
    boolean has_dep_flight     = (flightno1 != null);
    boolean has_dep_sec_flight = (flightno2 != null);
    boolean has_round_trip     = (flightno3 != null);
    boolean has_ret_sec_flight = (flightno4 != null);
    
    try{
      sql_handler.con.setAutoCommit(false);
      if (has_dep_flight)
      {
        BookFlight(flightno1, name);
        if (has_dep_sec_flight)
          BookFlight(flightno2, name);
      }
      
      if (has_round_trip)
      {
        BookFlight(flightno3, name);
        if (has_ret_sec_flight)
          BookFlight(flightno4, name);
      }
      sql_handler.con.commit();
      
      return;
      
    } catch (SQLException sqle) {
      System.out.println("Error: Booking failed - please see error for more information! \nYour request has been fully reverted.");
      System.out.println(sqle);
      sql_handler.con.rollback();
    }
    
    return;
    /*
    // process...
    if (flightno2 == null) {
      System.out.println("Only one flight selected - please confirm your booking: ");
      
      /*
      String query1 = "select src, dst, dep_date, to_char(dep_time, 'hh24:mi') as dept, to_char(arr_time, 'hh24:mi') as arrt, fare, seats, price " +
              "from available_flights where flightno='"+flightno1+"'";
      ResultSet rs = sql_handler.runSQLQuery(query1);

      String src = "", depdate = "01-JAN-90", dst = "", dept = "", arrt = "", fare = "", seats = "", price = "";
      String convdate = "01-JAN-90";

      while (rs.next()) {
        src = rs.getString("src");
        depdate = rs.getString("dep_date");
        dst = rs.getString("dst");
        dept = rs.getString("dept");
        arrt = rs.getString("arrt");
        fare = rs.getString("fare");
        seats = rs.getString("seats");
        price = rs.getString("price");
        DateFormat df = new SimpleDateFormat("dd-MMM-yy");
        DateFormat initialdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
          depdate = depdate.substring(0, 10);
          convdate = df.format(initialdf.parse(depdate));
        } catch (ParseException e) { System.out.println(e); }
      }
      System.out.println("-----------------------------------------");
      System.out.println("Flight Info:");
      System.out.println("    Flight #:  "+flightno1);
      System.out.println("    Price:     "+price);
      System.out.println("    Source:    "+src);
      System.out.println("    Dest.:     "+dst);
      System.out.println("    Dep. Date: "+convdate);
      System.out.println("    Dep. Time: "+dept);
      System.out.println("    Arr. Time: "+arrt);
      System.out.println("    Name:      "+name);
      System.out.println("    Country:   "+country);
      System.out.println("-----------------------------------------");
      
      System.out.println("\nDo you want to (B)ook this flight? Or (R)eturn to main page?");
      String confirm = scan.nextLine();
      
        try {
          sql_handler.con.setAutoCommit(false);
          GenerateViews();
          //String checkFlightExists = "select flightno from available_flights where flightno = '" + flightno1 "'";
          //no way to do queries in a transaction - use a catch { sql_handler.con.rollback(); }
          String query = "select * from available_flights where flightno='"+flightno1+"'";
          sql_handler.runSQLQuery(query);
          String addToTickets = "insert into tickets values (" + maxTno + ", '" + name + "', '" + pub_email + "', " + price + ")";
          sql_handler.runSQLStatement(addToTickets);
          String addToBookings = "insert into bookings values (" + maxTno + ", '" + flightno1 + "', '" + fare + "', '" + convdate + "', null)";
          sql_handler.runSQLStatement(addToBookings);
          if ()
          
          sql_handler.con.commit();
          System.out.println("+-=-=-=-=-=-=-=-=-=-=-=-=-=-=-==-=-=-=-=-=-=-=-=-=-=-=-=-=-+");
          System.out.println("Success - you have booked your flight!");
          System.out.println("Your ticket number is: " + maxTno);
          System.out.println("+-=-=-=-=-=-=-=-=-=-=-=-=-=-=-==-=-=-=-=-=-=-=-=-=-=-=-=-=-+\n");
        } catch (SQLException e) {
          System.out.println("Error: Invalid submission value - rollback completed.");
          System.out.println(e);
          sql_handler.con.rollback();
        }
      } else if (confirm.equals("R") || confirm.equals("r")) {
        MainHub();
      }
      // run query only once here
    } else {
      System.out.println("Two flights to be booked - please confirm your bookings:");
      String query1 = "select src, dst, dep_date, to_char(dep_time, 'hh24:mi') as dept, to_char(arr_time, 'hh24:mi') as arrt, fare, seats, price " +
              "from available_flights where flightno='"+flightno1+"'";
      String query2 = "select src, dst, dep_date, to_char(dep_time, 'hh24:mi') as dept, to_char(arr_time, 'hh24:mi') as arrt, fare, seats, price " +
              "from available_flights where flightno='"+flightno1+"'";
      ResultSet rs1 = sql_handler.runSQLQuery(query1);
      ResultSet rs2 = sql_handler.runSQLQuery(query2);

      String src1 = "", depdate1 = "01-JAN-90", dst1 = "", dept1 = "", arrt1 = "", fare1 = "", seats1 = "", price1 = "", convdate1 = "01-JAN-90";
      String src2 = "", depdate2 = "01-JAN-90", dst2 = "", dept2 = "", arrt2 = "", fare2 = "", seats2 = "", price2 = "", convdate2 = "01-JAN-90";

      DateFormat df = new SimpleDateFormat("dd-MMM-yy");
      DateFormat initialdf = new SimpleDateFormat("yyyy-MM-dd");

      while (rs1.next()) {
        src1 = rs1.getString("src");
        depdate1 = rs1.getString("dep_date");
        dst1 = rs1.getString("dst");
        dept1 = rs1.getString("dept");
        arrt1 = rs1.getString("arrt");
        fare1 = rs1.getString("fare");
        seats1 = rs1.getString("seats");
        price1 = rs1.getString("price");
        try {
          depdate1 = depdate1.substring(0, 10);
          convdate1 = df.format(initialdf.parse(depdate1));
        } catch (ParseException e) { System.out.println(e); }
      }

      while (rs2.next()) {
        src2 = rs2.getString("src");
        depdate2 = rs2.getString("dep_date");
        dst2 = rs2.getString("dst");
        dept2 = rs2.getString("dept");
        arrt2 = rs2.getString("arrt");
        fare2 = rs2.getString("fare");
        seats2 = rs2.getString("seats");
        price2 = rs2.getString("price");
        try {
          depdate2 = depdate2.substring(0, 10);
          convdate2 = df.format(initialdf.parse(depdate2));
        } catch (ParseException e) { System.out.println(e); }
      }

      System.out.println("-----------------------------------------");
      System.out.println("Flight #1 Info:");
      System.out.println("    Flight #:  "+flightno1);
      System.out.println("    Price:     "+price1);
      System.out.println("    Source:    "+src1);
      System.out.println("    Dest.:     "+dst1);
      System.out.println("    Dep. Date: "+convdate1);
      System.out.println("    Dep. Time: "+dept1);
      System.out.println("    Arr. Time: "+arrt1);
      System.out.println("    Name:      "+name);
      System.out.println("    Country:   "+country);
      System.out.println("-----------------------------------------");
      System.out.println("Flight #2 Info:");
      System.out.println("    Flight #:  "+flightno2);
      System.out.println("    Price:     "+price2);
      System.out.println("    Source:    "+src2);
      System.out.println("    Dest.:     "+dst2);
      System.out.println("    Dep. Date: "+convdate2);
      System.out.println("    Dep. Time: "+dept2);
      System.out.println("    Arr. Time: "+arrt2);
      System.out.println("    Name:      "+name);
      System.out.println("    Country:   "+country);
      System.out.println("-----------------------------------------");

      System.out.println("\nDo you want to (B)ook these flight? Or (R)eturn to main page?");
      String confirm = scan.nextLine();
      int maxTno1 = 0;
      int maxTno2 = 0;
      if (confirm.equals("B") || confirm.equals("b")) {
        String findTno = "select max(tno) as tno from tickets";
        ResultSet tnoVal = sql_handler.runSQLQuery(findTno);
        while (tnoVal.next()) {
          try { 
            String tmpTno = tnoVal.getString("tno");
            if (tmpTno != null) {
              maxTno1 = Integer.parseInt(tmpTno);
            }
          } catch (SQLException e) {}
        }
        maxTno1 = maxTno1 + 100;
        maxTno2 = maxTno1 + 1;
        try {
          sql_handler.con.setAutoCommit(false);
          GenerateViews();
          //String checkFlightExists = "select flightno from available_flights where flightno = '" + flightno1 "'";
          //no way to do queries in a transaction - use a catch { sql_handler.con.rollback(); }
          String addToTickets1 = "insert into tickets values (" + maxTno1 + ", '" + name + "', '" + pub_email + "', " + price1 + ")";
          String addToTickets2 = "insert into tickets values (" + maxTno2 + ", '" + name + "', '" + pub_email + "', " + price2 + ")";
          sql_handler.runSQLStatement(addToTickets1);
          sql_handler.runSQLStatement(addToTickets2);

          String addToBookings1 = "insert into bookings values (" + maxTno1 + ", '" + flightno1 + "', '" + fare1 + "', '" + convdate1 + "', null)";
          String addToBookings2 = "insert into bookings values (" + maxTno2 + ", '" + flightno2 + "', '" + fare2 + "', '" + convdate2 + "', null)";
          sql_handler.runSQLStatement(addToBookings1);
          sql_handler.runSQLStatement(addToBookings2);

          sql_handler.con.commit();
          System.out.println("+-=-=-=-=-=-=-=-=-=-=-=-=-=-=-==-=-=-=-=-=-=-=-=-=-=-=-=-=-+");
          System.out.println("Success - you have booked your flight!");
          System.out.println("Your ticket number for flight 1 is: " + maxTno1);
          System.out.println("Your ticket number for flight 1 is: " + maxTno2);
          System.out.println("+-=-=-=-=-=-=-=-=-=-=-=-=-=-=-==-=-=-=-=-=-=-=-=-=-=-=-=-=-+\n");
        } catch (SQLException e) {
          System.out.println("Error: Invalid submission value - rollback completed.");
          System.out.println(e);
          sql_handler.con.rollback();
        }
      } else if (confirm.equals("R") || confirm.equals("r")) {
        MainHub();
      }
    }
    MainHub();*/
  }

  /* List existing bookings. A user should be able to list all
  his/her existing bookings. The result will be given in a list
  form and will include for each booking, the ticket number,
  the passenger name, the departure date and the price. The
  user should be able to select a row and get more detailed
  information about the booking.

  // select b.tno, p.name, s.dep_date, t.paid_price
  // from bookings b, tickets t, passengers p, sch_flights s
  // where b.tno like t.tno and p.email like t.email and s.flightno like b.flightno

  /**
   *
   * @param role
   * @throws SQLException
   */
  public void ExistingBookings() throws SQLException {
    // search for user bookings
    // put them in a list, sep. by number index
    // System.out.println("pub_email: " + pub_email);
    System.out.println("Your current bookings for this account are: ");
    String query = "select b.tno, p.name, to_char(b.dep_date, 'yyyy-MM-dd') as dep_date, t.paid_price " +
      "from bookings b, tickets t, passengers p " +
      "where b.tno = t.tno and t.name = p.name and t.email = p.email and t.email = '" + pub_email + "'";
    ResultSet rs = sql_handler.runSQLQuery(query);
    int intId = 0;
    ArrayList<String> tnolist = new ArrayList<>();

    while (rs.next()) {
      String tno = rs.getString("tno");
      String name = rs.getString("name");
      String depdate = rs.getString("dep_date");
      String price = rs.getString("PAID_PRICE");
      tnolist.add(tno);
      intId++;

      System.out.println("+-=-=-=-=-=-=-=-=-=-=-=-=-=-=-==-=-=-=-=-=-=-=-=-=-=-=-=-=-+");
      System.out.println("Booking ID: " + intId);
      System.out.println("Ticket No.: " + tno);
      System.out.println("Name:       " + name);
      System.out.println("Dep. Date:  " + depdate);
      System.out.println("Price:      " + price);
    }
    System.out.println("+-=-=-=-=-=-=-=-=-=-=-=-=-=-=-==-=-=-=-=-=-=-=-=-=-=-=-=-=-+");
    System.out.println("Please select a booking by ID to view more information, "
                        + "or (e)xit.\n");
    while(true) {
      String i = scan.nextLine();
      if (isInteger(i, 10)) {
        Integer intIndex = Integer.parseInt(i);
        if (intIndex <= tnolist.size() && intIndex > 0) {
          intIndex = intIndex - 1;
          BookingDetail(tnolist.get(intIndex));
        } else {
          System.out.println("Invalid entry - please try again.");
        }
      } else if (i.equals("e") || i.equals("E")) {
        MainHub();
      } else {
        System.out.println("Invalid entry - please try again.");
      }
    }
  }

  // select tno, flight_no, fare, dep_date, seat
  // from bookings
  // where tno like 'TNO_INPUT'

  /**
   *
   * @param role
   * @param tno
   * @throws SQLException
   */
  public void BookingDetail(String tno) throws SQLException {
    Scanner scan = new Scanner(System.in);
    System.out.println("Your booking details is as follows: ");
    String query = "select b.tno, b.flightno, b.fare, p.name, p.email, p.country, to_char(b.dep_date, 'yyyy-MM-dd') as dep_date, t.paid_price " +
      "from bookings b, tickets t, passengers p " +
      "where b.tno = t.tno and t.email = p.email and t.email = '" + pub_email + "' and p.name = t.name and b.tno = " + tno;
    ResultSet rs = sql_handler.runSQLQuery(query);
    while (rs.next()) {
      String flightno = rs.getString("flightno");
      String fare = rs.getString("fare");
      String name = rs.getString("name");
      String email = rs.getString("email");
      String country = rs.getString("country");
      String depdate = rs.getString("dep_date");
      String price = rs.getString("PAID_PRICE");

      System.out.println("+-=-=-=-=-=-=-=-=-=-=-Flight Details-=-=-=-=-=-=-=-=-=-=-=-+");
      System.out.println("Ticket Number: " + tno);
      System.out.println("Flight Number: " + flightno);
      System.out.println("Fare Type:     " + fare);
      System.out.println("Dep. Date:     " + depdate);
      System.out.println("Price:         " + price);
      System.out.println("+-=-=-=-=-=-=-=-=-=-=-=-User Details-=-=-=-=-=-=-=-=-=-=-=-+");
      System.out.println("Email:         " + email);
      System.out.println("Name:          " + name);
      System.out.println("Country:       " + country);
      System.out.println("+-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-==-=-=-=-=-=-=-=-=-=-=-=-+");

    }
    System.out.println("Return to (b)ookings list, (c)ancel booking or (e)xit bookings page?");
    while(true) {
      String i = scan.nextLine();
      if (i.equals("b") || i.equals("B")) {
        ExistingBookings();
      }
      else if (i.equals("e") || i.equals("E")) {
        MainHub();
      } else if (i.equals("c") || i.equals("C")) {
        CancelBooking(tno);
      } else {
        System.out.println("Invalid entry - please try again.");
      }
    }
  }

  /* Cancel a booking. The user should be able to select a
  booking from those listed under "list existing bookings"
  and cancel it. The proper tables should be updated to reflect
  the cancelation and the cancelled seat should be returned to
  the system and is made available for future bookings.
  */

  // delete from bookings
  // where tno like 'TNO_INPUT'

  public void CancelBooking(String tno) throws SQLException { // pass in booking value in here?
    // delete the booking
    // return to mainhub
    try {
      sql_handler.con.setAutoCommit(false);
      String delbookings = "delete from bookings where tno = " + tno;
      String delticket = "delete from tickets where tno = " + tno;
      sql_handler.runSQLStatement(delbookings);
      sql_handler.runSQLStatement(delticket);
      sql_handler.con.commit();
      System.out.println("Booking has been deleted.");
      GenerateViews();
    } catch (SQLException e) {
      System.out.println("Error: Booking cancellation failed - please see error for more information! \nYour request has been fully reverted.");
      System.out.println(e);
      sql_handler.con.rollback();
    }
    MainHub();
  }

  /* Logout. There must be an option to log out of the system. At
  logout, the field last_login in users is set to the current system date.
  */

  // get the user into the rs... select * from users where user_id like 'INPUTUSERID'
  // rs.updateString(4, sysdate); // currently only in the result set: Indexing from 1.
  // rs.updateRow(); // makes the changes permanent

  public void Logout() throws SQLException {
    // logout
    // detail system date for last_login
    // return to main

    String statement = "update users "
                     + "set last_login=sysdate "
                     + "where email='"+pub_email+"'";

    sql_handler.runSQLStatement(statement);
    System.out.println("You have now been logged out.");
    WelcomeScreen();
  }

  /* AIRLINE AGENT ONLY: Record a flight departure. After a plane takes off,
  the user may want to record the departure. Your system should support the
  task and make necessary updates such as updating the act_dep_time.
  */

  // select * from sch_flights // flightno, dep_date, act_dep_time, act_arr_time
  // rs.updateString(3, INPUT_DATE);

  public void RecordDeparture() throws SQLException {
    String flightno = "";
    while(true) {
      System.out.println("Flight number: ");
      flightno = scan.nextLine();
      String findflightno = "select * from sch_flights where flightno = '" + flightno.toUpperCase() + "'";
      ResultSet rsflightno = sql_handler.runSQLQuery(findflightno);
      if (!rsflightno.next()) {
        System.out.println("Invalid flight number.");
      } else {
        break;
      }
    }
    System.out.println("Departure time: - format is 'yyyy/mm/dd hh24:mi:ss', example: 2003/05/03 21:02:44 or select (C)urrent time.");
    while(true) {
      String statement = "";
      String deptime = scan.nextLine();
      if (isValidDate(deptime)) {
        DateFormat df = new SimpleDateFormat("yyyy/mm/dd hh:mm:ss");
        try {
          java.util.Date depDate = new java.util.Date();
          depDate = df.parse(deptime);
          statement = "update sch_flights "
          + "set act_dep_time = (TO_DATE('" + deptime + "', 'yyyy/mm/dd hh24:mi:ss')) "
          + "where flightno = '"+flightno.toUpperCase()+"'";
          sql_handler.runSQLStatement(statement);
          System.out.println("Flight departure time successfully updated.");
          MainHub();
        } catch (ParseException e) {}
      } else if (deptime.equals("C") || deptime.equals("c")) {
        statement = "update sch_flights "
        + "set act_dep_time = sysdate "
        + "where flightno = '"+flightno.toUpperCase()+"'";
        sql_handler.runSQLStatement(statement);
        System.out.println("Flight departure time successfully updated.");
        MainHub();
      } else {
        System.out.println("Incorrect input - please try again.");
      }
    }
  }

  /* AIRLINE AGENT ONLY: Record a flight arrival. After a landing, the user may
  want to record the arrival and your system should support the task.
  */

  // select * from sch_flights // flightno, dep_date, act_dep_time, act_arr_time
  // rs.updateString(4, INPUT_DATE)

  public void RecordArrival() throws SQLException {
    String flightno = "";
    while(true) {
      System.out.println("Flight number: ");
      flightno = scan.nextLine();
      String findflightno = "select * from sch_flights where flightno = '" + flightno.toUpperCase() + "'";
      ResultSet rsflightno = sql_handler.runSQLQuery(findflightno);
      if (!rsflightno.next()) {
        System.out.println("Invalid flight number.");
      } else {
        break;
      }
    }
    System.out.println("Arrival time: - format is 'yyyy/mm/dd hh24:mi:ss', example: 2003/05/03 21:02:44 or select (C)urrent time.");
    while(true) {
      String statement = "";
      String arrtime = scan.nextLine();
      if (isValidDate(arrtime)) {
        DateFormat df = new SimpleDateFormat("yyyy/mm/dd hh:mm:ss");
        try {
          java.util.Date arrDate = new java.util.Date();
          arrDate = df.parse(arrtime);
          statement = "update sch_flights "
          + "set act_arr_time = (TO_DATE('" + arrtime + "', 'yyyy/mm/dd hh24:mi:ss')) "
          + "where flightno = '"+flightno.toUpperCase()+"'";
          sql_handler.runSQLStatement(statement);
          System.out.println("Flight arrival time successfully updated.");
          MainHub();
        } catch (ParseException e) {}
      } else if (arrtime.equals("C") || arrtime.equals("c")) {
        statement = "update sch_flights "
        + "set act_arr_time = sysdate "
        + "where flightno = '"+flightno.toUpperCase()+"'";
        sql_handler.runSQLStatement(statement);
        System.out.println("Flight arrival time successfully updated.");
        MainHub();
      } else {
        System.out.println("Incorrect input - please try again.");
      }
    }
  }

  /* CHOOSE ONE OF THREE OPTIONS:
  Support search and booking of round-trips. The system should offer an option for round-trips.
  If this option is selected, your system will get a return date from the user, and will list
  the flights in both directions, sorted by the sum of the price (from lowest to the highest).
  The user should be able to select an option and book it. (PREFERRED!)

  Support search and booking of flights with three connecting flights. In its default setting,
  your system will search for flights with two connections at most. In implementing this
  functionality, your system should offer an option to raise this maximum to three connections.
  Again this is an option to be set by user when running your system and cannot be the
  default setting of your application.

  Support search and booking for parties of size larger than one. There should be an option for
  the user to state the number of passengers. The search component of your system will only list
  flights that have enough seats for all party members. Both the seat pricing and the booking will
  be based on filling the lowest fare seats first before moving to the next fare. For example,
  suppose there are 2 seats available in the lowest fare and 5 seats in some higher-priced fare.
  For a party of size 4, your system will book those 2 lowest fare seats and another 2 seats in
  the next fare type that is available.
  */

  // select f.flightno, a1.acode, a2.acode, dep_time, dep_time + est_dur (not right...) as arr_time, 0 as num_conn, 0 as layover_time, ff1.price, ff1.limit - COUNT(b.seat) as open_seats
  // from flights f, flight_fares ff1, airports a1, airports a2, bookings b
  // where (f.src like 'SRC' or a1.name like 'SRC')
  // and (f.dst like 'DST' or a2.name like 'DST')
  // and (f.dep_time like 'DEP_TIME')
  // and f.src like a1.acode and f.dst like a2.acode and f.flightno like ff1.flightno
  // and b.flightno like f.flightno and b.fare like ff1.fare
  // UNION
  // (add the 1-connection flights ...)
  // GROUP BY flightno, ...
  // ORDER BY PRICE

  /**
   * FIXME: kappa
   * @throws SQLException
   */
  public void RoundTrips() throws SQLException 
  { 
    String srcACode = "";
    String destACode = "";
    String depDate = "";
    String retDate = "";

    while (!validAcode(srcACode))
    {
      System.out.println("Please enter the airport code for your source:");
      srcACode = scan.nextLine();
    }

    while (!validAcode(destACode))
    {
      System.out.println("Please enter the airport code for your destination:");
      destACode = scan.nextLine();
    }

    // add departure date
    System.out.println("Please enter your departure date in format DD/MMM/YYYY - eg: 01/10/2015 for October 10, 2015");
    depDate = scan.nextLine();
    
    System.out.println("Please enter your return date in format DD/MMM/YYYY - eg: 01/10/2015 for October 10, 2015");
    retDate = scan.nextLine();
    
    try {
      sql_handler.runSQLStatement("drop view good_dep_trips");
      sql_handler.runSQLStatement("drop view good_ret_trips");
    } catch (SQLException sqle) {
      
    }
    
    String stmt1 = "create view good_dep_trips (flightno1, flightno2, layover, price) as " +
                    "select flightno1, flightno2, layover, price " +
                    "from ( " +
                    "select flightno1, flightno2, layover, price, row_number() over (order by price asc) rn " +
                    "from " +
                    "(select flightno1, flightno2, layover, price " +
                    "from good_connections " +
                    "where to_char(dep_date,'DD/MM/YYYY')='"+ depDate +"' and src='"+srcACode.toUpperCase()+"' and dst='"+destACode.toUpperCase()+"' " +
                    "union " +
                    "select flightno flightno1, '' flightno2, 0 layover, price " +
                    "from available_flights " +
                    "where to_char(dep_date,'DD/MM/YYYY')='"+ depDate +"' and src='"+srcACode.toUpperCase()+"' and dst='"+destACode.toUpperCase()+"')) " +
                    "order by price";
    
    String stmt2 =  "create view good_ret_trips (flightno1, flightno2, layover, price) as " +
                    "select flightno1, flightno2, layover, price " +
                    "from ( " +
                    "select flightno1, flightno2, layover, price, row_number() over (order by price asc) rn " +
                    "from " +
                    "(select flightno1, flightno2, layover, price " +
                    "from good_connections " +
                    "where to_char(dep_date,'DD/MM/YYYY')='"+ retDate +"' and src='"+destACode.toUpperCase()+"' and dst='"+srcACode.toUpperCase()+"' " +
                    "union " +
                    "select flightno flightno1, '' flightno2, 0 layover, price " +
                    "from available_flights " +
                    "where to_char(dep_date,'DD/MM/YYYY')='"+ retDate +"' and src='"+destACode.toUpperCase()+"' and dst='"+srcACode.toUpperCase()+"')) " +
                    "order by price";
    
    String query =  "select gdt.flightno1 as dep_flightno1, gdt.flightno2 as dep_flightno2, gdt.layover as dep_layover, " +
                    "grt.flightno1 as ret_flightno1, grt.flightno2 as ret_flightno2, grt.layover as ret_layover, " +
                    "gdt.price+grt.price as price " +
                    "from good_dep_trips gdt, good_ret_trips grt " +
                    "group by gdt.flightno1, gdt.flightno2, gdt.layover, gdt.price, grt.flightno1, grt.flightno2, grt.layover, grt.price " +
                    "order by price";
    
    sql_handler.runSQLStatement(stmt1);
    sql_handler.runSQLStatement(stmt2);
    ResultSet rs = sql_handler.runSQLQuery(query);
    System.out.println("The following flight plans match your criteria:\n");
    ArrayList<String> flightnolist1 = new ArrayList<>();
    ArrayList<String> flightnolist2 = new ArrayList<>();
    ArrayList<String> flightnolist3= new ArrayList<>();
    ArrayList<String> flightnolist4 = new ArrayList<>();
    int planId = 1;    
    while(rs.next())
    {
      String dep_flightno1 = rs.getString("DEP_FLIGHTNO1");
      String dep_flightno2 = rs.getString("DEP_FLIGHTNO2");
      String dep_layover = rs.getString("DEP_LAYOVER");
      boolean has_sec_dep_flight = (dep_flightno2 != null);
      
      String ret_flightno1 = rs.getString("RET_FLIGHTNO1");
      String ret_flightno2 = rs.getString("RET_FLIGHTNO2");
      String ret_layover = rs.getString("RET_LAYOVER");
      boolean has_sec_ret_flight = (ret_flightno2 != null);
      
      String price = rs.getString("PRICE");
      
      flightnolist1.add(dep_flightno1);
      flightnolist2.add(dep_flightno2);

      query = "select src, dst, to_char(dep_time, 'hh24:mi') as dept, to_char(arr_time, 'hh24:mi') as arrt, seats " +
              "from available_flights where flightno='"+dep_flightno1+"'";
      SQLHandler sqlh1 = new SQLHandler(sql_handler.get_uname(), sql_handler.get_pword());
      ResultSet rs1 = sqlh1.runSQLQuery(query);
      rs1.next();
      String src1 = rs1.getString("src");
      String dst1 = rs1.getString("dst");
      String dep1 = rs1.getString("dept");
      String arr1 = rs1.getString("arrt");
      int sea1 = Integer.parseInt(rs1.getString("seats"));

      String src2 = "-";
      String dst2 = "-";
      String dep2 = "-";
      String arr2 = "-";
      int sea2 = 0;

      if (has_sec_dep_flight)
      {
        query = "select src, dst, to_char(dep_time, 'hh24:mi') as dept, to_char(arr_time, 'hh24:mi') as arrt, seats " +
            "from available_flights where flightno='"+dep_flightno2+"'";
        ResultSet rs2 = sqlh1.runSQLQuery(query);
        rs2.next();
        src2 = rs2.getString("src");
        dst2 = rs2.getString("dst");
        dep2 = rs2.getString("dept");
        arr2 = rs2.getString("arrt");
        sea2 = Integer.parseInt(rs2.getString("seats"));
      }

      System.out.println  ("-----------------------------------------");
      System.out.println  ("Flight Plan: " + planId);
      System.out.println  ("Price: " + price);
      if (has_sec_dep_flight)
      {
        System.out.println("Departing Flight Info:");
        System.out.println("    Number of Stops: 1");
        System.out.println("    Layover Time: " + (Integer.parseInt(dep_layover)*1440) +" minutes.");
        System.out.println("    Num Seats: " + Math.min(sea1, sea2));
        System.out.println("               Flight 1          Flight 2");
        System.out.println("               --------          --------");
        System.out.println("    Flight #:  "+dep_flightno1+"          "+dep_flightno2);
        System.out.println("    Source:    "+src1+"             "+src2);
        System.out.println("    Dest.:     "+dst1+"             "+dst2);
        System.out.println("    Dep. Time: "+dep1+"           "+dep2);
        System.out.println("    Arr. Time: "+arr1+"           "+arr2);
      }
      else
      {
        System.out.println("Departing Flight Info:");
        System.out.println("    Number of Stops: 0");
        System.out.println("    Num Seats: " + sea1);
        System.out.println("    Flight #:  "+dep_flightno1);
        System.out.println("    Source:    "+src1);
        System.out.println("    Dest.:     "+dst1);
        System.out.println("    Dep. Time: "+dep1);
        System.out.println("    Arr. Time: "+arr1);
      }
      
      flightnolist3.add(ret_flightno1);
      flightnolist4.add(ret_flightno2);

      query = "select src, dst, to_char(dep_time, 'hh24:mi') as dept, to_char(arr_time, 'hh24:mi') as arrt, seats " +
              "from available_flights where flightno='"+ret_flightno1+"'";
      rs1 = sqlh1.runSQLQuery(query);
      rs1.next();
      src1 = rs1.getString("src");
      dst1 = rs1.getString("dst");
      dep1 = rs1.getString("dept");
      arr1 = rs1.getString("arrt");
      sea1 = Integer.parseInt(rs1.getString("seats"));

      src2 = "-";
      dst2 = "-";
      dep2 = "-";
      arr2 = "-";
      sea2 = 0;

      if (has_sec_ret_flight)
      {
        query = "select src, dst, to_char(dep_time, 'hh24:mi') as dept, to_char(arr_time, 'hh24:mi') as arrt, seats " +
            "from available_flights where flightno='"+ret_flightno2+"'";
        ResultSet rs2 = sqlh1.runSQLQuery(query);
        rs2.next();
        src2 = rs2.getString("src");
        dst2 = rs2.getString("dst");
        dep2 = rs2.getString("dept");
        arr2 = rs2.getString("arrt");
        sea2 = Integer.parseInt(rs2.getString("seats"));
      }

      if (has_sec_ret_flight)
      {
        System.out.println("Return Flight Info:");
        System.out.println("    Number of Stops: 1");
        System.out.println("    Layover Time: " + (Integer.parseInt(ret_layover)*1440) +" minutes.");
        System.out.println("    Num Seats: " + Math.min(sea1, sea2));
        System.out.println("               Flight 1          Flight 2");
        System.out.println("               --------          --------");
        System.out.println("    Flight #:  "+ret_flightno1+"          "+ret_flightno2);
        System.out.println("    Source:    "+src1+"             "+src2);
        System.out.println("    Dest.:     "+dst1+"             "+dst2);
        System.out.println("    Dep. Time: "+dep1+"           "+dep2);
        System.out.println("    Arr. Time: "+arr1+"           "+arr2);
      }
      else
      {
        System.out.println("Return Flight Info:");
        System.out.println("    Number of Stops: 0");
        System.out.println("    Num Seats: " + sea1);
        System.out.println("    Flight #:  "+ret_flightno1);
        System.out.println("    Source:    "+src1);
        System.out.println("    Dest.:     "+dst1);
        System.out.println("    Dep. Time: "+dep1);
        System.out.println("    Arr. Time: "+arr1);
      }

      sqlh1.close();
      planId++;
    }
    System.out.println("Round-trips are currently being sorted by number of connections, and price.");
    
    while(true) {
      System.out.println("(R)eturn to main menu.");
      System.out.println("Or select a booking with the corresponding ID (eg. 1, 2, ...)");

      String i = scan.nextLine();
      if (i.equals("R") || i.equals("r")) {
        MainHub();
      } else if (isInteger(i,10)) {
        Integer intIndex = Integer.parseInt(i);
        if (intIndex <= flightnolist1.size() && intIndex > 0) {
          intIndex = intIndex - 1;
          MakeABooking(flightnolist1.get(intIndex), 
                       flightnolist2.get(intIndex),
                       flightnolist3.get(intIndex),
                       flightnolist4.get(intIndex));
        } else {
          System.out.println("Invalid entry - please try again.");
        }
      } else {
        System.out.println("Invalid entry - please try again.");
      }
    }
  }

  /**
   * Takes an email address and returns
   * true or false if valid or invalid.
   * @param e A string for the email.
   * @return boolean
   */
  private boolean validEmail(String e)
  {
    if (e.length() > 20)
      return false;

    String e_regex = "(\\w|\\.)+\\@\\w+\\.\\w+";
    Pattern p = Pattern.compile(e_regex);
    Matcher m = p.matcher(e);
    return m.matches();
  }

  /**
   * Takes a password and returns true or false
   * if it is valid or invalid respectively.
   * @param pword A string for the password.
   * @return boolean
   */
  private boolean validPassword(String pword)
  {
    if (pword.length() > 4 || pword.length() < 1)
      return false;

    String p_regex = "\\w+";
    Pattern p = Pattern.compile(p_regex);
    Matcher m = p.matcher(pword);
    return m.matches();
  }

  /**
   * FIXME: add method comment
   * @param s
   * @param radix
   * @return
   */
  // code taken from http://stackoverflow.com/questions/5439529/determine-if-a-string-is-an-integer-in-java
  // by corsiKa
  public static boolean isInteger(String s, int radix) {
    if(s.isEmpty()) return false;
    for(int i = 0; i < s.length(); i++) {
        if(i == 0 && s.charAt(i) == '-') {
            if(s.length() == 1) return false;
            else continue;
        }
        if(Character.digit(s.charAt(i),radix) < 0) return false;
    }
    return true;
  }

  // code taken from http://stackoverflow.com/questions/11480542/fastest-way-to-tell-if-a-string-is-a-valid-date
  // by victor.hernandez
  public static boolean isValidDate(String input) {
    boolean valid = false;
    try {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/mm/dd hh:mm:ss");
        java.util.Date output = dateFormat.parse(input);
        valid = true;
    } catch (ParseException e) {}
    return valid;
}

  /**
   * FIXME: finish comment
   * @param ac
   * @return
   * @throws SQLException
   */
  private boolean validAcode(String ac) throws SQLException
  {
    if (ac.equals(""))
      return false;

    String query = "select * from airports where acode='"+ac.toUpperCase()+"'";
    ResultSet rs = sql_handler.runSQLQuery(query);

    if (rs.next())
      return true;

    System.out.println("Sorry the airport code could not be matched.");

    query = "select * from airports where regexp_like(name, '"+ac+"', 'i')";
    rs = sql_handler.runSQLQuery(query);

    while (rs.next())
    {
      if (rs.isFirst())
      {
        System.out.println("Did you mean one of the following airports?");
        System.out.println("\nCode    Ariport Name");
        System.out.println  ("+--+    +-=-=-=-=-=+");
      }

      System.out.println(rs.getString("ACODE") + "     " + rs.getString("name"));
    }

    System.out.println();

    return false;
  }
}
