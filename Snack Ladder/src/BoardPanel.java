import javax.swing.*;
import java.awt.*;
import java.awt.geom.Line2D;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;

class BoardPanel extends JPanel {
    private final Board board;
    private final GameEngine gameEngine;

    private BufferedImage backgroundImage;
    private final String BG_IMAGE_RESOURCE_PATH = "Gambar/Desain tanpa judul.jpg";
    private final Color COLOR_BG = new Color(45, 60, 80); // Warna Dasar yang Seragam

    private Player animatingPlayer = null;
    private double animX, animY;
    private double targetX, targetY;
    private double startX, startY;
    private long animStartTime;
    private long animDuration;
    private Runnable onAnimComplete;
    private Timer animTimer;

    public BoardPanel(Board board, GameEngine gameEngine) {
        this.board = board;
        this.gameEngine = gameEngine;
        // setBackground dipertahankan untuk komponen, tetapi kita akan menggambar BG secara manual
        setBackground(new Color(30, 40, 60));

        try (InputStream is = BoardPanel.class.getResourceAsStream(BG_IMAGE_RESOURCE_PATH)) {
            if (is != null) {
                backgroundImage = ImageIO.read(is);
                setOpaque(true);
            } else {
                System.err.println("Gagal menemukan resource: " + BG_IMAGE_RESOURCE_PATH + ". Pastikan file ada di direktori source root.");
                backgroundImage = null;
            }
        } catch (IOException e) {
            System.err.println("Error saat membaca gambar: " + BG_IMAGE_RESOURCE_PATH + ". Menggunakan warna solid.");
            e.printStackTrace();
            backgroundImage = null;
        }

        animTimer = new Timer(16, e -> updateAnimation());
    }

    // ... (metode getCentroid, animatePlayerMovement, updateAnimation) ...

    private Point getCentroid(BoardNode node) {
        int size = BoardNode.SIZE;
        int centerX = node.getX() + size / 2;
        int centroidY = node.getY() + size / 2;
        return new Point(centerX, centroidY);
    }

    public void animatePlayerMovement(Player player, BoardNode startNode, BoardNode endNode, long durationMs, Runnable onComplete) {
        this.animatingPlayer = player;

        Point startCenter = getCentroid(startNode);
        Point targetCenter = getCentroid(endNode);

        double playerRadius = 9;

        this.startX = startCenter.x - playerRadius;
        this.startY = startCenter.y - playerRadius;
        this.targetX = targetCenter.x - playerRadius;
        this.targetY = targetCenter.y - playerRadius;

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
            progress = 1.0;
            animX = targetX;
            animY = targetY;
            animTimer.stop();

            Runnable callback = onAnimComplete;
            animatingPlayer = null;
            repaint();

            if (callback != null) callback.run();
        } else {
            double t = progress * progress * (3 - 2 * progress);

            animX = startX + (targetX - startX) * t;
            animY = startY + (targetY - startY) * t;
            repaint();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();

        int panelW = getWidth();
        int panelH = getHeight();

        // --- MODIFIKASI: 1. Bersihkan seluruh area panel dengan warna seragam ---
        // Ini memastikan tidak ada kebocoran warna dari panel lain yang terlihat.
        g2d.setColor(COLOR_BG);
        g2d.fillRect(0, 0, panelW, panelH);
        // --- AKHIR MODIFIKASI 1 ---

        // --- 2. MENGGAMBAR BACKGROUND DENGAN LOGIKA CONTAIN (Scale to Fit) ---
        if (backgroundImage != null) {
            int imageW = backgroundImage.getWidth();
            int imageH = backgroundImage.getHeight();

            double panelRatio = (double) panelW / panelH;
            double imageRatio = (double) imageW / imageH;

            int drawW, drawH;
            int drawX, drawY;

            if (imageRatio > panelRatio) {
                drawW = panelW;
                drawH = (int) (drawW / imageRatio);
                drawX = 0;
                drawY = (panelH - drawH) / 2;
            } else {
                drawH = panelH;
                drawW = (int) (imageRatio * drawH);
                drawX = (panelW - drawW) / 2;
                drawY = 0;
            }

            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY);

            g2d.drawImage(backgroundImage, drawX, drawY, drawW, drawH, this);
        }

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawNodes(g2d);
        drawConnectionsVisuals(g2d);
        drawPlayers(g2d);

        g2d.dispose();
    }

    // ... (rest of the drawing methods remain the same) ...

    private void drawNodes(Graphics2D g2d) {
        int size = BoardNode.SIZE;

        for (BoardNode node : board.getNodes()) {
            int x = node.getX();
            int y = node.getY();

            Color baseColor;
            if (node.getId() % 2 == 0) baseColor = new Color(220, 230, 240);
            else baseColor = new Color(190, 210, 230);

            g2d.setColor(baseColor);
            if (node.getId() == board.getTotalNodes()) g2d.setColor(new Color(60, 180, 75));
            else if (node.getId() == 1) g2d.setColor(new Color(180, 60, 75));

            g2d.fillOval(x, y, size, size);
            g2d.setColor(Color.DARK_GRAY);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawOval(x, y, size, size);

            Point center = getCentroid(node);
            int contentCenterX = center.x;
            int centroidY = center.y;

            int idFontSize = (size > 40) ? 18 : (size > 25 ? 14 : 10);
            int scoreOffset = (size > 40) ? 18 : (size / 3);

            g2d.setFont(new Font("Arial", Font.BOLD, idFontSize));
            String label = String.valueOf(node.getId());
            FontMetrics fm = g2d.getFontMetrics();
            g2d.setColor(Color.BLACK);

            int idDrawY = centroidY;
            g2d.drawString(label, contentCenterX - fm.stringWidth(label) / 2, idDrawY + fm.getAscent() / 2 - 4);

            if (board.hasScore(node.getId())) {
                int val = board.getScoreValue(node.getId());
                int ovalSize = Math.max(16, (int)(size * 0.35));
                int margin = Math.max(5, (int)(size * 0.1));

                int ovalX = (x + size) - ovalSize - margin;
                int ovalY = y + margin;

                g2d.setColor(new Color(255, 215, 0));
                g2d.fillOval(ovalX, ovalY, ovalSize, ovalSize);
                g2d.setColor(Color.BLACK);
                g2d.setStroke(new BasicStroke(1));
                g2d.drawOval(ovalX, ovalY, ovalSize, ovalSize);

                int scoreFontSize = Math.max(8, ovalSize - 12);
                g2d.setFont(new Font("Arial", Font.BOLD, scoreFontSize));
                String scoreLabel = String.valueOf(val);
                FontMetrics fmScore = g2d.getFontMetrics();
                g2d.drawString(scoreLabel, ovalX + (ovalSize - fmScore.stringWidth(scoreLabel)) / 2, ovalY + (ovalSize + fmScore.getAscent()) / 2 - 2);
            }

            if (node.getId() % 5 == 0 && node.getId() != board.getTotalNodes()) {
                int starR = Math.max(4, (int)(size * 0.1));

                int starX = contentCenterX - (size / 4);
                int starY = centroidY - scoreOffset;

                drawStar(g2d, starX, starY, starR, starR / 2, new Color(255, 215, 0));
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

        // Define stroke for dashed line
        float[] dashPattern = {10f, 5f}; // 10px solid, 5px space
        Stroke dashedStroke = new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10.0f, dashPattern, 0.0f);

        for (Map.Entry<Integer, Integer> entry : connections.entrySet()) {
            int startId = entry.getKey();
            int endId = entry.getValue();
            BoardNode startNode = board.getNodeById(startId);
            BoardNode endNode = board.getNodeById(endId);
            if (startNode == null || endNode == null) continue;

            Point startCenter = getCentroid(startNode);
            Point endCenter = getCentroid(endNode);

            double x1 = startCenter.x;
            double y1 = startCenter.y;
            double x2 = endCenter.x;
            double y2 = endCenter.y;

            Color mainColor;
            if (endId > startId) {
                // Tangga (Naik)
                mainColor = new Color(34, 139, 34, 220); // Hijau
            } else {
                // Ular (Turun)
                mainColor = new Color(220, 20, 60, 220); // Merah
            }

            g2d.setColor(mainColor);
            g2d.setStroke(dashedStroke); // Gunakan garis putus-putus

            // Gambar Garis Utama
            g2d.draw(new Line2D.Double(x1, y1, x2, y2));
        }
    }

    private void drawPlayers(Graphics2D g2d) {
        int playerSize = 18;
        List<BoardNode> allNodes = new ArrayList<>(board.getNodes());
        if (board.getOutsideNode() != null) allNodes.add(board.getOutsideNode());

        int size = BoardNode.SIZE;

        for (BoardNode node : allNodes) {
            List<Player> players = node.getOccupyingPlayers();
            if (players == null || players.isEmpty()) continue;

            Point center = getCentroid(node);
            int centerX = center.x;
            int centerY = center.y;

            if (node.getId() == 0) {
                BoardNode n1 = board.getNodeById(1);
                if (n1 != null) {
                    centerX = n1.getX() - BoardNode.SIZE/2 - 20;
                    centerY = n1.getY() + BoardNode.SIZE/2;
                } else {
                    centerX = Math.max(20, node.getX() + node.getSize() / 2);
                    centerY = Math.max(20, node.getY() + node.getSize() / 2);
                }
            }

            int n = players.size();
            int maxRadius = size / 2 - playerSize/2 - 4;
            int radius = Math.max( (n <= 1 ? 0 : Math.min(maxRadius,  (playerSize + 6) * n / 2 )), 10);
            radius = Math.min(radius, (int)(size * 0.25));

            for (int i = 0; i < n; i++) {
                Player player = players.get(i);

                if (player == animatingPlayer) continue;

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