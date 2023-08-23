package DBExceptions;
//a parent exception in the database application
public class DBException extends Exception {
    private final String failMessage;

    public DBException() {
        this.failMessage = "There is something wrong in the command";
    }

    public  DBException(String failMessage) {
        this.failMessage = failMessage;
    }

    public String toString(){
        return "[ERROR]: " + failMessage;
    }
}
