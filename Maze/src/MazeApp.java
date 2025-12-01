import javax.swing.*;
import java.awt.*;

public class MazeApp {
    private JFrame frame;
    private JPanel mainContainer;
    private CardLayout cardLayout;

    // Panel Instance
    private Maze originalMazePanel;
    private WeightedMaze weightedMazePanel;

    public MazeApp() {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}

        frame = new JFrame("Aplikasi Maze: Original vs Weighted Graph");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1050, 800);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);

        // Layout Utama: CardLayout untuk berganti antar panel
        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);

        // --- 1. MENU UTAMA ---
        JPanel menuPanel = new JPanel(new GridBagLayout());
        menuPanel.setBackground(new Color(30, 30, 30));

        JLabel titleLabel = new JLabel("Pilih Mode Maze");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);

        JButton btnOriginal = createStyledButton("Mode 1: Maze Original (DFS/BFS)");
        JButton btnWeighted = createStyledButton("Mode 2: Weighted Graph (Dijkstra/A*)");

        btnOriginal.addActionListener(e -> cardLayout.show(mainContainer, "ORIGINAL"));
        btnWeighted.addActionListener(e -> cardLayout.show(mainContainer, "WEIGHTED"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.gridx = 0; gbc.gridy = 0;
        menuPanel.add(titleLabel, gbc);
        gbc.gridy = 1;
        menuPanel.add(btnOriginal, gbc);
        gbc.gridy = 2;
        menuPanel.add(btnWeighted, gbc);

        // --- 2. SETUP MODE ORIGINAL (Maze.java) ---
        JPanel originalContainer = new JPanel(new BorderLayout());
        originalMazePanel = new Maze();

        JPanel originalControls = new JPanel();
        originalControls.setBackground(Color.DARK_GRAY);
        JButton btnGenOrig = new JButton("Generate Prim");
        JButton btnBFS = new JButton("Solve BFS");
        JButton btnDFS = new JButton("Solve DFS");
        JButton btnBack1 = new JButton("<< Kembali");

        btnGenOrig.addActionListener(e -> originalMazePanel.generatePrim());
        btnBFS.addActionListener(e -> originalMazePanel.solve(true));
        btnDFS.addActionListener(e -> originalMazePanel.solve(false));
        btnBack1.addActionListener(e -> cardLayout.show(mainContainer, "MENU"));

        originalControls.add(btnBack1);
        originalControls.add(btnGenOrig);
        originalControls.add(btnBFS);
        originalControls.add(btnDFS);

        originalContainer.add(originalMazePanel, BorderLayout.CENTER);
        originalContainer.add(originalControls, BorderLayout.SOUTH);

        // --- 3. SETUP MODE WEIGHTED (WeightedMaze.java) ---
        JPanel weightedContainer = new JPanel(new BorderLayout());
        weightedMazePanel = new WeightedMaze();

        JPanel weightedControls = new JPanel();
        weightedControls.setBackground(Color.DARK_GRAY);
        JButton btnGenWeight = new JButton("Generate Terrain");
        JButton btnDijkstra = new JButton("Solve Dijkstra");
        JButton btnAStar = new JButton("Solve A*");
        JButton btnBack2 = new JButton("<< Kembali");

        btnGenWeight.addActionListener(e -> weightedMazePanel.generateWeightedTerrain());
        btnDijkstra.addActionListener(e -> weightedMazePanel.solveWeighted(false)); // false = Dijkstra
        btnAStar.addActionListener(e -> weightedMazePanel.solveWeighted(true));     // true = A*
        btnBack2.addActionListener(e -> cardLayout.show(mainContainer, "MENU"));

        weightedControls.add(btnBack2);
        weightedControls.add(btnGenWeight);
        weightedControls.add(btnDijkstra);
        weightedControls.add(btnAStar);

        weightedContainer.add(weightedMazePanel, BorderLayout.CENTER);
        weightedContainer.add(weightedControls, BorderLayout.SOUTH);

        // Tambahkan ke Container Utama
        mainContainer.add(menuPanel, "MENU");
        mainContainer.add(originalContainer, "ORIGINAL");
        mainContainer.add(weightedContainer, "WEIGHTED");

        frame.add(mainContainer);
        frame.setVisible(true);
    }

    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 16));
        btn.setPreferredSize(new Dimension(350, 50));
        btn.setFocusPainted(false);
        return btn;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MazeApp::new);
    }
}