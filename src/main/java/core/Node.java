package core;

import javax.swing.tree.TreeNode;
import java.util.*;

/**
 * 如果父节点的version比当前结点的version新，表明当前结点已经失效
 */
public class Node implements TreeNode {
long size;
long update;
String name;
public Node parent;//parent属性不需要持久化，它只在内存中使用
Map<String, Node> sons;
int sonSize;//这个变量只在加载索引时有用，其他时候始终为0
ArrayList<String> sonNames;

public Node() {
}

public Map<String, Node> getSons() {
    return sons;
}

public void setSons(Map<String, Node> sons) {
    this.sons = sons;
}

public long getSize() {
    return size;
}

public void setSize(long size) {
    this.size = size;
}

public long getUpdate() {
    return update;
}

public void setUpdate(long update) {
    this.update = update;
}

public String getName() {
    return name;
}

public void setName(String name) {
    this.name = name;
}

@Override
public String toString() {
    return name;
}

List<String> getSonNames() {
    if (sonNames == null || (sons != null && sonNames.size() != sons.size())) {
        if (sons != null) {
            sonNames = new ArrayList<>(sons.keySet());
        } else {
            sonNames = new ArrayList<>();
        }
    }
    return sonNames;
}

@Override
public TreeNode getChildAt(int childIndex) {
    return sons.get(getSonNames().get(childIndex));
}

@Override
public int getChildCount() {
    return sons.size();
}

@Override
public TreeNode getParent() {
    return parent;
}

@Override
public int getIndex(TreeNode node) {
    Node no = (Node) node;
    return getSonNames().indexOf(no.name);
}

@Override
public boolean getAllowsChildren() {
    return true;
}

@Override
public boolean isLeaf() {
    return sons == null || sons.size() == 0;
}

public Node getChildByName(String name) {
    if (sons == null) return null;
    if (sons.size() == 0) return null;
    return sons.get(name);
}

@Override
public Enumeration children() {
    return new Enumeration() {
        int index = 0;

        @Override
        public boolean hasMoreElements() {
            return index == sons.size();
        }

        @Override
        public Object nextElement() {
            return sons.get(getSonNames().get(index++));
        }
    };
}
}
