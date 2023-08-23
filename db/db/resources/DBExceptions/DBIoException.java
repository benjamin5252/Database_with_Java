package DBExceptions;
//an exception responsible for the exception from io
public class DBIoException extends DBException {
    private final String failMessage;

    public DBIoException() {
        this.failMessage = "There is something wrong in the command";
    }

    public  DBIoException(String failMessage) {
        this.failMessage = failMessage;
    }

    public String toString(){
        return "[ERROR]: " + failMessage;
    }
}
