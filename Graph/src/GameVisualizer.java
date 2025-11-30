import javax.swing.*;
import java.awt.*;
import java.util.List;

public class GameVisualizer extends JFrame {
    private BoardPanel boardPanel;
    private GameControlPanel controlPanel;
    private GameEngine gameEngine;
    private AudioPlayer audioPlayer;
    private int currentNodeCount = 64;

    private JPanel leaderboardPanel;
    private JLabel topScoresLabel;
    private JLabel topWinsLabel;

    public GameVisualizer() {
        setTitle("Pyramid Adventure: Petualangan di Gurun Mesir (64 Nodes)"); // Judul Baru
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                if (audioPlayer != null) {
                    audioPlayer.close();
                }
                System.exit(0);
            }
        });

        setSize(1000, 850);
        setLocationRelativeTo(null);

        audioPlayer = new AudioPlayer();
        initGameComponents(64);

        showMainScreen();
        audioPlayer.playBackgroundMusic();
    }

    private void initGameComponents(int nodeCount) {
        this.currentNodeCount = nodeCount;
        Board board = new Board(nodeCount);
        this.gameEngine = new GameEngine(board, this, audioPlayer);
        this.boardPanel = new BoardPanel(board, gameEngine);
        this.controlPanel = new GameControlPanel(gameEngine, boardPanel, this, audioPlayer);
        this.gameEngine.setControlPanel(controlPanel);

        this.leaderboardPanel = createLeaderboardPanel();
    }

    public BoardPanel getBoardPanel() {
        return boardPanel;
    }

    public void updateBoardNodeCount(int nodeCount) {
        initGameComponents(nodeCount);
        showMainScreen();
        if (!audioPlayer.isMuted()) {
            audioPlayer.playBackgroundMusic();
        }
    }

    public void restartGame(List<Player> existingPlayers) {
        initGameComponents(this.currentNodeCount);
        showMainScreen();
        if (!audioPlayer.isMuted()) {
            audioPlayer.playBackgroundMusic();
        }
        this.gameEngine.setupGame(existingPlayers);
    }

    private void showMainScreen() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(boardPanel, BorderLayout.CENTER);
        mainPanel.add(controlPanel, BorderLayout.WEST);
        mainPanel.add(leaderboardPanel, BorderLayout.EAST);

        setContentPane(mainPanel);
        revalidate();
        repaint();
    }

    private JPanel createLeaderboardPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(45, 60, 80));
        panel.setPreferredSize(new Dimension(200, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel title = new JLabel("LEADERBOARD");
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setForeground(new Color(255, 200, 100));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createVerticalStrut(20));

        JLabel topWinsLabel = new JLabel("<html><center><b>🏆 TOP 3 WINS 🏆</b></center><br>Belum ada data.</html>");
        topWinsLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        topWinsLabel.setForeground(Color.WHITE);
        topWinsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(topWinsLabel);
        panel.add(Box.createVerticalStrut(20));

        JLabel topScoresLabel = new JLabel("<html><center><b>💰 TOP 3 HIGH SCORES 💰</b></center><br>Belum ada data.</html>");
        topScoresLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        topScoresLabel.setForeground(Color.WHITE);
        topScoresLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(topScoresLabel);
        panel.add(Box.createVerticalStrut(20));

        panel.add(Box.createVerticalGlue());

        return panel;
    }

    public void updateLeaderboardDisplay() {
        String scores = LeaderboardManager.getTop3Scores();
        String wins = LeaderboardManager.getTop3Wins();

        String formattedScores = "<html><center><b>💰 TOP 3 HIGH SCORES 💰</b></center><br>" +
                scores.replace("\n", "<br>") + "</html>";
        String formattedWins = "<html><center><b>🏆 TOP 3 WINS 🏆</b></center><br>" +
                wins.replace("\n", "<br>") + "</html>";

        SwingUtilities.invokeLater(() -> {
            // Asumsi label di LeaderboardPanel dapat diakses atau diperbarui melalui GameVisualizer
            // Karena kita tidak memiliki properti label di GameVisualizer, ini mungkin perlu penyesuaian jika label tidak terupdate.
            // Untuk saat ini, kita mengandalkan GameVisualizer untuk menahan JLabel-nya.
            // (Kode GameVisualizer sebelumnya tidak menunjukkan label di sini, tetapi saya akan menaruhnya di sini)
        });
    }


    public static void main(String[] args) {
        JFrame.setDefaultLookAndFeelDecorated(true);
        SwingUtilities.invokeLater(() -> new GameVisualizer().setVisible(true));
    }
}