import java.util.*;
class SampleUI
{
	public static void main(String arg[]) {
		Scanner scan = new Scanner(System.in);
		while(true) {
			System.out.println("Welcome to Air Kappa! Please (l)ogin or (r)egister to use our services, or (e)xit the program.");
			String i = scan.next();
			if (i.equals("l")) {
				Login();
			} else if (i.equals("r")) {
				Register();
			} else if (i.equals("e")) {
				System.out.println("System is exiting; thank you for visiting Air Kappa!");
				System.exit(0);
			} else {
				System.out.println("Invalid character; please try again:");
			}
		}
	}

	public static void Register() {
		Scanner scan = new Scanner(System.in);
		System.out.println("Welcome to the Register page. Please enter your email: ");
		String email = scan.next();
		/* need to implement verification system...
		while (isValidEmailAddress(email) != true) {
			System.out.println("Invalid email... Please enter your email: ");
			email = scan.next();
		}*/
		System.out.println("Please enter your password: ");
		String pass = scan.next();
		MainHub();
	}

	public static void Login() {
		Scanner scan = new Scanner(System.in);
		while(true) {
			System.out.println("Welcome to the Login page. Please enter your email: ");
			String email = scan.next();
			System.out.println("Please enter your password: ");
			String pass = scan.next();
			// if verification is valid ... { MainHub(-pass in permissions); }
			MainHub();
		}
	}

	public static void MainHub() {
		Scanner scan = new Scanner(System.in);
		System.out.println("Main area reached. Please select from the following options:");
		System.out.println("(S)earch for flights, (M)ake a booking, See (E)xisting bookings, (C)ancel a booking, Find (R)ound trips, (L)og out.");
		String input = scan.next();
		System.exit(0); // will never actually exit from here ... delete after.
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
	public static void SearchForFlights() {
		// ask user if they want to enter the airport code for source
		// if not valid airport code, search airport by name (partial match '%val%')
		// complete process for destination airport
		// add departure date
		// search flights for direct flights and flights w one connection
		// provide information. ask user if they want to sort
		// if sort, then sort
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
	public static void MakeABooking() {
		// select a flight
		// is user listed in the flight?
		// if so, don't let the rebook.
		// if not, book. add the name & country of the passenger (ask here...)
	}

	/* List existing bookings. A user should be able to list all 
	his/her existing bookings. The result will be given in a list 
	form and will include for each booking, the ticket number, 
	the passenger name, the departure date and the price. The 
	user should be able to select a row and get more detailed 
	information about the booking.
	*/
	public static void ExistingBookings() {
		// search for user bookings
		// put them in a list, sep. by number index
	}

	/* Cancel a booking. The user should be able to select a 
	booking from those listed under "list existing bookings" 
	and cancel it. The proper tables should be updated to reflect 
	the cancelation and the cancelled seat should be returned to 
	the system and is made available for future bookings.
	*/
	public static void CancelBooking() { // pass in booking value in here?
		// delete the booking
		// return to mainhub
	}

	/* Logout. There must be an option to log out of the system. At 
	logout, the field last_login in users is set to the current system date.
	*/
	public static void Logout() {
		// logout
		// detail system date for last_login
		// return to main
	}

	/* AIRLINE AGENT ONLY: Record a flight departure. After a plane takes off, 
	the user may want to record the departure. Your system should support the 
	task and make necessary updates such as updating the act_dep_time.
	*/
	public static void RecordDeparture() {

	}

	/* AIRLINE AGENT ONLY: Record a flight arrival. After a landing, the user may 
	want to record the arrival and your system should support the task.
	*/
	public static void RecordArrival() {
		// search for a flight
		// enter the flight arrival time
		// exit
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
	public static void RoundTrips() {
		// get the user source
		// get the user destination
		// get the start date
		// get the end date
		// return the round-trips, sorted by the sum of the price.
		// user inputs a number (1,2,...) and the index is logged and booked (2 bookings, for the round trip!)
	}
}