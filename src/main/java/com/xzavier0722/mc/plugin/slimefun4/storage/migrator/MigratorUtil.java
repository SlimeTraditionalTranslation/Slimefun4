package com.xzavier0722.mc.plugin.slimefun4.storage.migrator;

import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

class MigratorUtil {
    protected static boolean createDirBackup(File dir) {
        try {
            var oldDataDir = new File("data-storage/Slimefun/old_data/");
            oldDataDir.mkdirs();
            var zipPath = Files.createFile(Path.of("data-storage/Slimefun/old_data/" + dir.getName() + ".zip"));
            try (var zs = new ZipOutputStream(Files.newOutputStream(zipPath))) {
                var src = dir.toPath();
                try (var fs = Files.walk(src).filter(path -> !Files.isDirectory(path))) {
                    fs.forEach(path -> {
                        var zipEntry = new ZipEntry(src.relativize(path).toString());
                        try {
                            zs.putNextEntry(zipEntry);
                            Files.copy(path, zs);
                            zs.closeEntry();
                        } catch (IOException e) {
                            Slimefun.logger().log(Level.WARNING, "備份舊資料 " + dir.getName() + " 時發生問題", e);
                        }
                    });
                }
            }
            return true;
        } catch (Exception e) {
            Slimefun.logger().log(Level.WARNING, "備份舊資料 " + dir.getName() + " 時發生問題", e);
            return false;
        }
    }

    protected static void deleteOldFolder(File dir) {
        try {
            Files.delete(dir.toPath());
        } catch (Exception e) {
            Slimefun.logger().log(Level.WARNING, "刪除資料夾 " + dir.getAbsolutePath() + " 時發生錯誤", e);
        }
    }

    protected static boolean checkMigrateMark() {
        var backupData = new File("data-storage/Slimefun/old_data/");
        return backupData.exists() && backupData.isDirectory() && backupData.listFiles() != null && backupData.listFiles().length > 0;
    }
}
