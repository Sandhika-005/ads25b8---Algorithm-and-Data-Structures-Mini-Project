import java.awt.Color;

class Player {
    private final String name;
    private int currentPosition; // Node ID (1 to 64)
    private final Color color;

    public Player(String name, Color color) {
        this.name = name;
        this.currentPosition = 1; // Mulai dari node 1
        this.color = color;
    }

    public String getName() { return name; }
    public int getCurrentPosition() { return currentPosition; }
    public void setCurrentPosition(int currentPosition) { this.currentPosition = currentPosition; }
    public Color getColor() { return color; }

    @Override
    public String toString() {
        return name + " (Pos: " + currentPosition + ")";
    }
}