import javax.swing.*;
import java.awt.*;
import java.util.List;

class BoardPanel extends JPanel {
    private final Board board;
    private final GameEngine gameEngine;

    public BoardPanel(Board board, GameEngine gameEngine) {
        this.board = board;
        this.gameEngine = gameEngine;
        setBackground(new Color(30, 40, 60));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawNodes(g2d);
        drawPlayers(g2d);
    }

    private void drawNodes(Graphics2D g2d) {
        int size = BoardNode.SIZE;
        g2d.setFont(new Font("Arial", Font.BOLD, 18));

        for (BoardNode node : board.getNodes()) {
            int x = node.getX();
            int y = node.getY();

            // Pewarnaan seperti papan catur
            Color baseColor;
            if (node.getId() % 2 == 0) {
                baseColor = new Color(220, 230, 240);
            } else {
                baseColor = new Color(190, 210, 230);
            }

            g2d.setColor(baseColor);

            // Warna khusus untuk Start/Finish
            if (node.getId() == board.getTotalNodes()) {
                g2d.setColor(new Color(60, 180, 75)); // Finish (Hijau Tua)
            } else if (node.getId() == 1) {
                g2d.setColor(new Color(180, 60, 75)); // Start (Merah Tua)
            } else {
                g2d.setColor(baseColor);
            }

            g2d.fillRect(x, y, size, size);
            g2d.setColor(Color.DARK_GRAY);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRect(x, y, size, size);

            String label = String.valueOf(node.getId());
            FontMetrics fm = g2d.getFontMetrics();
            int textX = x + (size - fm.stringWidth(label)) / 2;
            int textY = y + (size + fm.getAscent()) / 2 - 4;
            g2d.setColor(Color.BLACK);
            g2d.drawString(label, textX, textY);
        }
    }

    // Connections are intentionally disabled (no connecting lines)
    private void drawConnections(Graphics2D g2d) {
        // no-op
    }

    private void drawPlayers(Graphics2D g2d) {
        int playerSize = 18;
        for (BoardNode node : board.getNodes()) {
            List<Player> players = node.getOccupyingPlayers();
            if (players.isEmpty()) continue;

            int nodeX = node.getX() + node.getSize() / 2;
            int nodeY = node.getY() + node.getSize() / 2;

            int offset = (players.size() - 1) * 10;

            for (int i = 0; i < players.size(); i++) {
                Player player = players.get(i);

                int drawX = nodeX + i * 20 - offset;

                g2d.setColor(player.getColor());
                g2d.fillOval(drawX - playerSize / 2, nodeY - playerSize / 2, playerSize, playerSize);

                g2d.setColor(Color.WHITE);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawOval(drawX - playerSize / 2, nodeY - playerSize / 2, playerSize, playerSize);

                if (player == gameEngine.getCurrentPlayer()) {
                    g2d.setColor(Color.YELLOW);
                    g2d.setStroke(new BasicStroke(4));
                    g2d.drawOval(drawX - playerSize / 2 - 2, nodeY - playerSize / 2 - 2, playerSize + 4, playerSize + 4);
                }
            }
        }
    }
}