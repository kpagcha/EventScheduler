package data.validation.validator;

import java.util.List;

import data.validation.ValidationError;

public interface Validator {
	public boolean validate();
	public List<ValidationError> getErrors();
}
