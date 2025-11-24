import java.util.ArrayList;
import java.util.List;

class UnifiedGraph {
    private List<UNode> nodes;
    private List<UEdge> edges;
    private int nextNodeId = 0;

    public UnifiedGraph() {
        this.nodes = new ArrayList<>();
        this.edges = new ArrayList<>();
    }

    public UNode addNode(int x, int y) {
        // generate alphabetical label (A, B, ..., Z, AA, AB, ...)
        String label = generateAlphaLabel(nextNodeId);
        UNode newNode = new UNode(nextNodeId++, label, x, y);
        this.nodes.add(newNode);
        return newNode;
    }

    public boolean removeNode(UNode node) {
        if (nodes.remove(node)) {
            edges.removeIf(edge -> edge.getSource().equals(node) || edge.getTarget().equals(node));
            return true;
        }
        return false;
    }

    public boolean addEdge(UNode n1, UNode n2, int weight) {
        if (n1.equals(n2) || getEdge(n1, n2) != null) {
            return false;
        }

        UEdge newEdge = new UEdge(n1, n2, weight);
        this.edges.add(newEdge);
        return true;
    }

    public boolean removeEdge(UEdge edge) {
        return edges.remove(edge);
    }

    public UEdge getEdge(UNode n1, UNode n2) {
        for (UEdge edge : edges) {
            if (edge.connects(n1, n2)) {
                return edge;
            }
        }
        return null;
    }

    public List<UNode> getNodes() {
        return nodes;
    }

    public List<UEdge> getEdges() {
        return edges;
    }

    public String[] getNodeLabels() {
        return nodes.stream().map(UNode::getLabel).toArray(String[]::new);
    }

    public int getNodeIdByLabel(String label) {
        for (UNode node : nodes) {
            if (node.getLabel().equals(label)) {
                return node.getId();
            }
        }
        return -1;
    }

    public UNode getNodeById(int id) {
        for (UNode node : nodes) {
            if (node.getId() == id) {
                return node;
            }
        }
        return null;
    }

    public UNode getNodeByLabel(String label) {
        for (UNode node : nodes) {
            if (node.getLabel().equals(label)) {
                return node;
            }
        }
        return null;
    }

    public void updateNodeLabel(int nodeId, String newLabel) {
        getNodeById(nodeId).setLabel(newLabel);
    }

    private String generateAlphaLabel(int index) {
        StringBuilder sb = new StringBuilder();
        int i = index;
        while (i >= 0) {
            sb.insert(0, (char) ('A' + (i % 26)));
            i = i / 26 - 1;
        }
        return sb.toString();
    }
}