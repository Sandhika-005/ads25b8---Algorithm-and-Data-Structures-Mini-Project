import java.util.*;

public class DijkstraPathFinder {

    // Mencari urutan node (jalur) dengan prioritas:
    // 1. Menang langsung (jika target terjangkau dalam maxSteps)
    // 2. Ambil Bintang (kelipatan 5) untuk extra turn (jika target jauh & bintang terjangkau)
    // 3. Maju biasa ke arah target
    public static List<Integer> findShortestPathSteps(Board board, int startNodeId, int targetNodeId, int maxSteps) {
        // 1. Jalankan Dijkstra dari Start Node ke SELURUH papan untuk mendapatkan peta jarak
        Map<Integer, Integer> dist = new HashMap<>();
        Map<Integer, Integer> prev = new HashMap<>();
        runDijkstra(board, startNodeId, dist, prev);

        // 2. Cek jalur normal ke Target Utama (Biasanya Node 64)
        List<Integer> pathDirectToTarget = reconstructPath(prev, startNodeId, targetNodeId);

        // Jika Target (Finish) bisa dicapai dengan dadu saat ini, langsung gas ke sana!
        if (!pathDirectToTarget.isEmpty() && pathDirectToTarget.size() <= maxSteps) {
            return pathDirectToTarget;
        }

        // 3. Jika Target tidak terjangkau, cari Bintang (Node kelipatan 5) yang terjangkau
        // Fitur: "kalo shortest path > maxSteps maka ke bintang dulu"
        Integer bestStarNode = null;
        int minDistanceToTargetFromStar = Integer.MAX_VALUE;

        // Iterasi semua node untuk mencari kandidat Bintang
        for (int nodeId = 1; nodeId <= board.getTotalNodes(); nodeId++) {
            // Syarat 1: Harus kelipatan 5 (Bintang)
            if (nodeId % 5 != 0) continue;

            // Syarat 2: Bukan posisi start saat ini
            if (nodeId == startNodeId) continue;

            // Syarat 3: Harus terjangkau dengan dadu saat ini
            int distToStar = dist.getOrDefault(nodeId, Integer.MAX_VALUE);
            if (distToStar > maxSteps) continue;

            // Jika node bintang ini punya koneksi (ular/tangga), kita harus cek tujuan akhirnya
            // Tapi dalam implementasi board ini, logika getNeighbors sudah menangani 'lompatan'
            // Jadi nodeId di sini adalah node yang BENAR-BENAR kita pijak.

            // Hitung sisa jarak dari Bintang ini ke Target Utama (Node 64)
            // Kita butuh hitung jarak lagi dari si Bintang ke 64
            int distStarToFinish = getShortestDistance(board, nodeId, targetNodeId);

            // Pilih Bintang yang paling mendekatkan kita ke Finish
            if (distStarToFinish < minDistanceToTargetFromStar) {
                minDistanceToTargetFromStar = distStarToFinish;
                bestStarNode = nodeId;
            }
        }

        // 4. Putuskan Jalur Akhir
        List<Integer> finalPath;
        if (bestStarNode != null) {
            // HORE! Ada bintang terjangkau. Ubah target ke Bintang tersebut.
            // System.out.println("Dijkstra: Mengalihkan rute ke Bintang (Node " + bestStarNode + ")");
            finalPath = reconstructPath(prev, startNodeId, bestStarNode);
        } else {
            // Tidak ada bintang terjangkau, gunakan jalur normal ke finish
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

    // Algoritma Dijkstra Inti
    private static void runDijkstra(Board board, int startNodeId, Map<Integer, Integer> dist, Map<Integer, Integer> prev) {
        PriorityQueue<Integer> pq = new PriorityQueue<>(Comparator.comparingInt(dist::get));

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

            // Di sini kita tidak break saat ketemu target, agar kita memetakan seluruh board
            // untuk keperluan pencarian bintang.

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

    // Helper: Hitung jarak terpendek antar dua node spesifik (digunakan untuk evaluasi Bintang)
    private static int getShortestDistance(Board board, int start, int end) {
        Map<Integer, Integer> d = new HashMap<>();
        Map<Integer, Integer> p = new HashMap<>(); // Dummy
        runDijkstra(board, start, d, p);
        return d.getOrDefault(end, Integer.MAX_VALUE);
    }

    // Helper: Rekonstruksi Jalur dari prev map
    private static List<Integer> reconstructPath(Map<Integer, Integer> prev, int startNodeId, int targetNodeId) {
        List<Integer> path = new ArrayList<>();
        Integer curr = targetNodeId;

        if (!prev.containsKey(curr) && curr != startNodeId) {
            return path; // Tidak ada jalur
        }

        while (curr != null && curr != startNodeId) {
            path.add(0, curr);
            curr = prev.get(curr);
        }
        return path;
    }

    private static List<Integer> getNeighbors(Board board, int nodeId) {
        List<Integer> neighbors = new ArrayList<>();
        int totalNodes = board.getTotalNodes();

        // Tetangga 1: Jalan Biasa (Maju 1 langkah)
        if (nodeId + 1 <= totalNodes) {
            neighbors.add(nodeId + 1);
        }

        // Tetangga 2: Lewat Jalur Koneksi (Tangga/Ular)
        if (board.getConnections().containsKey(nodeId)) {
            neighbors.add(board.getConnections().get(nodeId));
        }

        return neighbors;
    }
}