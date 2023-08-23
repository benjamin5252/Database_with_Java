package DBExceptions;
//an exception responsible for the exceptions from the process of parsing
public class DBParseException extends DBException {
    private final String failMessage;

    public DBParseException() {
        this.failMessage = "There is something wrong in the command";
    }

    public  DBParseException(String failMessage) {
        this.failMessage = failMessage;
    }

    public String toString(){
        return "[ERROR]: " + failMessage;
    }
}
