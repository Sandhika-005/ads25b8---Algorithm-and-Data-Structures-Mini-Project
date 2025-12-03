import javax.swing.*;
import java.awt.*;
import java.util.Random;
import javax.swing.Timer;

class GameControlPanel extends JPanel {
    private GameEngine gameEngine;
    private BoardPanel boardPanel;
    private JButton rollDiceButton;
    private JLabel turnLabel;
    private JPanel diceResultPanel;
    private JLabel colorDirectionLabel;
    private DiceAnimationPanel diceAnimationPanel;
    private JLabel statusLabel;

    private GameVisualizer mainApp;
    private JSpinner nodesSpinner;
    private JButton applyNodesButton;
    private JButton startGameButton;

    // Variabel Developer Mode DIHAPUS

    private AudioPlayer audioPlayer;
    private JButton muteButton;

    private final Color GREEN_RESULT = new Color(76, 175, 80);
    private final Color RED_RESULT = new Color(244, 67, 54);
    private final Color BG_DARK = new Color(45, 60, 80);
    private final Color ACCENT_COLOR = new Color(255, 193, 7);

    public GameControlPanel(GameEngine ge, BoardPanel bp, GameVisualizer app, AudioPlayer ap) {
        this.gameEngine = ge;
        this.boardPanel = bp;
        this.mainApp = app;
        this.audioPlayer = ap;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(BG_DARK);
        setPreferredSize(new Dimension(280, 800));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Node count control
        add(createTitleLabel("Graph Configuration", new Color(150, 150, 150)));
        add(Box.createVerticalStrut(8));
        JPanel nodesPanel = new JPanel();
        nodesPanel.setBackground(BG_DARK);
        nodesPanel.setMaximumSize(new Dimension(240, 40));
        nodesPanel.setLayout(new BorderLayout(6, 0));
        nodesSpinner = new JSpinner(new SpinnerNumberModel(64, 4, 200, 1));
        ((JSpinner.DefaultEditor) nodesSpinner.getEditor()).getTextField().setHorizontalAlignment(SwingConstants.CENTER);
        nodesPanel.add(new JLabel("Nodes:"), BorderLayout.WEST);
        nodesPanel.add(nodesSpinner, BorderLayout.CENTER);
        add(nodesPanel);
        add(Box.createVerticalStrut(6));
        applyNodesButton = createControlButton("APPLY NODES", ACCENT_COLOR);
        applyNodesButton.addActionListener(e -> {
            int nodes = (int) nodesSpinner.getValue();
            if (mainApp != null) mainApp.updateBoardNodeCount(nodes);
        });
        add(applyNodesButton);
        add(Box.createVerticalStrut(6));
        startGameButton = createControlButton("START GAME", new Color(33, 150, 243));
        startGameButton.addActionListener(e -> {
            applyNodesButton.setEnabled(false);
            nodesSpinner.setEnabled(false);
            startGameButton.setEnabled(false);
            if (gameEngine != null) SwingUtilities.invokeLater(() -> gameEngine.promptForPlayers());
        });
        add(startGameButton);
        add(Box.createVerticalStrut(10));

        // Mute/Unmute Button
        muteButton = createControlButton("MUTE BACKSOUND", new Color(100, 100, 100));
        if (audioPlayer != null && audioPlayer.isMuted()) {
            muteButton.setText("UNMUTE BACKSOUND");
            muteButton.setBackground(new Color(255, 69, 0));
        }

        muteButton.addActionListener(e -> {
            if (audioPlayer != null) {
                audioPlayer.toggleMute();
                if (audioPlayer.isMuted()) {
                    muteButton.setText("UNMUTE BACKSOUND");
                    muteButton.setBackground(new Color(255, 69, 0));
                } else {
                    muteButton.setText("MUTE BACKSOUND");
                    muteButton.setBackground(new Color(100, 100, 100));
                }
            }
        });
        add(muteButton);
        add(Box.createVerticalStrut(10));

        add(createDivider());
        add(Box.createVerticalStrut(30));

        add(createTitleLabel("Game Controls", new Color(150, 200, 255)));
        add(Box.createVerticalStrut(20));

        turnLabel = createStatusLabel("Turn: Waiting for Players...", Color.LIGHT_GRAY, 16);
        add(turnLabel);
        add(Box.createVerticalStrut(20));

        rollDiceButton = createControlButton("ROLL DICE", new Color(255, 87, 34));
        rollDiceButton.addActionListener(e -> {
            gameEngine.rollDiceAndMove();
        });
        rollDiceButton.setEnabled(false);
        add(rollDiceButton);
        add(Box.createVerticalStrut(30));
        add(createDivider());
        add(Box.createVerticalStrut(30));

        add(createTitleLabel("Roll Result", ACCENT_COLOR));
        add(Box.createVerticalStrut(10));

        diceResultPanel = createUniqueDicePanel();
        add(diceResultPanel);
        add(Box.createVerticalStrut(8));

        colorDirectionLabel = new JLabel("Langkah: -", SwingConstants.CENTER);
        colorDirectionLabel.setFont(new Font("Arial", Font.BOLD, 16));
        colorDirectionLabel.setForeground(Color.LIGHT_GRAY);
        colorDirectionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        colorDirectionLabel.setMaximumSize(new Dimension(240, 24));
        add(colorDirectionLabel);
        add(Box.createVerticalStrut(10));

        statusLabel = createStatusLabel("Status pemain: Posisi: Node 1", Color.LIGHT_GRAY, 14);
        add(statusLabel);
        add(Box.createVerticalStrut(10));

        add(Box.createVerticalGlue());
    }

    private JLabel createTitleLabel(String text, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 18));
        label.setForeground(color);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }

    private JLabel createStatusLabel(String text, Color color, int fontSize) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.PLAIN, fontSize));
        label.setForeground(color);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.setMaximumSize(new Dimension(240, 30));
        return label;
    }

    private Component createDivider() {
        JSeparator sep = new JSeparator(SwingConstants.HORIZONTAL);
        sep.setForeground(new Color(70, 90, 110));
        sep.setMaximumSize(new Dimension(240, 5));
        return sep;
    }

    private JButton createControlButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(240, 50));
        return button;
    }

    private JPanel createUniqueDicePanel() {
        DiceAnimationPanel diceAnimationPanel = new DiceAnimationPanel();
        diceAnimationPanel.setPreferredSize(new Dimension(200, 140));
        diceAnimationPanel.setOpaque(false);

        JPanel container = new JPanel(new GridBagLayout());
        container.setBackground(BG_DARK.brighter());
        container.setBorder(BorderFactory.createLineBorder(ACCENT_COLOR, 3));
        container.setAlignmentX(Component.CENTER_ALIGNMENT);
        container.setMaximumSize(new Dimension(240, 180));
        container.add(diceAnimationPanel);
        this.diceAnimationPanel = diceAnimationPanel;

        return container;
    }

    public void startDiceSpinAnimation(int durationMs, Runnable onFinish) {
        if (diceAnimationPanel != null) {
            diceAnimationPanel.setPipColor(Color.BLACK);
            colorDirectionLabel.setText("Rolling...");
            colorDirectionLabel.setForeground(Color.WHITE);
            if (diceResultPanel != null) diceResultPanel.setBackground(BG_DARK.brighter());
            diceAnimationPanel.startSpin(durationMs, onFinish);
        } else {
            if (onFinish != null) SwingUtilities.invokeLater(onFinish);
        }
    }

    public void updateTurnInfo(Player currentPlayer) {
        if (currentPlayer != null) {
            String name = currentPlayer.getName() + (currentPlayer.isAI() ? " (AI)" : "");
            turnLabel.setText("Turn: " + name);
            turnLabel.setForeground(currentPlayer.getColor());
            rollDiceButton.setEnabled(true);
        } else {
            turnLabel.setText("GAME OVER");
            turnLabel.setForeground(Color.YELLOW);
            rollDiceButton.setEnabled(false);
        }
    }

    public void updateDiceResult(int diceRoll, String resultColor, int moveDirection) {
        String directionText = (moveDirection == 1) ? "MAJU" : "MUNDUR";
        Color bgColor = "GREEN".equals(resultColor) ? GREEN_RESULT : RED_RESULT;
        Color fgColor = "GREEN".equals(resultColor) ? Color.BLACK : Color.WHITE;

        if (diceAnimationPanel != null) {
            diceAnimationPanel.setFace(diceRoll);
            if (diceResultPanel != null) diceResultPanel.setBackground(bgColor);
            diceAnimationPanel.setPipColor(Color.BLACK);
            colorDirectionLabel.setText("Langkah: " + diceRoll + " " + directionText);
            colorDirectionLabel.setForeground(fgColor);
            diceAnimationPanel.repaint();
        }

        Player currentPlayer = gameEngine.getCurrentPlayer();
        if (currentPlayer != null) {
            statusLabel.setText("Status pemain: Posisi: Node " + currentPlayer.getCurrentPosition());
        }

        boardPanel.repaint();
    }

    public void updatePlayerStatus(Player p) {
        SwingUtilities.invokeLater(() -> {
            if (p != null) {
                statusLabel.setText("Status pemain: Posisi: Node " + p.getCurrentPosition());
            } else {
                statusLabel.setText("Status pemain: Posisi: -");
            }
        });
    }

    public void enableRollButton(boolean enabled) {
        if (gameEngine.getCurrentPlayer() != null) {
            rollDiceButton.setEnabled(enabled);
        } else {
            rollDiceButton.setEnabled(false);
        }
    }

    private class DiceAnimationPanel extends JPanel {
        private Timer spinTimer;
        private final int spinInterval = 50;
        private final Random rnd = new Random();
        private int face = 1;
        private Color pipColor = Color.BLACK;
        private double animPhase = 0.0;
        private boolean spinning = false;

        public DiceAnimationPanel() {
            setPreferredSize(new Dimension(200, 140));
            setOpaque(false);
        }

        public void setFace(int f) {
            if (f < 1) f = 1;
            if (f > 6) f = 6;
            this.face = f;
            repaint();
        }

        public void setPipColor(Color c) {
            this.pipColor = c != null ? c : Color.BLACK;
            repaint();
        }

        public void startSpin(int durationMs, Runnable onFinish) {
            if (spinTimer != null && spinTimer.isRunning()) spinTimer.stop();

            animPhase = 0.0;
            spinning = true;
            spinTimer = new Timer(spinInterval, e -> {
                animPhase += 0.5;
                if (rnd.nextInt(3) == 0) {
                    setFace(rnd.nextInt(6) + 1);
                }
                repaint();
            });
            spinTimer.start();

            new Timer(durationMs, ev -> {
                if (spinTimer != null) spinTimer.stop();
                ((Timer) ev.getSource()).stop();
                spinning = false;
                setFace(rnd.nextInt(6) + 1);
                repaint();
                if (onFinish != null) SwingUtilities.invokeLater(onFinish);
            }).start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int size = Math.min(w, h) - 45; // Ukuran dadu
            int depth = 12; // Ketebalan 3D

            int x = (w - size) / 2 - depth / 2;
            int y = (h - size) / 2 - depth / 2;
            int arc = 35; // Sudut tumpul

            // 1. Bayangan Jatuh (Drop Shadow) - Realistis
            g2.setColor(new Color(0, 0, 0, 60));
            g2.fillRoundRect(x + depth + 5, y + depth + 12, size, size, arc, arc);

            // 2. Sisi Dadu (Efek 3D / Tebal) - Warna lebih gelap
            g2.setColor(new Color(180, 180, 180)); // Abu-abu gelap untuk sisi
            g2.fillRoundRect(x + depth, y + depth, size, size, arc, arc);

            // 3. Wajah Utama Dadu
            g2.setColor(new Color(255, 255, 255)); // Solid White
            g2.fillRoundRect(x, y, size, size, arc, arc);

            // 4. Highlight Tepi (Bevel Effect) - Agar terlihat glossy
            g2.setStroke(new BasicStroke(1.5f));
            g2.setColor(new Color(255, 255, 255, 220)); // Putih terang
            g2.drawRoundRect(x + 2, y + 2, size - 4, size - 4, arc, arc);

            // 5. Gambar Pips (Titik Dadu)
            int pipSize = size / 5;

            // Koordinat relatif untuk pips (0 = kiri/atas, 1 = tengah, 2 = kanan/bawah)
            int c1 = x + size / 5;
            int c2 = x + size / 2;
            int c3 = x + size - size / 5;

            int r1 = y + size / 5;
            int r2 = y + size / 2;
            int r3 = y + size - size / 5;

            java.util.function.BiConsumer<Integer, Integer> drawPip = (px, py) -> {
                int jitterX = 0, jitterY = 0;
                if (spinning) {
                    double phase = animPhase + (px + py) * 0.01;
                    jitterX = (int) Math.round(Math.cos(phase) * 3);
                    jitterY = (int) Math.round(Math.sin(phase) * 3);
                }

                int finalX = px - pipSize / 2 + jitterX;
                int finalY = py - pipSize / 2 + jitterY;

                // Inner Shadow (Cekungan)
                g2.setColor(new Color(200, 200, 200));
                g2.fillOval(finalX + 1, finalY + 1, pipSize, pipSize);

                // Pip Utama
                g2.setColor(pipColor);
                g2.fillOval(finalX, finalY, pipSize, pipSize);

                // Glossy Highlight (Kilauan pada pip)
                g2.setColor(new Color(255, 255, 255, 80));
                g2.fillOval(finalX + pipSize/4, finalY + pipSize/4, pipSize/4, pipSize/4);
            };

            switch (face) {
                case 1: drawPip.accept(c2, r2); break;
                case 2: drawPip.accept(c1, r1); drawPip.accept(c3, r3); break;
                case 3: drawPip.accept(c1, r1); drawPip.accept(c2, r2); drawPip.accept(c3, r3); break;
                case 4: drawPip.accept(c1, r1); drawPip.accept(c3, r1); drawPip.accept(c1, r3); drawPip.accept(c3, r3); break;
                case 5: drawPip.accept(c1, r1); drawPip.accept(c3, r1); drawPip.accept(c2, r2); drawPip.accept(c1, r3); drawPip.accept(c3, r3); break;
                case 6: drawPip.accept(c1, r1); drawPip.accept(c3, r1); drawPip.accept(c1, r2); drawPip.accept(c3, r2); drawPip.accept(c1, r3); drawPip.accept(c3, r3); break;
            }

            g2.dispose();
        }
    }
}