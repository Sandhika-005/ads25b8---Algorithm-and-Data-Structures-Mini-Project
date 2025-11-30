import javax.swing.*;
import java.awt.*;
import java.awt.geom.Line2D;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

class BoardPanel extends JPanel {
    private final Board board;
    private final GameEngine gameEngine;

    // --- VARIABEL ANIMASI ---
    private Player animatingPlayer = null;
    private double animX, animY; // Posisi saat ini untuk animasi
    private double targetX, targetY;
    private double startX, startY;
    private long animStartTime;
    private long animDuration;
    private Runnable onAnimComplete;
    private Timer animTimer;
    // ------------------------

    public BoardPanel(Board board, GameEngine gameEngine) {
        this.board = board;
        this.gameEngine = gameEngine;
        setBackground(new Color(30, 40, 60));

        // Timer untuk update frame animasi (approx 60 FPS)
        animTimer = new Timer(16, e -> updateAnimation());
    }

    // --- METHOD BARU: MEMULAI ANIMASI GERAK ---
    public void animatePlayerMovement(Player player, BoardNode startNode, BoardNode endNode, long durationMs, Runnable onComplete) {
        this.animatingPlayer = player;
        this.startX = startNode.getX() + BoardNode.SIZE / 2.0 - 9; // -9 karena radius player 18/2
        this.startY = startNode.getY() + BoardNode.SIZE / 2.0 - 9;
        this.targetX = endNode.getX() + BoardNode.SIZE / 2.0 - 9;
        this.targetY = endNode.getY() + BoardNode.SIZE / 2.0 - 9;

        this.animX = startX;
        this.animY = startY;

        this.animDuration = durationMs;
        this.animStartTime = System.currentTimeMillis();
        this.onAnimComplete = onComplete;

        animTimer.start();
    }

    private void updateAnimation() {
        long now = System.currentTimeMillis();
        long elapsed = now - animStartTime;
        double progress = (double) elapsed / animDuration;

        if (progress >= 1.0) {
            // Animasi Selesai
            progress = 1.0;
            animX = targetX;
            animY = targetY;
            animTimer.stop();

            // Simpan referensi callback dan player sebelum di-null-kan
            Runnable callback = onAnimComplete;
            animatingPlayer = null;
            repaint();

            if (callback != null) callback.run();
        } else {
            // Interpolasi Posisi (Ease In-Out sederhana)
            // Rumus smoothstep: t * t * (3 - 2 * t)
            double t = progress * progress * (3 - 2 * progress);

            animX = startX + (targetX - startX) * t;
            animY = startY + (targetY - startY) * t;
            repaint();
        }
    }
    // ------------------------------------------

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawNodes(g2d);
        drawConnectionsVisuals(g2d);
        drawPlayers(g2d);

        // Gambar Player yang sedang animasi di atas segalanya
        if (animatingPlayer != null) {
            int playerSize = 18;
            g2d.setColor(animatingPlayer.getColor());
            g2d.fillOval((int)animX, (int)animY, playerSize, playerSize);
            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawOval((int)animX, (int)animY, playerSize, playerSize);

            // Efek kilau saat bergerak
            g2d.setColor(new Color(255,255,255,100));
            g2d.fillOval((int)animX+3, (int)animY+3, 6, 6);
        }
    }

    private void drawNodes(Graphics2D g2d) {
        int size = BoardNode.SIZE;
        g2d.setFont(new Font("Arial", Font.BOLD, 18));

        for (BoardNode node : board.getNodes()) {
            int x = node.getX();
            int y = node.getY();

            Color baseColor;
            if (node.getId() % 2 == 0) baseColor = new Color(220, 230, 240);
            else baseColor = new Color(190, 210, 230);

            g2d.setColor(baseColor);
            if (node.getId() == board.getTotalNodes()) g2d.setColor(new Color(60, 180, 75));
            else if (node.getId() == 1) g2d.setColor(new Color(180, 60, 75));

            g2d.fillRect(x, y, size, size);
            g2d.setColor(Color.DARK_GRAY);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRect(x, y, size, size);

            if (board.hasScore(node.getId())) {
                int val = board.getScoreValue(node.getId());
                g2d.setColor(new Color(255, 215, 0));
                g2d.fillOval(x + 5, y + 5, 22, 22);
                g2d.setColor(Color.BLACK);
                g2d.setStroke(new BasicStroke(1));
                g2d.drawOval(x + 5, y + 5, 22, 22);
                g2d.setFont(new Font("Arial", Font.BOLD, 10));
                g2d.drawString(String.valueOf(val), x + 10, y + 20);
            }

            g2d.setFont(new Font("Arial", Font.BOLD, 18));
            String label = String.valueOf(node.getId());
            FontMetrics fm = g2d.getFontMetrics();
            g2d.setColor(Color.BLACK);
            g2d.drawString(label, x + (size - fm.stringWidth(label))/2, y + (size + fm.getAscent())/2 - 4);

            if (node.getId() % 5 == 0 && node.getId() != 1 && node.getId() != board.getTotalNodes()) {
                drawStar(g2d, x + size - 12, y + 12, 8, 4, new Color(255, 215, 0));
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

    private void drawConnectionsVisuals(Graphics2D g2d) {
        Map<Integer, Integer> connections = board.getConnections();
        double ladderWidth = 20.0;
        double rungSpacing = 15.0;

        for (Map.Entry<Integer, Integer> entry : connections.entrySet()) {
            int startId = entry.getKey();
            int endId = entry.getValue();
            BoardNode startNode = board.getNodeById(startId);
            BoardNode endNode = board.getNodeById(endId);
            if (startNode == null || endNode == null) continue;

            double x1 = startNode.getX() + BoardNode.SIZE / 2.0;
            double y1 = startNode.getY() + BoardNode.SIZE / 2.0;
            double x2 = endNode.getX() + BoardNode.SIZE / 2.0;
            double y2 = endNode.getY() + BoardNode.SIZE / 2.0;

            Color mainColor, rungColor;
            if (endId > startId) {
                mainColor = new Color(34, 139, 34, 220);
                rungColor = new Color(139, 69, 19, 220);
            } else {
                mainColor = new Color(220, 20, 60, 220);
                rungColor = new Color(255, 99, 71, 220);
            }

            double dx = x2 - x1;
            double dy = y2 - y1;
            double length = Math.sqrt(dx * dx + dy * dy);
            double ux = dx / length;
            double uy = dy / length;
            double perpX = -uy;
            double perpY = ux;
            double offsetX = perpX * (ladderWidth / 2.0);
            double offsetY = perpY * (ladderWidth / 2.0);

            g2d.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.setColor(mainColor);
            g2d.draw(new Line2D.Double(x1 - offsetX, y1 - offsetY, x2 - offsetX, y2 - offsetY));
            g2d.draw(new Line2D.Double(x1 + offsetX, y1 + offsetY, x2 + offsetX, y2 + offsetY));

            g2d.setStroke(new BasicStroke(2f));
            g2d.setColor(rungColor);
            for (double d = rungSpacing; d < length - rungSpacing/2; d += rungSpacing) {
                double midX = x1 + ux * d;
                double midY = y1 + uy * d;
                g2d.draw(new Line2D.Double(midX - offsetX, midY - offsetY, midX + offsetX, midY + offsetY));
            }

            // --- PERBAIKAN: HILANGKAN TITIK HIJAU (fillOval dihapus) ---
            // Kode g2d.fillOval(...) untuk ujung tangga sudah dihapus di sini
            // -----------------------------------------------------------
        }
    }

    private void drawPlayers(Graphics2D g2d) {
        int playerSize = 18;
        List<BoardNode> allNodes = new ArrayList<>(board.getNodes());
        if (board.getOutsideNode() != null) allNodes.add(board.getOutsideNode());

        for (BoardNode node : allNodes) {
            List<Player> players = node.getOccupyingPlayers();
            if (players == null || players.isEmpty()) continue;

            int centerX, centerY;
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

                // --- PENTING: JANGAN GAMBAR PLAYER YANG SEDANG ANIMASI ---
                // Player yang sedang animasi digambar terpisah di paintComponent agar posisinya mulus
                if (player == animatingPlayer) continue;
                // ---------------------------------------------------------

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