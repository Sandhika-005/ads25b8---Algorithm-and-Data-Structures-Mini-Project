import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class WeightedMaze extends Maze {

    private static final int COST_GRASS = 1;
    private static final int COST_MUD = 5;
    private static final int COST_WATER = 10;

    private final Color C_GRASS = new Color(0, 100, 0);
    private final Color C_MUD = new Color(139, 69, 19);
    private final Color C_WATER = new Color(0, 0, 205);

    private int[][] terrainGrid;
    private boolean useTerrainMode = false;

    public WeightedMaze() {
        super();
        terrainGrid = new int[ROWS][COLS];
    }

    @Override
    public void generatePrim() {
        useTerrainMode = false;
        super.generatePrim();
    }

    public void generateWeightedTerrain() {
        if (isGenerating || isSolving) return;
        useTerrainMode = true;
        super.generatePrim();
        if (statsCallback != null) statsCallback.accept("Generating Terrain...");

        new Thread(() -> {
            try { Thread.sleep(200); } catch(Exception e){}
            Random rand = new Random();
            for (int r = 0; r < ROWS; r++) {
                for (int c = 0; c < COLS; c++) {
                    double p = rand.nextDouble();
                    if (p < 0.60) terrainGrid[r][c] = COST_GRASS;
                    else if (p < 0.85) terrainGrid[r][c] = COST_MUD;
                    else terrainGrid[r][c] = COST_WATER;
                }
            }
            terrainGrid[0][0] = COST_GRASS;
            terrainGrid[ROWS-1][COLS-1] = COST_GRASS;

            if (statsCallback != null) statsCallback.accept("Terrain Generated.\n\nLegend:\n1 = Grass (Green)\n5 = Mud (Brown)\n10 = Water (Blue)");
            repaint();
        }).start();
    }

    public void solveWeighted(boolean useAStar) {
        if (isGenerating || isSolving) return;

        isSolving = true;
        resetSolver();
        String algoName = useAStar ? "A* (A-Star)" : "Dijkstra";
        if (statsCallback != null) statsCallback.accept("Running " + algoName + "...");

        new Thread(() -> {
            Map<Cell, Integer> dist = new HashMap<>();
            Map<Cell, Cell> parent = new HashMap<>();
            PriorityQueue<double[]> pq = new PriorityQueue<>(Comparator.comparingDouble(a -> a[0]));

            for(int r=0; r<ROWS; r++)
                for(int c=0; c<COLS; c++) dist.put(grid[r][c], Integer.MAX_VALUE);

            dist.put(startCell, 0);
            pq.add(new double[]{0, startCell.r, startCell.c});

            boolean found = false;
            int visitedNodesCount = 0;

            while (!pq.isEmpty()) {
                double[] currData = pq.poll();
                Cell current = grid[(int)currData[1]][(int)currData[2]];

                if (current == endCell) {
                    found = true;
                    break;
                }

                if (!current.searchVisited) {
                    current.searchVisited = true;
                    visitedNodesCount++;
                    if (visitedNodesCount % 5 == 0) visualize(1);
                }

                for (Cell neighbor : getConnectedNeighbors(current)) {
                    int cost = useTerrainMode ? terrainGrid[neighbor.r][neighbor.c] : 1;
                    int newDist = dist.get(current) + cost;

                    if (newDist < dist.get(neighbor)) {
                        dist.put(neighbor, newDist);
                        parent.put(neighbor, current);

                        double priority = newDist;
                        if(useAStar) priority += heuristic(neighbor, endCell);

                        pq.add(new double[]{priority, neighbor.r, neighbor.c});
                    }
                }
            }

            if (found) {
                reconstructPath(parent.get(endCell), parent);

                int finalCost = dist.get(endCell);
                int finalVisitedNodes = visitedNodesCount;

                // Update Statistik ke Panel Samping
                String result = String.format("""
                    Algorithm: %s
                    ----------------
                    Status: Finished
                    Total Cost: %d
                    Nodes Visited: %d
                    Efficiency: %.2f%%
                    """, algoName, finalCost, finalVisitedNodes, ((double)finalVisitedNodes/(ROWS*COLS))*100);

                if (statsCallback != null) statsCallback.accept(result);
            }
            isSolving = false;
            repaint();
        }).start();
    }

    private double heuristic(Cell a, Cell b) {
        return (Math.abs(a.r - b.r) + Math.abs(a.c - b.c));
    }

    private void reconstructPath(Cell curr, Map<Cell, Cell> parent) {
        while (curr != null) {
            finalPath.add(curr);
            curr = parent.get(curr);
            visualize(15);
        }
        Collections.reverse(finalPath);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.calculateDimensions();

        if (!useTerrainMode) {
            this.drawScan = true;
            super.paintComponent(g);
            return;
        }

        this.drawScan = false;

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Font untuk angka bobot
        g2.setFont(new Font("SansSerif", Font.BOLD, Math.max(10, cellSize / 2)));
        FontMetrics fm = g2.getFontMetrics();

        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                int w = terrainGrid[r][c];

                // Gambar Kotak Terrain
                if (w == COST_MUD) g2.setColor(C_MUD);
                else if (w == COST_WATER) g2.setColor(C_WATER);
                else g2.setColor(C_GRASS);
                g2.fillRect(startX + c * cellSize, startY + r * cellSize, cellSize, cellSize);

                // VISUALISASI ANGKA BOBOT DI TENGAH CELL
                if (cellSize > 15) { // Hanya gambar jika cell cukup besar
                    g2.setColor(new Color(255, 255, 255, 180)); // Putih transparan
                    String text = String.valueOf(w);
                    int textX = startX + c * cellSize + (cellSize - fm.stringWidth(text)) / 2;
                    int textY = startY + r * cellSize + ((cellSize - fm.getHeight()) / 2) + fm.getAscent();
                    g2.drawString(text, textX, textY);
                }

                // Scan Overlay
                if (grid[r][c].searchVisited) {
                    g2.setColor(new Color(255, 255, 255, 100));
                    g2.fillRect(startX + c * cellSize, startY + r * cellSize, cellSize, cellSize);
                }
            }
        }

        super.drawMazeElements(g2);
    }
}