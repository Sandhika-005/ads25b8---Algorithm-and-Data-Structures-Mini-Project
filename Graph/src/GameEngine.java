import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.util.*;
import java.util.List;

class GameEngine {
    private Board board;
    private GameControlPanel controlPanel;
    private GameVisualizer mainApp;
    private Queue<Player> turnQueue;
    private Player currentPlayer;
    private Stack<Integer> movementStack;

    private static final double GREEN_PROBABILITY = 0.8;
    private static final int MAX_PLAYERS = 5;
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

    public void promptForPlayers() {
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

        for (int i = 0; i < humanCount; i++) {
            String name = JOptionPane.showInputDialog(mainApp, "Masukkan nama untuk Pemain " + (i + 1) + ":", "Pemain " + (i + 1), JOptionPane.QUESTION_MESSAGE);
            if (name == null || name.trim().isEmpty()) name = "Pemain " + (i + 1);
            Player newPlayer = new Player(name, playerColors[colorIdx++ % playerColors.length], false);
            allPlayers.add(newPlayer);
        }

        for (int i = 0; i < aiCount; i++) {
            String aiName = "AI " + (i + 1);
            Player aiPlayer = new Player(aiName, playerColors[colorIdx++ % playerColors.length], true);
            allPlayers.add(aiPlayer);
        }

        Collections.shuffle(allPlayers);

        // Clear log removed

        for (Player p : allPlayers) {
            turnQueue.offer(p);
            placePlayerOnNode(p, 0);
        }

        currentPlayer = turnQueue.peek();
        if (controlPanel != null) controlPanel.updateTurnInfo(currentPlayer);
        mainApp.repaint();
        scheduleAutoRollIfNeeded();
    }

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

    public void rollDiceAndMove() {
        if (currentPlayer == null || !movementStack.isEmpty()) return;
        controlPanel.enableRollButton(false);
        startDiceAnimation();
    }

    private void startDiceAnimation() {
        if (controlPanel != null) {
            controlPanel.startDiceSpinAnimation(ANIMATION_DURATION, this::executeDiceRoll);
        } else {
            executeDiceRoll();
        }
    }

    private void executeDiceRoll() {
        int diceRoll = rng.nextInt(6) + 1;
        boolean isGreen = rng.nextDouble() < GREEN_PROBABILITY;
        String resultColor = isGreen ? "GREEN" : "RED";
        int moveDirection = isGreen ? 1 : -1;

        // Log removed

        if (controlPanel != null) controlPanel.updateDiceResult(diceRoll, resultColor, moveDirection);

        movementStack.clear();
        for (int i = 0; i < diceRoll; i++) {
            movementStack.push(moveDirection);
        }

        startMovementAnimation();
    }

    private void startMovementAnimation() {
        Timer timer = new Timer(MOVEMENT_INTERVAL, null);
        timer.addActionListener(e -> {
            if (movementStack.isEmpty()) {
                ((Timer) e.getSource()).stop();
                checkBoardConnection();
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

    private void checkBoardConnection() {
        if (currentPlayer == null) return;
        int currentPos = currentPlayer.getCurrentPosition();

        if (board.getConnections().containsKey(currentPos)) {
            int target = board.getConnections().get(currentPos);

            BoardNode oldNode = board.getNodeById(currentPos);
            BoardNode newNode = board.getNodeById(target);

            if (oldNode != null) oldNode.removePlayer(currentPlayer);
            currentPlayer.setCurrentPosition(target);
            if (newNode != null) newNode.addPlayer(currentPlayer);

            String type = target > currentPos ? "Tangga (Naik)" : "Ular (Turun)";
            // Log removed

            JOptionPane.showMessageDialog(mainApp,
                    currentPlayer.getName() + " mendarat di " + type + "!\nPindah ke Node " + target,
                    "Koneksi Ditemukan!", JOptionPane.INFORMATION_MESSAGE);

            mainApp.repaint();
        }
    }

    private void finalizeTurn() {
        Player acting = turnQueue.peek();
        if (acting != null) {
            int pos = acting.getCurrentPosition();
            boolean landedStar = (pos > 0) && (pos % 5 == 0) && (pos != board.getTotalNodes());
            if (landedStar) {
                JOptionPane.showMessageDialog(mainApp, acting.getName() + " mendapatkan giliran lagi karena mendarat pada bintang!", "Extra Turn", JOptionPane.INFORMATION_MESSAGE);
                // Log removed
            } else {
                Player finishedPlayer = turnQueue.poll();
                if (finishedPlayer != null) {
                    if (finishedPlayer.getCurrentPosition() < board.getTotalNodes()) {
                        turnQueue.offer(finishedPlayer);
                    }
                    // Log removed
                }
            }
        }

        if (turnQueue.isEmpty()) {
            currentPlayer = null;
        } else {
            currentPlayer = turnQueue.peek();
            // Log removed
        }

        if (controlPanel != null) controlPanel.updateTurnInfo(currentPlayer);
        if (controlPanel != null) controlPanel.enableRollButton(true);
        scheduleAutoRollIfNeeded();
    }

    private void movePlayerByOneStep(int direction) {
        if (currentPlayer == null) return;
        int oldPosId = currentPlayer.getCurrentPosition();
        int newPosId = oldPosId + direction;

        if (newPosId < 1) newPosId = 1;
        if (newPosId > board.getTotalNodes()) newPosId = board.getTotalNodes();

        BoardNode oldNode = board.getNodeById(oldPosId);
        if (oldNode != null) oldNode.removePlayer(currentPlayer);

        currentPlayer.setCurrentPosition(newPosId);

        BoardNode newNode = board.getNodeById(newPosId);
        if (newNode != null) newNode.addPlayer(currentPlayer);

        // Log removed
        mainApp.repaint();
    }

    private void checkWinCondition() {
        if (currentPlayer.getCurrentPosition() == board.getTotalNodes()) {
            // Log removed
            JOptionPane.showMessageDialog(mainApp, "🎉 Selamat! " + currentPlayer.getName() + " telah memenangkan permainan!", "Permainan Selesai", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void placePlayerOnNode(Player p, int nodeId) {
        BoardNode node = board.getNodeById(nodeId);
        if (node != null) node.addPlayer(p);
        p.setCurrentPosition(nodeId);
    }

    public Player getCurrentPlayer() { return currentPlayer; }
}