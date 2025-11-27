import javax.swing.*;
import java.awt.*;
import java.io.File;

public class GameVisualizer extends JFrame {
    private BoardPanel boardPanel;
    private GameControlPanel controlPanel;
    private GameEngine gameEngine;

    // --- AUDIO BARU ---
    private AudioPlayer audioPlayer;
    // --- AUDIO BARU ---

    public GameVisualizer() {
        setTitle("Dynamic Snake & Ladder Game (64 Nodes)");

        // Menambahkan Window Listener untuk menutup audio saat aplikasi ditutup
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                if (audioPlayer != null) {
                    audioPlayer.close();
                }
                // Pastikan exit setelah semua resource ditutup
                System.exit(0);
            }
        });

        setSize(1000, 850);
        setLocationRelativeTo(null);

        // --- AUDIO BARU: Inisialisasi AudioPlayer di awal
        audioPlayer = new AudioPlayer();
        // --- AUDIO BARU ---

        Board board = new Board(64);
        // --- UBAH: Kirim AudioPlayer ke GameEngine
        gameEngine = new GameEngine(board, this, audioPlayer);
        boardPanel = new BoardPanel(board, gameEngine);

        // --- UBAH: Kirim AudioPlayer ke GameControlPanel
        controlPanel = new GameControlPanel(gameEngine, boardPanel, this, audioPlayer);

        gameEngine.setControlPanel(controlPanel);

        showMainScreen();

        // --- AUDIO BARU: Mulai Backsound
        audioPlayer.playBackgroundMusic();
        // --- AUDIO BARU ---

        // Do not auto-prompt for players here. User should set node count and press START GAME.
    }

    // Rebuild board/game with new node count
    public void updateBoardNodeCount(int nodeCount) {
        Board board = new Board(nodeCount);
        // --- UBAH: Kirim AudioPlayer ke GameEngine baru
        this.gameEngine = new GameEngine(board, this, audioPlayer);
        this.boardPanel = new BoardPanel(board, gameEngine);

        // --- UBAH: Kirim AudioPlayer ke GameControlPanel baru
        this.controlPanel = new GameControlPanel(gameEngine, boardPanel, this, audioPlayer);
        this.gameEngine.setControlPanel(controlPanel);
        showMainScreen();

        // Mulai ulang backsound jika sedang tidak di-mute
        if (!audioPlayer.isMuted()) {
            audioPlayer.playBackgroundMusic();
        }
        // Do not auto-prompt after changing node count so user can fine-tune nodes and then press START GAME.
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
        GameVisualizer frame = new GameVisualizer();
        // Set default close operation ke DO_NOTHING agar Window Listener yang menangani penutupan
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        SwingUtilities.invokeLater(() -> frame.setVisible(true));
    }
}