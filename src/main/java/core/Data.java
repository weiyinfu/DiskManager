package core;

import core.Node;

import javax.swing.tree.TreePath;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Data {
public static Node root;

public static Node query(TreePath path) {
    List<String> a = Arrays.stream(path.getPath()).map(Object::toString).collect(Collectors.toList());
    if (a.size() <= 1) return root;
    Node now = root;
    for (String i : a.subList(1, a.size())) {
        now = now.getSons().get(i);
    }
    return now;
}
}
