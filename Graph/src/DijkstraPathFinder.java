import java.util.*;

public class DijkstraPathFinder {

    // Mencari urutan node (jalur) dari startNode sejauh 'maxSteps' langkah
    // menggunakan logika Dijkstra pada topologi papan (termasuk Tangga/Ular)
    public static List<Integer> findShortestPathSteps(Board board, int startNodeId, int targetNodeId, int maxSteps) {
        // Graf direpresentasikan sebagai Map<NodeID, List<NeighborID>>
        // Tetangga adalah: Node+1 (jalan biasa) DAN Node Tujuan Koneksi (jika ada)

        // 1. Setup Dijkstra
        Map<Integer, Integer> dist = new HashMap<>();
        Map<Integer, Integer> prev = new HashMap<>();
        PriorityQueue<Integer> pq = new PriorityQueue<>(Comparator.comparingInt(dist::get));

        for (int i = 0; i <= board.getTotalNodes(); i++) {
            dist.put(i, Integer.MAX_VALUE);
        }

        dist.put(startNodeId, 0);
        pq.add(startNodeId);

        Set<Integer> visited = new HashSet<>();

        while (!pq.isEmpty()) {
            int u = pq.poll();

            if (u == targetNodeId) break;
            if (visited.contains(u)) continue;
            visited.add(u);

            List<Integer> neighbors = getNeighbors(board, u);

            for (int v : neighbors) {
                // Bobot antar node dianggap 1 langkah
                int newDist = dist.get(u) + 1;

                if (newDist < dist.get(v)) {
                    dist.put(v, newDist);
                    prev.put(v, u);
                    pq.add(v);
                }
            }
        }

        // 2. Rekonstruksi Jalur (Backtracking)
        List<Integer> fullPath = new ArrayList<>();
        Integer curr = targetNodeId;

        // Cek apakah target terjangkau, jika tidak cari node terdekat yang dikunjungi
        if (dist.get(targetNodeId) == Integer.MAX_VALUE) {
            // Fallback: jika tidak nemu jalan ke 64 (jarang terjadi), ambil path seadanya
            return new ArrayList<>();
        }

        while (curr != null && curr != startNodeId) {
            fullPath.add(0, curr); // Add to front
            curr = prev.get(curr);
        }

        // 3. Potong jalur sesuai jumlah dadu (maxSteps)
        List<Integer> stepsToTake = new ArrayList<>();
        int steps = 0;
        for (Integer node : fullPath) {
            if (steps >= maxSteps) break;
            stepsToTake.add(node);
            steps++;
        }

        return stepsToTake;
    }

    private static List<Integer> getNeighbors(Board board, int nodeId) {
        List<Integer> neighbors = new ArrayList<>();
        int totalNodes = board.getTotalNodes();

        // Tetangga 1: Jalan Biasa (Maju 1 langkah)
        if (nodeId + 1 <= totalNodes) {
            neighbors.add(nodeId + 1);
        }

        // Tetangga 2: Lewat Jalur Koneksi (Tangga/Ular)
        // Dalam mode "Shortest Path", koneksi dianggap sebagai EDGE yang valid
        if (board.getConnections().containsKey(nodeId)) {
            neighbors.add(board.getConnections().get(nodeId));
        }

        return neighbors;
    }
}