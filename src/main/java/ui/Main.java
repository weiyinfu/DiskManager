package ui;

import core.Node;
import ui.conf.Config;
import ui.conf.OrderOrientation;
import ui.conf.OrderType;
import ui.conf.ShowType;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/***
 * 输入框有textchange事件，这个事件最好不要响应，而要响应keydown。
 * 因为textchange事件的触发条件包括：keydown和程序直接set
 * 如果在textchange里面执行settext会陷入死循环
 *
 * 故响应时尽量在上游响应，而非在数据发生变化之后响应
 * */
public class Main extends JFrame {
LeftPanel leftPanel;
RightPanel rightPanel;
MyMenuBar menuBar;
MyToolBar toolBar;
JSplitPane splitPane;
StatusBar statusBar;

static Config config = Config.load();

private ActionListener menuListener = new ActionListener() {
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() instanceof JMenuItem) {
            JMenuItem it = (JMenuItem) e.getSource();
            switch (it.getText()) {
                //排序方式
                case "按照文件类型排序":
                    config.orderType = OrderType.byType;
                    rightPanel.updateDisplay();
                    break;
                case "按照文件大小排序":
                    config.orderType = OrderType.bySize;
                    rightPanel.updateDisplay();
                    break;
                case "按照修改时间排序":
                    config.orderType = OrderType.byUpdateTime;
                    rightPanel.updateDisplay();
                    break;
                case "按照文件名称排序":
                    config.orderType = OrderType.byName;
                    rightPanel.updateDisplay();
                    break;
                //排序方向
                case "升序排列":
                    config.orderOrientation = OrderOrientation.asc;
                    rightPanel.updateDisplay();
                    break;
                case "降序排列":
                    config.orderOrientation = OrderOrientation.desc;
                    rightPanel.updateDisplay();
                    break;
                //显示方式
                case "详细信息":
                    config.showType = ShowType.table;
                    rightPanel.updateDisplay();
                    break;
                case "列表":
                    config.showType = ShowType.list;
                    rightPanel.updateDisplay();
                    break;
            }
        }
    }
};
//左半部分上下文菜单
private ActionListener leftPanelPopup = new ActionListener() {
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() instanceof JMenuItem) {
            JMenuItem menuItem = (JMenuItem) e.getSource();
            if (menuItem.getText().equals("打开")) {
                //直接打开选中的那个item，不打开鼠标悬浮的item了，那样定位有些复杂
                TreePath treePath = leftPanel.tree.getSelectionPath();
                Path path = Util.treePathtoPath(treePath);
                systemShow(path);
            }
        }
    }
};
//右半部分上下文菜单
private ActionListener rightPanelPopup = new ActionListener() {
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() instanceof JMenuItem) {
            JMenuItem menuItem = (JMenuItem) e.getSource();
            if (menuItem.getText().equals("打开")) {
                //直接打开选中的那个item，不打开鼠标悬浮的item了，那样定位有些复杂
                Node node = rightPanel.getSelected();
                if (node == null) return;
                Path path = Util.nodeToPath(node);
                systemShow(path);
            }
        }
    }
};

void bindEvents() {
    //绑定事件
    for (int i = 0; i < menuBar.getMenuCount(); i++) {
        JMenu m = menuBar.getMenu(i);
        for (Component j : m.getMenuComponents()) {
            if (j instanceof JMenuItem) {
                JMenuItem it = (JMenuItem) j;
                it.addActionListener(menuListener);
            }
        }
    }
    for (Component j : leftPanel.getComponentPopupMenu().getComponents()) {
        if (j instanceof JMenuItem) {
            JMenuItem it = (JMenuItem) j;
            it.addActionListener(leftPanelPopup);
        }
    }
    for (Component j : rightPanel.getComponentPopupMenu().getComponents()) {
        if (j instanceof JMenuItem) {
            JMenuItem it = (JMenuItem) j;
            it.addActionListener(rightPanelPopup);
        }
    }

     /*
    * 为了保证单选，使用mouseclick而不是addTreeSelectionListener
    * */
    leftPanel.tree.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON1) {
                TreePath path = leftPanel.tree.getClosestPathForLocation(e.getX(), e.getY());
                config.currentPath = Util.treePathtoPath(path);
                updateByPath();
            }

        }

        @Override
        public void mouseReleased(MouseEvent e) {
            /*右键显示上下文菜单
            * 带滚动条的panel的上下文菜单如何处理呢？
             */
            if (e.isPopupTrigger()) {
                Point pos = leftPanel.getLocationOnScreen();
                leftPanel.getComponentPopupMenu().show(leftPanel, e.getXOnScreen() - pos.x, e.getYOnScreen() - pos.y);
            }
        }
    });
    MouseAdapter rightPanelMouse = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            /*
            * 单击操作：
            * 如果是文件夹，进入文件夹
            * 如果是文件，调用系统程序打开文件
            * */
            if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                Node node = rightPanel.getSelected();
                Path path = Util.nodeToPath(node);
                if (Files.isReadable(path)) {
                    if (Files.isDirectory(path)) {//文件夹直接打开
                        config.currentPath = path;
                        updateByPath();
                    } else {//如果是文件，调用系统程序打开
                        systemShow(Util.nodeToPath(node));
                    }
                }
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            /*右键显示上下文菜单
            * 带滚动条的panel的上下文菜单如何处理呢？
             */
            if (e.isPopupTrigger()) {
                Point pos = rightPanel.getLocationOnScreen();
                rightPanel.getComponentPopupMenu().show(rightPanel, e.getXOnScreen() - pos.x, e.getYOnScreen() - pos.y);
            }
        }
    };
    rightPanel.table.addMouseListener(rightPanelMouse);
    rightPanel.list.addMouseListener(rightPanelMouse);

    addWindowListener(new WindowAdapter() {
        @Override
        public void windowClosing(WindowEvent e) {
            config.windowLocationX = Main.this.getLocationOnScreen().x;
            config.windowLocationY = Main.this.getLocationOnScreen().y;
            config.windowWidth = Main.this.getSize().width;
            config.windowHeight = Main.this.getSize().height;
            config.dividerLocation = splitPane.getDividerLocation();
            config.toolBarDividerLocation = toolBar.splitPane.getDividerLocation();
            config.save();
            System.exit(0);
        }
    });
}

void updateByPath() {
    leftPanel.updateDisplay();
    /*
     *数据驱动显示，而不是控制每个显示
     * */
    rightPanel.updateDisplay();
    statusBar.updateDisplay();
    setTitle(config.currentPath.toString());
}

void systemShow(Path path) {
    try {
        Desktop.getDesktop().open(path.toFile());
    } catch (IOException e) {
        JOptionPane.showConfirmDialog(this, e);
        e.printStackTrace();
    }
}

/**
 * 工具栏
 */
JToolBar getToolBar() {
    if (toolBar == null) {
        toolBar = new MyToolBar();
    }
    return toolBar;
}


/**
 * 主体Panel:包括左侧导航和右侧主体展示
 */
JPanel getMainPanel() {
    splitPane = new JSplitPane();
    JPanel panel = new JPanel(new BorderLayout());
    panel.add(splitPane);
    splitPane.setEnabled(true);
    splitPane.setLeftComponent(leftPanel);
    splitPane.setRightComponent(rightPanel);
    splitPane.setOneTouchExpandable(true);
    return panel;
}


Main() {
    menuBar = new MyMenuBar();
    leftPanel = new LeftPanel();
    rightPanel = new RightPanel();
    statusBar = new StatusBar();
    setJMenuBar(menuBar);
    JPanel mainPanel = getMainPanel();
    getContentPane().setLayout(new BorderLayout());
    getContentPane().add(BorderLayout.NORTH, getToolBar());
    getContentPane().add(BorderLayout.SOUTH, statusBar);
    getContentPane().add(BorderLayout.CENTER, mainPanel);
    bindEvents();
    if (config.getWindowHeight() != -1) {
        setSize(config.getWindowWidth(), config.getWindowHeight());
    } else {
        Dimension sz = getToolkit().getScreenSize();
        setSize(sz);
    }
    updateByPath();
    if (config.getWindowLocationX() == -1) {
        setLocationRelativeTo(null);
    } else {
        setLocation(config.getWindowLocationX(), config.getWindowLocationY());
    }

    setVisible(true);
    splitPane.setDividerLocation(config.dividerLocation);
}

public static void main(String[] args) {
    new Main();
}
}
