import java.awt.*;

class UEdge {
    private UNode source;
    private UNode target;
    private int weight;
    private Color color;
    private boolean inPath;

    public UEdge(UNode source, UNode target, int weight) {
        this.source = source;
        this.target = target;
        this.weight = weight;
        this.color = new Color(150, 150, 150);
        this.inPath = false;
    }

    public UNode getSource() {
        return source;
    }

    public UNode getTarget() {
        return target;
    }

    public int getWeight() {
        return weight;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public boolean isInPath() {
        return inPath;
    }

    public void setInPath(boolean inPath) {
        this.inPath = inPath;
    }

    public boolean connects(UNode n1, UNode n2) {
        return (source.equals(n1) && target.equals(n2)) || (source.equals(n2) && target.equals(n1));
    }

    public void resetVisualization() {
        color = new Color(150, 150, 150);
        inPath = false;
    }
}