import java.net.Socket;
import java.util.Scanner;
import java.io.PrintWriter;

public class Player implements Runnable {
    private Socket socket;
    private Scanner input;
    private PrintWriter output;
    private String name; // Player's name will be set later

    public Player(Socket socket) {
        this.socket = socket;
    }

    // Setter for the name
    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void sendMessage(String message) {
        output.println(message);
    }

    @Override
    public void run() {
        try {
            input = new Scanner(socket.getInputStream());
            output = new PrintWriter(socket.getOutputStream(), true);

            while (input.hasNextLine()) {
                var command = input.nextLine();
                if (command.startsWith("NAME")) {
                    // Set the player's name when the NAME command is received
                    String name = command.substring(5); // Assuming the format "NAME your_name"
                    setName(name);
                    output.println("Name set to " + name);
                } else if (command.startsWith("QUIT")) {
                    output.println("Goodbye " + name);
                    break;
                } else {
                    output.println("Echo: " + command);
                }
            }
        } catch (Exception e) {
            System.out.println("Error handling player: " + e.getMessage());
        } finally {
            try { socket.close(); } catch (Exception e) {
                System.out.println("Error closing socket: " + e.getMessage());
            }
        }
    }
}
