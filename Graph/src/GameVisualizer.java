import javax.swing.*;
import java.awt.*;
import java.util.List;

public class GameVisualizer extends JFrame {
    private BoardPanel boardPanel;
    private GameControlPanel controlPanel;
    private GameEngine gameEngine;
    private AudioPlayer audioPlayer;
    private int currentNodeCount = 64;

    public GameVisualizer() {
        setTitle("Dynamic Snake & Ladder Game (64 Nodes)");
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
    }

    // --- METHOD BARU: AKSESOR UNTUK ENGINE ---
    public BoardPanel getBoardPanel() {
        return boardPanel;
    }
    // -----------------------------------------

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

        setContentPane(mainPanel);
        revalidate();
        repaint();
    }

    public static void main(String[] args) {
        JFrame.setDefaultLookAndFeelDecorated(true);
        SwingUtilities.invokeLater(() -> new GameVisualizer().setVisible(true));
    }
}