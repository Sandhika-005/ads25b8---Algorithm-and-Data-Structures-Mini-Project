import javax.swing.*;
import java.awt.*;

class GameControlPanel extends JPanel {
    private GameEngine gameEngine;
    private BoardPanel boardPanel;
    private JButton rollDiceButton;
    private JLabel turnLabel;
    private JPanel resultPanel;

    private final Color GREEN_RESULT = new Color(60, 180, 75);
    private final Color RED_RESULT = new Color(200, 60, 60);

    public GameControlPanel(GameEngine ge, BoardPanel bp) {
        this.gameEngine = ge;
        this.boardPanel = bp;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(new Color(45, 60, 80));
        setPreferredSize(new Dimension(280, 800));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        add(createTitleLabel("Game Controls"));
        add(Box.createVerticalStrut(20));

        turnLabel = createStatusLabel("Turn: Waiting for Players...");
        add(turnLabel);
        add(Box.createVerticalStrut(20));

        rollDiceButton = createControlButton("ROLL DICE", new Color(200, 120, 50));
        rollDiceButton.addActionListener(e -> {
            rollDiceButton.setEnabled(false);
            gameEngine.rollDiceAndMove();
        });
        rollDiceButton.setEnabled(false);
        add(rollDiceButton);
        add(Box.createVerticalStrut(30));
        add(createDivider());
        add(Box.createVerticalStrut(30));

        add(createTitleLabel("Dice Result"));
        add(Box.createVerticalStrut(10));
        resultPanel = createResultPanel();
        add(resultPanel);

        add(Box.createVerticalGlue());
    }

    private JLabel createTitleLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 18));
        label.setForeground(new Color(150, 200, 255));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }

    private JLabel createStatusLabel(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.PLAIN, 16));
        label.setForeground(Color.LIGHT_GRAY);
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

    private JPanel createResultPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(2, 1, 5, 5));
        panel.setPreferredSize(new Dimension(200, 100));
        panel.setBackground(new Color(60, 80, 100));
        panel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));

        JLabel diceLbl = new JLabel("Dice: -", SwingConstants.CENTER);
        diceLbl.setFont(new Font("Monospaced", Font.BOLD, 24));
        diceLbl.setForeground(Color.WHITE);
        diceLbl.setName("dice");
        panel.add(diceLbl);

        JLabel colorLbl = new JLabel("Direction: -", SwingConstants.CENTER);
        colorLbl.setFont(new Font("Monospaced", Font.BOLD, 18));
        colorLbl.setForeground(Color.WHITE);
        colorLbl.setName("color");
        panel.add(colorLbl);

        panel.setAlignmentX(Component.CENTER_ALIGNMENT);
        return panel;
    }

    // --- Update UI ---
    public void updateTurnInfo(Player currentPlayer) {
        if (currentPlayer != null) {
            turnLabel.setText("Turn: " + currentPlayer.getName() + " (Node " + currentPlayer.getCurrentPosition() + ")");
            turnLabel.setForeground(currentPlayer.getColor());
            rollDiceButton.setEnabled(true);
        } else {
            turnLabel.setText("GAME OVER");
            turnLabel.setForeground(Color.YELLOW);
            rollDiceButton.setEnabled(false);
        }
    }

    public void updateDiceResult(int diceRoll, String resultColor) {
        JLabel diceLbl = (JLabel) findComponentByName(resultPanel, "dice");
        JLabel colorLbl = (JLabel) findComponentByName(resultPanel, "color");

        // PERBAIKAN: Konversi int ke String
        diceLbl.setText("Dice: " + diceRoll);
        colorLbl.setText("Direction: " + resultColor);

        if (resultColor.equals("GREEN")) {
            resultPanel.setBackground(GREEN_RESULT);
            diceLbl.setForeground(Color.BLACK);
            colorLbl.setForeground(Color.BLACK);
        } else {
            resultPanel.setBackground(RED_RESULT);
            diceLbl.setForeground(Color.WHITE);
            colorLbl.setForeground(Color.WHITE);
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