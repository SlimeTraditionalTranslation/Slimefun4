package com.xzavier0722.mc.plugin.slimefun4.storage.util;

import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;

public class FileUtils {
    public static boolean checkDirectoryExists(File dir) {
        return dir.exists() && dir.isDirectory() && dir.listFiles() != null && dir.listFiles().length > 0;
    }

    public static void deleteDir(File dir) {
        if (!dir.isDirectory()) return;

        try (var fs = Files.walk(dir.toPath())) {
            fs.map(Path::toFile).forEach(File::delete);
        } catch (Exception e) {
            Slimefun.logger().log(Level.WARNING, "刪除資料夾 " + dir.getName() + " 時失敗", e);
        }

        dir.delete();
    }
}
