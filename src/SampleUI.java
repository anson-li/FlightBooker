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

  UI(SQLHandler sql_handler, Console con)
  {
    num_uis += 1;
    this.sql_handler = sql_handler;
    this.con = con;
    this.scan = new Scanner(System.in);
    try {
      WelcomeScreen();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  };

  public void WelcomeScreen() throws SQLException {
    System.out.println("Welcome to Air Kappa!");
    while(true) {
      System.out.println("Please (l)ogin or (r)egister to use our services, "
                       + "\nor (e)xit the program.");
      String i = scan.nextLine();
      if (i.equals("l") || i.equals("L")) {
        Login();
      } else if (i.equals("r") || i.equals("R")) {
        Register();
      } else if (i.equals("e")  || i.equals("E")) {
        System.out.println("System is exiting; "
                         + "thank you for visiting Air Kappa!");
        scan.close();
        System.exit(0);
      } else {
        System.out.println("Invalid character; please try again:");
      }
    }
  }

  // create table users { email String, password String, role String, last_login DATE }
  // createString = "insert into users values ('email', 'password', user, sysdate)";
  // createString = "insert into users values ('email', 'password', poweruser, sysdate)";

  public void Register() throws SQLException {

    String email = "";
    String password = "";

    System.out.println("Registration:");
    try {
      email = con.readLine("Email: ");
      char[] pwArray1 = con.readPassword("Password: ");
      char[] pwArray2 = con.readPassword("Confirm Password:");
      if (Arrays.equals(pwArray1, pwArray2))
        password = String.valueOf(pwArray1);
      else
        return;
    } catch (IOError ioe){
      System.err.println(ioe.getMessage());
    }

    for (int i = 0; i < 20; i++)
      if (!validEmail(email))
        System.out.println("not valid");
      else
        System.out.println("valid");

    if (!validPassword(password));

    // check if user already in DB
    // add user to DB

    /* need to implement verification system...
    while (isValidEmailAddress(email) != true) {
      System.out.println("Invalid email... Please enter your email: ");
      email = scan.next();
    }*/
    MainHub();
  }

  private void Login() {
    System.out.println("Login.");
    String email = "";
    String pword = "";
    while(true) {
      email = con.readLine("Email: ");
      char[] pwordA = con.readPassword("Password:");
      pword = String.valueOf(pwordA);
      // if verification is valid ... { MainHub(-pass in permissions); }
      MainHub();
    }
  }

  public void MainHub() throws SQLException {
    Scanner scan = new Scanner(System.in);
    while(true) {
      System.out.println("Main area reached. Please select from the following options:");
      System.out.println("(S)earch for flights & make a booking, See (E)xisting bookings, (C)ancel a booking, Find (R)ound trips, (L)og out.");
      String input = scan.nextLine();
      if (input.equals("S") || input.equals("s")) {
        SearchForFlights();
      } else if (input.equals("E") || input.equals("e")) {
        ExistingBookings();
      } else if (input.equals("C") || input.equals("c")) {
        CancelBooking();
      } else if (input.equals("R") || input.equals("r")) {
        RoundTrips();
      } else if (input.equals("L") || input.equals("l")) {
        Logout();
      } else {
        System.out.println("Invalid character entered.");
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

  // after the requery - ORDER BY num_conn

  public void SearchForFlights() throws SQLException {
    Scanner scan = new Scanner(System.in);
    // ask user if they want to enter the airport code for source
    System.out.println("Please enter the airport code for your source:");
    String srcACode = scan.nextLine();
    // if not valid airport code, search airport by name (partial match '%val%')
    // complete process for destination airport
    System.out.println("Please enter the airport code for your destination:");
    String destACode = scan.nextLine();
    // add departure date
    System.out.println("Please enter your departure date in format DD-MMM-YYYY - eg: 01-OCT-2015");
    String strDate = scan.nextLine();
    DateFormat df = new SimpleDateFormat("dd-MMM-yy");
    java.util.Date depDate = new java.util.Date();
    try {
        depDate = df.parse(strDate);
    } catch (ParseException e) {
        e.printStackTrace();
    }
    String query = "select FLIGHTNO, SRC, DST, DEP_TIME, EST_DUR from flights where " +
                   "SRC LIKE '" + srcACode + "' AND DST LIKE '" + destACode + "' AND DEP_TIME LIKE '" + df.format(depDate).toString().toUpperCase() + "'";
    //System.out.println(query);
    ResultSet rs = sql_handler.runSQLQuery(query);
    // search flights for direct flights and flights w one connection
    // provide information. ask user if they want to sort
    // if sort, then sort
    System.out.println("The flights that match your description are as follows:");
    // system.out.println(flightslist)
    System.out.println("\nFLIGHTNO  SRC   DST   DEP_TIME    EST_DUR");
    System.out.println("--------  ---   ---   ----------  -------");
    while (rs.next()) {
      String flightno = rs.getString("FLIGHTNO");
      String src = rs.getString("SRC");
      String dst = rs.getString("DST");
      java.sql.Date deptime = rs.getDate("DEP_TIME");
      int estdur = rs.getInt("EST_DUR");

      System.out.println(flightno + "    " + src +"   " +dst+"   " +deptime+"   " +estdur);
    }


    System.out.println("\nFlights are currently being sorted by price."
                        + "\nWould you like to sort the result based on number of connections? Y/N");
    System.out.println("Alternatively, select a booking with the corresponding ID (eg. 1, 2, ...)");
    String i = scan.nextLine();
    if (i.equals("Y")) {

    } else if (i.equals("N")) {

    }
    MainHub();
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
  issued for any reason.
  */

  // if seat is empty..
  // insert into passengers values ('EMAIL', 'NAME', 'COUNTRY')
  // insert into tickets values (tno (gen), 'EMAIL', 'PAID PRICE')
  // insert into bookings values (tno (gen), flight_no, fare, dep_date, seat)

  public void MakeABooking() throws SQLException {
    // public static void MakeABooking(int Id)
    // select a flight
    // is user listed in the flight?
    // if so, don't let the rebook.
    // if not, book. add the name & country of the passenger (ask here...)
    System.out.println("Please enter the name of the passenger:");
    String name = scan.nextLine();
    System.out.println("Please enter the country of the passenger:");
    String country = scan.nextLine();
    // process...
    System.out.println("Success - you have booked your flight!");
    MainHub();
  }

  /* List existing bookings. A user should be able to list all
  his/her existing bookings. The result will be given in a list
  form and will include for each booking, the ticket number,
  the passenger name, the departure date and the price. The
  user should be able to select a row and get more detailed
  information about the booking.
  */

  // select b.tno, p.name, s.dep_date, t.paid_price
  // from bookings b, tickets t, passengers p, sch_flights s
  // where b.tno like t.tno and p.email like t.email and s.flightno like b.flightno

  public void ExistingBookings() throws SQLException {
    // search for user bookings
    // put them in a list, sep. by number index
    System.out.println("Your current bookings for this account are: ");
    //system.out.println(data)
    System.out.println("Please select a booking by index to view more information, "
                        + "or (e)xit.");
    String i = scan.nextLine();
    if (true) { // if the coming string is an integer - DONT KNOW HOW TO DO THIS ????
      BookingDetail();
    }
    MainHub();
  }

  // select tno, flight_no, fare, dep_date, seat
  // from bookings
  // where tno like 'TNO_INPUT'

  public void BookingDetail() throws SQLException {
    Scanner scan = new Scanner(System.in);
    System.out.println("Your booking details is as follows: ");
    System.out.println("Return to (b)ookings list, (c)ancel booking or (e)xit bookings page?");
    String i = scan.nextLine();
    if (i.equals("b") || i.equals("B")) {
      ExistingBookings();
    }
    else if (i.equals("e") || i.equals("E")) {
      MainHub();
    } else if (i.equals("c") || i.equals("C")) {
      CancelBooking(); //PASS VALUE
    } else {
      System.out.println("Invalid character - returning to main page.");
      MainHub();
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

  public void CancelBooking() throws SQLException { // pass in booking value in here?
    // delete the booking
    // return to mainhub
    System.out.println("Booking has been deleted.");
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
    System.out.println("You have now been logged out.");
    WelcomeScreen();
  }

  /* AIRLINE AGENT ONLY: Record a flight departure. After a plane takes off,
  the user may want to record the departure. Your system should support the
  task and make necessary updates such as updating the act_dep_time.
  */
  public void RecordDeparture() throws SQLException {
    System.out.println("Flight number:");
    String flightno = scan.nextLine();
    System.out.println("Departure time:");
    String deptime = scan.nextLine();
    MainHub();
  }

  /* AIRLINE AGENT ONLY: Record a flight arrival. After a landing, the user may
  want to record the arrival and your system should support the task.
  */
  public void RecordArrival() throws SQLException {
    // search for a flight
    // enter the flight arrival time
    // exit
    System.out.println("Flight number:");
    String flightno = scan.nextLine();
    System.out.println("Arrival time:");
    String arrtime = scan.nextLine();
    MainHub();
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
  public void RoundTrips() throws SQLException {
    // get the user source
    // get the user destination
    // get the start date
    // get the end date
    // return the round-trips, sorted by the sum of the price.
    // user inputs a number (1,2,...) and the index is logged and booked (2 bookings, for the round trip!)
    // ask user if they want to enter the airport code for source
    System.out.println("Please enter the airport code for your source:");
    String SrcACode = scan.nextLine();
    // if not valid airport code, search airport by name (partial match '%val%')
    // complete process for destination airport
    System.out.println("Please enter the airport code for your destination:");
    String DestACode = scan.nextLine();
    // add departure date
    System.out.println("Please enter your departure date in format MM/DD/YYYY");
    String DepDate = scan.nextLine();
    // add return date
    System.out.println("Please enter your return date in format MM/DD/YYYY");
    String ReturnDate = scan.nextLine();
    // search flights for direct flights and flights w one connection
    // provide information. ask user if they want to sort
    // if sort, then sort
    System.out.println("The round-trip flights that match your description are as follows:");
    // system.out.println(flightslist)
    System.out.println("Round-trips are currently being sorted by number of connections, and price.");
    String i = scan.nextLine();
    MainHub();
  }

  private boolean validEmail(String e)
  {
    String e_regex = "\\w+@\\w+.\\w+";
    Pattern p = Pattern.compile(e_regex);
    Matcher m = p.matcher(e);
    return m.matches();
  }

  private boolean validPassword(String p)
  {
    return false;
  }
}
