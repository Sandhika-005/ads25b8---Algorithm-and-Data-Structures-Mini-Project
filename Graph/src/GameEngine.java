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

    private Stack<Integer> movementStack; // Untuk normal (+1 / -1)
    private Queue<Integer> dijkstraMoveQueue; // Untuk mode Dijkstra (Node ID spesifik)

    private static final double GREEN_PROBABILITY = 0.8;
    private static final int MAX_PLAYERS = 5;
    private static final int ANIMATION_DURATION = 1500;
    private static final int MOVEMENT_INTERVAL = 350;
    private static final int AI_DELAY_MS = 800;

    private final Random rng = new Random();

    // New flag to indicate game finished
    private boolean gameOver = false;

    public GameEngine(Board board, GameVisualizer mainApp) {
        this.board = board;
        this.mainApp = mainApp;
        this.turnQueue = new LinkedList<>();
        this.movementStack = new Stack<>();
        this.dijkstraMoveQueue = new LinkedList<>();
    }

    public void setControlPanel(GameControlPanel controlPanel) {
        this.controlPanel = controlPanel;
    }

    public void promptForPlayers() {
        String[] modes = {"Play vs AI (1 Human + AI)", "Player vs Player (All Humans)", "Custom (Choose counts)"};
        int choice = JOptionPane.showOptionDialog(mainApp,
                "Pilih mode permainan:",
                "Pilih Mode",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                modes,
                modes[0]);

        if (choice == JOptionPane.CLOSED_OPTION) { System.exit(0); return; }

        try {
            if (choice == 0) { // Play vs AI
                String name = JOptionPane.showInputDialog(mainApp, "Masukkan nama pemain (Anda):", "Pemain 1", JOptionPane.QUESTION_MESSAGE);
                if (name == null) { System.exit(0); return; }
                int maxAi = MAX_PLAYERS - 1;
                String sa = JOptionPane.showInputDialog(mainApp, "Masukkan jumlah AI (1.." + maxAi + "):", "1");
                if (sa == null) { System.exit(0); return; }
                int aiCount = Integer.parseInt(sa.trim());
                if (aiCount < 1 || aiCount > maxAi) {
                    JOptionPane.showMessageDialog(mainApp, "Jumlah AI harus antara 1 dan " + maxAi + ".", "Error", JOptionPane.ERROR_MESSAGE);
                    promptForPlayers();
                    return;
                }
                // build players: 1 human (with name) + aiCount AIs
                List<Player> customPlayers = new ArrayList<>();
                Color[] playerColors = {Color.RED, Color.BLUE, new Color(60,180,75), Color.MAGENTA, Color.ORANGE};
                customPlayers.add(new Player(name.trim().isEmpty() ? "Pemain 1" : name.trim(), playerColors[0], false));
                for (int i = 0; i < aiCount; i++) {
                    customPlayers.add(new Player("AI " + (i + 1), playerColors[(i + 1) % playerColors.length], true));
                }
                Collections.shuffle(customPlayers);
                for (Player p : customPlayers) { turnQueue.offer(p); placePlayerOnNode(p, 0); }
                currentPlayer = turnQueue.peek();
                if (controlPanel != null) controlPanel.updateTurnInfo(currentPlayer);
                mainApp.repaint();
                scheduleAutoRollIfNeeded();
                return;
            } else if (choice == 1) { // Player vs Player
                String sh = JOptionPane.showInputDialog(mainApp, "Masukkan jumlah pemain (2.." + MAX_PLAYERS + "):", "2");
                if (sh == null) { System.exit(0); return; }
                int humanCount = Integer.parseInt(sh.trim());
                if (humanCount < 2 || humanCount > MAX_PLAYERS) {
                    JOptionPane.showMessageDialog(mainApp, "Jumlah pemain harus antara 2 dan " + MAX_PLAYERS + ".", "Error", JOptionPane.ERROR_MESSAGE);
                    promptForPlayers();
                    return;
                }
                // collect names for each human
                List<Player> humans = new ArrayList<>();
                Color[] playerColors = {Color.RED, Color.BLUE, new Color(60,180,75), Color.MAGENTA, Color.ORANGE};
                for (int i = 0; i < humanCount; i++) {
                    String name = JOptionPane.showInputDialog(mainApp, "Masukkan nama untuk Pemain " + (i + 1) + ":", "Pemain " + (i + 1), JOptionPane.QUESTION_MESSAGE);
                    if (name == null) { System.exit(0); return; }
                    if (name.trim().isEmpty()) name = "Pemain " + (i + 1);
                    humans.add(new Player(name, playerColors[i % playerColors.length], false));
                }
                Collections.shuffle(humans);
                for (Player p : humans) { turnQueue.offer(p); placePlayerOnNode(p, 0); }
                currentPlayer = turnQueue.peek();
                if (controlPanel != null) controlPanel.updateTurnInfo(currentPlayer);
                mainApp.repaint();
                scheduleAutoRollIfNeeded();
                return;
            } else { // Custom: fallback to previous separate prompts
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
                return;
            }
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
        if (gameOver) return; // do nothing if game already finished
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
        if (gameOver) return; // prevent rolls after game finished
        if (currentPlayer == null || (!movementStack.isEmpty() || !dijkstraMoveQueue.isEmpty())) return;
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

        if (controlPanel != null) controlPanel.updateDiceResult(diceRoll, resultColor, moveDirection);

        movementStack.clear();
        dijkstraMoveQueue.clear();

        int currentPos = currentPlayer.getCurrentPosition();

        // CEK BILANGAN PRIMA
        if (isPrime(currentPos) && moveDirection > 0) {
            JOptionPane.showMessageDialog(mainApp,
                    "✨ PRIME NUMBER POWER-UP! ✨\nPosisi " + currentPos + " adalah Prima.\nMengaktifkan Shortest Path (Dijkstra)!",
                    "Fitur Spesial", JOptionPane.INFORMATION_MESSAGE);

            List<Integer> path = DijkstraPathFinder.findShortestPathSteps(board, currentPos, board.getTotalNodes(), diceRoll);
            dijkstraMoveQueue.addAll(path);
        } else {
            // Gerakan Normal
            for (int i = 0; i < diceRoll; i++) {
                movementStack.push(moveDirection);
            }
        }

        startMovementAnimation();
    }

    private void startMovementAnimation() {
        // Timer ini mengontrol animasi langkah demi langkah
        Timer timer = new Timer(MOVEMENT_INTERVAL, null);
        timer.addActionListener(e -> {
            boolean isDijkstraActive = !dijkstraMoveQueue.isEmpty();
            boolean isNormalActive = !movementStack.isEmpty();

            // KONDISI BERHENTI: Ketika kedua antrian kosong
            if (!isDijkstraActive && !isNormalActive) {
                ((Timer) e.getSource()).stop();

                // PERBAIKAN: Fitur hanya dipanggil DI SINI (saat berhenti)
                // Cek koneksi (Tangga/Ular)
                boolean connectionTaken = checkBoardConnection();

                // Jika koneksi diambil, posisi berubah, jadi cek win condition di posisi baru
                // Jika tidak, cek di posisi saat ini.

                checkWinCondition();
                finalizeTurn(); // Cek Bintang (Extra Turn) terjadi di sini
            } else {
                // KONDISI BERGERAK: Hanya update posisi, JANGAN cek fitur di sini
                if (controlPanel != null) controlPanel.enableRollButton(false);

                if (isDijkstraActive) {
                    int nextNodeId = dijkstraMoveQueue.poll();
                    movePlayerToSpecificNode(nextNodeId);
                } else {
                    int direction = movementStack.pop();
                    movePlayerByOneStep(direction);
                }
            }
        });
        timer.start();
    }

    private boolean isPrime(int n) {
        if (n <= 1) return false;
        if (n == 2) return true;
        if (n % 2 == 0) return false;
        for (int i = 3; i <= Math.sqrt(n); i += 2) {
            if (n % i == 0) return false;
        }
        return true;
    }

    private void movePlayerToSpecificNode(int nodeId) {
        if (currentPlayer == null) return;
        int oldPos = currentPlayer.getCurrentPosition();

        BoardNode oldNode = board.getNodeById(oldPos);
        if (oldNode != null) oldNode.removePlayer(currentPlayer);

        currentPlayer.setCurrentPosition(nodeId);

        BoardNode newNode = board.getNodeById(nodeId);
        if (newNode != null) newNode.addPlayer(currentPlayer);

        // Update UI and status to reflect the most recent position
        if (controlPanel != null) controlPanel.updatePlayerStatus(currentPlayer);
        mainApp.repaint();
    }

    private void movePlayerByOneStep(int direction) {
        if (currentPlayer == null) return;
        int oldPosId = currentPlayer.getCurrentPosition();
        int newPosId = oldPosId + direction;

        if (newPosId < 1) newPosId = 1;
        if (newPosId > board.getTotalNodes()) newPosId = board.getTotalNodes();

        movePlayerToSpecificNode(newPosId);
    }

    // Mengembalikan true jika pemain berpindah karena koneksi
    private boolean checkBoardConnection() {
        if (currentPlayer == null) return false;
        int currentPos = currentPlayer.getCurrentPosition();

        if (board.getConnections().containsKey(currentPos)) {
            int target = board.getConnections().get(currentPos);
            movePlayerToSpecificNode(target);

            String type = target > currentPos ? "Tangga (Naik)" : "Ular (Turun)";
            JOptionPane.showMessageDialog(mainApp,
                    currentPlayer.getName() + " mendarat di " + type + "!\nPindah ke Node " + target,
                    "Koneksi Ditemukan!", JOptionPane.INFORMATION_MESSAGE);
            return true;
        }
        return false;
    }

    private void finalizeTurn() {
        if (gameOver) {
            // Ensure UI shows ended state and controls disabled
            currentPlayer = null;
            if (controlPanel != null) {
                controlPanel.updateTurnInfo(null);
                controlPanel.enableRollButton(false);
            }
            return;
        }

        Player acting = turnQueue.peek();
        if (acting != null) {
            int pos = acting.getCurrentPosition();

            // PERBAIKAN: Pengecekan Bintang terjadi di akhir turn
            boolean landedStar = (pos > 0) && (pos % 5 == 0) && (pos != board.getTotalNodes());

            if (landedStar) {
                JOptionPane.showMessageDialog(mainApp, acting.getName() + " mendapatkan giliran lagi karena mendarat pada bintang!", "Extra Turn", JOptionPane.INFORMATION_MESSAGE);
                // Jangan poll pemain dari antrian, biar dia main lagi
            } else {
                Player finishedPlayer = turnQueue.poll();
                if (finishedPlayer != null) {
                    if (finishedPlayer.getCurrentPosition() < board.getTotalNodes()) {
                        turnQueue.offer(finishedPlayer);
                    }
                }
            }
        }

        if (turnQueue.isEmpty()) {
            currentPlayer = null;
        } else {
            currentPlayer = turnQueue.peek();
        }

        if (controlPanel != null) controlPanel.updateTurnInfo(currentPlayer);
        if (controlPanel != null) controlPanel.enableRollButton(true);
        scheduleAutoRollIfNeeded();
    }

    private void checkWinCondition() {
        if (currentPlayer == null) return;
        if (currentPlayer.getCurrentPosition() == board.getTotalNodes()) {
            // Declare winner and stop the game
            JOptionPane.showMessageDialog(mainApp, "🎉 Selamat! " + currentPlayer.getName() + " telah memenangkan permainan!", "Permainan Selesai", JOptionPane.INFORMATION_MESSAGE);
            gameOver = true;
            // Clear remaining turns and disable controls
            turnQueue.clear();
            currentPlayer = null;
            if (controlPanel != null) {
                controlPanel.updateTurnInfo(null);
                controlPanel.enableRollButton(false);
            }
            // Refresh view
            mainApp.repaint();

            // Prompt to restart the game (ask user whether to restart and re-enter player counts).
            SwingUtilities.invokeLater(() -> {
                int res = JOptionPane.showConfirmDialog(mainApp,
                        "Permainan selesai. Ingin memulai ulang dan memasukkan jumlah pemain lagi?",
                        "Mulai Ulang?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (res == JOptionPane.YES_OPTION) {
                    // Rebuild the game with the same board size; this will recreate GameEngine and prompt for players.
                    mainApp.updateBoardNodeCount(board.getTotalNodes());
                } else {
                    // If user chooses NO (or closes), exit the application immediately
                    System.exit(0);
                }
            });
        }
    }

    private void placePlayerOnNode(Player p, int nodeId) {
        BoardNode node = board.getNodeById(nodeId);
        if (node != null) node.addPlayer(p);
        p.setCurrentPosition(nodeId);
        // Ensure status shows the player's current (just assigned) position
        if (controlPanel != null) controlPanel.updatePlayerStatus(p);
    }

    public Player getCurrentPlayer() { return currentPlayer; }
}