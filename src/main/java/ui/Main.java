package ui;

import javax.swing.*;
import java.awt.BorderLayout;

public class Main extends JFrame {
LeftPanel leftPanel;
RightPanel rightPanel;

/**
 * 菜单栏
 */
JMenuBar getMenu() {
    JMenuBar bar = new JMenuBar();
    JMenu file = new JMenu("File");
    bar.add(file);
    return bar;
}

/**
 * 工具栏
 */
JToolBar getToolBar() {
    JToolBar toolBar = new JToolBar();
    toolBar.add(new JLabel("haha"));
    toolBar.setFloatable(false);
    return toolBar;
}

/**
 * 状态栏
 */
JToolBar getStatusBar() {
    JToolBar statusBar = new JToolBar("status");
    statusBar.add(new JLabel("good night "));
    statusBar.setFloatable(false);
    return statusBar;
}

/**
 * 左侧树导航图
 */
JPanel getLeftView() {
    if (leftPanel == null) {
        leftPanel = new LeftPanel();
        leftPanel.tree.addTreeSelectionListener((e) -> {
            rightPanel.display(e);
        });
    }
    return leftPanel;
}

/**
 * 右侧主要展示图
 */
JPanel getRightView() {
    if (rightPanel == null) {
        rightPanel = new RightPanel();
    }
    return rightPanel;
}

/**
 * 主体Panel:包括左侧导航和右侧主体展示
 */
JPanel getMainPanel() {
    JPanel panel = new JPanel(new BorderLayout());
    JSplitPane splitPane = new JSplitPane();
    panel.add(splitPane);
    splitPane.setDividerLocation(0.4);
    splitPane.setEnabled(true);
    splitPane.setLeftComponent(getLeftView());
    splitPane.setRightComponent(getRightView());
    splitPane.setOneTouchExpandable(true);
    return panel;
}


Main() {
    setSize(700, 700);
    setJMenuBar(getMenu());
    getContentPane().setLayout(new BorderLayout());
    getContentPane().add(BorderLayout.NORTH, getToolBar());
    getContentPane().add(BorderLayout.SOUTH, getStatusBar());
    getContentPane().add(BorderLayout.CENTER, getMainPanel());
    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    setVisible(true);
}

public static void main(String[] args) {
    new Main();
}
}
