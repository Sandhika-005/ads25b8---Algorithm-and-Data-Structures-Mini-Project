import java.awt.Color;

class Player {
    private final String name;
    private int currentPosition; // Node ID (1 to 64)
    private final Color color;
    private final boolean isAI;

    public Player(String name, Color color) {
        this(name, color, false);
    }

    // new constructor to mark AI players
    public Player(String name, Color color, boolean isAI) {
        this.name = name;
        this.currentPosition = 1; // Mulai dari node 1
        this.color = color;
        this.isAI = isAI;
    }

    public boolean isAI() { return isAI; }

    public String getName() { return name; }
    public int getCurrentPosition() { return currentPosition; }
    public void setCurrentPosition(int currentPosition) { this.currentPosition = currentPosition; }
    public Color getColor() { return color; }

    @Override
    public String toString() {
        return name + (isAI ? " (AI)" : "") + " (Pos: " + currentPosition + ")";
    }
}