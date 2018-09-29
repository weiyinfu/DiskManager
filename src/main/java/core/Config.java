package core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Config {
//存放配置的目录
final static public Path home = Paths.get(System.getProperty("user.home")).resolve("DiskManager");
//存放索引的位置
public static final Path indexPath = home.resolve("index");
//存放界面配置文件的位置
public static final Path configPath = home.resolve("ui.json");
//建立索引的根目录
public static final Path root = Paths.get("/").toAbsolutePath();
//加载索引、建立索引使用的线程数
public static final int threadCount = 10;
//存储索引时每个索引的结点数
public static final int nodeCountPerIndexFile = 100000;

static {
    for (Path shouldHave : new Path[]{home, indexPath})
        if (!Files.exists(shouldHave)) {
            System.out.println("shouldhave " + shouldHave);
            try {
                Files.createDirectory(shouldHave);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    if (!Files.exists(root)) {
        throw new RuntimeException(root + " 不存在");
    }
}

}
