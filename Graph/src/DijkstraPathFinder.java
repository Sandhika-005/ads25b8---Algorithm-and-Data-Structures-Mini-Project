import java.util.*;

public class DijkstraPathFinder {

    public static List<Integer> findShortestPathSteps(Board board, int startNodeId, int targetNodeId, int maxSteps) {
        // 1. Jalankan Dijkstra dari Start Node ke SELURUH papan
        Map<Integer, Integer> dist = new HashMap<>();
        Map<Integer, Integer> prev = new HashMap<>();
        runDijkstra(board, startNodeId, dist, prev);

        // 2. Cek jalur normal ke Target Utama (Node 64)
        List<Integer> pathDirectToTarget = reconstructPath(prev, startNodeId, targetNodeId);

        // Jika Target (Finish) bisa dicapai dengan dadu saat ini, langsung ke sana!
        if (!pathDirectToTarget.isEmpty() && pathDirectToTarget.size() <= maxSteps) {
            return pathDirectToTarget;
        }

        // 3. Cari Bintang (Node kelipatan 5) yang jaraknya PAS dengan dadu
        Integer bestStarNode = null;
        int minDistanceToTargetFromStar = Integer.MAX_VALUE;

        for (int nodeId = 1; nodeId <= board.getTotalNodes(); nodeId++) {
            // Syarat 1: Harus kelipatan 5 (Bintang)
            if (nodeId % 5 != 0) continue;

            // Syarat 2: Bukan posisi start saat ini
            if (nodeId == startNodeId) continue;

            // Syarat 3: Harus terjangkau SESUAI dadu
            int distToStar = dist.getOrDefault(nodeId, Integer.MAX_VALUE);

            // PERBAIKAN BUG DI SINI:
            // Sebelumnya: if (distToStar > maxSteps)
            // Sekarang: if (distToStar != maxSteps)
            // Artinya: Kita hanya mengambil Bintang jika kita mendarat TEPAT di atasnya dengan dadu ini.
            // Jika dadu 6, tapi bintang ada di jarak 5, kita ABAIKAN bintang itu dan jalan terus ke node 6.
            if (distToStar != maxSteps) continue;

            // Hitung sisa jarak dari Bintang ini ke Target Utama
            int distStarToFinish = getShortestDistance(board, nodeId, targetNodeId);

            if (distStarToFinish < minDistanceToTargetFromStar) {
                minDistanceToTargetFromStar = distStarToFinish;
                bestStarNode = nodeId;
            }
        }

        // 4. Putuskan Jalur Akhir
        List<Integer> finalPath;
        if (bestStarNode != null) {
            // Ada bintang yang jaraknya PAS dengan dadu
            finalPath = reconstructPath(prev, startNodeId, bestStarNode);
        } else {
            // Tidak ada bintang yang pas, gunakan jalur normal sejauh maxSteps
            finalPath = pathDirectToTarget;
        }

        // 5. Potong jalur sesuai jumlah dadu (maxSteps)
        List<Integer> stepsToTake = new ArrayList<>();
        int steps = 0;
        for (Integer node : finalPath) {
            if (steps >= maxSteps) break;
            stepsToTake.add(node);
            steps++;
        }

        return stepsToTake;
    }

    // --- Helper Methods (Tidak Berubah) ---

    private static void runDijkstra(Board board, int startNodeId, Map<Integer, Integer> dist, Map<Integer, Integer> prev) {
        PriorityQueue<Integer> pq = new PriorityQueue<>(Comparator.comparingInt(dist::get));
        for (int i = 0; i <= board.getTotalNodes(); i++) dist.put(i, Integer.MAX_VALUE);

        dist.put(startNodeId, 0);
        pq.add(startNodeId);
        Set<Integer> visited = new HashSet<>();

        while (!pq.isEmpty()) {
            int u = pq.poll();
            if (visited.contains(u)) continue;
            visited.add(u);

            for (int v : getNeighbors(board, u)) {
                int newDist = dist.get(u) + 1;
                if (newDist < dist.get(v)) {
                    dist.put(v, newDist);
                    prev.put(v, u);
                    pq.add(v);
                }
            }
        }
    }

    private static int getShortestDistance(Board board, int start, int end) {
        Map<Integer, Integer> d = new HashMap<>();
        Map<Integer, Integer> p = new HashMap<>();
        runDijkstra(board, start, d, p);
        return d.getOrDefault(end, Integer.MAX_VALUE);
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

    private static List<Integer> getNeighbors(Board board, int nodeId) {
        List<Integer> neighbors = new ArrayList<>();
        int totalNodes = board.getTotalNodes();
        if (nodeId + 1 <= totalNodes) neighbors.add(nodeId + 1);
        if (board.getConnections().containsKey(nodeId)) neighbors.add(board.getConnections().get(nodeId));
        return neighbors;
    }
}