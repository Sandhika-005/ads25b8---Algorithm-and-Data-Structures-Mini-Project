import java.util.*;

class DijkstraAlgorithm {
    private UnifiedGraph graph;
    private Map<UNode, Integer> distances;
    private Map<UNode, UNode> previous;

    private static final int INF = Integer.MAX_VALUE;

    public DijkstraAlgorithm(UnifiedGraph graph) {
        this.graph = graph;
        this.distances = new HashMap<>();
        this.previous = new HashMap<>();
    }

    public List<UNode> findShortestPath(UNode source, UNode target) {
        for (UNode node : graph.getNodes()) {
            distances.put(node, INF);
            previous.put(node, null);
        }
        distances.put(source, 0);
        PriorityQueue<UNode> pq = new PriorityQueue<>(Comparator.comparingInt(distances::get));
        pq.add(source);

        while (!pq.isEmpty()) {
            UNode u = pq.poll();
            int distU = distances.get(u);

            if (distU == INF) continue;
            if (u.equals(target)) break;

            for (UEdge edge : graph.getEdges()) {
                if (edge.getSource().equals(u) || edge.getTarget().equals(u)) {
                    UNode v;
                    if (edge.getSource().equals(u)) {
                        v = edge.getTarget();
                    } else {
                        v = edge.getSource();
                    }

                    int newDist = distU + edge.getWeight();

                    if (newDist < distances.getOrDefault(v, INF)) {
                        distances.put(v, newDist);
                        previous.put(v, u);
                        pq.remove(v);
                        pq.add(v);
                    }
                }
            }
        }

        if (distances.getOrDefault(target, INF) == INF) {
            return null;
        }

        List<UNode> path = new ArrayList<>();
        UNode current = target;
        while (current != null) {
            path.add(0, current);
            current = previous.get(current);
        }
        return path;
    }

    public int getDistance(UNode node) {
        return distances.getOrDefault(node, INF);
    }
}