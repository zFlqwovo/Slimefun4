package com.xzavier0722.mc.plugin.slimefun4.storage.controller;

import io.github.bakedlibs.dough.config.Config;
import org.apache.commons.lang.NotImplementedException;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

@Deprecated
public class BlockDataConfigWrapper extends Config {
    private final SlimefunBlockData blockData;

    public BlockDataConfigWrapper(SlimefunBlockData blockData) {
        super(new File(""));
        this.blockData = blockData;
    }

    @Override
    public void save() {

    }

    @Override
    protected void store(@Nonnull String path, Object value) {

    }

    @Override
    public Date getDate(@Nonnull String path) {
        return new Date(getLong(path));
    }

    @Override
    public boolean getBoolean(@Nonnull String path) {
        return Boolean.parseBoolean(getString(path));
    }

    @Override
    public boolean createFile() {
        return true;
    }

    @Override
    public String getString(@Nonnull String path) {
        return blockData.getData(path);
    }

    @Override
    public long getLong(@Nonnull String path) {
        return Long.parseLong(getString(path));
    }

    @Override
    public int getInt(@Nonnull String path) {
        return Integer.parseInt(getString(path));
    }

    @Nonnull
    @Override
    public Set<String> getKeys() {
        return new HashSet<>(blockData.getAllData().keySet());
    }

    @Nonnull
    @Override
    public Set<String> getKeys(@Nonnull String path) {
        return getKeys();
    }

    @Override
    public Chunk getChunk(@Nonnull String path) {
        return null;
    }

    @Override
    public World getWorld(@Nonnull String path) {
        return Bukkit.getWorld(getString(path));
    }

    @Override
    public double getDouble(@Nonnull String path) {
        return Double.parseDouble(getString(path));
    }


    @Override
    public File getFile() {
        return null;
    }

    @Nullable
    @Override
    public String getHeader() {
        return null;
    }

    @Override
    public float getFloat(@Nonnull String path) {
        return Float.parseFloat(getString(path));
    }

    @Override
    public boolean contains(@Nonnull String path) {
        return getString(path) != null;
    }

    @Override
    public Inventory getInventory(@Nonnull String path, @Nonnull String title) {
        return null;
    }

    @Override
    public Inventory getInventory(@Nonnull String path, int size, @Nonnull String title) {
        return null;
    }

    @Nullable
    @Override
    public ItemStack getItem(@Nonnull String path) {
        return null;
    }

    @Nonnull
    @Override
    public List<Integer> getIntList(@Nonnull String path) {
        return Collections.emptyList();
    }

    @Nonnull
    @Override
    public Location getLocation(@Nonnull String path) {
        return null;
    }

    @Nullable
    @Override
    public Object getValue(@Nonnull String path) {
        return getString(path);
    }

    @Nonnull
    @Override
    public List<String> getStringList(@Nonnull String path) {
        return Collections.emptyList();
    }

    @Override
    public <T> T getOrSetDefault(@Nonnull String path, T value) {
        if (!(value instanceof String str)) {
            throw new NotImplementedException();
        }
        var curr = getString(path);

        if (curr == null) {
            blockData.setData(path, str);
            return value;
        }
        return (T) curr;
    }

    @Override
    public FileConfiguration getConfiguration() {
        return null;
    }

    @Override
    public UUID getUUID(@Nonnull String path) {
        return UUID.fromString(getString(path));
    }

    @Nonnull
    @Override
    public <T> Optional<T> getValueAs(@Nonnull Class<T> c, @Nonnull String path) {
        throw new NotImplementedException();
    }

    @Override
    public void setLogger(@Nonnull Logger logger) {

    }

    @Override
    public void setDefaultValue(@Nonnull String path, @Nullable Object value) {
        if (!(value instanceof String str)) {
            throw new NotImplementedException();
        }
        if (getString(path) == null) {
            blockData.setData(path, str);
        }
    }

    @Override
    public void setValue(@Nonnull String path, Object value) {
        if (!(value instanceof String str)) {
            throw new NotImplementedException();
        }
        blockData.setData(path, str);
    }

    @Override
    public void setHeader(@Nullable String header) {

    }

    @Override
    public void save(@Nonnull File file) {

    }

    @Override
    public void reload() {

    }

    @Override
    public void clear() {

    }
}
