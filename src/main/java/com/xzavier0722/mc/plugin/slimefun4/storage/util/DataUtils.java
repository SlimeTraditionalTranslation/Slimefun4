package com.xzavier0722.mc.plugin.slimefun4.storage.util;

import io.github.thebusybiscuit.slimefun4.core.debug.Debug;
import io.github.thebusybiscuit.slimefun4.core.debug.TestCase;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

public class DataUtils {
    public static String itemStack2String(ItemStack itemStack) {
        Debug.log(TestCase.BACKPACK, "Serializing itemstack: " + itemStack);

        var stream = new ByteArrayOutputStream();
        try (var bs = new BukkitObjectOutputStream(stream)) {
            bs.writeObject(itemStack);
            return Base64Coder.encodeLines(stream.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static ItemStack string2ItemStack(String base64Str) {
        if (base64Str == null || base64Str.isEmpty() || base64Str.isBlank()) {
            return null;
        }

        Debug.log(TestCase.BACKPACK, "Deserializing itemstack: " + base64Str);

        var stream = new ByteArrayInputStream(Base64Coder.decodeLines(base64Str));
        try (var bs = new BukkitObjectInputStream(stream)) {
            var result = (ItemStack) bs.readObject();

            Debug.log(TestCase.BACKPACK, "Deserialized itemstack: " + result);

            if (result.getType().isAir()) {
                Slimefun.logger().log(Level.WARNING, "反序列化数据库中的物品失败! 对应物品无法显示.");
            }

            return result;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String blockDataBase64(String text) {
        return Slimefun.getDatabaseManager().isBlockDataBase64Enabled() ? Base64Coder.encodeString(text) : text;
    }

    public static String blockDataDebase64(String base64Str) {
        return Slimefun.getDatabaseManager().isBlockDataBase64Enabled() ? Base64Coder.decodeString(base64Str) : base64Str;
    }

    public static String profileDataBase64(String text) {
        return Slimefun.getDatabaseManager().isProfileDataBase64Enabled() ? Base64Coder.encodeString(text) : text;
    }

    public static String profileDataDebase64(String base64Str) {
        return Slimefun.getDatabaseManager().isProfileDataBase64Enabled() ? Base64Coder.decodeString(base64Str) : base64Str;
    }
}
