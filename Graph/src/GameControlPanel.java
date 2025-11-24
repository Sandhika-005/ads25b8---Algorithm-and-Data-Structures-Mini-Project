import javax.swing.*;
import java.awt.*;

class GameControlPanel extends JPanel {
    private GameEngine gameEngine;
    private BoardPanel boardPanel;
    private JButton rollDiceButton;
    private JLabel turnLabel;
    private JPanel diceResultPanel;
    private JLabel statusLabel;
    private JLabel diceValueLabel;
    private JLabel colorDirectionLabel;

    private final Color GREEN_RESULT = new Color(76, 175, 80);
    private final Color RED_RESULT = new Color(244, 67, 54);
    private final Color BG_DARK = new Color(45, 60, 80);
    private final Color ACCENT_COLOR = new Color(255, 193, 7);

    public GameControlPanel(GameEngine ge, BoardPanel bp) {
        this.gameEngine = ge;
        this.boardPanel = bp;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(BG_DARK);
        setPreferredSize(new Dimension(280, 800));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        add(createTitleLabel("Graph Configuration (Disabled)", new Color(150, 150, 150)));
        add(createDivider());
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
        add(Box.createVerticalStrut(10));

        statusLabel = createStatusLabel("Status pemain: Posisi: Node 1", Color.WHITE, 14);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        add(statusLabel);

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
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BG_DARK.brighter());
        panel.setBorder(BorderFactory.createLineBorder(ACCENT_COLOR, 3));
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.setMaximumSize(new Dimension(240, 180));

        diceValueLabel = new JLabel("?", SwingConstants.CENTER);
        // PERBAIKAN: Gunakan font standar besar (bukan Segoe UI Emoji)
        diceValueLabel.setFont(new Font("Arial", Font.BOLD, 60));
        diceValueLabel.setForeground(Color.WHITE);
        diceValueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(diceValueLabel);

        colorDirectionLabel = new JLabel("Langkah: -", SwingConstants.CENTER);
        colorDirectionLabel.setFont(new Font("Arial", Font.BOLD, 16));
        colorDirectionLabel.setForeground(Color.LIGHT_GRAY);
        colorDirectionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(colorDirectionLabel);

        panel.add(Box.createVerticalStrut(5));

        return panel;
    }

    public void updateTurnInfo(Player currentPlayer) {
        if (currentPlayer != null) {
            turnLabel.setText("Turn: " + currentPlayer.getName());
            turnLabel.setForeground(currentPlayer.getColor());
            statusLabel.setText("Status pemain: Posisi: Node " + currentPlayer.getCurrentPosition());
            rollDiceButton.setEnabled(true);
        } else {
            turnLabel.setText("GAME OVER");
            turnLabel.setForeground(Color.YELLOW);
            statusLabel.setText("");
            rollDiceButton.setEnabled(false);
        }
    }

    public void updateDiceAnimation(int tempRoll) {
        diceValueLabel.setText(String.valueOf(tempRoll));
        colorDirectionLabel.setText("Rolling...");

        diceResultPanel.setBackground(new Color(90, 110, 130));
        diceValueLabel.setForeground(Color.YELLOW);
        colorDirectionLabel.setForeground(Color.WHITE);

        boardPanel.repaint();
    }

    public void updateDiceResult(int diceRoll, String resultColor, int moveDirection) {

        String directionText = (moveDirection == 1) ? "MAJU" : "MUNDUR";
        Color bgColor = resultColor.equals("GREEN") ? GREEN_RESULT : RED_RESULT;
        Color fgColor = resultColor.equals("GREEN") ? Color.BLACK : Color.WHITE;

        diceValueLabel.setText(String.valueOf(diceRoll));
        colorDirectionLabel.setText("Langkah: " + diceRoll + " " + directionText);

        diceResultPanel.setBackground(bgColor);
        diceValueLabel.setForeground(fgColor);
        colorDirectionLabel.setForeground(fgColor);

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
}