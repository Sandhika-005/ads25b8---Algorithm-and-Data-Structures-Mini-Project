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

    private static final int BOARD_W = 850;
    private static final int BOARD_H = 627;
    private static final int CONTROL_W = 280;
    private static final int LEAD_W = 200;

    public GameVisualizer() {
        setTitle("Victoria Island Adventure: Petualangan di Pulau Victoria");
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

        // setSize(BOARD_W + CONTROL_W + LEAD_W, BOARD_H + 50); -- Dihapus, menggunakan pack() di main
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

        // --- KOREKSI: Setting background mainPanel untuk mencegah painting leak ---
        // Warna gelap yang konsisten (sama seperti background ControlPanel)
        mainPanel.setBackground(new Color(45, 60, 80));
        // --- AKHIR KOREKSI ---

        mainPanel.add(boardPanel, BorderLayout.CENTER);

        boardPanel.setPreferredSize(new Dimension(BOARD_W, BOARD_H));

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
        panel.setPreferredSize(new Dimension(LEAD_W, CONTROL_W));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel title = new JLabel("LEADERBOARD");
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setForeground(new Color(255, 200, 100));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createVerticalStrut(20));

        this.topWinsLabel = new JLabel("<html><center><b>🏆 TOP 3 WINS 🏆</b></center><br>Belum ada data.</html>");
        this.topWinsLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        this.topWinsLabel.setForeground(Color.WHITE);
        this.topWinsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(this.topWinsLabel);
        panel.add(Box.createVerticalStrut(20));

        this.topScoresLabel = new JLabel("<html><center><b>💰 TOP 3 HIGH SCORES 💰</b></center><br>Belum ada data.</html>");
        this.topScoresLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        this.topScoresLabel.setForeground(Color.WHITE);
        this.topScoresLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(this.topScoresLabel);
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
            if (this.topScoresLabel != null) {
                this.topScoresLabel.setText(formattedScores);
            }
            if (this.topWinsLabel != null) {
                this.topWinsLabel.setText(formattedWins);
            }
        });
    }


    public static void main(String[] args) {
        JFrame.setDefaultLookAndFeelDecorated(true);
        SwingUtilities.invokeLater(() -> {
            GameVisualizer frame = new GameVisualizer();
            frame.pack();
            frame.setVisible(true);
        });
    }
}