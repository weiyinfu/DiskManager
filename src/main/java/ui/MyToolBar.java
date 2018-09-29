package ui;

import javax.swing.*;
import java.awt.BorderLayout;

public class MyToolBar extends JToolBar {
JSplitPane splitPane;
JTextField address;
JTextField search;

MyToolBar() {
    add(new JButton("后退"));
    add(new JButton("前进"));
    add(new JButton("向上"));
    JPanel panel = new JPanel(new BorderLayout());
    splitPane = new JSplitPane();
    address = new JTextField("地址");
    splitPane.setLeftComponent(address);
    search = new JTextField("搜索");
    splitPane.setRightComponent(search);
    panel.add(splitPane);
    add(panel);
    splitPane.setDividerLocation(Main.config.toolBarDividerLocation);
    setFloatable(false);
}
}
