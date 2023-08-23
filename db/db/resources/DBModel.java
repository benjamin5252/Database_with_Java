import DBExceptions.*;
import java.util.*;

//a class to keep the data used in memory
//can be a common memory for all of the classes who need it
public class DBModel {
    //a 2d ArrayList to hold the data for output to the client
    public ArrayList<ArrayList<String>> outputData = new ArrayList<ArrayList<String>>();
    //an ArrayList to hold the tables used
    ArrayList<String> tableUsed = new ArrayList<String>();
    //an string to hold the database used
    public String databaseUsed = null;

}
