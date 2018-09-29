package core;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static core.Config.indexPath;

/**
 * 多线程文件大小求解器
 * <p>
 * 伟大的程序都是丑陋的
 */
public class FileSizeGetter {
final static ExecutorService service = Executors.newFixedThreadPool(Config.threadCount);

public void getSize(Path path, Node node, Runnable callback) {
    try {
        if (!Files.isReadable(path)) {
            node.size = 0;
            callback.run();
            return;
        }
        if (!Files.isDirectory(path)) {
            node.size = Files.size(path);
            callback.run();
            return;
        }
        if (node.sons == null) node.sons = new TreeMap<>();
        //没有判断exsits这句话会出错,很奇怪
        List<String> sons = Files.list(path).filter(sonPath -> Files.exists(sonPath) && Files.isReadable(sonPath) && !Files.isSymbolicLink(sonPath)).map(sonPath -> sonPath.getFileName().toString()).collect(Collectors.toList());
        node.sons.keySet().retainAll(sons);//更新key
        if (sons.size() == 0) {
            callback.run();
            return;
        }
        //因为自定义同步，所以不需要atomicInteger之类的
        int[] counter = {sons.size()};
        for (String fileName : sons) {
            Path sonPath = path.resolve(fileName);
            Node son = node.sons.get(fileName);
            //如果依然有效，那么只需要更改版本号即可
            if (son != null && son.getUpdate() == Files.getLastModifiedTime(sonPath).toMillis()) {
                synchronized (node) {
                    counter[0]--;
                    if (counter[0] == 0) {
                        callback.run();
                    }
                }
                continue;
            }
            if (son != null) {
                synchronized (node) {
                    node.size -= son.size;
                }
            }
            son = new Node();
            son.name = fileName;
            son.parent = node;
            son.update = Files.getLastModifiedTime(sonPath).toMillis();
            node.sons.put(fileName, son);
            if (Files.isDirectory(sonPath)) {
                son.sons = new ConcurrentSkipListMap<>();
            }
            //现在已经把子节点放入了父节点
            Node finalSon = son;
            service.submit(() -> {
                getSize(sonPath, finalSon, () -> {
                    //如果当前这个任务是最后一个任务,那么调用callback
                    synchronized (node) {
                        node.size += finalSon.size;
                        counter[0]--;
                        if (counter[0] == 0) {
                            callback.run();
                        }
                    }
                });
            });
        }
    } catch (Exception e) {
        e.printStackTrace();
        //线程抛出异常之后直接退出
        System.exit(-1);
    }
}

//这个函数是最主要的函数
public Node loadAndUpdate() {
    try {
        Node node = loadMultiIndexFiles();
        System.out.println("load index over");
        node.name = Config.root.toString();
        getSize(Config.root, node, () -> {
            System.out.println("更新索引成功");
            service.shutdown();
            new Thread(() -> {//保存操作可以另开一个线程慢慢去保存吧
                save(node);
                System.out.println("save index over");
            }).start();
        });
        //等待全部加载完成
        while (!service.isShutdown()) {
            Thread.sleep(200);
        }
        return node;
    } catch (Exception e) {
        e.printStackTrace();
        throw new RuntimeException("load index error");
    }
}

Node readOneNode(DataInputStream cin) throws IOException {
    Node node = new Node();
    node.size = cin.readLong();
    node.update = cin.readLong();
    int nameLen = cin.readInt();
    byte[] nameByte = new byte[nameLen];
    cin.readFully(nameByte);
    node.name = new String(nameByte, Charset.forName("utf8"));
    node.sonSize = cin.readInt();
    if (node.sonSize > 0) {
        node.sons = new ConcurrentSkipListMap<>();
    }
    return node;
}

void writeOneNode(DataOutputStream cout, Node node) throws IOException {
    cout.writeLong(node.size);//一个long
    cout.writeLong(node.update);//一个long
    byte[] namebytes = node.name.getBytes(Charset.forName("utf8"));
    cout.writeInt(namebytes.length);//一个int
    cout.write(namebytes);//若干byte
    if (node.sons == null) cout.writeInt(0);//一个int,表示儿子个数
    else {
        cout.writeInt(node.sons.size());
    }
}

/**
 * 加载一个索引文件
 */
LinkedList<Node> loadOneIndexFile(Path indexPath) {
    LinkedList<Node> li = new LinkedList<>();
    try {
        DataInputStream cin = new DataInputStream(new BufferedInputStream(Files.newInputStream(indexPath)));
    /*
    * 在队列中，无儿子且有父亲的结点会被撵出去
    * 无儿子没有父亲的结点不会被撵出去
    * 有儿子的结点不会被撵出去
    * 简言之：
    * 要么没有父亲，要么有儿子，你才能留在栈里面，否则你必须出栈
    * 或者说：
    * 你出栈的条件是，你已经有了父亲并且你已经没了儿子
    * */
        while (true) {
            try {
                //新node入队之前需要把前面的清空一下
                if (!li.isEmpty() && li.peek().sonSize == 0 && li.peek().parent != null) {
                    Node son = li.pop();
                    li.peek().sons.put(son.name, son);
                    li.peek().sonSize--;
                    continue;
                }
                //读一个结点
                Node node = readOneNode(cin);
                if (!li.isEmpty() && li.peek().sonSize > 0) node.parent = li.peek();
                li.push(node);
            } catch (IOException e) {
                break;
            }
        }
    } catch (IOException e) {
        e.printStackTrace();
        System.exit(0);
    }
    return li;
}

/**
 * 实验发现load和save是很耗费时间的
 * save可以开辟线程直接后台去save
 * load可以设置成分块加载，然后合并各个块
 */
Node loadMultiIndexFiles() {
    try {
        //列出索引下的全部文件，按照字符串大小进行排序
        if (!Files.exists(Config.indexPath)) return new Node();
        List<Path> pathList = Files.list(indexPath).filter(indexPath -> indexPath.getFileName().toString().matches("index\\d+.bin")).sorted().collect(Collectors.toList());
        if (pathList.size() == 0) return new Node();
        final long[] latch = {pathList.size()};//计数已经完成的任务数
        //完成了的任务放在数组中
        ArrayList<LinkedList<Node>> links = new ArrayList<>(pathList.size());
        for (int i = 0; i < pathList.size(); i++) links.add(null);
        for (int i = 0; i < pathList.size(); i++) {
            int finalI = i;
            service.submit(() -> {
                LinkedList<Node> li = loadOneIndexFile(pathList.get(finalI));
                links.set(finalI, li);
                synchronized (latch) {
                    latch[0]--;
                }
            });
        }
        while (latch[0] > 0) {
            Thread.sleep(1);
        }
        //把链表串联起来
        return mergeLinks(links);
    } catch (Exception ex) {
        ex.printStackTrace();
        throw new RuntimeException("error loadMultiIndexFiles index");
    }

}

/**
 * 把各个线程取得的结果合并起来
 */
Node mergeLinks(List<LinkedList<Node>> links) {
    LinkedList<Node> sta = new LinkedList<>();
    int i = 0;
    while (true) {
        //新node入队之前需要把前面的清空一下
        //这里linked必然不会为弹空的,如果弹空了说明数据有问题
        if (!sta.isEmpty() && sta.peek().sonSize == 0 && sta.peek().parent != null) {
            Node son = sta.pop();
            sta.peek().sons.put(son.name, son);
            sta.peek().sonSize--;
            continue;
        }
        if (links.get(i).isEmpty()) {
            i++;
            if (i == links.size()) break;
        }
        Node k = links.get(i).removeLast();
        if (!sta.isEmpty() && sta.peek().sonSize > 0) k.parent = sta.peek();
        //链表push是在头部添加,add在尾部添加
        sta.push(k);
    }
    if (sta.size() != 1) {
        throw new RuntimeException("after load the total node should be 1 but is " + sta.size());
    }
    return sta.get(0);
}

//删除旧索引
void removeOldIndex(Set<Path> except) {
    try {
        if (!Files.exists(Config.indexPath)) return;
        Files.list(Config.indexPath).filter(x -> !except.contains(x)).forEach(path -> {
            try {
                Files.delete(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    } catch (Exception e) {
        e.printStackTrace();
    }
}

/**
 * 自定义树结构序列化
 * <p>
 * <p>
 * 并行序列化树结构
 * 这是一个高级的课题，涉及到如何设计数据格式
 */

void save(Node root) {
    try {
        Stack<Node> sta = new Stack<>();
        sta.add(root);
        int indexId = 0;//索引文件的Id
        int indexWritten = 0;//当前索引文件已经写了的个数
        DataOutputStream cout = null;
        Set<Path> validPaths = new TreeSet<>();
        while (!sta.isEmpty()) {
            if (cout == null || indexWritten == Config.nodeCountPerIndexFile) {
                if (cout != null) cout.close();
                //加前导0的意义是可以直接按照字符串进行排序
                Path nowPath = indexPath.resolve(String.format("index%05d.bin", indexId));
                validPaths.add(nowPath);
                cout = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(nowPath)));
                indexId++;
                indexWritten = 0;
            }
            Node node = sta.pop();
            writeOneNode(cout, node);
            indexWritten++;
            if (node.sons != null) {
                for (Node i : node.sons.values()) {
                    sta.push(i);
                }
            }
        }
        if (cout != null) cout.close();
        removeOldIndex(validPaths);
    } catch (IOException e) {
        e.printStackTrace();
        throw new RuntimeException("save node error");
    }

}

public static void main(String[] args) throws IOException {
    FileSizeGetter getter = new FileSizeGetter();
    Node node = getter.loadAndUpdate();
    System.out.println(node.name);
    System.out.println(node.sons.keySet());
}
}