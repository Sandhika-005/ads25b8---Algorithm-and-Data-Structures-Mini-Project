import java.awt.Color;

class Player {
    private final String name;
    private int currentPosition; // Node ID (0 means outside, 1..N are board nodes)
    private final Color color;
    private final boolean isAI;

    // --- FITUR BARU ---
    private int turnCount = 0;           // Menghitung berapa kali pemain jalan
    private boolean autoPilotActive = false; // Status Dijkstra Permanen
    // ------------------

    public Player(String name, Color color) {
        this(name, color, false);
    }

    public Player(String name, Color color, boolean isAI) {
        this.name = name;
        this.currentPosition = 0; // start outside (node 0)
        this.color = color;
        this.isAI = isAI;
        // Reset status saat inisialisasi
        this.turnCount = 0;
        this.autoPilotActive = false;
    }

    // --- GETTERS & SETTERS BARU ---
    public int getTurnCount() { return turnCount; }
    public void incrementTurnCount() { turnCount++; }

    public boolean isAutoPilotActive() { return autoPilotActive; }
    public void setAutoPilotActive(boolean active) { this.autoPilotActive = active; }
    // ------------------------------

    public boolean isAI() { return isAI; }
    public String getName() { return name; }
    public int getCurrentPosition() { return currentPosition; }
    public void setCurrentPosition(int currentPosition) { this.currentPosition = currentPosition; }
    public Color getColor() { return color; }

    @Override
    public String toString() {
        String status = autoPilotActive ? " [AUTO-PILOT]" : "";
        return name + (isAI ? " (AI)" : "") + " (Pos: " + currentPosition + ")" + status;
    }
}