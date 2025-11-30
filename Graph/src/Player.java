import java.awt.Color;
import java.awt.geom.Point2D; // Import yang diperlukan
import java.awt.Point; // Import yang diperlukan
import java.util.HashSet;
import java.util.Set;

class Player {
    private final String name;
    private int currentPosition;
    private final Color color;
    private final boolean isAI;

    private int turnCount = 0;
    private boolean primePowerActive = false;
    private int score = 0;

    // BARU: Variabel untuk animasi (koordinat sementara)
    private Point2D animatedCoordinates = new Point(0, 0);

    // Set untuk mencatat tangga mana yang pernah dinaiki (Node Tujuan Tangga)
    private Set<Integer> climbedLadders = new HashSet<>();

    public Player(String name, Color color) {
        this(name, color, false);
    }

    public Player(String name, Color color, boolean isAI) {
        this.name = name;
        this.currentPosition = 0;
        this.color = color;
        this.isAI = isAI;
        resetState();
    }

    // Reset status untuk main lagi
    public void resetState() {
        this.currentPosition = 0;
        this.turnCount = 0;
        this.primePowerActive = false;
        this.score = 0;
        this.climbedLadders.clear();
        this.animatedCoordinates = new Point(0, 0); // Reset animasi
    }

    public void addClimbedLadder(int ladderEndNodeId) {
        climbedLadders.add(ladderEndNodeId);
    }

    public boolean hasClimbedLadder(int ladderEndNodeId) {
        return climbedLadders.contains(ladderEndNodeId);
    }

    public int getScore() { return score; }
    public void addScore(int points) { this.score += points; }

    public int getTurnCount() { return turnCount; }
    public void incrementTurnCount() { turnCount++; }

    public boolean isPrimePowerActive() { return primePowerActive; }
    public void setPrimePowerActive(boolean active) { this.primePowerActive = active; }

    public boolean isAI() { return isAI; }
    public String getName() { return name; }
    public int getCurrentPosition() { return currentPosition; }
    public void setCurrentPosition(int currentPosition) { this.currentPosition = currentPosition; }
    public Color getColor() { return color; }

    // BARU: Getter/Setter untuk animasi
    public Point2D getAnimatedCoordinates() { return animatedCoordinates; }
    public void setAnimatedCoordinates(Point2D coords) { this.animatedCoordinates = coords; }

    @Override
    public String toString() {
        String status = primePowerActive ? " [PRIME POWER]" : "";
        return name + (isAI ? " (AI)" : "") + " (Pos: " + currentPosition + ") [Score: " + score + "]" + status;
    }
}