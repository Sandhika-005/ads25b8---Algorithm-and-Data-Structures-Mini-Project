import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class WeightedMaze extends Maze {

    // Bobot Terrain
    private static final int COST_GRASS = 1;
    private static final int COST_MUD = 5;
    private static final int COST_WATER = 10;

    // Warna Terrain
    private final Color C_GRASS = new Color(34, 139, 34);    // Hijau Tua
    private final Color C_MUD = new Color(139, 69, 19);      // Coklat
    private final Color C_WATER = new Color(30, 144, 255);   // Biru

    // Peta Terrain (Menyimpan tipe bobot untuk setiap sel)
    private int[][] terrainGrid;

    public WeightedMaze() {
        super(); // Panggil konstruktor Maze asli
        terrainGrid = new int[ROWS][COLS];
        // Default semua grass
        for (int[] row : terrainGrid) Arrays.fill(row, COST_GRASS);
    }

    // 1. Generate Maze dengan Terrain
    public void generateWeightedTerrain() {
        if (isGenerating || isSolving) return;

        // Panggil generate dinding dari class Induk (Maze Asli)
        super.generatePrim();

        // Tambahkan terrain secara acak setelah maze struktur jadi
        new Thread(() -> {
            // Tunggu sedikit agar animasi dinding selesai (opsional)
            try { Thread.sleep(500); } catch (Exception e) {}

            Random rand = new Random();
            for (int r = 0; r < ROWS; r++) {
                for (int c = 0; c < COLS; c++) {
                    double p = rand.nextDouble();
                    if (p < 0.60) terrainGrid[r][c] = COST_GRASS;     // 60% Rumput
                    else if (p < 0.85) terrainGrid[r][c] = COST_MUD;  // 25% Lumpur
                    else terrainGrid[r][c] = COST_WATER;              // 15% Air
                }
            }
            // Pastikan Start & End adalah rumput agar fair
            terrainGrid[0][0] = COST_GRASS;
            terrainGrid[ROWS - 1][COLS - 1] = COST_GRASS;

            repaint();
        }).start();
    }

    // 2. Algoritma Dijkstra & A*
    public void solveWeighted(boolean useAStar) {
        if (isGenerating || isSolving) return;
        isSolving = true;

        // Reset state visual
        resetSolver();

        new Thread(() -> {
            // Map untuk menyimpan jarak terpendek (gScore) dan parent
            Map<Cell, Integer> dist = new HashMap<>();
            Map<Cell, Cell> parent = new HashMap<>();

            // Priority Queue menyimpan array: [fScore, r, c]
            PriorityQueue<double[]> pq = new PriorityQueue<>(Comparator.comparingDouble(a -> a[0]));

            // Init jarak infinity
            for(int r=0; r<ROWS; r++) {
                for(int c=0; c<COLS; c++) {
                    dist.put(grid[r][c], Integer.MAX_VALUE);
                }
            }

            dist.put(startCell, 0);
            double startHeuristic = useAStar ? heuristic(startCell, endCell) : 0;
            pq.add(new double[]{startHeuristic, startCell.r, startCell.c});

            boolean found = false;
            Set<Cell> visited = new HashSet<>();

            while (!pq.isEmpty()) {
                double[] currentData = pq.poll();
                int r = (int) currentData[1];
                int c = (int) currentData[2];
                Cell current = grid[r][c];

                if (visited.contains(current)) continue;
                visited.add(current);

                // Visualisasi scanning (set flag di parent)
                current.searchVisited = true;

                // Animasi visual
                if (visited.size() % 5 == 0) {
                    visualize(1);
                }

                if (current == endCell) {
                    found = true;
                    break;
                }

                // Cek Tetangga
                List<Cell> neighbors = getConnectedNeighbors(current);
                for (Cell neighbor : neighbors) {
                    if (visited.contains(neighbor)) continue;

                    int newDist = dist.get(current) + terrainGrid[neighbor.r][neighbor.c];

                    if (newDist < dist.get(neighbor)) {
                        dist.put(neighbor, newDist);
                        parent.put(neighbor, current);

                        double fScore = newDist;
                        if (useAStar) {
                            fScore += heuristic(neighbor, endCell);
                        }
                        pq.add(new double[]{fScore, neighbor.r, neighbor.c});
                    }
                }
            }

            if (found) {
                // Rekonstruksi jalur
                reconstructPath(parent);
            }
            isSolving = false;
            repaint();
        }).start();
    }

    private double heuristic(Cell a, Cell b) {
        // Manhattan Distance
        return Math.abs(a.r - b.r) + Math.abs(a.c - b.c);
    }

    private void reconstructPath(Map<Cell, Cell> parentMap) {
        Cell curr = endCell;
        while (curr != null) {
            finalPath.add(curr);
            curr = parentMap.get(curr);
            visualize(10); // Animasi tracing
        }
        Collections.reverse(finalPath);
    }

    // 3. Override Paint untuk menggambar Terrain Warna-warni
    @Override
    protected void paintComponent(Graphics g) {
        // Jangan panggil super.paintComponent agar tidak double draw background
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // A. Gambar Background Terrain
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                int x = c * CELL_SIZE;
                int y = r * CELL_SIZE;

                Color color = Color.BLACK;
                if (terrainGrid != null) {
                    int w = terrainGrid[r][c];
                    if (w == COST_GRASS) color = C_GRASS;
                    else if (w == COST_MUD) color = C_MUD;
                    else if (w == COST_WATER) color = C_WATER;
                }

                g2.setColor(color);
                g2.fillRect(x, y, CELL_SIZE, CELL_SIZE);

                // Efek Scanning (Biru transparan) khusus Weighted
                if (grid[r][c].searchVisited) {
                    g2.setColor(new Color(255, 255, 255, 100)); // Putih transparan
                    g2.fillRect(x, y, CELL_SIZE, CELL_SIZE);
                }
            }
        }

        // B. Gambar Struktur Maze (Dinding, Start, End, Jalur Final)
        // Kita panggil helper dari parent
        super.drawMazeElements(g2);
    }
}