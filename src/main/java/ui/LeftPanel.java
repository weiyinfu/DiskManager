package ui;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import java.awt.BorderLayout;
import java.awt.Font;

/**
 * 左侧树形导航栏
 */
public class LeftPanel extends JPanel {
DefaultTreeModel treeModel;
JTree tree;

LeftPanel() {
    setLayout(new BorderLayout());
    treeModel = new DefaultTreeModel(Util.root);
    tree = new JTree(treeModel);
    tree.setEditable(false);
    tree.setFont(new Font("微软雅黑", Font.PLAIN, 17));
    JScrollPane scrollPane = new JScrollPane(tree);
    add(scrollPane);
    //添加popupMenu
    JPopupMenu popupMenu = new JPopupMenu();
    popupMenu.add(new JMenuItem("打开"));
    setComponentPopupMenu(popupMenu);
}


public void updateDisplay() {
    //清空旧有的选中项
    tree.removeSelectionPaths(tree.getSelectionPaths());
    tree.setSelectionPath(Util.pathToTreePath(Main.config.currentPath));
}
}
