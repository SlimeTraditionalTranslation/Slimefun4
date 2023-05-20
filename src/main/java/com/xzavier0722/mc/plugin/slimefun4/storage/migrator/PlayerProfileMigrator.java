package com.xzavier0722.mc.plugin.slimefun4.storage.migrator;

import com.xzavier0722.mc.plugin.slimefun4.storage.util.FileUtils;
import io.github.bakedlibs.dough.config.Config;
import io.github.thebusybiscuit.slimefun4.api.researches.Research;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import java.io.File;
import java.util.HashSet;
import java.util.UUID;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

public class PlayerProfileMigrator {
    private static final File playerFolder = new File("data-storage/Slimefun/Players/");
    private static volatile boolean migrateLock = false;

    public static boolean isOldDataExists() {
        return FileUtils.checkDirectoryExists(playerFolder);
    }

    public static void checkOldData() {
        if (isOldDataExists()) {
            Slimefun.logger().log(Level.WARNING, "偵測到使用舊型檔案式儲存的玩家資料，請使用 /sf migrate 轉移舊資料到資料庫中！");
        }
    }

    /**
     * To check the existence of old player data stored as yml
     * and try to migrate them to database
     */
    public static MigrateStatus migrateOldData() {
        if (migrateLock) {
            return MigrateStatus.MIGRATING;
        }

        migrateLock = true;
        var result = MigrateStatus.SUCCESS;

        var listFiles = playerFolder.listFiles();
        if (!isOldDataExists()) {
            migrateLock = false;
            return MigrateStatus.MIGRATED;
        }

        var migratedCount = 0;
        var total = listFiles.length;

        for (File file : listFiles) {
            if (!file.getName().endsWith(".yml")) {
                continue;
            }

            try {
                var uuid = UUID.fromString(file.getName().replace(".yml", ""));
                var p = Bukkit.getOfflinePlayer(uuid);

                if (!p.hasPlayedBefore()) {
                    Slimefun.logger().log(Level.INFO, "偵測到從未加入過伺服器的玩家資料，已自動跳過：" + uuid);
                    total--;
                    continue;
                }

                migratePlayerProfile(p);

                migratedCount++;
                Slimefun.logger().log(Level.INFO, "成功轉移玩家資料：" + p.getName() + "(" + migratedCount + "/" + total + ")");
            } catch (IllegalArgumentException ignored) {
                Slimefun.logger().log(Level.WARNING, "偵測到不合法的玩家名稱資料檔案：'" + file.getName() + "'");
                // illegal player name, skip
            }
        }

        if (MigratorUtil.createDirBackup(playerFolder)) {
            Slimefun.logger().log(Level.INFO, "成功轉移 {0} 個玩家資料！轉移前的資料已儲存在 ./data-storage/Slimefun/old_data 下", migratedCount);
            playerFolder.delete();
        }

        migrateLock = false;

        return result;
    }

    private static void migratePlayerProfile(@Nonnull OfflinePlayer p) {
        var uuid = p.getUniqueId();
        var configFile = new Config("data-storage/Slimefun/Players/" + uuid + ".yml");

        if (!configFile.getFile().exists()) {
            return;
        }

        var controller = Slimefun.getDatabaseManager().getProfileDataController();
        var profile = controller.getProfile(p);
        if (null == profile) {
            profile = controller.createProfile(p);
        }

        // Research migrate
        for (String researchID : configFile.getKeys("researches")) {
            var research = Research.getResearchByID(Integer.parseInt(researchID));

            if (research.isEmpty()) {
                continue;
            }

            profile.setResearched(research.get(), true);
        }

        // Backpack migrate
        var max = 0;
        for (String backpackID : configFile.getKeys("backpacks")) {
            var bpID = Integer.parseInt(backpackID);
            if (max < bpID) {
                max = bpID;
            }
            var size = configFile.getInt("backpacks." + bpID + ".size");

            var bp = controller.createBackpack(p, "", bpID, size);

            var changedSlot = new HashSet<Integer>();

            for (String key : configFile.getKeys("backpacks." + bpID + ".contents")) {
                var bpKey = Integer.parseInt(key);
                var item = configFile.getItem("backpacks." + bpID + ".contents." + bpKey);
                bp.getInventory().setItem(bpKey, item);
                changedSlot.add(bpKey);
            }

            controller.saveBackpackInventory(bp, changedSlot);
        }
        profile.setBackpackCount(max);
    }

}
