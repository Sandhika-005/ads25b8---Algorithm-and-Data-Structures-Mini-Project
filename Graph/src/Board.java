import java.awt.geom.Line2D;
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

    private Map<Integer, Integer> connections;
    private Map<Integer, Integer> nodeScores;

    public Board(int totalNodes) {
        this.totalNodes = totalNodes;
        this.nodes = new ArrayList<>();
        this.connections = new HashMap<>();
        this.nodeScores = new HashMap<>();

        // 1. Init Nodes dulu agar kita punya koordinat X,Y untuk cek visual
        initializeBoardNodes();

        // 2. Init Tangga dengan aturan khusus & cek silang
        initializeSpecificLadders();

        // 3. Init Score
        initializeRandomScores();

        outsideNode = new BoardNode(0, -BoardNode.SIZE - 20, -BoardNode.SIZE - 20);
    }

    // --- LOGIKA UTAMA: 3 VERTIKAL, 2 DIAGONAL, TANPA SILANG ---
    private void initializeSpecificLadders() {
        Random rnd = new Random();
        connections.clear();

        // --- STEP 1: 3 TANGGA VERTIKAL (Lurus ke atas) ---
        int verticalCount = 0;
        int attempts = 0;
        while (verticalCount < 3 && attempts < 2000) {
            attempts++;
            // Pilih start node (hindari baris paling atas)
            int startId = rnd.nextInt(totalNodes - NODES_PER_ROW - 1) + 2;

            // Hitung posisi visual X start node
            int startRow = (startId - 1) / NODES_PER_ROW;
            int startColIdx = (startId - 1) % NODES_PER_ROW;
            // Jika row genap (0,2..) arah kiri->kanan. Jika ganjil (1,3..) kanan->kiri.
            int visualX = (startRow % 2 == 0) ? startColIdx : (NODES_PER_ROW - 1 - startColIdx);

            // Tentukan target row (naik 1 - 3 baris)
            int rowsUp = rnd.nextInt(3) + 1;
            int endRow = startRow + rowsUp;

            if (endRow * NODES_PER_ROW >= totalNodes) continue;

            // Cari ID di endRow yang memiliki visualX SAMA (Vertikal)
            int endColIdx = (endRow % 2 == 0) ? visualX : (NODES_PER_ROW - 1 - visualX);
            int endId = endRow * NODES_PER_ROW + endColIdx + 1;

            if (endId > totalNodes) continue;

            // Validasi Node & Crossing
            if (isValidConnection(startId, endId)) {
                connections.put(startId, endId);
                verticalCount++;
            }
        }

        // --- STEP 2: 2 TANGGA DIAGONAL (Miring, bukan horizontal) ---
        int diagonalCount = 0;
        attempts = 0;
        while (diagonalCount < 2 && attempts < 2000) {
            attempts++;
            // Start bebas
            int startId = rnd.nextInt(totalNodes - NODES_PER_ROW - 1) + 2;
            int startRow = (startId - 1) / NODES_PER_ROW;

            // Cari End Node
            // Syarat Diagonal: Beda Baris (rowsUp > 0) DAN Beda Kolom Visual
            int rowsUp = rnd.nextInt(3) + 1; // Naik 1-3 baris
            int endRow = startRow + rowsUp;

            if (endRow * NODES_PER_ROW >= totalNodes) continue;

            // Random kolom di baris tujuan
            int endColIdx = rnd.nextInt(NODES_PER_ROW);
            int endId = endRow * NODES_PER_ROW + endColIdx + 1;

            if (endId > totalNodes) continue;

            // Cek apakah benar diagonal (Visual X tidak boleh sama)
            if (getVisualX(startId) == getVisualX(endId)) continue;

            // Validasi Node & Crossing
            if (isValidConnection(startId, endId)) {
                connections.put(startId, endId);
                diagonalCount++;
            }
        }
    }

    // Helper untuk cek validitas koneksi
    private boolean isValidConnection(int startId, int endId) {
        // 1. Cek Node Sibuk (sudah dipakai)
        if (isNodeBusy(startId) || isNodeBusy(endId)) return false;

        // 2. Cek Intersection (Silang) dengan tangga yang SUDAH ADA
        BoardNode p1 = getNodeById(startId);
        BoardNode p2 = getNodeById(endId);

        for (Map.Entry<Integer, Integer> entry : connections.entrySet()) {
            BoardNode p3 = getNodeById(entry.getKey());
            BoardNode p4 = getNodeById(entry.getValue());

            // Cek apakah garis (p1-p2) memotong garis (p3-p4)
            if (Line2D.linesIntersect(p1.getX(), p1.getY(), p2.getX(), p2.getY(),
                    p3.getX(), p3.getY(), p4.getX(), p4.getY())) {
                return false; // Ada silangan!
            }
        }

        return true;
    }

    private int getVisualX(int nodeId) {
        int row = (nodeId - 1) / NODES_PER_ROW;
        int col = (nodeId - 1) % NODES_PER_ROW;
        return (row % 2 == 0) ? col : (NODES_PER_ROW - 1 - col);
    }

    private boolean isNodeBusy(int id) {
        // Cek apakah node sudah dipakai sebagai start atau end koneksi lain
        return connections.containsKey(id) || connections.containsValue(id) || id == 1 || id == totalNodes;
    }
    // ----------------------------------------------

    private void initializeRandomScores() {
        Random rnd = new Random();
        int scoreCount = 0;
        int maxScoreNodes = 8;

        while (scoreCount < maxScoreNodes) {
            int nodeId = rnd.nextInt(totalNodes - 2) + 2;
            if (nodeScores.containsKey(nodeId)) continue;
            // Jangan taruh koin di node yang ada tangga/ular agar visual tidak numpuk
            if (connections.containsKey(nodeId) || connections.containsValue(nodeId)) continue;

            int scoreValue = (rnd.nextInt(5) + 1) * 10;
            nodeScores.put(nodeId, scoreValue);
            scoreCount++;
        }
    }

    public int collectScore(int nodeId) {
        if (nodeScores.containsKey(nodeId)) {
            int score = nodeScores.get(nodeId);
            nodeScores.remove(nodeId);
            return score;
        }
        return 0;
    }

    public boolean hasScore(int nodeId) {
        return nodeScores.containsKey(nodeId);
    }

    public int getScoreValue(int nodeId) {
        return nodeScores.getOrDefault(nodeId, 0);
    }

    // Dipanggil untuk inisialisasi node (X, Y)
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
                if (!rightToLeft) nodeX = PADDING + col * (BoardNode.SIZE + SPACING);
                else nodeX = PADDING + (NODES_PER_ROW - 1 - col) * (BoardNode.SIZE + SPACING);
                nodes.add(new BoardNode(correctedNodeId++, nodeX, nodeY));
            }
        }
    }

    public BoardNode getNodeById(int id) {
        if (id == 0) return outsideNode;
        if (id >= 1 && id <= totalNodes) return nodes.get(id - 1);
        return null;
    }
    public BoardNode getOutsideNode() { return outsideNode; }
    public int getTotalNodes() { return totalNodes; }
    public List<BoardNode> getNodes() { return nodes; }
    public Map<Integer, Integer> getConnections() { return connections; }
}