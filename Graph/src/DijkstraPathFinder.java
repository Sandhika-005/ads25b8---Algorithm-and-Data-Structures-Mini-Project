import java.util.*;

public class DijkstraPathFinder {

    public static List<Integer> findShortestPathSteps(Board board, int startNodeId, int targetNodeId, int maxSteps) {
        // 1. Jalankan Dijkstra dari Start Node ke SELURUH papan
        Map<Integer, Integer> dist = new HashMap<>();
        Map<Integer, Integer> prev = new HashMap<>();
        runDijkstra(board, startNodeId, dist, prev);

        // 2. Ambil jalur terpendek ke Target Utama (Finish / Node 64)
        List<Integer> fullPath = reconstructPath(prev, startNodeId, targetNodeId);

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

    // --- Helper Methods ---

    private static void runDijkstra(Board board, int startNodeId, Map<Integer, Integer> dist, Map<Integer, Integer> prev) {
        PriorityQueue<Integer> pq = new PriorityQueue<>(Comparator.comparingInt(dist::get));

        // Inisialisasi jarak
        for (int i = 0; i <= board.getTotalNodes(); i++) {
            dist.put(i, Integer.MAX_VALUE);
        }

        dist.put(startNodeId, 0);
        pq.add(startNodeId);
        Set<Integer> visited = new HashSet<>();

        while (!pq.isEmpty()) {
            int u = pq.poll();
            if (visited.contains(u)) continue;
            visited.add(u);

            for (int v : getNeighbors(board, u)) {
                if (dist.get(u) == Integer.MAX_VALUE) continue;

                int newDist = dist.get(u) + 1;
                if (newDist < dist.get(v)) {
                    dist.put(v, newDist);
                    prev.put(v, u);
                    pq.add(v);
                }
            }
        }
    }

    private static List<Integer> reconstructPath(Map<Integer, Integer> prev, int startNodeId, int targetNodeId) {
        List<Integer> path = new ArrayList<>();
        Integer curr = targetNodeId;

        if (!prev.containsKey(curr) && curr != startNodeId) return path;

        while (curr != null && curr != startNodeId) {
            path.add(0, curr);
            curr = prev.get(curr);
        }
        return path;
    }

    /**
     * Menentukan tetangga (kemungkinan langkah).
     * PERBAIKAN: Menambahkan pengecekan Tangga pada Node SAAT INI (nodeId)
     */
    private static List<Integer> getNeighbors(Board board, int nodeId) {
        List<Integer> neighbors = new ArrayList<>();
        int totalNodes = board.getTotalNodes();

        // 1. Cek apakah KITA SEDANG BERDIRI DI KAKI TANGGA?
        // (Penting agar Dijkstra tahu kita bisa langsung naik tangga dari posisi sekarang)
        if (board.getConnections().containsKey(nodeId)) {
            int dest = board.getConnections().get(nodeId);
            // Pastikan ini Tangga (Naik)
            if (dest > nodeId) {
                neighbors.add(dest);
            }
        }

        // 2. Cek Langkah Normal ke Depan (nodeId + 1)
        int nextNode = nodeId + 1;
        if (nextNode <= totalNodes) {
            neighbors.add(nextNode);

            // 3. Cek apakah node di depan (nodeId + 1) adalah Kaki Tangga?
            // (Kasus: Bypass/Passing Through - jalan ke depan lalu langsung naik)
            if (board.getConnections().containsKey(nextNode)) {
                int dest = board.getConnections().get(nextNode);
                if (dest > nextNode) {
                    neighbors.add(dest);
                }
            }
        }

        return neighbors;
    }
}