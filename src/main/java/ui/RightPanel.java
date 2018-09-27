package ui;

import core.Data;
import core.Node;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.BorderLayout;
import java.awt.Font;

public class RightPanel extends JPanel {
JTree tree;
JTable table;
JList<String> list;
DefaultListModel<String> listModel = new DefaultListModel<>();
TableModel tableModel = new DefaultTableModel();

RightPanel() {
    setLayout(new BorderLayout());
    list = new JList<>(listModel);
    JScrollPane scrollPane = new JScrollPane(list);
    listModel.addElement("weidiao");
    list.setFont(new Font("微软雅黑", Font.PLAIN, 17));
    add(scrollPane);
}

String showSize(long sz) {
    if (sz < 1024) {
        return sz + "B";
    } else if (sz < (1 << 20)) {
        return String.format("%.2fk", sz * 1.0 / (1 << 10));
    } else if (sz < (1 << 30)) {
        return String.format("%.2fM", sz * 1.0 / (1 << 20));
    } else {
        return String.format("%.2fG", sz * 1.0 / (1 << 30));
    }
}

public void display(TreeSelectionEvent e) {
    listModel.clear();
    Node node = Data.query(e.getPath());
    if (node.getSons() == null) {
        listModel.addElement(node.getName() + " : " + showSize(node.getSize()));
    } else {
        //添加排序，按照文件大小、更改时间、名称、类型（根据后缀名）
        for (Node i : node.getSons().values()) {
            listModel.addElement(i.getName() + " : " + showSize(i.getSize()));
        }
    }
}
}
