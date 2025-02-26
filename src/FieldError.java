package mg.p16.classe;
import java.util.ArrayList;
import java.util.List;

public class FieldError {
    private String fieldName;
    private List<String> errorMessages = new ArrayList<>();

    public FieldError(String fieldName) {
        this.fieldName = fieldName;
    }

    public void addError(String message) {
        errorMessages.add(message);
    }

    public String getFieldName() {
        return fieldName;
    }

    public List<String> getErrorMessages() {
        return errorMessages;
    }

    public boolean hasErrors() {
        return !errorMessages.isEmpty();
    }

    public String getFormattedErrors() {
        return String.join(", ", errorMessages);
    }

    @Override
    public String toString() {
        return getFormattedErrors();
    }
}
