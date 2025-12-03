import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.Point;
import java.util.HashSet;
import java.util.Set;

class Player {
    private final String name;
    private int currentPosition;
    private final Color color;
    private final boolean isAI;
    private final String tokenImagePath; // Path ke gambar bidak

    private int turnCount = 0;
    private boolean primePowerActive = false;
    private int score = 0;

    private Point2D animatedCoordinates = new Point(0, 0);
    private Set<Integer> climbedLadders = new HashSet<>();

    // MODIFIED: Constructor baru untuk pemain manusia (dengan path gambar)
    public Player(String name, Color color, String tokenImagePath) {
        this(name, color, tokenImagePath, false);
    }

    // MODIFIED: Full Constructor
    public Player(String name, Color color, String tokenImagePath, boolean isAI) {
        this.name = name;
        this.currentPosition = 0;
        this.color = color;
        this.isAI = isAI;
        this.tokenImagePath = tokenImagePath;
        resetState();
    }

    // Reset status untuk main lagi
    public void resetState() {
        this.currentPosition = 0;
        this.turnCount = 0;
        this.primePowerActive = false;
        this.score = 0;
        this.climbedLadders.clear();
        this.animatedCoordinates = new Point(0, 0);
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

    // NEW: Getter untuk Image Path
    public String getTokenImagePath() { return tokenImagePath; }

    // BARU: Getter/Setter untuk animasi
    public Point2D getAnimatedCoordinates() { return animatedCoordinates; }
    public void setAnimatedCoordinates(Point2D coords) { this.animatedCoordinates = coords; }

    @Override
    public String toString() {
        String status = primePowerActive ? " [PRIME POWER]" : "";
        return name + (isAI ? " (AI)" : "") + " (Pos: " + currentPosition + ") [Score: " + score + "]" + status;
    }
}