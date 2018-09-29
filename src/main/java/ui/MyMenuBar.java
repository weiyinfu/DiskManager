package ui;

import ui.conf.Config;
import ui.conf.OrderOrientation;
import ui.conf.OrderType;
import ui.conf.ShowType;

import javax.swing.*;

/**
 * 菜单栏
 * 对于本来就繁琐的事物,不要想着去优化
 * <p>
 * 写得重复累赘便于更改/便于定位问题
 */
public class MyMenuBar extends JMenuBar {
MyMenuBar() {
    Config config = Main.config;
    JMenu view = new JMenu("视图");
    ButtonGroup orderMethodGroup = new ButtonGroup();
    JRadioButtonMenuItem radio = null;
    view.add(new JLabel("排序方式"));
    radio = new JRadioButtonMenuItem("按照文件类型排序");
    radio.setSelected(config.orderType == OrderType.byType);
    orderMethodGroup.add(radio);
    view.add(radio);
    radio = new JRadioButtonMenuItem("按照文件大小排序");
    radio.setSelected(config.orderType == OrderType.bySize);
    orderMethodGroup.add(radio);
    view.add(radio);
    radio = new JRadioButtonMenuItem("按照修改时间排序");
    orderMethodGroup.add(radio);
    view.add(radio);
    radio.setSelected(config.orderType == OrderType.byUpdateTime);
    radio = new JRadioButtonMenuItem("按照文件名称排序");
    orderMethodGroup.add(radio);
    view.add(radio);
    radio.setSelected(config.orderType == OrderType.byName);
    view.add(new JSeparator());
    view.add(new JLabel("排序方向"));
    ButtonGroup orderButtonGroup = new ButtonGroup();
    radio = new JRadioButtonMenuItem("升序排列");
    radio.setSelected(config.orderOrientation == OrderOrientation.asc);
    orderButtonGroup.add(radio);
    view.add(radio);
    radio = new JRadioButtonMenuItem("降序排列");
    orderButtonGroup.add(radio);
    radio.setSelected(config.orderOrientation == OrderOrientation.desc);
    view.add(radio);
    view.add(new JSeparator());
    view.add(new JLabel("显示方式"));
    ButtonGroup showButtonGroup = new ButtonGroup();
    radio = new JRadioButtonMenuItem("详细信息");
    view.add(radio);
    showButtonGroup.add(radio);
    radio.setSelected(config.showType == ShowType.table);
    radio = new JRadioButtonMenuItem("列表");
    view.add(radio);
    showButtonGroup.add(radio);
    radio.setSelected(config.showType == ShowType.list);
    add(view);
}
}
