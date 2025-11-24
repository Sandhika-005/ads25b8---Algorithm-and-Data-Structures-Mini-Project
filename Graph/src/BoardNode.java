import java.awt.*;
import java.util.ArrayList;
import java.util.List;

class BoardNode {
    private final int id;
    private int x, y;
    public static final int SIZE = 60;
    private Color color;
    private List<Player> occupyingPlayers;

    public BoardNode(int id, int x, int y) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.color = new Color(240, 240, 240);
        this.occupyingPlayers = new ArrayList<>();
    }

    // Getters and Setters
    public int getId() { return id; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getSize() { return SIZE; }
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