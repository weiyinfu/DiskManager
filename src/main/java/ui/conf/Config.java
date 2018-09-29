package ui.conf;

import com.alibaba.fastjson.JSON;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

/**
 * 界面操作涉及的一些配置
 */
public class Config {
//排序方式
public OrderType orderType = OrderType.bySize;
//排序方向
public OrderOrientation orderOrientation = OrderOrientation.desc;
//展示方式:列表还是表格
public ShowType showType = ShowType.table;
//当前文件夹
public Path currentPath = core.Config.root;
//窗口的大小和位置
public int windowLocationX = -1, windowLocationY = -1, windowWidth = -1, windowHeight = -1;
//窗体主要部分分隔条的位置
public int dividerLocation = 100;
//工具栏分隔条位置
public int toolBarDividerLocation = 100;

//加载配置
public static Config load() {
    if (Files.exists(core.Config.configPath)) {
        try {
            String s = Files.readAllLines(core.Config.configPath, Charset.forName("utf8")).stream().collect(Collectors.joining());
            if (s.length() == 0) {
                throw new RuntimeException("error ui config ");
            }
            return JSON.parseObject(s, Config.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    return new Config();
}

//保存配置
public void save() {
    try {
        BufferedWriter cout = Files.newBufferedWriter(core.Config.configPath, Charset.forName("utf8"));
        cout.write(JSON.toJSONString(this));
        cout.close();
    } catch (IOException e) {
        e.printStackTrace();
    }
}

public int getDividerLocation() {
    return dividerLocation;
}

public void setDividerLocation(int dividerLocation) {
    this.dividerLocation = dividerLocation;
}

public OrderOrientation getOrderOrientation() {
    return orderOrientation;
}

public void setOrderOrientation(OrderOrientation orderOrientation) {
    this.orderOrientation = orderOrientation;
}

public ShowType getShowType() {
    return showType;
}

public void setShowType(ShowType showType) {
    this.showType = showType;
}

public Path getCurrentPath() {
    return currentPath;
}

public void setCurrentPath(Path currentPath) {
    this.currentPath = currentPath;
}

public OrderType getOrderType() {
    return orderType;
}

public void setOrderType(OrderType orderType) {
    this.orderType = orderType;
}

public int getWindowLocationX() {
    return windowLocationX;
}

public void setWindowLocationX(int windowLocationX) {
    this.windowLocationX = windowLocationX;
}

public int getWindowLocationY() {
    return windowLocationY;
}

public void setWindowLocationY(int windowLocationY) {
    this.windowLocationY = windowLocationY;
}

public int getWindowWidth() {
    return windowWidth;
}

public void setWindowWidth(int windowWidth) {
    this.windowWidth = windowWidth;
}

public int getWindowHeight() {
    return windowHeight;
}

public void setWindowHeight(int windowHeight) {
    this.windowHeight = windowHeight;
}
}
