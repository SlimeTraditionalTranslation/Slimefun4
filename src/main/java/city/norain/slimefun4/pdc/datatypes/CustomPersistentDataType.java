package city.norain.slimefun4.pdc.datatypes;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

public class CustomPersistentDataType<T extends ConfigurationSerializable> implements PersistentDataType<byte[], T> {
    private final Class<T> type;

    public CustomPersistentDataType(final Class<T> type) {
        this.type = type;
    }

    @Override
    public Class<byte[]> getPrimitiveType() {
        return byte[].class;
    }

    @Override
    public Class<T> getComplexType() {
        return type;
    }

    @Override
    public byte[] toPrimitive(T t, PersistentDataAdapterContext persistentDataAdapterContext) {
        try (final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             final BukkitObjectOutputStream bukkitObjectOutputStream = new BukkitObjectOutputStream(outputStream)) {
            bukkitObjectOutputStream.writeObject(t);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Unable to serialize custom persistent data (" + type.getName() + ")", e);
        }
    }

    @Override
    public T fromPrimitive(byte[] bytes, PersistentDataAdapterContext persistentDataAdapterContext) {
        try (final ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
             final BukkitObjectInputStream bukkitObjectInputStream = new BukkitObjectInputStream(inputStream)) {
            return (T) bukkitObjectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Unable to deserialize custom persistent data (" + type.getName() + ")", e);
        }
    }
}
