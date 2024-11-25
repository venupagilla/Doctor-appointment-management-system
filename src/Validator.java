import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class Validator {
    public static boolean isValidName(String name) {
        return name != null && name.matches("[a-zA-Z ]+");
    }

    public static boolean isValidAge(int age) {
        return age > 0 && age <= 120;
    }

    public static boolean isValidContact(String contact) {
        return contact != null && contact.matches("\\d{10}");
    }

    public static boolean isValidDate(String date) {
        try {
            LocalDate parsedDate = LocalDate.parse(date);
            return !parsedDate.isBefore(LocalDate.now());
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}
