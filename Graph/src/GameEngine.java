import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.util.*;
import java.util.List;

class GameEngine {
    private Board board;
    private GameControlPanel controlPanel;
    private GameVisualizer mainApp;

    // Data Structure: Queue untuk giliran pemain
    private Queue<Player> turnQueue;
    private Player currentPlayer;

    // Data Structure: Stack untuk langkah gerakan
    private Stack<Integer> movementStack;

    private static final double GREEN_PROBABILITY = 0.8;
    private static final int MAX_PLAYERS = 5;

    public GameEngine(Board board, GameVisualizer mainApp) {
        this.board = board;
        this.mainApp = mainApp;
        this.turnQueue = new LinkedList<>();
        this.movementStack = new Stack<>();
    }

    public void setControlPanel(GameControlPanel controlPanel) {
        this.controlPanel = controlPanel;
    }

    // --- Inisialisasi Pemain ---
    public void promptForPlayers() {
        String input = JOptionPane.showInputDialog(mainApp, "Masukkan jumlah pemain (min 2, max " + MAX_PLAYERS + "):", "Mulai Permainan", JOptionPane.QUESTION_MESSAGE);
        if (input == null || input.trim().isEmpty()) {
            System.exit(0); // Keluar jika dibatalkan
            return;
        }
        try {
            int count = Integer.parseInt(input);
            if (count < 2 || count > MAX_PLAYERS) {
                JOptionPane.showMessageDialog(mainApp, "Jumlah pemain harus antara 2 dan " + MAX_PLAYERS + ".", "Error", JOptionPane.ERROR_MESSAGE);
                promptForPlayers();
                return;
            }
            initializePlayers(count);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(mainApp, "Input tidak valid. Masukkan angka.", "Error", JOptionPane.ERROR_MESSAGE);
            promptForPlayers();
        }
    }

    private void initializePlayers(int count) {
        List<Player> allPlayers = new ArrayList<>();
        Color[] playerColors = {Color.RED, Color.BLUE, new Color(60, 180, 75), Color.MAGENTA, Color.ORANGE};

        for (int i = 0; i < count; i++) {
            String name = JOptionPane.showInputDialog(mainApp, "Masukkan nama untuk Pemain " + (i + 1) + ":", "Pemain " + (i + 1), JOptionPane.QUESTION_MESSAGE);
            if (name == null || name.trim().isEmpty()) name = "Pemain " + (i + 1);

            Player newPlayer = new Player(name, playerColors[i % playerColors.length]);
            allPlayers.add(newPlayer);
        }

        Collections.shuffle(allPlayers);

        for (Player p : allPlayers) {
            turnQueue.offer(p);
            board.getNodeById(1).addPlayer(p);
        }

        currentPlayer = turnQueue.peek();
        controlPanel.updateTurnInfo(currentPlayer);
        mainApp.repaint();
    }

    // --- Logika Dadu dan Gerakan ---
    public void rollDiceAndMove() {
        if (currentPlayer == null || !movementStack.isEmpty()) return;

        int diceRoll = new Random().nextInt(6) + 1;

        double prob = new Random().nextDouble();
        boolean isGreen = prob < GREEN_PROBABILITY;
        String resultColor = isGreen ? "GREEN" : "RED";
        int moveDirection = isGreen ? 1 : -1;

        controlPanel.updateDiceResult(diceRoll, resultColor);

        // Data Structure: Stack (menyimpan langkah-langkah)
        movementStack.clear();
        for (int i = 0; i < diceRoll; i++) {
            movementStack.push(moveDirection);
        }

        startMovementAnimation();
    }

    private void startMovementAnimation() {
        Timer timer = new Timer(350, null);
        timer.addActionListener(e -> {
            if (movementStack.isEmpty()) {
                ((Timer) e.getSource()).stop();

                // Cek menang, lalu pindah giliran
                checkWinCondition();

                Player finishedPlayer = turnQueue.poll();
                if (finishedPlayer != null) {
                    // Hanya masukkan kembali ke Queue jika pemain belum menang
                    if (finishedPlayer.getCurrentPosition() < board.getTotalNodes()) {
                        turnQueue.offer(finishedPlayer);
                    }
                    // Jika Queue kosong setelah pemenang, permainan selesai.
                    if (turnQueue.isEmpty()) {
                        currentPlayer = null;
                    } else {
                        currentPlayer = turnQueue.peek();
                    }
                }

                controlPanel.updateTurnInfo(currentPlayer);
                controlPanel.enableRollButton(true);
            } else {
                controlPanel.enableRollButton(false);
                // Data Structure: Stack (mengambil langkah dari atas)
                int direction = movementStack.pop();
                movePlayerByOneStep(direction);
            }
        });
        timer.start();
    }

    private void movePlayerByOneStep(int direction) {
        int oldPosId = currentPlayer.getCurrentPosition();
        int newPosId = oldPosId + direction;

        // Batasan papan
        if (newPosId < 1) newPosId = 1;
        if (newPosId > board.getTotalNodes()) newPosId = board.getTotalNodes();

        // Update posisi
        BoardNode oldNode = board.getNodeById(oldPosId);
        if (oldNode != null) oldNode.removePlayer(currentPlayer);

        currentPlayer.setCurrentPosition(newPosId);

        BoardNode newNode = board.getNodeById(newPosId);
        if (newNode != null) newNode.addPlayer(currentPlayer);

        mainApp.repaint();
    }

    private void checkWinCondition() {
        if (currentPlayer.getCurrentPosition() == board.getTotalNodes()) {
            JOptionPane.showMessageDialog(mainApp, "🎉 Selamat! " + currentPlayer.getName() + " telah memenangkan permainan!", "Permainan Selesai", JOptionPane.INFORMATION_MESSAGE);
            // Pemenang akan dihapus dari Queue di logika startMovementAnimation
        }
    }

    public Player getCurrentPlayer() { return currentPlayer; }
}