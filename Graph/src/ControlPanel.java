//import javax.swing.*;
//import java.awt.*;
//import java.util.Arrays;
//
//class ControlPanel extends JPanel {
//    private GraphPanel graphPanel;
//    private JButton addNodeButton;
//    private JComboBox<String> sourceNodeCombo;
//    private JComboBox<String> targetNodeCombo;
//    private JTextField weightField;
//    private JButton addEdgeButton;
//
//    public ControlPanel(GraphPanel gp) {
//        this.graphPanel = gp;
//        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
//        setBackground(new Color(45, 60, 80));
//        setPreferredSize(new Dimension(280, 800));
//        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
//
//        add(createTitleLabel("Graph Management"));
//        add(Box.createVerticalStrut(15));
//        add(createDivider());
//
//        addNodeButton = createControlButton("Add Node (Click Canvas)", new Color(60, 170, 100));
//        addNodeButton.addActionListener(e -> graphPanel.setMode(GraphPanel.Mode.ADDING_NODE));
//        add(addNodeButton);
//        add(Box.createVerticalStrut(15));
//        add(new JLabel("Right-click an existing node to delete it.", SwingConstants.CENTER)).setForeground(Color.LIGHT_GRAY);
//        add(Box.createVerticalStrut(20));
//        add(createDivider());
//        add(Box.createVerticalStrut(20));
//
//        add(createTitleLabel("Add Weighted Edge"));
//        add(Box.createVerticalStrut(10));
//
//        sourceNodeCombo = createNodeComboBox();
//        add(createLabelPanel("Source:", sourceNodeCombo));
//        add(Box.createVerticalStrut(10));
//
//        targetNodeCombo = createNodeComboBox();
//        add(createLabelPanel("Target:", targetNodeCombo));
//        add(Box.createVerticalStrut(10));
//
//        weightField = new JTextField("10");
//        weightField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
//        add(createLabelPanel("Weight:", weightField));
//        add(Box.createVerticalStrut(15));
//
//        addEdgeButton = createControlButton("Connect Nodes", new Color(200, 120, 50));
//        addEdgeButton.addActionListener(e -> addEdge());
//        add(addEdgeButton);
//
//        add(Box.createVerticalStrut(20));
//        add(createDivider());
//        add(Box.createVerticalGlue());
//    }
//
//    public void setGraphPanel(GraphPanel gp) {
//        this.graphPanel = gp;
//        updateNodeLists();
//    }
//
//    private JLabel createTitleLabel(String text) {
//        JLabel label = new JLabel(text);
//        label.setFont(new Font("Arial", Font.BOLD, 18));
//        label.setForeground(new Color(150, 200, 255));
//        label.setAlignmentX(Component.CENTER_ALIGNMENT);
//        return label;
//    }
//
//    private Component createDivider() {
//        JSeparator sep = new JSeparator(SwingConstants.HORIZONTAL);
//        sep.setForeground(new Color(70, 90, 110));
//        sep.setMaximumSize(new Dimension(240, 5));
//        return sep;
//    }
//
//    private JPanel createLabelPanel(String labelText, JComponent component) {
//        JPanel panel = new JPanel(new BorderLayout(5, 5));
//        panel.setOpaque(false);
//        JLabel label = new JLabel(labelText);
//        label.setForeground(Color.WHITE);
//        panel.add(label, BorderLayout.WEST);
//        panel.add(component, BorderLayout.CENTER);
//        panel.setMaximumSize(new Dimension(240, 40));
//        return panel;
//    }
//
//    private JButton createControlButton(String text, Color color) {
//        JButton button = new JButton(text);
//        button.setFont(new Font("Arial", Font.BOLD, 14));
//        button.setBackground(color);
//        button.setForeground(Color.WHITE);
//        button.setFocusPainted(false);
//        button.setAlignmentX(Component.CENTER_ALIGNMENT);
//        button.setMaximumSize(new Dimension(240, 40));
//        return button;
//    }
//
//    private JComboBox<String> createNodeComboBox() {
//        JComboBox<String> combo = new JComboBox<>();
//        combo.setFont(new Font("Arial", Font.PLAIN, 14));
//        combo.setBackground(Color.WHITE);
//        return combo;
//    }
//
//    public void updateNodeLists() {
//        String[] labels = graphPanel.getGraph().getNodeLabels();
//        Object selectedSource = sourceNodeCombo.getSelectedItem();
//        Object selectedTarget = targetNodeCombo.getSelectedItem();
//        sourceNodeCombo.setModel(new DefaultComboBoxModel<>(labels));
//        targetNodeCombo.setModel(new DefaultComboBoxModel<>(labels));
//
//        if (selectedSource != null && Arrays.asList(labels).contains(selectedSource)) {
//            sourceNodeCombo.setSelectedItem(selectedSource);
//        }
//        if (selectedTarget != null && Arrays.asList(labels).contains(selectedTarget)) {
//            targetNodeCombo.setSelectedItem(selectedTarget);
//        }
//
//        graphPanel.repaint();
//    }
//
//    private void addEdge() {
//        String sourceLabel = (String) sourceNodeCombo.getSelectedItem();
//        String targetLabel = (String) targetNodeCombo.getSelectedItem();
//        int weight;
//
//        if (sourceLabel == null || targetLabel == null) {
//            JOptionPane.showMessageDialog(this, "Please select both source and target nodes.", "Error", JOptionPane.ERROR_MESSAGE);
//            return;
//        }
//
//        try {
//            weight = Integer.parseInt(weightField.getText());
//            if (weight <= 0) {
//                JOptionPane.showMessageDialog(this, "Weight must be a positive integer.", "Error", JOptionPane.ERROR_MESSAGE);
//                return;
//            }
//        } catch (NumberFormatException ex) {
//            JOptionPane.showMessageDialog(this, "Invalid weight input. Please enter a number.", "Error", JOptionPane.ERROR_MESSAGE);
//            return;
//        }
//
//        UNode n1 = graphPanel.getGraph().getNodeByLabel(sourceLabel);
//        UNode n2 = graphPanel.getGraph().getNodeByLabel(targetLabel);
//
//        if (n1.equals(n2)) {
//            JOptionPane.showMessageDialog(this, "Cannot create an edge from a node to itself.", "Error", JOptionPane.ERROR_MESSAGE);
//            return;
//        }
//
//        if (graphPanel.getGraph().addEdge(n1, n2, weight)) {
//            JOptionPane.showMessageDialog(this, "Edge added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
//            graphPanel.resetVisualization();
//            graphPanel.repaint();
//        } else {
//            JOptionPane.showMessageDialog(this, "Edge already exists!", "Error", JOptionPane.ERROR_MESSAGE);
//        }
//    }
//}