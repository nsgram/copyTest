import java.sql.Timestamp;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateConverter {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    public static String formatDate(Object date) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }

        Instant instant;
        if (date instanceof Date) {
            instant = ((Date) date).toInstant();
        } else if (date instanceof Timestamp) {
            instant = ((Timestamp) date).toInstant();
        } else {
            throw new IllegalArgumentException("Unsupported date type: " + date.getClass().getName());
        }

        LocalDate localDate = instant.atZone(ZoneId.systemDefault()).toLocalDate();
        return FORMATTER.format(localDate);
    }

    public static void main(String[] args) throws ParseException {
        Date utilDate = new Date();
        Timestamp sqlTimestamp = new Timestamp(System.currentTimeMillis());

        System.out.println("Formatted java.util.Date: " + formatDate(utilDate));
        System.out.println("Formatted java.sql.Timestamp: " + formatDate(sqlTimestamp));
    }
}
