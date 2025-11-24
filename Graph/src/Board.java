import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

class Board {
    private final int totalNodes;
    private List<BoardNode> nodes;
    // extra outside node (id = 0) where players start before entering node 1
    private BoardNode outsideNode;
    private final int NODES_PER_ROW = 8;
    // No padding/spacing so nodes are contiguous (touching)
    private final int PADDING = 0;
    private final int SPACING = 0;

    public Board(int totalNodes) {
        this.totalNodes = totalNodes;
        this.nodes = new ArrayList<>();
        initializeBoardNodes();
        // create an "outside" node with id 0 positioned off-screen (won't be drawn)
        outsideNode = new BoardNode(0, -BoardNode.SIZE - 20, -BoardNode.SIZE - 20);
    }

    private void initializeBoardNodes() {
        int correctedNodeId = 1;
        // compute rows to fit any number of nodes (round up)
        int totalRows = (totalNodes + NODES_PER_ROW - 1) / NODES_PER_ROW;

        // Loop dari baris paling ATAS (row = 0) hingga paling BAWAH
        outer:
        for (int row = 0; row < totalRows; row++) {

            // visualRow: Menghitung baris dari perspektif GUI (0=Atas, 7=Bawah)
            int visualRow = totalRows - 1 - row;
            int nodeY = PADDING + visualRow * (BoardNode.SIZE + SPACING);

            boolean rightToLeft = (row % 2 != 0);

            for (int col = 0; col < NODES_PER_ROW; col++) {

                // stop when we've placed all nodes
                if (correctedNodeId > totalNodes) break outer;

                int nodeX;

                if (!rightToLeft) {
                    // Baris 0, 2, 4, 6 (dari atas) = Baris 7, 5, 3, 1 (dari bawah): Kiri ke Kanan
                    nodeX = PADDING + col * (BoardNode.SIZE + SPACING);
                } else {
                    // Baris 1, 3, 5, 7 (dari atas) = Baris 6, 4, 2, 0 (dari bawah): Kanan ke Kiri
                    nodeX = PADDING + (NODES_PER_ROW - 1 - col) * (BoardNode.SIZE + SPACING);
                }

                // Simpan node dengan ID berurutan 1 hingga 64
                nodes.add(new BoardNode(correctedNodeId++, nodeX, nodeY));
            }
        }
    }

    public BoardNode getNodeById(int id) {
        // id == 0 -> outside node
        if (id == 0) return outsideNode;
        if (id >= 1 && id <= totalNodes) {
            return nodes.get(id - 1);
        }
        return null;
    }

    // expose outside node for clarity if needed
    public BoardNode getOutsideNode() { return outsideNode; }

    public int getTotalNodes() { return totalNodes; }
    public List<BoardNode> getNodes() { return nodes; }
}