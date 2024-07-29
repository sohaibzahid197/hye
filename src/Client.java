import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private static final String SERVER_ADDRESS = "10.14.1.201";

    private static final int SERVER_PORT = 13337;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             Scanner scanner = new Scanner(System.in)) {

            // Receive player ID from the server
            String playerId = in.readLine();
            System.out.println("Player ID: " + playerId);

            // Provide player name
            System.out.print("Enter your name: ");
            String playerName = scanner.nextLine();
            out.println(playerName);

            // Main client-server communication loop
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println("Server: " + inputLine);

                if (inputLine.contains("GAME_START") || inputLine.contains("Round Results:") || inputLine.contains("You won this round")) {
                    System.out.println("Enter your guess (between 0 and 100), 'p' to play again, or 'Quit' to pause:");
                    String guessInput = scanner.nextLine();
                    if ("p".equalsIgnoreCase(guessInput) || "q".equalsIgnoreCase(guessInput)) {
                        out.println(guessInput.toUpperCase());
                    } else {
                        out.println("GUESS " + guessInput);
                    }
                } else if ("QUIT".equals(inputLine) || inputLine.contains("Game Over:")) {
                    System.out.println("Type 'quit' to exit or 'p' to play again:");
                    String command = scanner.nextLine();
                    out.println(command.toUpperCase());
                    if ("QUIT".equalsIgnoreCase(command)) {
                        break;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error with client: " + e.getMessage());
            e.printStackTrace();
        } finally {
            System.out.println("Disconnected from server.");
        }
    }
}
