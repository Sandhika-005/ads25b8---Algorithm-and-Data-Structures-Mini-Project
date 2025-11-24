import javax.swing.*;
import javax.swing.Timer; // WAJIB: Gunakan Swing Timer
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
            System.exit(0);
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

        controlPanel.enableRollButton(false);
        startDiceAnimation();
    }

    // --- ANIMASI DADU ---
    private void startDiceAnimation() {
        final int ANIMATION_DURATION = 1500;
        final int UPDATE_INTERVAL = 100;

        Timer animationTimer = new Timer(UPDATE_INTERVAL, null);
        final int totalSteps = ANIMATION_DURATION / UPDATE_INTERVAL;
        final int[] stepCount = {0};

        animationTimer.addActionListener(e -> {
            if (stepCount[0] < totalSteps) {
                // Tampilkan angka acak (1-6) selama animasi
                int tempRoll = new Random().nextInt(6) + 1;
                controlPanel.updateDiceAnimation(tempRoll);
                stepCount[0]++;
            } else {
                // Animasi selesai, eksekusi hasil
                animationTimer.stop();
                executeDiceRoll();
            }
        });
        animationTimer.start();
    }

    private void executeDiceRoll() {
        // 1. Roll Dadu (1-6)
        int diceRoll = new Random().nextInt(6) + 1;

        // 2. Tentukan Arah (Green/Red)
        double prob = new Random().nextDouble();
        boolean isGreen = prob < GREEN_PROBABILITY;
        String resultColor = isGreen ? "GREEN" : "RED";
        int moveDirection = isGreen ? 1 : -1;

        // Tampilkan hasil akhir ke ControlPanel
        controlPanel.updateDiceResult(diceRoll, resultColor, moveDirection);

        // 3. Masukkan Langkah ke Stack
        movementStack.clear();
        for (int i = 0; i < diceRoll; i++) {
            movementStack.push(moveDirection);
        }

        // 4. Jalankan Animasi Gerakan Pemain
        startMovementAnimation();
    }
    // --- AKHIR ANIMASI DADU ---

    private void startMovementAnimation() {
        Timer timer = new Timer(350, null);
        timer.addActionListener(e -> {
            if (movementStack.isEmpty()) {
                ((Timer) e.getSource()).stop();

                checkWinCondition();

                Player finishedPlayer = turnQueue.poll();
                if (finishedPlayer != null) {
                    if (finishedPlayer.getCurrentPosition() < board.getTotalNodes()) {
                        turnQueue.offer(finishedPlayer);
                    }
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
                int direction = movementStack.pop();
                movePlayerByOneStep(direction);
            }
        });
        timer.start();
    }

    private void movePlayerByOneStep(int direction) {
        int oldPosId = currentPlayer.getCurrentPosition();
        int newPosId = oldPosId + direction;

        if (newPosId < 1) newPosId = 1;
        if (newPosId > board.getTotalNodes()) newPosId = board.getTotalNodes();

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
        }
    }

    public Player getCurrentPlayer() { return currentPlayer; }
}