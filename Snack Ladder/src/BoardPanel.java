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

    // --- Variabel untuk Gambar Latar Belakang ---
    private BufferedImage backgroundImage;
    private final String BG_IMAGE_RESOURCE_PATH = "Gambar/back3.jpg";
    // ---------------------------------------------------

    // --- VARIABEL ANIMASI ---
    private Player animatingPlayer = null;
    private double animX, animY;
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

        // --- Memuat Gambar Latar Belakang menggunakan ClassLoader ---
        try (InputStream is = BoardPanel.class.getResourceAsStream(BG_IMAGE_RESOURCE_PATH)) {
            if (is != null) {
                backgroundImage = ImageIO.read(is);
                setOpaque(true);
            } else {
                System.err.println("Gagal menemukan resource: " + BG_IMAGE_RESOURCE_PATH + ". Pastikan folder 'Gambar' ada di direktori source root.");
                backgroundImage = null;
            }
        } catch (IOException e) {
            System.err.println("Error saat membaca gambar: " + BG_IMAGE_RESOURCE_PATH + ". Menggunakan warna solid.");
            e.printStackTrace();
            backgroundImage = null;
        }
        // ------------------------------------------

        animTimer = new Timer(16, e -> updateAnimation());
    }

    // --- HELPER: Menghitung Centroid Node ---
    private Point getCentroid(BoardNode node) {
        int size = BoardNode.SIZE;
        int centerX = node.getX() + size / 2;
        int centroidY;

        if (node.isPointUp()) {
            centroidY = (int) (node.getY() + size * 0.60);
        } else {
            centroidY = (int) (node.getY() + size * 0.40);
        }
        return new Point(centerX, centroidY);
    }

    // --- METHOD ANIMASI GERAK (Menggunakan Centroid) ---
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
        Graphics2D g2d = (Graphics2D) g;

        // --- MENGGAMBAR BACKGROUND DENGAN LOGIKA PEMOTONGAN DAN KUALITAS TINGGI ---
        if (backgroundImage != null) {
            int panelW = getWidth();
            int panelH = getHeight();
            int imageW = backgroundImage.getWidth();
            int imageH = backgroundImage.getHeight();

            double panelRatio = (double) panelW / panelH;
            double imageRatio = (double) imageW / imageH;

            int drawW, drawH;
            int drawX, drawY;

            if (imageRatio > panelRatio) {
                drawH = panelH;
                drawW = (int) (imageRatio * drawH);
                drawX = (panelW - drawW) / 2; // Pusatkan secara horizontal
                drawY = 0;
            } else {
                drawW = panelW;
                drawH = (int) (drawW / imageRatio);
                drawY = (panelH - drawH) / 2; // Pusatkan secara vertikal
                drawX = 0;
            }

            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY);

            g2d.drawImage(backgroundImage, drawX, drawY, drawW, drawH, this);
        } else {
            super.paintComponent(g);
        }
        // ---------------------------------------------

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawNodes(g2d);
        drawConnectionsVisuals(g2d);
        drawPlayers(g2d);

        if (animatingPlayer != null) {
            int playerSize = 18;
            g2d.setColor(animatingPlayer.getColor());
            g2d.fillOval((int)animX, (int)animY, playerSize, playerSize);
            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawOval((int)animX, (int)animY, playerSize, playerSize);

            g2d.setColor(new Color(255,255,255,100));
            g2d.fillOval((int)animX+3, (int)animY+3, 6, 6);
        }
    }

    private void drawNodes(Graphics2D g2d) {
        int size = BoardNode.SIZE;

        for (BoardNode node : board.getNodes()) {
            int x = node.getX();
            int y = node.getY();
            boolean isPointUp = node.isPointUp();

            // --- Hitung Vertex Segitiga ---
            int[] xs = new int[3];
            int[] ys = new int[3];

            if (isPointUp) {
                xs[0] = x + size / 2; ys[0] = y;
                xs[1] = x;            ys[1] = y + size;
                xs[2] = x + size;     ys[2] = y + size;
            } else {
                xs[0] = x;            ys[0] = y;
                xs[1] = x + size;     ys[1] = y;
                xs[2] = x + size / 2; ys[2] = y + size;
            }
            Polygon triangle = new Polygon(xs, ys, 3);

            Color baseColor;
            if (node.getId() % 2 == 0) baseColor = new Color(220, 230, 240);
            else baseColor = new Color(190, 210, 230);

            g2d.setColor(baseColor);
            if (node.getId() == board.getTotalNodes()) g2d.setColor(new Color(60, 180, 75));
            else if (node.getId() == 1) g2d.setColor(new Color(180, 60, 75));

            g2d.fill(triangle);
            g2d.setColor(Color.DARK_GRAY);
            g2d.setStroke(new BasicStroke(2));
            g2d.draw(triangle);

            Point center = getCentroid(node);
            int contentCenterX = center.x;
            int centroidY = center.y;

            int idFontSize = (size > 40) ? 18 : (size > 25 ? 14 : 10);

            int scoreOffset = (size > 40) ? 18 : (size / 3);

            // --- Gambar Node ID (Angka) ---
            g2d.setFont(new Font("Arial", Font.BOLD, idFontSize));
            String label = String.valueOf(node.getId());
            FontMetrics fm = g2d.getFontMetrics();
            g2d.setColor(Color.BLACK);

            // Posisikan ID TEPAT di pusat Centroid (idDrawY)
            int idDrawY = centroidY;
            g2d.drawString(label, contentCenterX - fm.stringWidth(label) / 2, idDrawY + fm.getAscent() / 2 - 4);

            // --- Gambar Skor (Oval) ---
            if (board.hasScore(node.getId())) {
                int val = board.getScoreValue(node.getId());
                int ovalSize = Math.max(16, (int)(size * 0.35));
                int margin = Math.max(5, (int)(size * 0.1));

                // --- POSISI BARU: Sudut Kanan Atas (Absolute to Bounding Box) ---
                int ovalX = (x + size) - ovalSize - margin; // Kanan Bounding Box - ukuran oval - margin
                int ovalY = y + margin;                     // Atas Bounding Box + margin
                // -----------------------------------------------------------------

                g2d.setColor(new Color(255, 215, 0));
                g2d.fillOval(ovalX, ovalY, ovalSize, ovalSize);
                g2d.setColor(Color.BLACK);
                g2d.setStroke(new BasicStroke(1));
                g2d.drawOval(ovalX, ovalY, ovalSize, ovalSize);

                // Gambar teks skor
                int scoreFontSize = Math.max(8, ovalSize - 12);
                g2d.setFont(new Font("Arial", Font.BOLD, scoreFontSize));
                String scoreLabel = String.valueOf(val);
                FontMetrics fmScore = g2d.getFontMetrics();
                g2d.drawString(scoreLabel, ovalX + (ovalSize - fmScore.stringWidth(scoreLabel)) / 2, ovalY + (ovalSize + fmScore.getAscent()) / 2 - 2);
            }

            // --- Gambar Bintang ---
            if (node.getId() % 5 == 0 && node.getId() != board.getTotalNodes()) {
                int starR = Math.max(4, (int)(size * 0.1));

                // Posisikan bintang di pojok Kiri Atas (jauh dari skor di Kanan Atas)
                int starX = contentCenterX - (size / 4);
                int starY;

                if (isPointUp) {
                    starY = centroidY - scoreOffset;
                } else {
                    starY = centroidY + scoreOffset;
                }

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

    /**
     * DIKEMBALIKAN: Menggambar koneksi sebagai struktur tangga dengan rel samping dan anak tangga.
     */
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

            Point startCenter = getCentroid(startNode);
            Point endCenter = getCentroid(endNode);

            double x1 = startCenter.x;
            double y1 = startCenter.y;
            double x2 = endCenter.x;
            double y2 = endCenter.y;

            Color mainColor, rungColor;
            if (endId > startId) {
                // Tangga (Naik)
                mainColor = new Color(34, 139, 34, 220); // Hijau untuk tangga naik
                rungColor = new Color(139, 69, 19, 220); // Coklat untuk anak tangga
            } else {
                // Ular (Turun) - Meskipun logika di GameEngine sudah dihapus, visual tetap ada
                mainColor = new Color(220, 20, 60, 220); // Merah untuk ular/turun
                rungColor = new Color(255, 99, 71, 220); // Merah muda untuk anak tangga
            }

            double dx = x2 - x1;
            double dy = y2 - y1;
            double length = Math.sqrt(dx * dx + dy * dy);

            if (length == 0) continue;

            double ux = dx / length;
            double uy = dy / length;
            double perpX = -uy;
            double perpY = ux;
            double offsetX = perpX * (ladderWidth / 2.0);
            double offsetY = perpY * (ladderWidth / 2.0);

            // 1. Gambar dua rel samping
            g2d.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.setColor(mainColor);
            g2d.draw(new Line2D.Double(x1 - offsetX, y1 - offsetY, x2 - offsetX, y2 - offsetY));
            g2d.draw(new Line2D.Double(x1 + offsetX, y1 + offsetY, x2 + offsetX, y2 + offsetY));

            // 2. Gambar anak tangga
            g2d.setStroke(new BasicStroke(2f));
            g2d.setColor(rungColor);
            for (double d = rungSpacing; d < length - rungSpacing/2; d += rungSpacing) {
                double midX = x1 + ux * d;
                double midY = y1 + uy * d;
                g2d.draw(new Line2D.Double(midX - offsetX, midY - offsetY, midX + offsetX, midY + offsetY));
            }
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