package core;

import com.alibaba.fastjson.JSON;
import common.Config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * 多线程文件大小求解器
 */
public class FileSizeGetter {
ExecutorService service = Executors.newFixedThreadPool(Config.threadCount);
NodeSerializer serializer = new NodeSerializer();

synchronized void updateSize(Node node, long delta) {
    node.size += delta;
}

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
        //没有这句话会出错,很奇怪
        List<String> sons = Files.list(path).filter(sonPath -> Files.exists(sonPath)).map(sonPath -> sonPath.getFileName().toString()).collect(Collectors.toList());
        node.sons.keySet().retainAll(sons);//更新key
        if (sons.size() == 0) {
            callback.run();
            return;
        }
        CountDownLatch counter = new CountDownLatch(sons.size());
        for (String fileName : sons) {
            Path sonPath = path.resolve(fileName);
            Node son = node.sons.get(fileName);
            //如果依然有效，那么只需要更改版本号即可
            if (son != null && son.getUpdate() == Files.getLastModifiedTime(sonPath).toMillis()) {
                counter.countDown();
                if (counter.getCount() == 0) {
                    callback.run();
                }
                continue;
            }
            if (son != null) {
                updateSize(node, -son.size);
            }
            son = new Node();
            son.name = fileName;
            son.update = Files.getLastModifiedTime(sonPath).toMillis();
            node.sons.put(fileName, son);
            if (Files.isDirectory(sonPath)) {
                son.sons = new TreeMap<>();
            }
            //现在已经把子节点放入了父节点
            Node finalSon = son;
            service.submit(() -> {
                getSize(sonPath, finalSon, () -> {
                    updateSize(node, finalSon.size);
                    //如果当前这个任务是最后一个任务,那么调用callback
                    counter.countDown();
                    if (counter.getCount() == 0) {
                        callback.run();
                    }
                });
            });
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
}

public Node loadIndex() {
    try {
        Node node;
        if (!Files.exists(Config.indexPath)) {
            node = new Node();
            System.out.println("索引不存在,即将创建");
        } else {
            node = serializer.load(Config.indexPath);
            System.out.println("load over");
        }
        if (Config.root.getFileName() == null) {
            node.name = System.getProperty("user.name");
        } else {
            node.name = Config.root.getFileName().toString();
        }
        getSize(Config.root, node, () -> {
            System.out.println("更新索引成功,正在保存");
            serializer.save(node, Config.indexPath);
            service.shutdown();
        });
        //等待全部加载完成
        while (!service.isShutdown()) {
            Thread.sleep(1000);
        }
        return node;
    } catch (Exception e) {
        e.printStackTrace();
        throw new RuntimeException("load index error");
    }
}

public static void main(String[] args) throws IOException {
    FileSizeGetter getter = new FileSizeGetter();
    Node node = getter.loadIndex();
    System.out.println(node.size);
}
}