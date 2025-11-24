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

    // Timing / animation constants
    private static final int ANIMATION_DURATION = 1500;
    private static final int MOVEMENT_INTERVAL = 350;
    private static final int AI_DELAY_MS = 800;

    private final Random rng = new Random();

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
        // Ask for number of human players and AI players (kept behavior but clearer)
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

        // Clear and log game start
        if (controlPanel != null) {
            controlPanel.clearLog();
            StringBuilder sb = new StringBuilder();
            sb.append("Game started with players: ");
            for (int i = 0; i < allPlayers.size(); i++) {
                Player p = allPlayers.get(i);
                sb.append(p.getName()).append(p.isAI() ? " (AI)" : "");
                if (i < allPlayers.size() - 1) sb.append(", ");
            }
            controlPanel.log(sb.toString());
        }

        // place players on outside node (id 0) and enqueue
        for (Player p : allPlayers) {
            turnQueue.offer(p);
            placePlayerOnNode(p, 0);
        }

        currentPlayer = turnQueue.peek();
        if (controlPanel != null) controlPanel.updateTurnInfo(currentPlayer);
        mainApp.repaint();
        scheduleAutoRollIfNeeded();
    }

    // If current player is AI, schedule an automatic roll after a short delay
    private void scheduleAutoRollIfNeeded() {
        if (currentPlayer != null && currentPlayer.isAI()) {
            if (controlPanel != null) controlPanel.enableRollButton(false);
            Timer t = new Timer(AI_DELAY_MS, e -> {
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
        // Delegate the visual spin to the control panel which will call executeDiceRoll() when done
        if (controlPanel != null) {
            controlPanel.startDiceSpinAnimation(ANIMATION_DURATION, this::executeDiceRoll);
        } else {
            executeDiceRoll();
        }
    }

    private void executeDiceRoll() {
        // 1. Roll Dadu (1-6)
        int diceRoll = rng.nextInt(6) + 1;

        // 2. Tentukan Arah (Green/Red)
        boolean isGreen = rng.nextDouble() < GREEN_PROBABILITY;
         String resultColor = isGreen ? "GREEN" : "RED";
         int moveDirection = isGreen ? 1 : -1;

         // Log roll result
        if (currentPlayer != null) log(currentPlayer.getName() + " rolled " + diceRoll + " (" + resultColor + ")");

         // Tampilkan hasil akhir ke ControlPanel
        if (controlPanel != null) controlPanel.updateDiceResult(diceRoll, resultColor, moveDirection);

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
        Timer timer = new Timer(MOVEMENT_INTERVAL, null);
        timer.addActionListener(e -> {
            if (movementStack.isEmpty()) {
                ((Timer) e.getSource()).stop();
                checkWinCondition();
                finalizeTurn();
            } else {
                controlPanel.enableRollButton(false);
                int direction = movementStack.pop();
                movePlayerByOneStep(direction);
            }
        });
        timer.start();
     }

    // finalize turn behavior: star extra-turn or rotate queue; update UI & schedule AI
    private void finalizeTurn() {
        Player acting = turnQueue.peek();
        if (acting != null) {
            int pos = acting.getCurrentPosition();
            boolean landedStar = (pos > 0) && (pos % 5 == 0) && (pos != board.getTotalNodes());
            if (landedStar) {
                JOptionPane.showMessageDialog(mainApp, acting.getName() + " mendapatkan giliran lagi karena mendarat pada bintang!", "Extra Turn", JOptionPane.INFORMATION_MESSAGE);
                log(acting.getName() + " landed on star node " + pos + " and gets an extra turn()");
                // keep acting at front
            } else {
                Player finishedPlayer = turnQueue.poll();
                if (finishedPlayer != null) {
                    if (finishedPlayer.getCurrentPosition() < board.getTotalNodes()) {
                        turnQueue.offer(finishedPlayer);
                    }
                    log(finishedPlayer.getName() + " turn ended.");
                }
            }
        }

        if (turnQueue.isEmpty()) {
            currentPlayer = null;
        } else {
            currentPlayer = turnQueue.peek();
            if (currentPlayer != null) log("Next: " + currentPlayer.getName() + (currentPlayer.isAI() ? " (AI)" : ""));
        }

        if (controlPanel != null) controlPanel.updateTurnInfo(currentPlayer);
        if (controlPanel != null) controlPanel.enableRollButton(true);
        scheduleAutoRollIfNeeded();
    }

     private void movePlayerByOneStep(int direction) {
        if (currentPlayer == null) return;
        int oldPosId = currentPlayer.getCurrentPosition();
        int newPosId = oldPosId + direction;

        // clamp to valid board node range (1..N)
        if (newPosId < 1) newPosId = 1;
        if (newPosId > board.getTotalNodes()) newPosId = board.getTotalNodes();

        BoardNode oldNode = board.getNodeById(oldPosId);
        if (oldNode != null) oldNode.removePlayer(currentPlayer);

        currentPlayer.setCurrentPosition(newPosId);

        BoardNode newNode = board.getNodeById(newPosId);
        if (newNode != null) newNode.addPlayer(currentPlayer);

        log(currentPlayer.getName() + " moved from " + oldPosId + " to " + newPosId);
        mainApp.repaint();
     }

     private void checkWinCondition() {
         if (currentPlayer.getCurrentPosition() == board.getTotalNodes()) {
            log(currentPlayer.getName() + " has won the game!");
            JOptionPane.showMessageDialog(mainApp, "🎉 Selamat! " + currentPlayer.getName() + " telah memenangkan permainan!", "Permainan Selesai", JOptionPane.INFORMATION_MESSAGE);
         }
     }

    // Helper: safely log via controlPanel
    private void log(String msg) {
        if (controlPanel != null) controlPanel.log(msg);
    }

    // Helper: move player to nodeId (used at initialization)
    private void placePlayerOnNode(Player p, int nodeId) {
        BoardNode node = board.getNodeById(nodeId);
        if (node != null) node.addPlayer(p);
        p.setCurrentPosition(nodeId);
    }

     public Player getCurrentPlayer() { return currentPlayer; }
}

