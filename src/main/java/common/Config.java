package common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Config {
final static public Path home = Paths.get(System.getProperty("user.home")).resolve("DiskManager");
public static final Path indexPath = home.resolve("index.bin");
public static final Path root = Paths.get("/");
public static final int threadCount = 1;

static {
    if (!Files.exists(home)) {
        try {
            Files.createDirectory(home);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
}
