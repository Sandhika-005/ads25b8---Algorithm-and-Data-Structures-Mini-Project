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
        // Ask for number of human players and AI players
        try {
            String sh = JOptionPane.showInputDialog(mainApp, "Masukkan jumlah pemain manusia (min 1):", "Mulai Permainan", JOptionPane.QUESTION_MESSAGE);
            if (sh == null) { System.exit(0); return; }
            int humanCount = Integer.parseInt(sh.trim());

            String sa = JOptionPane.showInputDialog(mainApp, "Masukkan jumlah AI (>=0):", "Mulai Permainan", JOptionPane.QUESTION_MESSAGE);
            if (sa == null) { System.exit(0); return; }
            int aiCount = Integer.parseInt(sa.trim());

            int total = humanCount + aiCount;
            if (total < 2 || total > MAX_PLAYERS || humanCount < 1) {
                JOptionPane.showMessageDialog(mainApp, "Total pemain harus antara 2 dan " + MAX_PLAYERS + " dengan minimal 1 pemain manusia.", "Error", JOptionPane.ERROR_MESSAGE);
                promptForPlayers();
                return;
            }
            initializePlayers(humanCount, aiCount);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(mainApp, "Input tidak valid. Masukkan angka.", "Error", JOptionPane.ERROR_MESSAGE);
            promptForPlayers();
        }
    }

    private void initializePlayers(int humanCount, int aiCount) {
        List<Player> allPlayers = new ArrayList<>();
        Color[] playerColors = {Color.RED, Color.BLUE, new Color(60, 180, 75), Color.MAGENTA, Color.ORANGE};
        int colorIdx = 0;

        // Human players: ask names
        for (int i = 0; i < humanCount; i++) {
            String name = JOptionPane.showInputDialog(mainApp, "Masukkan nama untuk Pemain " + (i + 1) + ":", "Pemain " + (i + 1), JOptionPane.QUESTION_MESSAGE);
            if (name == null || name.trim().isEmpty()) name = "Pemain " + (i + 1);
            Player newPlayer = new Player(name, playerColors[colorIdx++ % playerColors.length], false);
            allPlayers.add(newPlayer);
        }

        // AI players: automatic names
        for (int i = 0; i < aiCount; i++) {
            String aiName = "AI " + (i + 1);
            Player aiPlayer = new Player(aiName, playerColors[colorIdx++ % playerColors.length], true);
            allPlayers.add(aiPlayer);
        }

        Collections.shuffle(allPlayers);

        for (Player p : allPlayers) {
            turnQueue.offer(p);
            board.getNodeById(1).addPlayer(p);
        }

        currentPlayer = turnQueue.peek();
        controlPanel.updateTurnInfo(currentPlayer);
        mainApp.repaint();
        scheduleAutoRollIfNeeded();
    }

    // If current player is AI, schedule an automatic roll after a short delay
    private void scheduleAutoRollIfNeeded() {
        if (currentPlayer != null && currentPlayer.isAI()) {
            if (controlPanel != null) controlPanel.enableRollButton(false);
            Timer t = new Timer(800, e -> {
                ((Timer) e.getSource()).stop();
                rollDiceAndMove();
            });
            t.setRepeats(false);
            t.start();
        }
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
        // Delegate the visual spin to the control panel which will call executeDiceRoll() when done
        if (controlPanel != null) {
            controlPanel.startDiceSpinAnimation(ANIMATION_DURATION, this::executeDiceRoll);
        } else {
            // fallback if no control panel present
            executeDiceRoll();
        }
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
                scheduleAutoRollIfNeeded();
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