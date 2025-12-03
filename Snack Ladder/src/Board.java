import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Collections;
import java.util.Comparator;
import java.awt.Point;

class Board {
    private final int totalNodes;
    private List<BoardNode> nodes;
    private BoardNode outsideNode;

    private final int PADDING = 20;
    private final int SPACING = 5;

    private int N_SIZE;
    private int boardWidth, boardHeight;

    private Map<Integer, Integer> connections;
    private Map<Integer, Integer> nodeScores;

    public Board(int totalNodes) {
        this.totalNodes = Math.min(totalNodes, 64);
        this.boardWidth = 750;
        this.boardHeight = 750;
        this.nodes = new ArrayList<>();
        this.connections = new HashMap<>();
        this.nodeScores = new HashMap<>();

        N_SIZE = 20;
        BoardNode.SIZE = N_SIZE;

        initializeBoardNodes(); // Penempatan dari log terakhir
        initializeSimpleConnections();
        initializeSimpleScores();

        outsideNode = new BoardNode(0, -N_SIZE - 20, -N_SIZE - 20, true);
    }

    private void initializeBoardNodes() {
        nodes.clear();

        List<Point> nodePositions = new ArrayList<>();

        nodePositions.add(new Point(44, 472));  // 1
        nodePositions.add(new Point(74, 502));  // 2
        nodePositions.add(new Point(88, 468));  // 3
        nodePositions.add(new Point(127, 441)); // 4
        nodePositions.add(new Point(150, 461)); // 5
        nodePositions.add(new Point(182, 513)); // 6
        nodePositions.add(new Point(215, 536)); // 7
        nodePositions.add(new Point(273, 521)); // 8
        nodePositions.add(new Point(239, 496)); // 9
        nodePositions.add(new Point(291, 472)); // 10
        nodePositions.add(new Point(322, 470)); // 11
        nodePositions.add(new Point(328, 512)); // 12
        nodePositions.add(new Point(369, 509)); // 13
        nodePositions.add(new Point(421, 537)); // 14
        nodePositions.add(new Point(418, 468)); // 15
        nodePositions.add(new Point(489, 524)); // 16
        nodePositions.add(new Point(518, 516)); // 17
        nodePositions.add(new Point(501, 484)); // 18
        nodePositions.add(new Point(492, 452)); // 19
        nodePositions.add(new Point(550, 470)); // 20
        nodePositions.add(new Point(540, 422)); // 21 <--- KOREKSI POSISI
        nodePositions.add(new Point(605, 417)); // 22
        nodePositions.add(new Point(525, 362)); // 23
        nodePositions.add(new Point(623, 369)); // 24
        nodePositions.add(new Point(572, 330)); // 25
        nodePositions.add(new Point(631, 295)); // 26
        nodePositions.add(new Point(579, 263)); // 27
        nodePositions.add(new Point(595, 226)); // 28
        nodePositions.add(new Point(564, 194)); // 29
        nodePositions.add(new Point(504, 174)); // 30
        nodePositions.add(new Point(505, 132)); // 31
        nodePositions.add(new Point(428, 150)); // 32
        nodePositions.add(new Point(405, 236)); // 33
        nodePositions.add(new Point(335, 219)); // 34
        nodePositions.add(new Point(353, 169)); // 35
        nodePositions.add(new Point(316, 130)); // 36
        nodePositions.add(new Point(303, 178)); // 37
        nodePositions.add(new Point(271, 198)); // 38
        nodePositions.add(new Point(254, 140)); // 39
        nodePositions.add(new Point(199, 143)); // 40
        nodePositions.add(new Point(186, 189)); // 41
        nodePositions.add(new Point(228, 240)); // 42
        nodePositions.add(new Point(187, 243)); // 43
        nodePositions.add(new Point(212, 282)); // 44
        nodePositions.add(new Point(128, 240)); // 45
        nodePositions.add(new Point(162, 297)); // 46
        nodePositions.add(new Point(219, 348)); // 47
        nodePositions.add(new Point(116, 335)); // 48
        nodePositions.add(new Point(158, 376)); // 49
        nodePositions.add(new Point(218, 417)); // 50
        nodePositions.add(new Point(268, 411)); // 51
        nodePositions.add(new Point(328, 426)); // 52
        nodePositions.add(new Point(391, 406)); // 53
        nodePositions.add(new Point(445, 398)); // 54
        nodePositions.add(new Point(488, 379)); // 55
        nodePositions.add(new Point(500, 325)); // 56
        nodePositions.add(new Point(507, 265)); // 57
        nodePositions.add(new Point(456, 290)); // 58
        nodePositions.add(new Point(362, 285)); // 59
        nodePositions.add(new Point(297, 260)); // 60
        nodePositions.add(new Point(260, 291)); // 61
        nodePositions.add(new Point(292, 334)); // 62
        nodePositions.add(new Point(329, 363)); // 63
        nodePositions.add(new Point(396, 375)); // 64

        int currentId = 1;
        int nodesToCreate = Math.min(this.totalNodes, nodePositions.size());

        for (int i = 0; i < nodesToCreate; i++) {
            Point pos = nodePositions.get(i);
            int nodeX = pos.x;
            int nodeY = pos.y;

            nodes.add(new BoardNode(currentId++, nodeX, nodeY, true));
        }
    }

    private void initializeSimpleConnections() {
        connections.clear();
        Random rnd = new Random();

        int numLadders = Math.min(5, totalNodes / 10);
        if (numLadders < 1 && totalNodes > 10) numLadders = 1;

        int maxJump = totalNodes / 4;
        if (maxJump < 5) maxJump = 5;

        for (int i = 0; i < numLadders; i++) {
            int startNode;
            int endNode;

            do {
                startNode = rnd.nextInt(totalNodes - maxJump - 1) + 2;
                int jump = rnd.nextInt(maxJump - 1) + 2;
                endNode = startNode + jump;

                if (endNode >= totalNodes) {
                    endNode = totalNodes - 1;
                }

            } while (startNode >= endNode ||
                    connections.containsKey(startNode) || connections.containsValue(startNode) ||
                    connections.containsKey(endNode) || connections.containsValue(endNode) ||
                    startNode < 2 || endNode >= totalNodes);

            connections.put(startNode, endNode);
        }
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