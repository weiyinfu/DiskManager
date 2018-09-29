package ui;

import core.Node;
import ui.conf.Config;
import ui.conf.OrderOrientation;
import ui.conf.OrderType;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.CardLayout;
import java.awt.Font;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class RightPanel extends JPanel {
class MyTable extends JTable {
    boolean editable = false;

    public MyTable(DefaultTableModel tableModel) {
        super(tableModel);
    }

    void setEditable(boolean editable) {
        this.editable = editable;
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return editable;
    }

    boolean getEditable() {
        return editable;
    }
}

MyTable table;
JList<String> list;
DefaultListModel<String> listModel = new DefaultListModel<>();
DefaultTableModel tableModel = new DefaultTableModel();
CardLayout card;
//当前的数据

RightPanel() {
    //定义list
    list = new JList<>(listModel);
    listModel.addElement("weidiao");
    list.setFont(new Font("微软雅黑", Font.PLAIN, 17));
    //定义table
    table = new MyTable(tableModel);
    table.setEditable(false);
    table.setRowSelectionAllowed(true);
    table.setFont(new Font("微软雅黑", Font.PLAIN, 17));
    //表格高度太小,使之空余出来一些
    table.setRowHeight(table.getFontMetrics(table.getFont()).getHeight() + 5);
    tableModel.setDataVector(new Object[][]{{"天下大势", "为我所控"}}, new Object[]{"oen", "two"});
    //定义卡片布局
    card = new CardLayout();
    setLayout(card);
    add("list", new JScrollPane(list));
    add("table", new JScrollPane(table));
    Config config = Main.config;
    switch (config.showType) {
        case table:
            card.show(this, "table");
            break;
        case list:
            card.show(this, "list");
            break;
        default:
            throw new RuntimeException("unkown show type");
    }

    //添加popupMenu
    JPopupMenu popupMenu = new JPopupMenu();
    popupMenu.add(new JMenuItem("打开"));
    setComponentPopupMenu(popupMenu);
}

List<Node> getData(Node node) {
    Config config = Main.config;
    if (node.getSons() == null) {
        return Collections.singletonList(node);
    } else {
        return node.getSons().values().stream().sorted((x, y) -> {
            int res = 0;
            if (config.orderType == OrderType.bySize) {
                res = Long.compare(x.getSize(), y.getSize());
            } else if (config.orderType == OrderType.byType) {
                res = Util.getFileType(x.getName()).compareToIgnoreCase(Util.getFileType(y.getName()));
            } else if (config.orderType == OrderType.byName) {
                res = x.getName().compareToIgnoreCase(y.getName());
            } else if (config.orderType == OrderType.byUpdateTime) {
                res = Long.compare(x.getUpdate(), y.getUpdate());
            } else {
                throw new RuntimeException("unkown order type");
            }
            if (config.orderOrientation == OrderOrientation.asc) {
                res *= 1;
            } else {
                res *= -1;
            }
            return res;
        }).collect(Collectors.toList());
    }
}


public Node getSelected() {
    List<Node> nowData = getData(Util.pathToNode(Main.config.currentPath));
    Config config = Main.config;
    switch (config.showType) {
        case list: {
            int index = list.getSelectedIndex();
            if (index == -1) return null;
            return nowData.get(index);
        }
        case table: {
            int index = table.getSelectedRow();
            if (index == -1) return null;
            return nowData.get(index);
        }
        default: {
            throw new RuntimeException("unkown showtype");
        }
    }
}

public void updateDisplay() {
    Node node = Util.pathToNode(Main.config.currentPath);
    //    记录当前文件夹，下次打开时也用这个文件夹
    Config config = Main.config;
    List<Node> nowData = getData(node);
    switch (config.showType) {
        case list: {
            card.show(this, "list");
            listModel.clear();
            for (Node i : nowData) {
                listModel.addElement(String.format("%-30s | %-20s | %-20s", i.getName(), Util.sizeToString(i.getSize()), Util.timeToString(i.getUpdate())));
            }
            break;
        }
        case table: {
            card.show(this, "table");
            Object[] header = new Object[]{"名称", "修改日期", "大小"};
            Object[][] a = new Object[nowData.size()][];
            for (int j = 0; j < a.length; j++) {
                Node i = nowData.get(j);
                a[j] = new Object[]{i.getName(), Util.timeToString(i.getUpdate()), Util.sizeToString(i.getSize())};
            }
            tableModel.setDataVector(a, header);
            break;
        }
        default:
            throw new RuntimeException("unkown show type");
    }
}
}
