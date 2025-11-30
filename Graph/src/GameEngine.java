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
    private Queue<Integer> dijkstraMoveQueue;

    private static final double GREEN_PROBABILITY = 0.8;
    private static final int MAX_PLAYERS = 5;
    private static final int ANIMATION_DURATION = 1500;
    private static final int MOVEMENT_INTERVAL = 350;
    private static final int AI_DELAY_MS = 800;

    private final Random rng = new Random();
    private boolean gameOver = false;
    private AudioPlayer audioPlayer;

    public GameEngine(Board board, GameVisualizer mainApp, AudioPlayer audioPlayer) {
        this.board = board;
        this.mainApp = mainApp;
        this.turnQueue = new LinkedList<>();
        this.movementStack = new Stack<>();
        this.dijkstraMoveQueue = new LinkedList<>();
        this.audioPlayer = audioPlayer;
    }

    public void setControlPanel(GameControlPanel controlPanel) {
        this.controlPanel = controlPanel;
    }

    public void promptForPlayers() {
        String[] modes = {"Play vs AI (1 Human + AI)", "Player vs Player (All Humans)", "Custom (Choose counts)"};
        int choice = JOptionPane.showOptionDialog(mainApp, "Pilih mode permainan:", "Pilih Mode",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, modes, modes[0]);

        if (choice == JOptionPane.CLOSED_OPTION) { System.exit(0); return; }

        try {
            if (choice == 0) { // Play vs AI
                String name = JOptionPane.showInputDialog(mainApp, "Masukkan nama pemain (Anda):", "Pemain 1");
                if (name == null) { System.exit(0); return; }

                int maxAi = MAX_PLAYERS - 1;
                int aiCount = getValidIntInput("Masukkan jumlah AI (1.." + maxAi + "):", 1, maxAi);
                if (aiCount == -1) { System.exit(0); return; }

                List<Player> customPlayers = new ArrayList<>();
                Color[] playerColors = {Color.RED, Color.BLUE, new Color(60,180,75), Color.MAGENTA, Color.ORANGE};

                customPlayers.add(new Player(name.trim().isEmpty() ? "Pemain 1" : name.trim(), playerColors[0], false));
                for (int i = 0; i < aiCount; i++) {
                    customPlayers.add(new Player("AI " + (i + 1), playerColors[(i + 1) % playerColors.length], true));
                }
                Collections.shuffle(customPlayers);
                setupGame(customPlayers);

            } else if (choice == 1) { // PvP
                int humanCount = getValidIntInput("Masukkan jumlah pemain (2.." + MAX_PLAYERS + "):", 2, MAX_PLAYERS);
                if (humanCount == -1) { System.exit(0); return; }

                List<Player> humans = new ArrayList<>();
                Color[] playerColors = {Color.RED, Color.BLUE, new Color(60,180,75), Color.MAGENTA, Color.ORANGE};

                for (int i = 0; i < humanCount; i++) {
                    String name = JOptionPane.showInputDialog(mainApp, "Masukkan nama untuk Pemain " + (i + 1) + ":", "Pemain " + (i + 1));
                    if (name == null) name = "Pemain " + (i+1);
                    humans.add(new Player(name, playerColors[i % playerColors.length], false));
                }
                Collections.shuffle(humans);
                setupGame(humans);

            } else {
                initializePlayers(1, 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
            promptForPlayers();
        }
    }

    private int getValidIntInput(String message, int min, int max) {
        while (true) {
            String input = JOptionPane.showInputDialog(mainApp, message);
            if (input == null) return -1;
            try {
                int value = Integer.parseInt(input.trim());
                if (value >= min && value <= max) return value;
                JOptionPane.showMessageDialog(mainApp, "Mohon masukkan angka antara " + min + " dan " + max + ".");
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(mainApp, "Input tidak valid! Harap masukkan angka.");
            }
        }
    }

    // Dipanggil oleh GameVisualizer saat restart
    public void setupGame(List<Player> players) {
        turnQueue.clear();
        for (Player p : players) {
            turnQueue.offer(p);
            placePlayerOnNode(p, 0);
        }
        currentPlayer = turnQueue.peek();
        if (controlPanel != null) controlPanel.updateTurnInfo(currentPlayer);
        mainApp.repaint();
        scheduleAutoRollIfNeeded();
    }

    private void initializePlayers(int humanCount, int aiCount) {}

    private void scheduleAutoRollIfNeeded() {
        if (gameOver) return;
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
        if (gameOver) return;
        if (currentPlayer == null || (!movementStack.isEmpty() || !dijkstraMoveQueue.isEmpty())) return;
        controlPanel.enableRollButton(false);
        startDiceAnimation();
    }

    private void startDiceAnimation() {
        audioPlayer.playEffect("rollDice");
        if (controlPanel != null) {
            controlPanel.startDiceSpinAnimation(ANIMATION_DURATION, this::executeDiceRoll);
        } else {
            executeDiceRoll();
        }
    }

    private void executeDiceRoll() {
        int currentPos = currentPlayer.getCurrentPosition();
        int diceRoll = rng.nextInt(6) + 1;
        boolean isGreen = rng.nextDouble() < GREEN_PROBABILITY;
        int moveDirection = isGreen ? 1 : -1;

        if (currentPlayer.isPrimePowerActive()) {
            isGreen = true;
            moveDirection = 1;
        }

        String resultColor = isGreen ? "GREEN" : "RED";
        if (controlPanel != null) controlPanel.updateDiceResult(diceRoll, resultColor, moveDirection);

        movementStack.clear();
        dijkstraMoveQueue.clear();

        // JIKA PUNYA POWER -> GUNA DIJKSTRA (Akan pakai tangga jika ada)
        if (currentPlayer.isPrimePowerActive()) {
            List<Integer> path = DijkstraPathFinder.findShortestPathSteps(board, currentPos, board.getTotalNodes(), diceRoll);
            dijkstraMoveQueue.addAll(path);
        } else {
            // JIKA TIDAK -> NORMAL WALK (Akan jalan melewati tangga tanpa naik)
            for (int i = 0; i < diceRoll; i++) {
                movementStack.push(moveDirection);
            }
        }

        startMovementAnimation();
    }

    private void startMovementAnimation() {
        Timer timer = new Timer(MOVEMENT_INTERVAL, null);
        timer.addActionListener(e -> {
            boolean isDijkstraActive = !dijkstraMoveQueue.isEmpty();
            boolean isNormalActive = !movementStack.isEmpty();

            if (!isDijkstraActive && !isNormalActive) {
                ((Timer) e.getSource()).stop();
                checkWinCondition();
                finalizeTurn();
            } else {
                if (controlPanel != null) controlPanel.enableRollButton(false);

                if (isDijkstraActive) {
                    int nextNodeId = dijkstraMoveQueue.poll();
                    movePlayerToSpecificNode(nextNodeId);

                    // Cek jika langkah ini sebenarnya adalah memanjat tangga (untuk tracking)
                    // (Opsional: Bisa ditambahkan visualisasi naik tangga di sini)
                    // Jika node baru > node lama + 1, berarti lompat (tangga)
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

        Map<Integer, Integer> connections = board.getConnections();
        boolean connectionTriggered = false;

        // MAJU (+1)
        if (direction > 0 && connections.containsKey(newPosId)) {
            int target = connections.get(newPosId);
            // TANGGA (Naik)
            if (target > newPosId) {
                if (currentPlayer.isPrimePowerActive()) {
                    movePlayerToSpecificNode(target);
                    // Catat bahwa pemain telah memanjat tangga ini
                    currentPlayer.addClimbedLadder(target);
                    JOptionPane.showMessageDialog(mainApp, currentPlayer.getName() + " menggunakan PRIME POWER untuk naik Tangga!\nPindah ke Node " + target, "Tangga Dinaiki!", JOptionPane.INFORMATION_MESSAGE);
                    connectionTriggered = true;
                } else {
                    System.out.println(currentPlayer.getName() + " melewati tangga karena tidak punya Prime Power.");
                }
            } else if (target < newPosId) {
                // ULAR (Turun)
                movePlayerToSpecificNode(target);
                JOptionPane.showMessageDialog(mainApp, "⚠️ TERGELINCIR MUNDUR! ⚠️\nKembali dari Node " + newPosId + " ke Node " + target, "Ular!", JOptionPane.WARNING_MESSAGE);
                connectionTriggered = true;
            }
        }

        // MUNDUR (-1)
        if (direction < 0) {
            for (Map.Entry<Integer, Integer> entry : connections.entrySet()) {
                if (entry.getValue() == newPosId) {
                    int start = entry.getKey();

                    // Jika ini Tangga (start < end/newPosId)
                    if (start < newPosId) {
                        // Cek apakah pemain pernah naik tangga ini?
                        if (currentPlayer.hasClimbedLadder(newPosId)) {
                            movePlayerToSpecificNode(start);
                            JOptionPane.showMessageDialog(mainApp, "⚠️ TERGELINCIR MUNDUR! ⚠️\nAnda merosot kembali ke bawah tangga.", "Koneksi Terbalik", JOptionPane.WARNING_MESSAGE);
                            connectionTriggered = true;
                        }
                    } else {
                        // Jika Ular (start > end), abaikan (biasanya ular tidak naik mundur)
                    }
                    break;
                }
            }
        }

        if (connectionTriggered) {
            audioPlayer.playEffectImmediately("connection");
        }
    }

    private void finalizeTurn() {
        if (gameOver) {
            currentPlayer = null;
            if (controlPanel != null) {
                controlPanel.updateTurnInfo(null);
                controlPanel.enableRollButton(false);
            }
            return;
        }

        Player acting = turnQueue.peek();
        if (acting != null) {
            acting.incrementTurnCount();
            int pos = acting.getCurrentPosition();

            if (board.hasScore(pos)) {
                int gatheredScore = board.collectScore(pos);
                acting.addScore(gatheredScore);
                audioPlayer.playEffectImmediately("star");
                JOptionPane.showMessageDialog(mainApp,
                        acting.getName() + " mendapatkan " + gatheredScore + " Poin!",
                        "Score Get!", JOptionPane.INFORMATION_MESSAGE);
                mainApp.repaint();
            }

            if (isPrime(pos)) {
                acting.setPrimePowerActive(true);
                audioPlayer.playEffect("prime");
                JOptionPane.showMessageDialog(mainApp, "🌟 PRIME POWER AKTIF! 🌟\n" + acting.getName() + " mendarat di Prima (Node "+pos+").\n" + "Turn depan: Bisa pakai Tangga & Jalur Terpendek!", "Power Up", JOptionPane.INFORMATION_MESSAGE);
            } else {
                acting.setPrimePowerActive(false);
            }

            boolean landedStar = (pos > 0) && (pos % 5 == 0) && (pos != board.getTotalNodes());
            if (landedStar) {
                audioPlayer.playEffectImmediately("star");
                JOptionPane.showMessageDialog(mainApp, acting.getName() + " dapat Extra Turn (Bintang)!", "Extra Turn", JOptionPane.INFORMATION_MESSAGE);
            } else {
                Player finishedPlayer = turnQueue.poll();
                if (finishedPlayer != null && finishedPlayer.getCurrentPosition() < board.getTotalNodes()) {
                    turnQueue.offer(finishedPlayer);
                }
            }
        }

        if (turnQueue.isEmpty()) {
            currentPlayer = null;
        } else {
            currentPlayer = turnQueue.peek();
        }

        if (controlPanel != null) {
            controlPanel.updateTurnInfo(currentPlayer);
            controlPanel.enableRollButton(true);
            if (currentPlayer != null) controlPanel.updatePlayerStatus(currentPlayer);
        }
        scheduleAutoRollIfNeeded();
    }

    private void checkWinCondition() {
        if (currentPlayer == null) return;
        if (currentPlayer.getCurrentPosition() == board.getTotalNodes()) {
            audioPlayer.stopBackgroundMusic();
            audioPlayer.playEffectImmediately("win");

            LeaderboardManager.addWin(currentPlayer.getName());

            LeaderboardManager.updateScore(currentPlayer.getName(), currentPlayer.getScore());
            for(Player p : turnQueue) {
                LeaderboardManager.updateScore(p.getName(), p.getScore());
            }

            String topScores = LeaderboardManager.getTop3Scores();
            String topWins = LeaderboardManager.getTop3Wins();

            String message = "🎉 Selamat! " + currentPlayer.getName() + " Menang!\n\n" +
                    "=== TOP 3 WINNERS ===\n" + topWins + "\n" +
                    "=== TOP 3 HIGH SCORES ===\n" + topScores;

            JOptionPane.showMessageDialog(mainApp, message, "Leaderboard & Game Over", JOptionPane.INFORMATION_MESSAGE);

            gameOver = true;
            if (controlPanel != null) {
                controlPanel.updateTurnInfo(null);
                controlPanel.enableRollButton(false);
            }
            mainApp.repaint();

            SwingUtilities.invokeLater(() -> {
                int res = JOptionPane.showConfirmDialog(mainApp, "Main lagi?", "Restart", JOptionPane.YES_NO_OPTION);
                if (res == JOptionPane.YES_OPTION) {
                    // LOGIKA RESTART OTOMATIS
                    List<Player> allPlayers = new ArrayList<>();
                    // Masukkan pemenang dulu
                    if (currentPlayer != null) allPlayers.add(currentPlayer);
                    // Masukkan sisa pemain dalam antrian
                    allPlayers.addAll(turnQueue);

                    // Reset status pemain (posisi 0, score 0, dll)
                    for (Player p : allPlayers) {
                        p.resetState();
                    }

                    // Restart via Visualizer
                    mainApp.restartGame(allPlayers);
                } else {
                    audioPlayer.playEffectImmediately("gameOver");
                    System.exit(0);
                }
            });
        }
    }

    private void placePlayerOnNode(Player p, int nodeId) {
        BoardNode node = board.getNodeById(nodeId);
        if (node != null) node.addPlayer(p);
        p.setCurrentPosition(nodeId);
        if (controlPanel != null) controlPanel.updatePlayerStatus(p);
    }

    public Player getCurrentPlayer() { return currentPlayer; }
}