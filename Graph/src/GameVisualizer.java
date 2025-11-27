import javax.swing.*;
import java.awt.*;

public class GameVisualizer extends JFrame {
    private BoardPanel boardPanel;
    private GameControlPanel controlPanel;
    private GameEngine gameEngine;

    public GameVisualizer() {
        setTitle("Dynamic Snake & Ladder Game (64 Nodes)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 850); // Ukuran disesuaikan karena log dihapus
        setLocationRelativeTo(null);

        Board board = new Board(64);
        gameEngine = new GameEngine(board, this);
        boardPanel = new BoardPanel(board, gameEngine);
        controlPanel = new GameControlPanel(gameEngine, boardPanel, this);

        gameEngine.setControlPanel(controlPanel);

        showMainScreen();

        // Do not auto-prompt for players here. User should set node count and press START GAME.
    }

    // Rebuild board/game with new node count
    public void updateBoardNodeCount(int nodeCount) {
        Board board = new Board(nodeCount);
        this.gameEngine = new GameEngine(board, this);
        this.boardPanel = new BoardPanel(board, gameEngine);
        this.controlPanel = new GameControlPanel(gameEngine, boardPanel, this);
        this.gameEngine.setControlPanel(controlPanel);
        showMainScreen();
        // Do not auto-prompt after changing node count so user can fine-tune nodes and then press START GAME.
    }

    private void showMainScreen() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(boardPanel, BorderLayout.CENTER);
        mainPanel.add(controlPanel, BorderLayout.WEST);

        // Log container dihapus

        setContentPane(mainPanel);
        revalidate();
        repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GameVisualizer().setVisible(true));
    }
}