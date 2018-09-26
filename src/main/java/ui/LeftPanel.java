package ui;

import core.Data;
import core.FileSizeGetter;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import java.awt.BorderLayout;
import java.awt.Font;

public class LeftPanel extends JPanel {
DefaultTreeModel treeModel;
JTree tree;

LeftPanel() {
    setLayout(new BorderLayout());
    buildTreeModel();
    tree = new JTree(treeModel);
    tree.setFont(new Font("微软雅黑", Font.PLAIN, 17));
    JScrollPane scrollPane = new JScrollPane(tree);
    add(scrollPane);
}

/**
 * 初始化数据
 */
void buildTreeModel() {
    FileSizeGetter getter = new FileSizeGetter();
    Data.root = getter.loadIndex();
    MyTreeNode root = new MyTreeNode(Data.root, null);
    treeModel = new DefaultTreeModel(root);
}

}
