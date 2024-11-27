import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileToByteArray {
    public static void main(String[] args) {
        Path filePath = Path.of("example.txt"); // Replace with your file path

        try {
            byte[] fileBytes = Files.readAllBytes(filePath);
            System.out.println("File converted to byte array successfully!");
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
