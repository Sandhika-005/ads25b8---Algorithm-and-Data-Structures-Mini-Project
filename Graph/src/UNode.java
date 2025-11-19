import java.awt.*;

class UNode {
    private int id;
    private int x, y;
    private String label;
    private static final int RADIUS = 35;
    private Color color;
    private boolean highlighted;
    private boolean inPath;

    public UNode(int id, String label, int x, int y) {
        this.id = id;
        this.label = label;
        this.x = x;
        this.y = y;
        this.color = new Color(100, 150, 250);
        this.highlighted = false;
        this.inPath = false;
    }


    public UNode(int id, String label) {
        this(id, label, 0, 0);
    }

    public int getId() {
        return id;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getRadius() {
        return RADIUS;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public boolean isHighlighted() {
        return highlighted;
    }

    public void setHighlighted(boolean highlighted) {
        this.highlighted = highlighted;
    }

    public boolean isInPath() {
        return inPath;
    }

    public void setInPath(boolean inPath) {
        this.inPath = inPath;
    }

    public boolean contains(int px, int py) {
        int dx = px - x;
        int dy = py - y;
        return dx * dx + dy * dy <= RADIUS * RADIUS;
    }

    public void resetVisualization() {
        color = new Color(100, 150, 250);
        inPath = false;
    }
}