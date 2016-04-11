package data.validation;

public class ValidationError {
	protected String message;
	
	public ValidationError(String msg) {
		message = msg;
	}
	
	public void setMessage(String msg) {
		message = msg;
	}
	
	public String getMessage() {
		return message;
	}
	
	public String toString() {
		return message;
	}
}
