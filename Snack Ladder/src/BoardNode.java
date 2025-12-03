import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

class BoardNode {
    private final int id;
    private int x, y;
    public static int SIZE = 60;
    private final boolean isPointUp;
    private Color color;
    private List<Player> occupyingPlayers;

    public BoardNode(int id, int x, int y, boolean isPointUp) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.isPointUp = isPointUp;
        this.color = new Color(240, 240, 240);
        this.occupyingPlayers = new ArrayList<>();
    }

    public int getId() { return id; }
    public int getX() { return x; }
    public int getY() { return y; }

    // --- MODIFIKASI: Setter X dan Y DIHAPUS ---
    /* public void setX(int x) { this.x = x; } */
    /* public void setY(int y) { this.y = y; } */
    // --- AKHIR MODIFIKASI ---

    public int getSize() { return SIZE; }
    public boolean isPointUp() { return isPointUp; }
    public List<Player> getOccupyingPlayers() { return occupyingPlayers; }

    public void addPlayer(Player player) {
        if (!occupyingPlayers.contains(player)) {
            occupyingPlayers.add(player);
        }
    }

    public void removePlayer(Player player) {
        occupyingPlayers.remove(player);
    }
}