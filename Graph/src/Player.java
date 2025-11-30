import java.awt.Color;

class Player {
    private final String name;
    private int currentPosition; // Node ID (0 means outside, 1..N are board nodes)
    private final Color color;
    private final boolean isAI;

    // --- FITUR BARU ---
    private int turnCount = 0;
    // Mengganti autoPilotActive menjadi primePowerActive
    // Status ini menentukan apakah pemain punya "kekuatan" di turn ini (bisa naik tangga & cari jalan pintas)
    private boolean primePowerActive = false;
    // ------------------

    public Player(String name, Color color) {
        this(name, color, false);
    }

    public Player(String name, Color color, boolean isAI) {
        this.name = name;
        this.currentPosition = 0; // start outside (node 0)
        this.color = color;
        this.isAI = isAI;
        this.turnCount = 0;
        this.primePowerActive = false;
    }

    public int getTurnCount() { return turnCount; }
    public void incrementTurnCount() { turnCount++; }

    // Getter & Setter untuk Prime Power
    public boolean isPrimePowerActive() { return primePowerActive; }
    public void setPrimePowerActive(boolean active) { this.primePowerActive = active; }

    public boolean isAI() { return isAI; }
    public String getName() { return name; }
    public int getCurrentPosition() { return currentPosition; }
    public void setCurrentPosition(int currentPosition) { this.currentPosition = currentPosition; }
    public Color getColor() { return color; }

    @Override
    public String toString() {
        // Tampilkan status POWER jika aktif
        String status = primePowerActive ? " [PRIME POWER]" : "";
        return name + (isAI ? " (AI)" : "") + " (Pos: " + currentPosition + ")" + status;
    }
}