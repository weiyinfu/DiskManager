package ui;

import core.Config;
import core.FileSizeGetter;
import core.Node;

import javax.swing.tree.TreePath;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 一些用于转换的工具函数
 */
public class Util {
public static Node root;

static {
    root = new FileSizeGetter().loadAndUpdate();
}

public static Node pathToNode(Path path) {
    Node now = root;
    path = Config.root.relativize(path);
    for (int i = 0; i < path.getNameCount(); i++) {
        String p = path.getName(i).toString();
        if (now.getSons() == null) return root;
        if (now.getSons().get(p) == null) return root;
        now = now.getSons().get(p);
    }
    return now;
}

static String sizeToString(long sz) {
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

static String getFileType(String s) {
    int point = s.lastIndexOf('.');
    if (point == -1) return "";
    return s.substring(point + 1);
}

static Path nodeToPath(Node node) {
    List<String> a = new LinkedList<>();
    while (node.parent != null) {
        a.add(node.getName());
        node = node.parent;
    }
    Path path = Config.root;
    for (int i = a.size() - 1; i >= 0; i--) {
        path = path.resolve(a.get(i));
    }
    return path;
}

static String timeToString(long time) {
    return new SimpleDateFormat("yyyy年MM月dd日 HH:mm").format(new Date(time));
}


public static Path treePathtoPath(TreePath selectionPath) {
    List<String> a = Arrays.stream(selectionPath.getPath()).map(x -> x + "").collect(Collectors.toList());
    Path now = Config.root;
    for (int i = 1; i < a.size(); i++) {
        now = now.resolve(a.get(i));
    }
    return now;
}

public static TreePath pathToTreePath(Path path) {
    Path sonPath = Config.root.relativize(path);
    List<Object> a = new ArrayList<>(sonPath.getNameCount() + 1);
    a.add(root);
    Node now = root;
    for (int i = 0; i < sonPath.getNameCount(); i++) {
        Node next = now.getChildByName(sonPath.getName(i).toString());
        if (next == null) break;
        now = next;
        a.add(now);
    }
    return new TreePath(a.toArray());
}


}
