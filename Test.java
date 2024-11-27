import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

public class FileToBase64 {
    public static void main(String[] args) {
        Path filePath = Path.of("example.txt"); // Replace with your file path

        try {
            byte[] fileBytes = Files.readAllBytes(filePath);
            System.out.println("File converted to byte array successfully!");

            // Convert byte array to Base64 string
            String base64String = Base64.getEncoder().encodeToString(fileBytes);
            System.out.println("Base64 Encoded String:\n" + base64String);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
