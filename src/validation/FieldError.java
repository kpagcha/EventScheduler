package validation;

public class FieldError extends ValidationError {
	public String field;
	
	public FieldError(String field, String msg) {
		super(msg);
		this.field = field;
	}
	
	public void setField(String field) {
		this.field = field;
	}
	
	public String getField() {
		return field;
	}
	
	public String toString() {
		return field + ": " + message;
	}
}
