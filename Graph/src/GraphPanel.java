//import javax.swing.*;
//import java.awt.*;
//import java.awt.event.*;
//import java.util.List;
//import java.util.stream.Collectors;
//
//class GraphPanel extends JPanel {
//    public enum Mode { NAVIGATING, ADDING_NODE }
//
//    private UnifiedGraph graph;
//    private GraphVisualizer mainApp;
//    private ControlPanel controlPanel;
//    private UNode draggedNode;
//    private UNode hoveredNode;
//    private List<Integer> dijkstraPath;
//    private Timer animationTimer;
//    private int animationStep = 0;
//    private Mode currentMode = Mode.NAVIGATING;
//
//    private final Color PATH_COLOR = new Color(255, 100, 50);
//
//    public GraphPanel(UnifiedGraph graph, GraphVisualizer mainApp, ControlPanel cp) {
//        this.graph = graph;
//        this.mainApp = mainApp;
//        this.controlPanel = cp;
//        setLayout(null);
//        setBackground(new Color(30, 40, 60));
//        setupUI();
//        setupMouseListeners();
//    }
//
//    public UnifiedGraph getGraph() { return graph; }
//    public void setMode(Mode newMode) {
//        this.currentMode = newMode;
//        setCursor(newMode == Mode.ADDING_NODE ? Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR) : Cursor.getDefaultCursor());
//    }
//
//    private void setupUI() {
//        JButton runDijkstraButton = createControlButton("Run Dijkstra", new Color(220, 100, 50), 150, 20, 200, 40);
//        runDijkstraButton.addActionListener(e -> runDijkstra());
//        add(runDijkstraButton);
//
//        JButton resetButton = createControlButton("Reset Colors", new Color(150, 50, 150), 360, 20, 150, 40);
//        resetButton.addActionListener(e -> resetVisualization());
//        add(resetButton);
//
//        JButton matrixButton = createControlButton("Data Matrix", new Color(50, 150, 200), 520, 20, 150, 40);
//        matrixButton.addActionListener(e -> showMatrixDialog());
//        add(matrixButton);
//
//        JButton clearButton = createControlButton("Clear All", new Color(200, 50, 50), 680, 20, 150, 40);
//        clearButton.addActionListener(e -> clearAllNodes());
//        add(clearButton);
//    }
//
//    private JButton createControlButton(String text, Color bg, int x, int y, int w, int h) {
//        JButton button = new JButton(text);
//        button.setFont(new Font("Arial", Font.BOLD, 14));
//        button.setBackground(bg);
//        button.setForeground(Color.WHITE);
//        button.setFocusPainted(false);
//        button.setBounds(x, y, w, h);
//        return button;
//    }
//
//    private void setupMouseListeners() {
//        addMouseListener(new MouseAdapter() {
//            @Override
//            public void mousePressed(MouseEvent e) {
//                UNode clickedNode = findNodeAt(e.getX(), e.getY());
//                if (SwingUtilities.isLeftMouseButton(e)) {
//                    draggedNode = clickedNode;
//                }
//            }
//
//            @Override
//            public void mouseReleased(MouseEvent e) {
//                draggedNode = null;
//            }
//
//            @Override
//            public void mouseClicked(MouseEvent e) {
//                UNode clickedNode = findNodeAt(e.getX(), e.getY());
//
//                if (currentMode == Mode.ADDING_NODE && SwingUtilities.isLeftMouseButton(e)) {
//                    if (clickedNode == null) {
//                        graph.addNode(e.getX(), e.getY());
//                        controlPanel.updateNodeLists();
//                        setMode(Mode.NAVIGATING);
//                        resetVisualization();
//                    }
//                } else if (e.getClickCount() == 2 && clickedNode != null) {
//                    editNodeLabel(clickedNode);
//                } else if (SwingUtilities.isRightMouseButton(e)) {
//                    if (clickedNode != null) {
//                        showRightClickMenu(e, clickedNode);
//                    } else {
//                        // if not clicking a node, check for an edge under cursor
//                        UEdge clickedEdge = findEdgeAt(e.getX(), e.getY());
//                        if (clickedEdge != null) {
//                            showEdgeContextMenu(e, clickedEdge);
//                        }
//                    }
//                }
//            }
//        });
//
//        addMouseMotionListener(new MouseMotionAdapter() {
//            @Override
//            public void mouseDragged(MouseEvent e) {
//                if (draggedNode != null) {
//                    draggedNode.setX(e.getX());
//                    draggedNode.setY(e.getY());
//                    repaint();
//                }
//            }
//            @Override
//            public void mouseMoved(MouseEvent e) {
//                hoveredNode = findNodeAt(e.getX(), e.getY());
//                for (UNode node : graph.getNodes()) {
//                    node.setHighlighted(node.equals(hoveredNode));
//                }
//                repaint();
//            }
//        });
//    }
//
//    private UNode findNodeAt(int x, int y) {
//        for (UNode node : graph.getNodes()) {
//            if (node.contains(x, y)) {
//                return node;
//            }
//        }
//        return null;
//    }
//
//    private void showRightClickMenu(MouseEvent e, UNode node) {
//        JPopupMenu menu = new JPopupMenu();
//
//        JMenuItem deleteNodeItem = new JMenuItem("Delete Node " + node.getLabel());
//        deleteNodeItem.addActionListener(ev -> deleteNode(node));
//        menu.add(deleteNodeItem);
//
//        JMenuItem renameItem = new JMenuItem("Rename Node " + node.getLabel());
//        renameItem.addActionListener(ev -> editNodeLabel(node));
//        menu.add(renameItem);
//
//        // list adjacent edges properly and provide delete/edit-weight options
//        boolean anyAdjacent = false;
//        for (UEdge edge : graph.getEdges()) {
//            if (edge.getSource().equals(node) || edge.getTarget().equals(node)) {
//                if (!anyAdjacent) {
//                    menu.addSeparator();
//                    JLabel subtitle = new JLabel("Adjacent Edges:");
//                    subtitle.setFont(new Font("Arial", Font.BOLD, 12));
//                    menu.add(subtitle);
//                    anyAdjacent = true;
//                }
//                UNode neighbor = edge.getSource().equals(node) ? edge.getTarget() : edge.getSource();
//                JMenuItem deleteEdgeItem = new JMenuItem("Delete edge to " + neighbor.getLabel() + " (W:" + edge.getWeight() + ")");
//                deleteEdgeItem.addActionListener(ev -> deleteEdge(edge));
//                menu.add(deleteEdgeItem);
//
//                JMenuItem editWeightItem = new JMenuItem("Edit weight to " + neighbor.getLabel() + " (W:" + edge.getWeight() + ")");
//                editWeightItem.addActionListener(ev -> editEdgeWeight(edge));
//                menu.add(editWeightItem);
//            }
//        }
//
//        menu.show(e.getComponent(), e.getX(), e.getY());
//    }
//
//    // New: context menu when right-click directly on an edge
//    private void showEdgeContextMenu(MouseEvent e, UEdge edge) {
//        JPopupMenu menu = new JPopupMenu();
//
//        String label = edge.getSource().getLabel() + " ↔ " + edge.getTarget().getLabel();
//        JMenuItem info = new JMenuItem("Edge " + label + " (W:" + edge.getWeight() + ")");
//        info.setEnabled(false);
//        menu.add(info);
//
//        JMenuItem editWeight = new JMenuItem("Edit Weight...");
//        editWeight.addActionListener(ev -> editEdgeWeight(edge));
//        menu.add(editWeight);
//
//        JMenuItem delete = new JMenuItem("Delete Edge");
//        delete.addActionListener(ev -> deleteEdge(edge));
//        menu.add(delete);
//
//        menu.show(e.getComponent(), e.getX(), e.getY());
//    }
//
//    // New: allow changing edge weight with validation, then repaint
//    private void editEdgeWeight(UEdge edge) {
//        String current = String.valueOf(edge.getWeight());
//        String input = (String) JOptionPane.showInputDialog(this,
//                "Enter new weight for edge " + edge.getSource().getLabel() + " ↔ " + edge.getTarget().getLabel() + ":",
//                "Edit Edge Weight",
//                JOptionPane.PLAIN_MESSAGE,
//                null,
//                null,
//                current);
//        if (input == null) return;
//        try {
//            int w = Integer.parseInt(input.trim());
//            if (w <= 0) {
//                JOptionPane.showMessageDialog(this, "Weight must be a positive integer.", "Error", JOptionPane.ERROR_MESSAGE);
//                return;
//            }
//            edge.setWeight(w);
//            resetVisualization();
//            repaint();
//        } catch (NumberFormatException ex) {
//            JOptionPane.showMessageDialog(this, "Invalid number.", "Error", JOptionPane.ERROR_MESSAGE);
//        }
//    }
//
//    // New: detect an edge near the given point (distance to segment)
//    private UEdge findEdgeAt(int px, int py) {
//        final double THRESH = 8.0; // pixels tolerance
//        for (UEdge edge : graph.getEdges()) {
//            UNode a = edge.getSource();
//            UNode b = edge.getTarget();
//            double ax = a.getX(), ay = a.getY();
//            double bx = b.getX(), by = b.getY();
//            double abx = bx - ax, aby = by - ay;
//            double apx = px - ax, apy = py - ay;
//            double ab2 = abx * abx + aby * aby;
//            if (ab2 == 0) continue;
//            double t = (apx * abx + apy * aby) / ab2;
//            if (t < 0) t = 0;
//            if (t > 1) t = 1;
//            double cx = ax + t * abx;
//            double cy = ay + t * aby;
//            double dx = px - cx;
//            double dy = py - cy;
//            double dist2 = dx * dx + dy * dy;
//            if (dist2 <= THRESH * THRESH) {
//                return edge;
//            }
//        }
//        return null;
//    }
//
//    private void deleteNode(UNode node) {
//        if (graph.removeNode(node)) {
//            controlPanel.updateNodeLists();
//            resetVisualization();
//        }
//    }
//
//    private void deleteEdge(UEdge edge) {
//        if (graph.removeEdge(edge)) {
//            resetVisualization();
//        }
//    }
//
//    private void clearAllNodes() {
//        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete all nodes and edges?", "Confirm Clear", JOptionPane.YES_NO_OPTION);
//        if (confirm == JOptionPane.YES_OPTION) {
//            graph.getNodes().clear();
//            graph.getEdges().clear();
//            controlPanel.updateNodeLists();
//            resetVisualization();
//        }
//    }
//
//    private void editNodeLabel(UNode node) {
//        String newLabel = JOptionPane.showInputDialog(this,
//                "Enter new label for node " + node.getLabel() + ":",
//                node.getLabel());
//        if (newLabel != null && !newLabel.trim().isEmpty()) {
//            if (graph.getNodeByLabel(newLabel.trim()) != null && !graph.getNodeByLabel(newLabel.trim()).equals(node)) {
//                JOptionPane.showMessageDialog(this, "Label already in use!", "Error", JOptionPane.ERROR_MESSAGE);
//                return;
//            }
//            graph.updateNodeLabel(node.getId(), newLabel.trim());
//            controlPanel.updateNodeLists();
//            repaint();
//        }
//    }
//
//    private void runDijkstra() {
//        if (graph.getNodes().size() < 2) {
//            JOptionPane.showMessageDialog(this, "Need at least two nodes for Dijkstra's.", "Error", JOptionPane.ERROR_MESSAGE);
//            return;
//        }
//
//        resetVisualization();
//        String[] nodeLabels = graph.getNodeLabels();
//
//        String sourceLabel = (String) JOptionPane.showInputDialog(this, "Select source node:", "Dijkstra - Source", JOptionPane.QUESTION_MESSAGE, null, nodeLabels, nodeLabels[0]);
//        if (sourceLabel == null) return;
//        String targetLabel = (String) JOptionPane.showInputDialog(this, "Select target node:", "Dijkstra - Target", JOptionPane.QUESTION_MESSAGE, null, nodeLabels, nodeLabels[nodeLabels.length-1]);
//        if (targetLabel == null) return;
//
//        UNode source = graph.getNodeByLabel(sourceLabel);
//        UNode target = graph.getNodeByLabel(targetLabel);
//
//        DijkstraAlgorithm dijkstra = new DijkstraAlgorithm(graph);
//        List<UNode> pathNodes = dijkstra.findShortestPath(source, target);
//
//        if (pathNodes == null) {
//            JOptionPane.showMessageDialog(this, "No path exists from " + source.getLabel() + " to " + target.getLabel() + "!", "Result", JOptionPane.INFORMATION_MESSAGE);
//            return;
//        }
//
//        // store the path node IDs (no reversed() call)
//        dijkstraPath = pathNodes.stream().map(UNode::getId).collect(Collectors.toList());
//
//        animateDijkstraPath(pathNodes);
//
//        int distance = dijkstra.getDistance(target);
//        JOptionPane.showMessageDialog(this,
//                "Shortest path: " + getPathString(pathNodes) + "\nTotal distance: " + distance,
//                "Dijkstra Result", JOptionPane.INFORMATION_MESSAGE);
//    }
//
//    private void animateDijkstraPath(List<UNode> pathNodes) {
//        animationStep = 0;
//        if (animationTimer != null) animationTimer.stop();
//
//        animationTimer = new Timer(500, new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                if (animationStep >= pathNodes.size()) {
//                    animationTimer.stop();
//                    return;
//                }
//
//                UNode currentNode = pathNodes.get(animationStep);
//                currentNode.setColor(PATH_COLOR);
//                currentNode.setInPath(true);
//
//                if (animationStep > 0) {
//                    UNode prevNode = pathNodes.get(animationStep - 1);
//                    highlightEdge(prevNode, currentNode);
//                }
//
//                animationStep++;
//                repaint();
//            }
//        });
//        animationTimer.start();
//    }
//
//    private void highlightEdge(UNode from, UNode to) {
//        UEdge edge = graph.getEdge(from, to);
//        if (edge != null) {
//            edge.setColor(PATH_COLOR);
//            edge.setInPath(true);
//        }
//    }
//
//    private String getPathString(List<UNode> path) {
//        return path.stream()
//                .map(UNode::getLabel)
//                .collect(Collectors.joining(" → "));
//    }
//
//    public void resetVisualization() {
//        if (animationTimer != null) animationTimer.stop();
//        for (UNode node : graph.getNodes()) {
//            node.resetVisualization();
//        }
//        for (UEdge edge : graph.getEdges()) {
//            edge.resetVisualization();
//        }
//        dijkstraPath = null;
//        animationStep = 0;
//        repaint();
//    }
//
//    private void showMatrixDialog() {
//        int n = graph.getNodes().size();
//        if (n == 0) {
//            JOptionPane.showMessageDialog(this, "The graph is empty.", "Data View", JOptionPane.INFORMATION_MESSAGE);
//            return;
//        }
//
//        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Weight Matrix", true);
//        dialog.setSize(600, 550);
//        dialog.setLocationRelativeTo(this);
//
//        String[] labels = graph.getNodeLabels();
//
//        JPanel matrixPanel = new JPanel(new GridLayout(n + 1, n + 1, 2, 2));
//        matrixPanel.setBackground(Color.WHITE);
//        matrixPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
//
//        // Column Headers
//        matrixPanel.add(new JLabel(""));
//        for (int i = 0; i < n; i++) {
//            JLabel lbl = new JLabel(labels[i], SwingConstants.CENTER);
//            lbl.setFont(new Font("Arial", Font.BOLD, 14));
//            matrixPanel.add(lbl);
//        }
//
//        // Data Rows
//        for (int i = 0; i < n; i++) {
//            JLabel rowLbl = new JLabel(labels[i], SwingConstants.CENTER);
//            rowLbl.setFont(new Font("Arial", Font.BOLD, 14));
//            matrixPanel.add(rowLbl);
//
//            for (int j = 0; j < n; j++) {
//                String cellValue = "0";
//                Color bgColor = Color.WHITE;
//                if (i != j) {
//                    UNode n1 = graph.getNodeByLabel(labels[i]);
//                    UNode n2 = graph.getNodeByLabel(labels[j]);
//                    UEdge edge = graph.getEdge(n1, n2);
//                    if (edge != null) {
//                        cellValue = String.valueOf(edge.getWeight());
//                        bgColor = new Color(200, 255, 200);
//                    }
//                }
//
//                JLabel cellLbl = new JLabel(cellValue, SwingConstants.CENTER);
//                cellLbl.setFont(new Font("Monospaced", Font.PLAIN, 14));
//                cellLbl.setOpaque(true);
//                cellLbl.setBorder(BorderFactory.createLineBorder(Color.GRAY));
//                cellLbl.setBackground(bgColor);
//                matrixPanel.add(cellLbl);
//            }
//        }
//
//        dialog.add(new JScrollPane(matrixPanel));
//        dialog.setVisible(true);
//    }
//
//    @Override
//    protected void paintComponent(Graphics g) {
//        super.paintComponent(g);
//        Graphics2D g2d = (Graphics2D) g;
//        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//
//        drawEdges(g2d);
//        drawNodes(g2d);
//        drawInfo(g2d);
//    }
//
//    private void drawEdges(Graphics2D g2d) {
//        g2d.setStroke(new BasicStroke(3));
//        for (UEdge edge : graph.getEdges()) {
//            UNode src = edge.getSource();
//            UNode tgt = edge.getTarget();
//
//            g2d.setColor(edge.isInPath() ? PATH_COLOR : edge.getColor());
//            g2d.drawLine(src.getX(), src.getY(), tgt.getX(), tgt.getY());
//
//            int midX = (src.getX() + tgt.getX()) / 2;
//            int midY = (src.getY() + tgt.getY()) / 2;
//            g2d.setFont(new Font("Arial", Font.BOLD, 16));
//            g2d.setColor(Color.WHITE);
//            g2d.fillOval(midX - 15, midY - 15, 30, 30);
//            g2d.setColor(new Color(50, 50, 100));
//            String weightStr = String.valueOf(edge.getWeight());
//            FontMetrics fm = g2d.getFontMetrics();
//            int textX = midX - fm.stringWidth(weightStr) / 2;
//            int textY = midY + fm.getAscent() / 2 - 2;
//            g2d.drawString(weightStr, textX, textY);
//        }
//    }
//
//    private void drawNodes(Graphics2D g2d) {
//        for (UNode node : graph.getNodes()) {
//            int x = node.getX();
//            int y = node.getY();
//            int r = node.getRadius();
//
//            if (node.isHighlighted() || draggedNode == node) {
//                g2d.setColor(new Color(255, 255, 100));
//                g2d.fillOval(x - r - 5, y - r - 5, 2*r + 10, 2*r + 10);
//            }
//
//            g2d.setColor(node.getColor());
//            g2d.fillOval(x - r, y - r, 2 * r, 2 * r);
//
//            g2d.setColor(Color.WHITE);
//            g2d.setStroke(new BasicStroke(3));
//            g2d.drawOval(x - r, y - r, 2 * r, 2 * r);
//
//            g2d.setColor(Color.WHITE);
//            g2d.setFont(new Font("Arial", Font.BOLD, 18));
//            String label = node.getLabel();
//            FontMetrics fm = g2d.getFontMetrics();
//            int textX = x - fm.stringWidth(label) / 2;
//            int textY = y + fm.getAscent() / 2 - 2;
//            g2d.drawString(label, textX, textY);
//        }
//    }
//
//    private void drawInfo(Graphics2D g2d) {
//        g2d.setColor(Color.WHITE);
//        g2d.setFont(new Font("Arial", Font.PLAIN, 14));
//
//        String algoResult = dijkstraPath != null ? "Shortest Path: " + getPathString(dijkstraPath.stream().map(graph::getNodeById).collect(Collectors.toList())) : "";
//
//        g2d.drawString("Mode: Dynamic Weighted Undirected Graph", 20, getHeight() - 60);
//        g2d.drawString("Nodes: " + graph.getNodes().size(), 20, getHeight() - 40);
//        g2d.drawString("Result: " + algoResult, 20, getHeight() - 20);
//
//        g2d.setColor(Color.WHITE);
//        g2d.drawString("Double-click node to rename", getWidth() - 300, getHeight() - 40);
//        g2d.drawString("Drag nodes to move, Right-click to Delete", getWidth() - 300, getHeight() - 20);
//    }
//}