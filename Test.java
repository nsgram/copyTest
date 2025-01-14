import java.time.LocalDateTime;

public class TimestampExample {
    public static void main(String[] args) {
        String currentTimestamp = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("ddMMyyyyHHmmss"));
        System.out.println("Current Timestamp: " + currentTimestamp);
    }
}
