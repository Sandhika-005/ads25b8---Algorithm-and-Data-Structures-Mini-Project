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

    private final Color GREEN_RESULT = new Color(76, 175, 80);
    private final Color RED_RESULT = new Color(244, 67, 54);
    private final Color BG_DARK = new Color(45, 60, 80);
    private final Color ACCENT_COLOR = new Color(255, 193, 7);

    public GameControlPanel(GameEngine ge, BoardPanel bp, GameVisualizer app) {
        this.gameEngine = ge;
        this.boardPanel = bp;
        this.mainApp = app;
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
        diceAnimationPanel = new DiceAnimationPanel();
        diceAnimationPanel.setPreferredSize(new Dimension(200, 140));
        diceAnimationPanel.setOpaque(false); // Transparan agar background panel terlihat

        JPanel container = new JPanel(new GridBagLayout());
        container.setBackground(BG_DARK.brighter());
        container.setBorder(BorderFactory.createLineBorder(ACCENT_COLOR, 3));
        container.setAlignmentX(Component.CENTER_ALIGNMENT);
        container.setMaximumSize(new Dimension(240, 180));
        container.add(diceAnimationPanel);

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

    public void enableRollButton(boolean enabled) {
        if (gameEngine.getCurrentPlayer() != null) {
            rollDiceButton.setEnabled(enabled);
        } else {
            rollDiceButton.setEnabled(false);
        }
    }

    /**
     * Panel Dadu dengan visualisasi 3D yang lebih bagus
     */
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
            int side = Math.min(w, h) - 40; // Sedikit lebih kecil agar muat shadow
            int x = (w - side) / 2;
            int y = (h - side) / 2 - 5;
            int arc = 30; // Sudut lebih bulat

            // 1. Gambar Bayangan Dadu
            g2.setColor(new Color(0, 0, 0, 60));
            g2.fillRoundRect(x + 5, y + 8, side, side, arc, arc);

            // 2. Gambar Body Dadu dengan Gradasi (Efek 3D)
            GradientPaint bodyGrad = new GradientPaint(
                    x, y, Color.WHITE,
                    x + side, y + side, new Color(220, 220, 220)
            );
            g2.setPaint(bodyGrad);
            g2.fillRoundRect(x, y, side, side, arc, arc);

            // 3. Highlight Putih di pojok kiri atas (Kilauan)
            g2.setPaint(new Color(255, 255, 255, 150));
            g2.fillRoundRect(x + 5, y + 5, side / 2, side / 2, arc, arc);

            // 4. Border Halus
            g2.setColor(new Color(180, 180, 180));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(x, y, side, side, arc, arc);

            // 5. Gambar Titik Dadu (Pips)
            g2.setColor(pipColor);
            int pipRadius = side / 10;
            int ox = x + side / 6;
            int oy = y + side / 6;
            int mx = x + side / 2;
            int my = y + side / 2;
            int rx = x + side - side / 6;
            int by = y + side - side / 6;

            java.util.function.BiConsumer<Integer,Integer> pip = (px, py) -> {
                int jitterX = 0, jitterY = 0;
                if (spinning) {
                    double phase = animPhase + (px + py) * 0.01;
                    jitterX = (int) Math.round(Math.cos(phase) * 3);
                    jitterY = (int) Math.round(Math.sin(phase) * 3);
                }

                // Shadow pip kecil
                g2.setColor(new Color(200, 200, 200));
                g2.fillOval(px - pipRadius + 1 + jitterX, py - pipRadius + 1 + jitterY, pipRadius * 2, pipRadius * 2);

                // Pip utama
                g2.setColor(pipColor);
                g2.fillOval(px - pipRadius + jitterX, py - pipRadius + jitterY, pipRadius * 2, pipRadius * 2);
            };

            switch (face) {
                case 1: pip.accept(mx, my); break;
                case 2: pip.accept(ox, oy); pip.accept(rx, by); break;
                case 3: pip.accept(ox, oy); pip.accept(mx, my); pip.accept(rx, by); break;
                case 4: pip.accept(ox, oy); pip.accept(rx, oy); pip.accept(ox, by); pip.accept(rx, by); break;
                case 5: pip.accept(ox, oy); pip.accept(rx, oy); pip.accept(mx, my); pip.accept(ox, by); pip.accept(rx, by); break;
                case 6: pip.accept(ox, oy); pip.accept(ox, my); pip.accept(ox, by); pip.accept(rx, oy); pip.accept(rx, my); pip.accept(rx, by); break;
            }

            g2.dispose();
        }
    }
}