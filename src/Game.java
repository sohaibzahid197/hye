import java.util.*;
import java.io.PrintWriter;

public class Game {
    private final int gameId;
    private List<Integer> guesses = new ArrayList<>();
    private Map<UUID, Integer> playerPoints = new HashMap<>();
    private Map<UUID, String> playerNames = new HashMap<>();
    private Map<UUID, PrintWriter> playerOutputs = new HashMap<>();
    private List<UUID> players = new ArrayList<>();
    private boolean running = false;
    private static final int MAX_PLAYERS = 6;
    private static final int STARTING_POINTS = 5;
    private int roundNumber = 0;

    public Game() {
        this.gameId = 1;
    }

    public synchronized void joinGame(UUID playerId, String playerName, PrintWriter out) {
        if (running) {
            out.println("Game is already in progress.");
            return;
        }
        if (players.size() >= MAX_PLAYERS) {
            out.println("Game is full.");
            return;
        }
        players.add(playerId);
        playerNames.put(playerId, playerName);
        playerPoints.put(playerId, STARTING_POINTS);
        playerOutputs.put(playerId, out);
        notifyPlayers("Player " + playerName + " has joined the game. Total players: " + players.size());

        if (players.size() >= 2 && !running) {
            startGame();
        }
    }

    public synchronized void leaveGame(UUID playerId) {
        if (!players.contains(playerId)) return;
        notifyPlayers("Player " + playerNames.get(playerId) + " has been eliminated from the game.");
        players.remove(playerId);
        playerNames.remove(playerId);
        playerPoints.remove(playerId);
        playerOutputs.remove(playerId);
        checkEndOfGame();
    }

    public synchronized void submitGuess(UUID playerId, int guess) {
        if (!running || playerPoints.get(playerId) <= 0) {
            playerOutputs.get(playerId).println("You cannot participate this round or game has not started.");
            return;
        }
        if (guess < 0 || guess > 100) {
            playerOutputs.get(playerId).println("Guess must be between 0 and 100.");
            return;
        }
        guesses.add(guess);
        notifyPlayers("Player " + playerNames.get(playerId) + " guessed: " + guess);
        if (guesses.size() == players.size()) {
            evaluateRound();
        }
    }

    public synchronized void startGame() {
        if (players.size() < 2) {
            notifyPlayers("Waiting for more players...");
            return;
        }
        running = true;
        roundNumber++; // Ensure round number increments when the game starts
        guesses.clear();
        notifyPlayers("Round " + roundNumber + ": GAME_START");
    }

    private void evaluateRound() {
        double sum = guesses.stream().mapToInt(Integer::intValue).sum();
        double average = sum / guesses.size();
        double target = (2.0 / 3) * average;

        boolean isTie = new HashSet<>(guesses).size() == 1;
        if (isTie) {
            notifyPlayers("Round " + roundNumber + ": This round is a tie. All players guessed: " + guesses.get(0));
            resetForNextRound();
            return;
        }

        UUID winnerId = null;
        double minDifference = Double.MAX_VALUE;
        for (int i = 0; i < players.size(); i++) {
            UUID playerId = players.get(i);
            double guess = guesses.get(i);
            double difference = Math.abs(guess - target);
            if (difference < minDifference) {
                minDifference = difference;
                winnerId = playerId;
            }
        }

        for (UUID playerId : players) {
            int newScore = playerPoints.get(playerId) - 1;
            playerPoints.put(playerId, newScore);
            if (playerId.equals(winnerId)) {
                newScore += 2;
                playerPoints.put(playerId, newScore);
                notifyPlayer(playerId, "Round " + roundNumber + ": You won this round with a guess of " + guesses.get(players.indexOf(playerId)));
            } else {
                notifyPlayer(playerId, "Round " + roundNumber + ": You lost this round, your new score is: " + newScore);
                if (newScore <= 0) {
                    notifyPlayer(playerId, "You have been eliminated from the game.");
                    leaveGame(playerId);
                }
            }
        }

        resetForNextRound();
    }

    private void resetForNextRound() {
        guesses.clear();
        roundNumber++;  // Increment round number for the next round
        notifyPlayers("Round " + roundNumber + ": GAME_START");
    }

    private void checkEndOfGame() {
        if (players.size() == 1) {
            UUID lastPlayerId = players.iterator().next();
            notifyPlayers("Game Over: Winner is " + playerNames.get(lastPlayerId) + " with " + playerPoints.get(lastPlayerId) + " points.");
            running = false;
        }
    }

    private void notifyPlayers(String message) {
        for (UUID playerId : players) {
            notifyPlayer(playerId, message);
        }
    }

    private void notifyPlayer(UUID playerId, String message) {
        PrintWriter out = playerOutputs.get(playerId);
        if (out != null) {
            out.println(message);
        }
    }
}
