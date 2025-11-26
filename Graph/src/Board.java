import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

class Board {
    private final int totalNodes;
    private List<BoardNode> nodes;
    private BoardNode outsideNode;
    private final int NODES_PER_ROW = 8;
    private final int PADDING = 0;
    private final int SPACING = 0;

    // Map untuk menyimpan koneksi: Kunci = Node Asal, Nilai = Node Tujuan
    private Map<Integer, Integer> connections;

    public Board(int totalNodes) {
        this.totalNodes = totalNodes;
        this.nodes = new ArrayList<>();
        this.connections = new HashMap<>();

        initializeBoardNodes();
        initializeRandomConnections(); // Koneksi sekarang hanya akan membuat Tangga (Naik)

        outsideNode = new BoardNode(0, -BoardNode.SIZE - 20, -BoardNode.SIZE - 20);
    }

    private void initializeRandomConnections() {
        Random rnd = new Random();
        int connectionsMade = 0;

        while (connectionsMade < 5) {
            // Pilih dua node acak
            int nodeA = rnd.nextInt(totalNodes - 2) + 2;
            int nodeB = rnd.nextInt(totalNodes - 2) + 2;

            if (nodeA == nodeB) continue;

            // LOGIKA BARU: Pastikan Start < End
            // Ini menjamin koneksi selalu "Naik" (Tangga) dan visualisasinya Hijau
            int start = Math.min(nodeA, nodeB);
            int end = Math.max(nodeA, nodeB);

            if (connections.containsKey(start)) continue;
            if (connections.containsValue(start)) continue;
            if (Math.abs(start - end) < 3) continue;

            // Simpan koneksi
            connections.put(start, end);
            connectionsMade++;
        }
    }

    private void initializeBoardNodes() {
        int correctedNodeId = 1;
        int totalRows = (totalNodes + NODES_PER_ROW - 1) / NODES_PER_ROW;

        outer:
        for (int row = 0; row < totalRows; row++) {
            int visualRow = totalRows - 1 - row;
            int nodeY = PADDING + visualRow * (BoardNode.SIZE + SPACING);

            boolean rightToLeft = (row % 2 != 0);

            for (int col = 0; col < NODES_PER_ROW; col++) {
                if (correctedNodeId > totalNodes) break outer;

                int nodeX;
                if (!rightToLeft) {
                    nodeX = PADDING + col * (BoardNode.SIZE + SPACING);
                } else {
                    nodeX = PADDING + (NODES_PER_ROW - 1 - col) * (BoardNode.SIZE + SPACING);
                }

                nodes.add(new BoardNode(correctedNodeId++, nodeX, nodeY));
            }
        }
    }

    public BoardNode getNodeById(int id) {
        if (id == 0) return outsideNode;
        if (id >= 1 && id <= totalNodes) {
            return nodes.get(id - 1);
        }
        return null;
    }

    public BoardNode getOutsideNode() { return outsideNode; }
    public int getTotalNodes() { return totalNodes; }
    public List<BoardNode> getNodes() { return nodes; }
    public Map<Integer, Integer> getConnections() { return connections; }
}