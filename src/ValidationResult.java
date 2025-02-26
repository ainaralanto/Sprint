package mg.p16.classe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ValidationResult {
    private String redirectPage;
    private Map<String, FieldError> errors = new HashMap<>();
    private Map<String, Object> values = new HashMap<>();

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public void addValue(String field, Object value) {
        values.put(field, value);
    }

    public Map<String, Object> getValues() {
        return values;
    }

    public String getRedirectPage() {
        return redirectPage;
    }

    public void setRedirectPage(String redirectPage) {
        this.redirectPage = redirectPage;
    }

    public void addError(String field, String errorMessage) {
        errors.computeIfAbsent(field, FieldError::new).addError(errorMessage);
    }

    public Map<String, FieldError> getErrors() {
        return errors;
    }

    public String getErrorMessage(String fieldName) {
        FieldError error = errors.get(fieldName);
        return (error != null && error.hasErrors()) ? error.getFormattedErrors() : null;
    }
}

