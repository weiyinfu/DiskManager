package core;

import com.alibaba.fastjson.JSON;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.TreeMap;

/**
 * 自定义树结构序列化
 */
public class NodeSerializer {
class WrapNode {
    Node node;
    int sonSize;

    WrapNode(Node node, int sonSize) {
        this.node = node;
        this.sonSize = sonSize;
    }
}

Node load(Path path) {
    try {
        DataInputStream cin = new DataInputStream(new BufferedInputStream(Files.newInputStream(path)));
        List<WrapNode> nodes = new ArrayList<>();

        try {
            while (true) {
                Node node = new Node();
                node.size = cin.readLong();
                node.update = cin.readLong();
                int nameLen = cin.readInt();
                byte[] nameByte = new byte[nameLen];
                cin.readFully(nameByte);
                node.name = new String(nameByte, Charset.forName("utf8"));
                int sonSize = cin.readInt();
                if (sonSize > 0) {
                    node.sons = new TreeMap<>();
                }
                nodes.add(new WrapNode(node, sonSize));
            }
        } catch (IOException e) {
        }
        Stack<WrapNode> sta = new Stack<>();
        sta.add(nodes.get(0));
        int index = 1;
        while (!sta.isEmpty()) {
            if (sta.peek().sonSize == 0) {
                Node son = sta.pop().node;
                if (sta.isEmpty()) break;
                sta.peek().node.sons.put(son.name, son);
                sta.peek().sonSize--;
                continue;
            }
            WrapNode now = nodes.get(index++);
            sta.add(now);
        }
        if (index < nodes.size()) {
            throw new RuntimeException("error index format");
        }
        return nodes.get(0).node;
    } catch (IOException e) {
        e.printStackTrace();
        throw new RuntimeException("load index error");
    }
}


void save(Node root, Path path) {
    try {
        DataOutputStream cout = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(path)));
        Stack<Node> sta = new Stack<>();
        sta.add(root);
        while (!sta.isEmpty()) {
            Node node = sta.pop();
            cout.writeLong(node.size);//一个long
            cout.writeLong(node.update);//一个long
            byte[] namebytes = node.name.getBytes(Charset.forName("utf8"));
            cout.writeInt(namebytes.length);//一个int
            cout.write(namebytes);//若干byte
            if (node.sons == null) cout.writeInt(0);//一个int,表示儿子个数
            else {
                cout.writeInt(node.sons.size());
                for (Node i : node.sons.values()) {
                    sta.push(i);
                }
            }
        }
        cout.close();
    } catch (IOException e) {
        e.printStackTrace();
        throw new RuntimeException("save node error");
    }
}
}
