# CMPUT 291 MINI-PROJECT 1:
Satyen Alkolkar and Anson Li

#### What is this project?
This project is an airline search tool that searches for flights, and allows users to interact with flights, bookings, as well as engage in other activities.


#### What's in this folder?
The project is stored in the following structure:

* /documentation stores the design document, as well as additional references used in the project.
* /temp stores files used throughout the development of the project, but are nonessential to the running of the project.
* /src contains all of the java files used in this project.

#### How do I run this application?
1. Enter /src/
2. Use the following code: javac FlightBooker.java SQLHandler.java UI.java 
3. Ensure that your classpath is correct with: CLASSPATH=$CLASSPATH:
4. Run: java FlightBooker

#### This application doesn't work! What's going on?
1. If you are encountering an SQL structure issue, please run the prerequisite SQL statements in order to access the application. We recommend using /temp/SQL_Table_Create in order to get the structures, then adding whatever flights and associated information you feel is necessary.
2. If you are getting 'could not find or load main class...', please run CLASSPATH=$CLASSPATH: before running the java command again.

#### To-do List:
* [FIXED] case-insensitive acodes
* return immediately after booking 
* [FIXED] consistent styling (+-=-=-=-=-+ system?)
* removing ghost sql statements
* refactoring?
* [FIXED] descriptive message if the sql query fails for rollbacks... (re: bookings)
* [FIXED] regenerate views after bookings is cancelled 
* in general, more descriptive error messages in catch exceptions.
* [FIXED] check if flight number is valid in airlineagent - flight arrival and departure
* add comments 
* [FIXED] remove commented-out code
* 0 flight plans should say something else (round trip & regular flights)

#### References:
* http://www.tutorialspoint.com/jdbc/jdbc-sample-code.htm
* http://www.tutorialspoint.com/java/io/console_readpassword.htm