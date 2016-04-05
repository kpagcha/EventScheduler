package validation.validators;

import java.util.List;

import validation.ValidationError;

public interface Validator {
	public boolean validate();
	public List<ValidationError> getErrors();
}
