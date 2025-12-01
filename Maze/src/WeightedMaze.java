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
            repaint();
        }).start();
    }

    public void solveWeighted(boolean useAStar) {
        if (isGenerating || isSolving) return;

        isSolving = true;
        resetSolver();

        new Thread(() -> {
            Map<Cell, Integer> dist = new HashMap<>();
            Map<Cell, Cell> parent = new HashMap<>();
            PriorityQueue<double[]> pq = new PriorityQueue<>(Comparator.comparingDouble(a -> a[0]));

            for(int r=0; r<ROWS; r++)
                for(int c=0; c<COLS; c++) dist.put(grid[r][c], Integer.MAX_VALUE);

            dist.put(startCell, 0);
            pq.add(new double[]{0, startCell.r, startCell.c});

            boolean found = false;
            Set<Cell> visited = new HashSet<>();

            while (!pq.isEmpty()) {
                double[] currData = pq.poll();
                Cell current = grid[(int)currData[1]][(int)currData[2]];

                if (visited.contains(current)) continue;
                visited.add(current);
                current.searchVisited = true;

                if (visited.size() % 5 == 0) visualize(1);

                if (current == endCell) {
                    found = true;
                    break;
                }

                for (Cell neighbor : getConnectedNeighbors(current)) {
                    if (visited.contains(neighbor)) continue;

                    int cost = useTerrainMode ? terrainGrid[neighbor.r][neighbor.c] : 1;
                    int newDist = dist.get(current) + cost;

                    if (newDist < dist.get(neighbor)) {
                        dist.put(neighbor, newDist);
                        parent.put(neighbor, current);

                        double priority = newDist;
                        if(useAStar) priority += (Math.abs(neighbor.r - endCell.r) + Math.abs(neighbor.c - endCell.c));

                        pq.add(new double[]{priority, neighbor.r, neighbor.c});
                    }
                }
            }

            if (found) reconstructPath(parent.get(endCell), parent);
            isSolving = false;
            repaint();
        }).start();
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
        super.calculateDimensions(); // Hitung ukuran agar sinkron

        // Jika Standard Mode, aktifkan drawScan parent
        if (!useTerrainMode) {
            this.drawScan = true;
            super.paintComponent(g);
            return;
        }

        // Jika Terrain Mode, matikan drawScan parent (kita gambar sendiri overlay-nya)
        this.drawScan = false;

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Gambar Terrain
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                int w = terrainGrid[r][c];
                if (w == COST_MUD) g2.setColor(C_MUD);
                else if (w == COST_WATER) g2.setColor(C_WATER);
                else g2.setColor(C_GRASS);

                g2.fillRect(startX + c * cellSize, startY + r * cellSize, cellSize, cellSize);

                // Custom Scan Overlay untuk Terrain
                if (grid[r][c].searchVisited) {
                    g2.setColor(new Color(255, 255, 255, 120));
                    g2.fillRect(startX + c * cellSize, startY + r * cellSize, cellSize, cellSize);
                }
            }
        }

        super.drawMazeElements(g2);
    }
}