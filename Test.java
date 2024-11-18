import java.util.UUID;

public class RandomStringGenerator {
    public static void main(String[] args) {
        String randomString = generateRandomString();
        System.out.println(randomString);
    }

    public static String generateRandomString() {
        double random = Math.random() + 1;
        String base36String = Double.toString(random, 36);
        return base36String.substring(base36String.indexOf('.') + 1);
    }
}
