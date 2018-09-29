package ui;

import core.Node;

import javax.swing.JLabel;
import javax.swing.JToolBar;
import java.awt.Font;

public class StatusBar extends JToolBar {
public JLabel label = new JLabel();

StatusBar() {
    label.setFont(new Font("微软雅黑", Font.PLAIN, 15));
    setFloatable(false);
    add(label);
}

public void display(Node node) {
    int sonCount = node.getSons() == null ? 0 : node.getSons().size();
    label.setText(node.getName() + " " + sonCount + "项," + Util.sizeToString(node.getSize()) + " 上次更新时间" + Util.timeToString(node.getUpdate()));
}

public void updateDisplay() {
    Node node = Util.pathToNode(Main.config.currentPath);
    display(node);
}
}
