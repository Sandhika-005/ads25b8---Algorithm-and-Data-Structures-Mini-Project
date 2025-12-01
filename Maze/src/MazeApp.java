import javax.swing.*;
import java.awt.*;

public class MazeApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Hapus UIManager.setLookAndFeel agar warna tombol bekerja dengan benar

            JFrame frame = new JFrame("All-in-One Maze Solver (Responsive)");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            // Start Maximize (Fullscreen Windowed) dan BISA DI-RESIZE
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            frame.setResizable(true);

            WeightedMaze mazePanel = new WeightedMaze();

            // --- Panel Utama untuk Kontrol ---
            JPanel controlPanel = new JPanel(new GridLayout(2, 1, 5, 5));
            controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            controlPanel.setBackground(new Color(45, 45, 45));

            // Panel Generator
            JPanel genPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
            genPanel.setOpaque(false);

            JButton btnStandard = createButton("1. Standard Maze", new Color(80, 80, 80));
            JButton btnTerrain = createButton("2. Terrain Map", new Color(34, 139, 34));

            genPanel.add(btnStandard);
            genPanel.add(btnTerrain);

            // Panel Solver
            JPanel solvePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
            solvePanel.setOpaque(false);

            JButton btnBFS = createButton("BFS", new Color(0, 100, 200));
            JButton btnDFS = createButton("DFS", new Color(128, 0, 128));
            JButton btnDijkstra = createButton("Dijkstra", new Color(200, 60, 0));
            JButton btnAStar = createButton("A*", new Color(210, 180, 0));
            btnAStar.setForeground(Color.BLACK);

            solvePanel.add(btnBFS);
            solvePanel.add(btnDFS);
            solvePanel.add(btnDijkstra);
            solvePanel.add(btnAStar);

            controlPanel.add(genPanel);
            controlPanel.add(solvePanel);

            // --- Action Listeners ---
            btnStandard.addActionListener(e -> mazePanel.generatePrim());
            btnTerrain.addActionListener(e -> mazePanel.generateWeightedTerrain());
            btnBFS.addActionListener(e -> mazePanel.solve(true));
            btnDFS.addActionListener(e -> mazePanel.solve(false));
            btnDijkstra.addActionListener(e -> mazePanel.solveWeighted(false));
            btnAStar.addActionListener(e -> mazePanel.solveWeighted(true));

            // Layout Frame
            frame.setLayout(new BorderLayout());
            // Maze diletakkan di CENTER tanpa wrapper agar mengisi seluruh area
            frame.add(mazePanel, BorderLayout.CENTER);
            frame.add(controlPanel, BorderLayout.SOUTH);

            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    private static JButton createButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);

        // Trik agar warna background muncul di semua OS
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);

        btn.setPreferredSize(new Dimension(170, 45));
        return btn;
    }
}