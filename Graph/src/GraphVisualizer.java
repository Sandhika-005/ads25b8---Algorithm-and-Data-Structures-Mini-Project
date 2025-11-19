import javax.swing.*;
import java.awt.*;

public class GraphVisualizer extends JFrame {
    private GraphPanel graphPanel;
    private ControlPanel controlPanel;

    public GraphVisualizer() {
        setTitle("Dynamic Weighted Graph Manager with Dijkstra");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1300, 850);
        setLocationRelativeTo(null);

        UnifiedGraph graph = new UnifiedGraph();
        controlPanel = new ControlPanel(this.graphPanel);
        graphPanel = new GraphPanel(graph, this, controlPanel);
        controlPanel.setGraphPanel(graphPanel);

        showMainScreen();
    }

    private void showMainScreen() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(graphPanel, BorderLayout.CENTER);
        mainPanel.add(controlPanel, BorderLayout.WEST);
        setContentPane(mainPanel);
        revalidate();
        repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GraphVisualizer().setVisible(true));
    }
}