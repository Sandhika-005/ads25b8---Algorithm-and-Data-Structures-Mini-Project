import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

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
        drawConnections(g2d);
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

            // --- VISUALISASI SCORE (KOIN) ---
            if (board.hasScore(node.getId())) {
                int val = board.getScoreValue(node.getId());
                // Gambar lingkaran kuning (Koin) di pojok kiri atas
                g2d.setColor(new Color(255, 215, 0)); // Emas
                g2d.fillOval(x + 5, y + 5, 22, 22);
                g2d.setColor(Color.BLACK);
                g2d.setStroke(new BasicStroke(1));
                g2d.drawOval(x + 5, y + 5, 22, 22);

                // Tulis nilai score kecil
                g2d.setFont(new Font("Arial", Font.BOLD, 10));
                g2d.drawString(String.valueOf(val), x + 10, y + 20);
            }
            // --------------------------------

            // Kembalikan font besar untuk nomor node
            g2d.setFont(new Font("Arial", Font.BOLD, 18));
            String label = String.valueOf(node.getId());
            FontMetrics fm = g2d.getFontMetrics();
            int textX = x + (size - fm.stringWidth(label)) / 2;
            int textY = y + (size + fm.getAscent()) / 2 - 4;
            g2d.setColor(Color.BLACK);
            g2d.drawString(label, textX, textY);

            // Draw star icon for nodes that are multiples of 5
            if (node.getId() % 5 == 0 && node.getId() != 1 && node.getId() != board.getTotalNodes()) {
                int starCx = x + size - 12;
                int starCy = y + 12;
                int outerR = 8;
                int innerR = 4;
                drawStar(g2d, starCx, starCy, outerR, innerR, new Color(255, 215, 0)); // gold star
            }
        }
    }

    private void drawStar(Graphics2D g2d, int cx, int cy, int outerR, int innerR, Color color) {
        double angle = Math.PI / 2 * 3;
        int points = 5;
        int[] xs = new int[points * 2];
        int[] ys = new int[points * 2];
        for (int i = 0; i < points * 2; i++) {
            int r = (i % 2 == 0) ? outerR : innerR;
            xs[i] = cx + (int) Math.round(Math.cos(angle) * r);
            ys[i] = cy + (int) Math.round(Math.sin(angle) * r);
            angle += Math.PI / points;
        }
        Polygon star = new Polygon(xs, ys, xs.length);
        g2d.setColor(color);
        g2d.fillPolygon(star);
        g2d.setColor(Color.DARK_GRAY);
        g2d.setStroke(new BasicStroke(1));
        g2d.drawPolygon(star);
    }

    private void drawConnections(Graphics2D g2d) {
        Map<Integer, Integer> connections = board.getConnections();
        g2d.setStroke(new BasicStroke(4));

        for (Map.Entry<Integer, Integer> entry : connections.entrySet()) {
            int startId = entry.getKey();
            int endId = entry.getValue();

            BoardNode startNode = board.getNodeById(startId);
            BoardNode endNode = board.getNodeById(endId);

            if (startNode == null || endNode == null) continue;

            int x1 = startNode.getX() + BoardNode.SIZE / 2;
            int y1 = startNode.getY() + BoardNode.SIZE / 2;
            int x2 = endNode.getX() + BoardNode.SIZE / 2;
            int y2 = endNode.getY() + BoardNode.SIZE / 2;

            if (endId > startId) {
                g2d.setColor(new Color(0, 180, 0, 180)); // Hijau Transparan
            } else {
                g2d.setColor(new Color(200, 0, 0, 180)); // Merah Transparan
            }

            g2d.drawLine(x1, y1, x2, y2);
            g2d.fillOval(x2 - 6, y2 - 6, 12, 12);
        }
    }

    private void drawPlayers(Graphics2D g2d) {
        int playerSize = 18;
        List<BoardNode> allNodes = new ArrayList<>(board.getNodes());
        if (board.getOutsideNode() != null) allNodes.add(board.getOutsideNode());

        for (BoardNode node : allNodes) {
            List<Player> players = node.getOccupyingPlayers();
            if (players == null || players.isEmpty()) continue;

            int centerX;
            int centerY;
            if (node.getId() == 0) {
                BoardNode n1 = board.getNodeById(1);
                if (n1 != null) {
                    centerX = n1.getX() - BoardNode.SIZE/2 - 20;
                    centerY = n1.getY() + BoardNode.SIZE/2;
                } else {
                    centerX = Math.max(20, node.getX() + node.getSize() / 2);
                    centerY = Math.max(20, node.getY() + node.getSize() / 2);
                }
            } else {
                centerX = node.getX() + node.getSize() / 2;
                centerY = node.getY() + node.getSize() / 2;
            }

            int n = players.size();
            int maxRadius = node.getSize() / 2 - playerSize/2 - 4;
            int radius = Math.max( (n <= 1 ? 0 : Math.min(maxRadius,  (playerSize + 6) * n / 2 )), 10);

            for (int i = 0; i < n; i++) {
                Player player = players.get(i);
                int drawX, drawY;

                if (n == 1) {
                    drawX = centerX - playerSize / 2;
                    drawY = centerY - playerSize / 2;
                } else {
                    double angle = -Math.PI/2 + (2 * Math.PI * i) / n;
                    int px = (int) Math.round(centerX + Math.cos(angle) * radius);
                    int py = (int) Math.round(centerY + Math.sin(angle) * radius);
                    drawX = px - playerSize / 2;
                    drawY = py - playerSize / 2;
                }

                g2d.setColor(player.getColor());
                g2d.fillOval(drawX, drawY, playerSize, playerSize);

                g2d.setColor(Color.WHITE);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawOval(drawX, drawY, playerSize, playerSize);

                if (player == gameEngine.getCurrentPlayer()) {
                    g2d.setColor(Color.YELLOW);
                    g2d.setStroke(new BasicStroke(3));
                    g2d.drawOval(drawX - 3, drawY - 3, playerSize + 6, playerSize + 6);
                }
            }
        }
    }
}