import java.util.ArrayList;
import java.util.List;

class Board {
    private final int totalNodes;
    private List<BoardNode> nodes;
    private final int NODES_PER_ROW = 8;
    private final int PADDING = 70;

    public Board(int totalNodes) {
        this.totalNodes = totalNodes;
        this.nodes = new ArrayList<>();
        initializeBoardNodes();
    }

    private void initializeBoardNodes() {
        int totalRows = totalNodes / NODES_PER_ROW;
        int nodeId = 1;

        // Baris pertama kekanan (kiri ke kanan), kedua kekiri (kanan ke kiri), dst
        for (int row = 0; row < totalRows; row++) {
            int actualRow = totalRows - 1 - row;
            boolean leftToRight = (row % 2 == 0); // baris genap (mulai dari 0) kekanan

            for (int col = 0; col < NODES_PER_ROW; col++) {
                if (nodeId > totalNodes) break;

                int nodeX;
                int nodeY = PADDING + actualRow * (BoardNode.SIZE + 10);

                if (leftToRight) {
                    nodeX = PADDING + col * (BoardNode.SIZE + 10);
                } else {
                    nodeX = PADDING + (NODES_PER_ROW - 1 - col) * (BoardNode.SIZE + 10);
                }

                nodes.add(new BoardNode(nodeId++, nodeX, nodeY));
            }
        }
    }

    public BoardNode getNodeById(int id) {
        if (id >= 1 && id <= totalNodes) {
            return nodes.get(id - 1);
        }
        return null;
    }

    public int getTotalNodes() { return totalNodes; }
    public List<BoardNode> getNodes() { return nodes; }
}