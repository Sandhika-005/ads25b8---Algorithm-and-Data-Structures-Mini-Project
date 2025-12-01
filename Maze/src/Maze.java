import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class Maze extends JPanel {

    // --- Konfigurasi Tampilan & Kecepatan ---
    private final int CELL_SIZE = 20;
    private final int COLS = 50;
    private final int ROWS = 35;

    // Kecepatan Animasi
    private final int SOLVE_DELAY = 15; // Kecepatan scan (biru)
    private final int PATH_DELAY = 30;  // Kecepatan animasi garis solusi (kuning)
    private final int GEN_BATCH = 20;   // Percepatan generate maze (biar tidak lama)

    // Warna (Tema Dark Mode)
    private final Color COLOR_BG = Color.BLACK;
    private final Color COLOR_WALL = Color.WHITE;
    private final Color COLOR_START = new Color(0, 255, 0);      // Hijau (Start)
    private final Color COLOR_END = new Color(255, 0, 0);        // Merah (Finish)
    private final Color COLOR_SOLUTION = new Color(255, 255, 0); // Kuning Neon (Jalur)
    private final Color COLOR_SEARCH_BODY = new Color(0, 255, 255, 40); // Cyan Transparan (Scan)
    private final Color COLOR_SEARCH_HEAD = new Color(0, 255, 255);     // Cyan (Kepala Scan)

    // --- Struktur Data ---
    private Cell[][] grid;
    private Cell startCell, endCell;
    private Cell currentSearchCell;
    private List<Cell> finalPath;

    // --- State ---
    private boolean isGenerating = false;
    private boolean isSolving = false;

    // Constructor
    public Maze() {
        setPreferredSize(new Dimension(COLS * CELL_SIZE + 1, ROWS * CELL_SIZE + 1));
        setBackground(COLOR_BG);
        setupGrid();
    }

    // =========================================
    // 1. Struktur Data Cell
    // =========================================
    private class Cell {
        int r, c;
        // [Top, Right, Bottom, Left] -> True = Ada Dinding
        boolean[] walls = {true, true, true, true};
        boolean visited = false;       // Untuk Prim (Generate)
        boolean searchVisited = false; // Untuk Solver (BFS/DFS)
        Cell parent = null;            // Untuk backtracking jalur

        public Cell(int r, int c) {
            this.r = r;
            this.c = c;
        }
    }

    private void setupGrid() {
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

                    // Optimasi agar generate tidak terlalu lambat
                    loopCount++;
                    if (loopCount % GEN_BATCH == 0) {
                        visualize(1);
                    }
                }
            }

            // Buka dinding di start dan end agar terlihat seperti pintu
            grid[0][0].walls[3] = false;
            grid[ROWS-1][COLS-1].walls[1] = false;

            isGenerating = false;
            repaint();
        }).start();
    }

    private void addFrontier(Cell cell, ArrayList<Cell> frontier) {
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
    // 3. Solver (BFS & DFS) dengan Animasi
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

            // --- FASE 1: Scanning (Area Biru) ---
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
                visualize(SOLVE_DELAY); // Animasi scanning
            }

            // --- FASE 2: Animasi Jalur Solusi (Garis Kuning) ---
            if (found) {
                // Rekonstruksi jalur dari End ke Start
                List<Cell> completeRoute = new ArrayList<>();
                Cell temp = endCell;
                while (temp != null) {
                    completeRoute.add(temp);
                    temp = temp.parent;
                }
                // Balik urutan jadi Start ke End
                Collections.reverse(completeRoute);

                currentSearchCell = null; // Hilangkan kotak biru "kepala"
                visualize(100); // Jeda sejenak sebelum garis kuning muncul

                // Gambar garis kuning langkah demi langkah
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
    // Helper Methods
    // =========================================
    private void removeWalls(Cell a, Cell b) {
        int dr = a.r - b.r;
        int dc = a.c - b.c;
        if (dr == 1) { a.walls[0] = false; b.walls[2] = false; }
        if (dr == -1){ a.walls[2] = false; b.walls[0] = false; }
        if (dc == 1) { a.walls[3] = false; b.walls[1] = false; }
        if (dc == -1){ a.walls[1] = false; b.walls[3] = false; }
    }

    private List<Cell> getNeighbors(Cell c, boolean onlyVisited) {
        List<Cell> list = new ArrayList<>();
        int[][] dirs = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        for (int[] d : dirs) {
            int nr = c.r + d[0], nc = c.c + d[1];
            if (isValid(nr, nc) && grid[nr][nc].visited == onlyVisited) list.add(grid[nr][nc]);
        }
        return list;
    }

    private List<Cell> getConnectedNeighbors(Cell c) {
        List<Cell> list = new ArrayList<>();
        if (!c.walls[0] && isValid(c.r - 1, c.c)) list.add(grid[c.r - 1][c.c]);
        if (!c.walls[1] && isValid(c.r, c.c + 1)) list.add(grid[c.r][c.c + 1]);
        if (!c.walls[2] && isValid(c.r + 1, c.c)) list.add(grid[c.r + 1][c.c]);
        if (!c.walls[3] && isValid(c.r, c.c - 1)) list.add(grid[c.r][c.c - 1]);
        return list;
    }

    private boolean isValid(int r, int c) { return r >= 0 && r < ROWS && c >= 0 && c < COLS; }

    private void resetSolver() {
        finalPath.clear();
        for(int r=0; r<ROWS; r++) {
            for(int c=0; c<COLS; c++) {
                grid[r][c].searchVisited = false;
                grid[r][c].parent = null;
            }
        }
        repaint();
    }

    private void visualize(int delay) {
        try { SwingUtilities.invokeLater(this::repaint); if (delay > 0) Thread.sleep(delay); } catch (Exception e) {}
    }

    // =========================================
    // Menggambar (Rendering)
    // =========================================
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. Gambar START dan END (Kotak Hijau & Merah)
        if (startCell != null) {
            g2.setColor(COLOR_START);
            g2.fillRect(startCell.c * CELL_SIZE + 4, startCell.r * CELL_SIZE + 4, CELL_SIZE - 8, CELL_SIZE - 8);
        }
        if (endCell != null) {
            g2.setColor(COLOR_END);
            g2.fillRect(endCell.c * CELL_SIZE + 4, endCell.r * CELL_SIZE + 4, CELL_SIZE - 8, CELL_SIZE - 8);
        }

        // 2. Gambar Scanning Area (Biru Transparan)
        if (isSolving) {
            g2.setColor(COLOR_SEARCH_BODY);
            for (int r = 0; r < ROWS; r++) {
                for (int c = 0; c < COLS; c++) {
                    if (grid[r][c].searchVisited) {
                        g2.fillRect(c * CELL_SIZE + 2, r * CELL_SIZE + 2, CELL_SIZE - 4, CELL_SIZE - 4);
                    }
                }
            }
            // Kepala pencari (kotak terang)
            if (currentSearchCell != null) {
                g2.setColor(COLOR_SEARCH_HEAD);
                g2.fillRect(currentSearchCell.c * CELL_SIZE + 5, currentSearchCell.r * CELL_SIZE + 5, CELL_SIZE - 10, CELL_SIZE - 10);
            }
        }

        // 3. Gambar Dinding (Garis Putih)
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

        // 4. Gambar Jalur Solusi (Garis Kuning) - Layer Paling Atas
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

    // =========================================
    // Main Method
    // =========================================
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}

            JFrame frame = new JFrame("Maze Generator & Solver");
            Maze mazePanel = new Maze(); // Menggunakan class Maze

            JPanel btnPanel = new JPanel();
            btnPanel.setBackground(Color.DARK_GRAY);

            JButton btnGen = new JButton("1. Generate");
            JButton btnBFS = new JButton("2. Solve BFS");
            JButton btnDFS = new JButton("3. Solve DFS");

            Font btnFont = new Font("Arial", Font.BOLD, 12);
            for(JButton b : new JButton[]{btnGen, btnBFS, btnDFS}) { b.setFont(btnFont); b.setFocusPainted(false); }

            btnGen.addActionListener(e -> mazePanel.generatePrim());
            btnBFS.addActionListener(e -> mazePanel.solve(true));
            btnDFS.addActionListener(e -> mazePanel.solve(false));

            btnPanel.add(btnGen); btnPanel.add(btnBFS); btnPanel.add(btnDFS);

            frame.setLayout(new BorderLayout());
            frame.add(mazePanel, BorderLayout.CENTER);
            frame.add(btnPanel, BorderLayout.SOUTH);

            frame.pack();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.setResizable(false);
            frame.setVisible(true);
        });
    }
}