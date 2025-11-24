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
        int index = 1;
        int totalRows = totalNodes / NODES_PER_ROW;

        // Pola Ular Tangga, dimulai dari bawah (Node 1)
        for (int row = 0; row < totalRows; row++) {
            int actualRow = totalRows - 1 - row;
            boolean rightToLeft = (actualRow % 2 != 0);

            for (int col = 0; col < NODES_PER_ROW; col++) {
                if (index > totalNodes) break;

                int nodeX;
                // Node 1 di baris paling bawah, sehingga Y terbesar
                int nodeY = PADDING + actualRow * (BoardNode.SIZE + 10);

                if (!rightToLeft) {
                    nodeX = PADDING + col * (BoardNode.SIZE + 10);
                } else {
                    nodeX = PADDING + (NODES_PER_ROW - 1 - col) * (BoardNode.SIZE + 10);
                }

                nodes.add(new BoardNode(index++, nodeX, nodeY));
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