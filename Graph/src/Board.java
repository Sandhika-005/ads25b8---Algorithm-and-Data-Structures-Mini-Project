import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Collections;
import java.util.Comparator;

class Board {
    private final int totalNodes;
    private List<BoardNode> nodes;
    private BoardNode outsideNode;

    // Konstanta dimensi layar yang tersedia (sesuaikan jika ukuran jendela berubah)
    private final int MAX_BOARD_WIDTH = 750;
    private final int MAX_BOARD_HEIGHT = 750;

    // Konstanta untuk padding dan spacing yang tetap
    private final int PADDING = 20;
    private final int SPACING = 5;

    // N_SIZE akan dihitung secara dinamis di constructor
    private int N_SIZE;

    private Map<Integer, Integer> connections;
    private Map<Integer, Integer> nodeScores;

    public Board(int totalNodes) {
        this.totalNodes = totalNodes;
        this.nodes = new ArrayList<>();
        this.connections = new HashMap<>();
        this.nodeScores = new HashMap<>();

        // 1. Hitung ukuran node N_SIZE yang optimal
        calculateNodeSize();

        // 2. Init Nodes dengan tata letak PIRAMIDA
        initializeBoardNodes();

        // 3. Init Tangga & Ular (Disederhanakan)
        initializeSimpleConnections();

        // 4. Init Score (Disederhanakan)
        initializeSimpleScores();

        // Outside node disesuaikan
        outsideNode = new BoardNode(0, -N_SIZE - 20, -N_SIZE - 20, true);
    }

    // --- FUNGSI BARU: Menghitung Ukuran Node N_SIZE yang optimal ---
    private void calculateNodeSize() {
        // 1. Hitung jumlah baris R untuk piramida (R(R+1)/2 >= totalNodes)
        int R_rows = (int) Math.ceil((-1 + Math.sqrt(1 + 8 * totalNodes)) / 2.0);
        if (R_rows < 1) R_rows = 1;

        // 2. Tentukan lebar maksimum (jumlah node di baris dasar)
        int maxNodesInBase = R_rows;

        // 3. Hitung N_SIZE berdasarkan batasan Lebar (Width Constraint)
        int widthConstraint = (MAX_BOARD_WIDTH - 2 * PADDING - (maxNodesInBase - 1) * SPACING) / maxNodesInBase;

        // 4. Hitung N_SIZE berdasarkan batasan Tinggi (Height Constraint)
        int heightConstraint = (MAX_BOARD_HEIGHT - 2 * PADDING - (R_rows - 1) * SPACING) / R_rows;

        // 5. Ambil nilai N_SIZE terkecil dari kedua batasan agar piramida muat
        N_SIZE = Math.min(widthConstraint, heightConstraint);

        // N_SIZE tidak boleh terlalu kecil
        N_SIZE = Math.max(N_SIZE, 10);

        // Perbarui SIZE global di BoardNode agar BoardPanel menggunakan ukuran baru ini
        BoardNode.SIZE = N_SIZE;
    }

    // --- LOGIKA initializeBoardNodes (Bottom-Up Zigzag) ---
    private void initializeBoardNodes() {
        nodes.clear();

        // 1. Hitung jumlah baris R untuk piramida
        int R_rows = (int) Math.ceil((-1 + Math.sqrt(1 + 8 * totalNodes)) / 2.0);
        if (R_rows < 1) R_rows = 1;

        // 2. Tentukan jumlah node per baris (dari atas ke bawah: 1, 2, 3... R)
        List<Integer> nodesPerRowList = new ArrayList<>();
        int nodesLeft = totalNodes;
        int maxNodesInRow = R_rows;

        for (int r = 0; r < R_rows; r++) {
            int num = maxNodesInRow - r; // R, R-1, ..., 1 (Bottom to Top)
            if (nodesLeft > 0) {
                int actualNodes = Math.min(nodesLeft, num);
                // Tambahkan ke depan, index 0 = baris paling atas (puncak)
                nodesPerRowList.add(0, actualNodes);
                nodesLeft -= actualNodes;
            }
        }
        R_rows = nodesPerRowList.size();
        int maxRowNodes = R_rows > 0 ? nodesPerRowList.get(R_rows - 1) : 0; // Baris paling bawah (dasar)
        int maxRowWidth = maxRowNodes * N_SIZE + (maxRowNodes > 0 ? (maxRowNodes - 1) * SPACING : 0);

        int currentId = 1;

        // Loop dari baris paling bawah (visualRow R_rows - 1) ke atas (visualRow 0)
        for (int r = R_rows - 1; r >= 0; r--) {
            int numNodes = nodesPerRowList.get(r);

            // visualRow: 0 (puncak), 1, 2, ..., R_rows - 1 (dasar)
            int visualRow = r;

            // Hitung Y position
            int nodeY = PADDING + visualRow * (N_SIZE + SPACING);

            // Hitung X position (untuk memusatkan baris)
            int rowWidth = numNodes * N_SIZE + (numNodes > 0 ? (numNodes - 1) * SPACING : 0);
            int startX = PADDING + (maxRowWidth - rowWidth) / 2;

            // Arah segitiga bergantian: Dasar (visualRow R_rows-1) ke Atas
            boolean isPointUp;
            if (visualRow == R_rows - 1) {
                // Baris paling bawah (dasar) harus menunjuk ke ATAS
                isPointUp = true;
            } else {
                // Baris lainnya bergantian agar mengisi ruang dengan rapi.
                isPointUp = visualRow % 2 == (R_rows - 1) % 2;
            }

            // Logika Zigzag: Baris genap (dari bawah) ke Kanan, baris ganjil ke Kiri.
            boolean rightToLeft = (visualRow % 2 != (R_rows - 1) % 2);

            List<BoardNode> rowNodes = new ArrayList<>();
            for (int c = 0; c < numNodes; c++) {
                int nodeX = startX + c * (N_SIZE + SPACING);

                // ID sementara
                rowNodes.add(new BoardNode(currentId++, nodeX, nodeY, isPointUp));
            }

            // Terapkan pengurutan Zigzag untuk urutan ID
            if (rightToLeft) {
                Collections.reverse(rowNodes);
            }

            nodes.addAll(rowNodes);
        }

        // Perbaiki ID (sortir)
        Collections.sort(nodes, Comparator.comparingInt(BoardNode::getId));
    }

    // --- LOGIKA KONEKSI & SCORE SEDERHANA (Disimpan) ---
    private void initializeSimpleConnections() {
        connections.clear();
        // Tangga (Naik)
        connections.put(5, 15);
        connections.put(20, 35);
        connections.put(45, 60);
        // Ular (Turun)
        connections.put(30, 10);
        connections.put(55, 40);
    }

    private void initializeSimpleScores() {
        nodeScores.clear();
        Random rnd = new Random();
        int maxScoreNodes = Math.min(8, totalNodes / 5);

        for (int i = 0; i < maxScoreNodes; i++) {
            int nodeId = rnd.nextInt(totalNodes - 2) + 2;
            if (nodeScores.containsKey(nodeId)) continue;
            if (connections.containsKey(nodeId) || connections.containsValue(nodeId)) continue;

            int scoreValue = (rnd.nextInt(5) + 1) * 10;
            nodeScores.put(nodeId, scoreValue);
        }
    }

    // Metode getter dan logika score (disimpan)
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