package Service;


public class ExceptionSer extends RuntimeException {
    public ExceptionSer(String message) {
        super(message);
    }
    public ExceptionSer(Throwable cause) {
        super(cause);
    }
    public ExceptionSer(String message, Throwable cause) {
        super(message, cause);
    }
}