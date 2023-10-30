import org.example.Main;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MainTest {
    @Test
    public void testGetGreeting() {
        String expectedGreeting = "hello. New message";
        String actualGreeting = Main.createMessage("New message");

        // Use JUnit's assertion to check if the actual and expected greetings are the same.
        assertEquals(expectedGreeting, actualGreeting);
    }
}
