import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class Maze extends JPanel {

    // --- Konfigurasi Tampilan & Kecepatan ---
    // Diubah ke PROTECTED agar bisa diakses subclass
    protected final int CELL_SIZE = 20;
    protected final int COLS = 50;
    protected final int ROWS = 35;

    // Kecepatan Animasi
    protected final int SOLVE_DELAY = 15;
    protected final int PATH_DELAY = 30;
    protected final int GEN_BATCH = 20;

    // Warna (Tema Dark Mode)
    protected final Color COLOR_BG = Color.BLACK;
    protected final Color COLOR_WALL = Color.WHITE;
    protected final Color COLOR_START = new Color(0, 255, 0);      // Hijau
    protected final Color COLOR_END = new Color(255, 0, 0);        // Merah
    protected final Color COLOR_SOLUTION = new Color(255, 255, 0); // Kuning
    protected final Color COLOR_SEARCH_BODY = new Color(0, 255, 255, 40);
    protected final Color COLOR_SEARCH_HEAD = new Color(0, 255, 255);

    // --- Struktur Data ---
    // Diubah ke PROTECTED
    protected Cell[][] grid;
    protected Cell startCell, endCell;
    protected Cell currentSearchCell;
    protected List<Cell> finalPath;

    // --- State ---
    protected boolean isGenerating = false;
    protected boolean isSolving = false;

    // Constructor
    public Maze() {
        setPreferredSize(new Dimension(COLS * CELL_SIZE + 1, ROWS * CELL_SIZE + 1));
        setBackground(COLOR_BG);
        setupGrid();
    }

    // =========================================
    // 1. Struktur Data Cell
    // =========================================
    // Diubah ke PROTECTED
    protected class Cell {
        int r, c;
        boolean[] walls = {true, true, true, true}; // [Top, Right, Bottom, Left]
        boolean visited = false;
        boolean searchVisited = false;
        Cell parent = null;

        public Cell(int r, int c) {
            this.r = r;
            this.c = c;
        }
    }

    // Diubah ke PROTECTED
    protected void setupGrid() {
        grid = new Cell[ROWS][COLS];
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                grid[r][c] = new Cell(r, c);
            }
        }
        startCell = grid[0][0];
        endCell = grid[ROWS - 1][COLS - 1];
        finalPath = new ArrayList<>();
        repaint();
    }

    // =========================================
    // 2. Generate Maze (Prim's Algorithm)
    // =========================================
    public void generatePrim() {
        if (isGenerating || isSolving) return;
        setupGrid();
        isGenerating = true;

        new Thread(() -> {
            ArrayList<Cell> frontier = new ArrayList<>();
            Random rand = new Random();

            startCell.visited = true;
            addFrontier(startCell, frontier);

            int loopCount = 0;

            while (!frontier.isEmpty()) {
                int index = rand.nextInt(frontier.size());
                Cell current = frontier.remove(index);

                List<Cell> visitedNeighbors = getNeighbors(current, true);

                if (!visitedNeighbors.isEmpty()) {
                    Cell neighbor = visitedNeighbors.get(rand.nextInt(visitedNeighbors.size()));
                    removeWalls(current, neighbor);
                    current.visited = true;
                    addFrontier(current, frontier);

                    loopCount++;
                    if (loopCount % GEN_BATCH == 0) {
                        visualize(1);
                    }
                }
            }
            // Buka pintu start/end
            grid[0][0].walls[3] = false;
            grid[ROWS-1][COLS-1].walls[1] = false;

            isGenerating = false;
            repaint();
        }).start();
    }

    // Diubah ke PROTECTED
    protected void addFrontier(Cell cell, ArrayList<Cell> frontier) {
        int[][] dirs = {{-1,0}, {1,0}, {0,-1}, {0,1}};
        for(int[] d : dirs) {
            int nr = cell.r + d[0];
            int nc = cell.c + d[1];
            if(isValid(nr, nc) && !grid[nr][nc].visited && !frontier.contains(grid[nr][nc])) {
                frontier.add(grid[nr][nc]);
            }
        }
    }

    // =========================================
    // 3. Solver (BFS & DFS)
    // =========================================
    public void solve(boolean useBFS) {
        if (isGenerating || isSolving) return;
        resetSolver();
        isSolving = true;

        new Thread(() -> {
            Collection<Cell> structure = useBFS ? new LinkedList<>() : new Stack<>();

            if (useBFS) ((Queue<Cell>)structure).add(startCell);
            else ((Stack<Cell>)structure).push(startCell);

            startCell.searchVisited = true;
            boolean found = false;

            while (!structure.isEmpty()) {
                Cell current;
                if (useBFS) current = ((Queue<Cell>)structure).poll();
                else current = ((Stack<Cell>)structure).pop();

                currentSearchCell = current;

                if (current == endCell) {
                    found = true;
                    break;
                }

                List<Cell> neighbors = getConnectedNeighbors(current);
                for (Cell next : neighbors) {
                    if (!next.searchVisited) {
                        next.searchVisited = true;
                        next.parent = current;
                        if (useBFS) ((Queue<Cell>)structure).add(next);
                        else ((Stack<Cell>)structure).push(next);
                    }
                }
                visualize(SOLVE_DELAY);
            }

            if (found) {
                // Backtracking jalur
                List<Cell> completeRoute = new ArrayList<>();
                Cell temp = endCell;
                while (temp != null) {
                    completeRoute.add(temp);
                    temp = temp.parent;
                }
                Collections.reverse(completeRoute);

                currentSearchCell = null;
                visualize(100);

                for (Cell step : completeRoute) {
                    finalPath.add(step);
                    visualize(PATH_DELAY);
                }
            }

            currentSearchCell = null;
            isSolving = false;
            repaint();
        }).start();
    }

    // =========================================
    // Helper Methods (PROTECTED)
    // =========================================
    protected void removeWalls(Cell a, Cell b) {
        int dr = a.r - b.r;
        int dc = a.c - b.c;
        if (dr == 1) { a.walls[0] = false; b.walls[2] = false; }
        if (dr == -1){ a.walls[2] = false; b.walls[0] = false; }
        if (dc == 1) { a.walls[3] = false; b.walls[1] = false; }
        if (dc == -1){ a.walls[1] = false; b.walls[3] = false; }
    }

    protected List<Cell> getNeighbors(Cell c, boolean onlyVisited) {
        List<Cell> list = new ArrayList<>();
        int[][] dirs = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        for (int[] d : dirs) {
            int nr = c.r + d[0], nc = c.c + d[1];
            if (isValid(nr, nc) && grid[nr][nc].visited == onlyVisited) list.add(grid[nr][nc]);
        }
        return list;
    }

    protected List<Cell> getConnectedNeighbors(Cell c) {
        List<Cell> list = new ArrayList<>();
        if (!c.walls[0] && isValid(c.r - 1, c.c)) list.add(grid[c.r - 1][c.c]);
        if (!c.walls[1] && isValid(c.r, c.c + 1)) list.add(grid[c.r][c.c + 1]);
        if (!c.walls[2] && isValid(c.r + 1, c.c)) list.add(grid[c.r + 1][c.c]);
        if (!c.walls[3] && isValid(c.r, c.c - 1)) list.add(grid[c.r][c.c - 1]);
        return list;
    }

    protected boolean isValid(int r, int c) { return r >= 0 && r < ROWS && c >= 0 && c < COLS; }

    protected void resetSolver() {
        finalPath.clear();
        for(int r=0; r<ROWS; r++) {
            for(int c=0; c<COLS; c++) {
                grid[r][c].searchVisited = false;
                grid[r][c].parent = null;
            }
        }
        repaint();
    }

    protected void visualize(int delay) {
        try { SwingUtilities.invokeLater(this::repaint); if (delay > 0) Thread.sleep(delay); } catch (Exception e) {}
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Subclass akan meng-override bagian ini jika ingin custom terrain
        // Tapi kita sediakan method draw default agar bisa dipanggil subclass
        drawMazeElements(g2);
    }

    protected void drawMazeElements(Graphics2D g2) {
        // 1. Gambar START dan END
        if (startCell != null) {
            g2.setColor(COLOR_START);
            g2.fillRect(startCell.c * CELL_SIZE + 4, startCell.r * CELL_SIZE + 4, CELL_SIZE - 8, CELL_SIZE - 8);
        }
        if (endCell != null) {
            g2.setColor(COLOR_END);
            g2.fillRect(endCell.c * CELL_SIZE + 4, endCell.r * CELL_SIZE + 4, CELL_SIZE - 8, CELL_SIZE - 8);
        }

        // 2. Gambar Scanning Area (Original)
        if (isSolving && !(this instanceof WeightedMaze)) { // Cek agar tidak tumpang tindih dgn weighted
            g2.setColor(COLOR_SEARCH_BODY);
            for (int r = 0; r < ROWS; r++) {
                for (int c = 0; c < COLS; c++) {
                    if (grid[r][c].searchVisited) {
                        g2.fillRect(c * CELL_SIZE + 2, r * CELL_SIZE + 2, CELL_SIZE - 4, CELL_SIZE - 4);
                    }
                }
            }
            if (currentSearchCell != null) {
                g2.setColor(COLOR_SEARCH_HEAD);
                g2.fillRect(currentSearchCell.c * CELL_SIZE + 5, currentSearchCell.r * CELL_SIZE + 5, CELL_SIZE - 10, CELL_SIZE - 10);
            }
        }

        // 3. Gambar Dinding
        g2.setColor(COLOR_WALL);
        g2.setStroke(new BasicStroke(2));
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                int x = c * CELL_SIZE;
                int y = r * CELL_SIZE;
                if (grid[r][c].walls[0]) g2.drawLine(x, y, x + CELL_SIZE, y);
                if (grid[r][c].walls[1]) g2.drawLine(x + CELL_SIZE, y, x + CELL_SIZE, y + CELL_SIZE);
                if (grid[r][c].walls[2]) g2.drawLine(x + CELL_SIZE, y + CELL_SIZE, x, y + CELL_SIZE);
                if (grid[r][c].walls[3]) g2.drawLine(x, y + CELL_SIZE, x, y);
            }
        }

        // 4. Gambar Jalur Solusi
        if (!finalPath.isEmpty()) {
            g2.setColor(COLOR_SOLUTION);
            g2.setStroke(new BasicStroke(3));
            for (int i = 0; i < finalPath.size() - 1; i++) {
                Cell a = finalPath.get(i);
                Cell b = finalPath.get(i + 1);
                g2.drawLine(
                        a.c * CELL_SIZE + CELL_SIZE / 2, a.r * CELL_SIZE + CELL_SIZE / 2,
                        b.c * CELL_SIZE + CELL_SIZE / 2, b.r * CELL_SIZE + CELL_SIZE / 2
                );
            }
        }
    }
}