package core;

import java.util.Map;

/**
 * 如果父节点的version比当前结点的version新，表明当前结点已经失效
 */
public class Node {
long size;
long update;
String name;
Map<String, Node> sons;

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
}
