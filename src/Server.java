import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final int PORT = 13337;
    private static Map<UUID, String> playerMap = new HashMap<>();
    private static Game game = new Game(); // Assume this class is defined elsewhere in your code.
    private static ExecutorService pool = Executors.newFixedThreadPool(10); // Pool size can be adjusted as needed.

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is running on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Connected to client: " + clientSocket.getRemoteSocketAddress());

                // Start a new thread to handle the client
                pool.execute(new ClientHandler(clientSocket));
            }
        } catch (Exception e) {
            System.err.println("Server failed to start: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket clientSocket;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try (
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
            ) {
                UUID playerId = UUID.randomUUID();
                out.println("PLAYER_ID:" + playerId);

                String playerName = in.readLine();
                playerMap.put(playerId, playerName);
                System.out.println("Player " + playerName + " connected with ID: " + playerId);
                game.joinGame(playerId, playerName, out);

                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    System.out.println("Received from " + playerName + ": " + inputLine);

                    if (inputLine.startsWith("GUESS")) {
                        int guess = Integer.parseInt(inputLine.substring(6));
                        game.submitGuess(playerId, guess);
                    } else if (inputLine.equalsIgnoreCase("P")) {
                        game.startGame();  // Handle restarting the game
                    } else if (inputLine.equalsIgnoreCase("Q")) {
                        System.out.println("Game paused by " + playerName);
                        // Handle pausing logic here
                    } else if (inputLine.equals("QUIT")) {
                        game.leaveGame(playerId);
                        break;
                    }
                }
            } catch (Exception e) {
                System.err.println("Error handling client " + clientSocket + ": " + e.getMessage());
            } finally {
                try {
                    clientSocket.close();
                } catch (Exception e) {
                    System.err.println("Failed to close client socket: " + e.getMessage());
                }
            }
        }

    }
}
