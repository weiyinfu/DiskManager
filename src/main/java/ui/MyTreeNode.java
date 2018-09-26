package ui;

import core.Node;

import javax.swing.tree.TreeNode;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;

public class MyTreeNode extends Node implements TreeNode {
List<MyTreeNode> sons;
TreeNode parent;

MyTreeNode(Node node, TreeNode parent) {
    if (node.getSons() != null) {
        sons = new ArrayList<>(node.getSons().values().stream().map(x -> new MyTreeNode(x, this)).collect(Collectors.toList()));
    }
    this.parent = parent;
    setName(node.getName());
    setSize(node.getSize());
    setUpdate(node.getUpdate());
}

@Override
public TreeNode getChildAt(int childIndex) {
    return sons.get(childIndex);
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
    return sons.indexOf(node);
}

@Override
public boolean getAllowsChildren() {
    return true;
}

@Override
public boolean isLeaf() {
    return sons == null || sons.size() == 0;
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
            return sons.get(index++);
        }
    };
}

@Override
public String toString() {
    return getName();
}
}
