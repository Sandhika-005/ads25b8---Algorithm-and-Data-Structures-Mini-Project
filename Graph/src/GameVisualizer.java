import javax.swing.*;
import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class GameVisualizer extends JFrame {
    private BoardPanel boardPanel;
    private GameControlPanel controlPanel;
    private GameEngine gameEngine;

    // Game log UI (moved from control panel to opposite side)
    private JTextArea gameLogArea;
    private JScrollPane gameLogScroll;
    private final DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm:ss");

    public GameVisualizer() {
        setTitle("Dynamic Snake & Ladder Game (64 Nodes)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1300, 850);
        setLocationRelativeTo(null);

        Board board = new Board(64);
        gameEngine = new GameEngine(board, this);
        boardPanel = new BoardPanel(board, gameEngine);
        controlPanel = new GameControlPanel(gameEngine, boardPanel, this);

        gameEngine.setControlPanel(controlPanel);

        // initialize game log UI
        gameLogArea = new JTextArea();
        gameLogArea.setEditable(false);
        gameLogArea.setLineWrap(true);
        gameLogArea.setWrapStyleWord(true);
        gameLogArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        gameLogScroll = new JScrollPane(gameLogArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        gameLogScroll.setPreferredSize(new Dimension(300, 800));
        gameLogScroll.setMinimumSize(new Dimension(250, 200));

        showMainScreen();

        SwingUtilities.invokeLater(() -> gameEngine.promptForPlayers());
    }

    // Rebuild board/game with new node count (called from GameControlPanel)
    public void updateBoardNodeCount(int nodeCount) {
        Board board = new Board(nodeCount);
        this.gameEngine = new GameEngine(board, this);
        this.boardPanel = new BoardPanel(board, gameEngine);
        this.controlPanel = new GameControlPanel(gameEngine, boardPanel, this);
        this.gameEngine.setControlPanel(controlPanel);
        showMainScreen();
        SwingUtilities.invokeLater(() -> gameEngine.promptForPlayers());
    }

    private void showMainScreen() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(boardPanel, BorderLayout.CENTER);
        mainPanel.add(controlPanel, BorderLayout.WEST);

        // create log container on the right (EAST)
        JPanel logContainer = new JPanel(new BorderLayout());
        JLabel logTitle = new JLabel("Game Log", SwingConstants.CENTER);
        logTitle.setFont(new Font("Arial", Font.BOLD, 16));
        logContainer.add(logTitle, BorderLayout.NORTH);
        logContainer.add(gameLogScroll, BorderLayout.CENTER);
        logContainer.setPreferredSize(new Dimension(300, 800));
        mainPanel.add(logContainer, BorderLayout.EAST);

        setContentPane(mainPanel);
        revalidate();
        repaint();
    }

    // Called by GameControlPanel / GameEngine to append lines to the game log (thread-safe)
    public void appendToGameLog(String message) {
        if (gameLogArea == null) return;
        String time = LocalTime.now().format(timeFmt);
        SwingUtilities.invokeLater(() -> {
            gameLogArea.append("[" + time + "] " + message + "\n");
            gameLogArea.setCaretPosition(gameLogArea.getDocument().getLength());
        });
    }

    public void clearGameLog() {
        if (gameLogArea == null) return;
        SwingUtilities.invokeLater(() -> gameLogArea.setText(""));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GameVisualizer().setVisible(true));
    }
}