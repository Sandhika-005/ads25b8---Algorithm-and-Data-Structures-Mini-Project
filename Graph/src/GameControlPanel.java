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

    // status label shown under dice/result to indicate current player's position/status
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
            // delegate to main application to rebuild board/game
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

        // place "Langkah:" label outside the dice box (below the box)
        colorDirectionLabel = new JLabel("Langkah: -", SwingConstants.CENTER);
        colorDirectionLabel.setFont(new Font("Arial", Font.BOLD, 16));
        colorDirectionLabel.setForeground(Color.LIGHT_GRAY);
        colorDirectionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        colorDirectionLabel.setMaximumSize(new Dimension(240, 24));
        add(colorDirectionLabel);
        add(Box.createVerticalStrut(10));

        // Status label to show current player's node/position
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
        // Create a container panel whose background will show the result color (red/green).
        // The dice square inside remains neutral; pips and pip color are changed.
        diceAnimationPanel = new DiceAnimationPanel();
        diceAnimationPanel.setPreferredSize(new Dimension(200, 140));

        JPanel container = new JPanel(new GridBagLayout());
        container.setBackground(BG_DARK.brighter());
        container.setBorder(BorderFactory.createLineBorder(ACCENT_COLOR, 3));
        container.setAlignmentX(Component.CENTER_ALIGNMENT);
        container.setMaximumSize(new Dimension(240, 180));
        container.add(diceAnimationPanel); // centered

        return container;
    }

    // Called by GameEngine: play a spin animation for durationMs then run onFinish (on EDT)
    public void startDiceSpinAnimation(int durationMs, Runnable onFinish) {
        if (diceAnimationPanel != null) {
            // Ensure neutral appearance while spinning (no result color shown)
            diceAnimationPanel.setPipColor(Color.BLACK);
            // neutral dice square while spinning
            diceAnimationPanel.setDiceSquareColor(new Color(250, 250, 250));
            colorDirectionLabel.setText("Rolling...");
            colorDirectionLabel.setForeground(Color.WHITE);
            if (diceResultPanel != null) diceResultPanel.setBackground(BG_DARK.brighter()); // result color stays on container
            // start animation
            diceAnimationPanel.startSpin(durationMs, onFinish);
        } else {
            // fallback: directly run callback
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

    public void updateDiceAnimation(int tempRoll) {
        if (diceAnimationPanel != null) {
            diceAnimationPanel.setFace(tempRoll);
            colorDirectionLabel.setText("Rolling...");
            colorDirectionLabel.setForeground(Color.WHITE);
            if (diceResultPanel != null) diceResultPanel.setBackground(BG_DARK.brighter());
            diceAnimationPanel.setPipColor(Color.BLACK);
            diceAnimationPanel.setDiceSquareColor(new Color(250, 250, 250)); // keep neutral during spin
            diceAnimationPanel.repaint();
            boardPanel.repaint();
        }
    }

    // Apply final dice result (called after spin finishes)
    public void updateDiceResult(int diceRoll, String resultColor, int moveDirection) {
        String directionText = (moveDirection == 1) ? "MAJU" : "MUNDUR";
        Color bgColor = "GREEN".equals(resultColor) ? GREEN_RESULT : RED_RESULT;
        Color fgColor = "GREEN".equals(resultColor) ? Color.BLACK : Color.WHITE;

        if (diceAnimationPanel != null) {
            diceAnimationPanel.setFace(diceRoll);
            // Apply result color to container (outside the dice square)
            if (diceResultPanel != null) diceResultPanel.setBackground(bgColor);
            // Keep dice square neutral; use a contrasting pip color so pips remain visible.
            diceAnimationPanel.setPipColor(Color.BLACK);
            diceAnimationPanel.setDiceSquareColor(new Color(250, 250, 250));
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

    private Component findComponentByName(Container container, String name) {
        for (Component component : container.getComponents()) {
            if (name.equals(component.getName())) {
                return component;
            }
            if (component instanceof Container) {
                Component found = findComponentByName((Container) component, name);
                if (found != null) return found;
            }
        }
        return null;
    }

    /**
     * Small inner panel that animates a rotating "dice" square and updates the face value.
     * It uses a Swing Timer for animation and stops itself after durationMs, then invokes the callback.
     */
    private class DiceAnimationPanel extends JPanel {
        private Timer spinTimer;
        private final int spinInterval = 50;
        private final Random rnd = new Random();
        private int face = 1; // current face 1..6
        private Color pipColor = Color.BLACK;
        private Color diceSquareColor = new Color(250, 250, 250); // neutral square
        private double animPhase = 0.0;     // drives pip motion
        private boolean spinning = false;

        public DiceAnimationPanel() {
            setPreferredSize(new Dimension(200, 140));
            setOpaque(false); // container provides background; dice square is drawn explicitly
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

        public void setDiceSquareColor(Color c) {
            this.diceSquareColor = (c != null) ? c : new Color(250, 250, 250);
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
            int side = Math.min(w, h) - 30;
            int cx = w / 2;
            int cy = h / 2 - 10;
            int x = cx - side / 2;
            int y = cy - side / 2;

            // draw static dice square (neutral)
            g2.setColor(diceSquareColor);
            g2.fillRoundRect(x, y, side, side, 10, 10);
            g2.setColor(Color.DARK_GRAY);
            g2.setStroke(new BasicStroke(2));
            g2.drawRoundRect(x, y, side, side, 10, 10);

            // draw pips according to current face
            g2.setColor(pipColor);
            int pipRadius = Math.max(4, side / 12);
            double jitterAmp = spinning ? Math.max(2, side / 40.0) : 0.0;
            double scaleAmp = spinning ? 1.0 + Math.abs(Math.sin(animPhase)) * 0.25 : 1.0;
            int ox = x + side / 6;
            int oy = y + side / 6;
            int mx = x + side / 2;
            int my = y + side / 2;
            int rx = x + side - side / 6;
            int by = y + side - side / 6;

            java.util.function.BiConsumer<Integer,Integer> pip = (px, py) -> {
                int jitterX = 0;
                int jitterY = 0;
                if (spinning) {
                    double phase = animPhase + (px + py) * 0.01;
                    jitterX = (int) Math.round(Math.cos(phase) * jitterAmp);
                    jitterY = (int) Math.round(Math.sin(phase) * jitterAmp);
                }
                int r = (int) Math.round(pipRadius * scaleAmp);
                g2.fillOval(px - r + jitterX, py - r + jitterY, r * 2, r * 2);
            };

            switch (face) {
                case 1:
                    pip.accept(mx, my);
                    break;
                case 2:
                    pip.accept(ox, oy);
                    pip.accept(rx, by);
                    break;
                case 3:
                    pip.accept(ox, oy);
                    pip.accept(mx, my);
                    pip.accept(rx, by);
                    break;
                case 4:
                    pip.accept(ox, oy);
                    pip.accept(rx, oy);
                    pip.accept(ox, by);
                    pip.accept(rx, by);
                    break;
                case 5:
                    pip.accept(ox, oy);
                    pip.accept(rx, oy);
                    pip.accept(mx, my);
                    pip.accept(ox, by);
                    pip.accept(rx, by);
                    break;
                case 6:
                    pip.accept(ox, oy);
                    pip.accept(ox, my);
                    pip.accept(ox, by);
                    pip.accept(rx, oy);
                    pip.accept(rx, my);
                    pip.accept(rx, by);
                    break;
            }

            g2.dispose();
        }
    }
}

