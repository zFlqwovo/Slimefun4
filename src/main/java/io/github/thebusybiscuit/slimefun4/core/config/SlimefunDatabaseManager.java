package io.github.thebusybiscuit.slimefun4.core.config;

import com.xzavier0722.mc.plugin.slimefun4.storage.adapter.IDataSourceAdapter;
import com.xzavier0722.mc.plugin.slimefun4.storage.adapter.mysql.MysqlAdapter;
import com.xzavier0722.mc.plugin.slimefun4.storage.adapter.mysql.MysqlConfig;
import com.xzavier0722.mc.plugin.slimefun4.storage.adapter.sqlite.SqliteAdapter;
import com.xzavier0722.mc.plugin.slimefun4.storage.adapter.sqlite.SqliteConfig;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.BlockDataController;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.ControllerHolder;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.ProfileDataController;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.StorageType;
import io.github.bakedlibs.dough.config.Config;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import javax.annotation.Nullable;

public class SlimefunDatabaseManager {
    private static final String PROFILE_CONFIG_FILE_NAME = "profile-storage.yml";
    private final Slimefun plugin;
    private final Config databaseConfig;

    private int readExecutorThread;
    private int writeExecutorThread;
    private StorageType storageType;
    private IDataSourceAdapter<?> adapter;


    public SlimefunDatabaseManager(Slimefun plugin) {
        this.plugin = plugin;
        if (!new File(plugin.getDataFolder(), PROFILE_CONFIG_FILE_NAME).exists()) {
            plugin.saveResource(PROFILE_CONFIG_FILE_NAME, false);
        }

        databaseConfig = new Config(plugin, PROFILE_CONFIG_FILE_NAME);
    }

    public void init() {
        storageType = StorageType.valueOf(databaseConfig.getString("storageType"));
        readExecutorThread = databaseConfig.getInt("readExecutorThread");
        writeExecutorThread = storageType == StorageType.SQLITE ? 1 : databaseConfig.getInt("writeExecutorThread");

        try {
            initAdapter();
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "加载数据库适配器失败", e);
            return;
        }

        var profileController = ControllerHolder.createController(ProfileDataController.class, storageType);
        profileController.init(adapter, readExecutorThread, writeExecutorThread);
    }

    private void initAdapter() throws IOException {
        switch (storageType) {
            case MYSQL -> {
                adapter = new MysqlAdapter();

                ((IDataSourceAdapter<MysqlConfig>) adapter).prepare(
                        new MysqlConfig(
                                databaseConfig.getString("mysql.host"),
                                databaseConfig.getInt("mysql.port"),
                                databaseConfig.getString("mysql.database"),
                                databaseConfig.getString("mysql.tablePrefix"),
                                databaseConfig.getString("mysql.user"),
                                databaseConfig.getString("mysql.password"),
                                databaseConfig.getBoolean("mysql.useSSL"),
                                databaseConfig.getInt("mysql.maxConnection")
                        ));
            }
            case SQLITE -> {
                adapter = new SqliteAdapter();
                var profilePath = new File("data-storage/Slimefun", "profile.db");
                profilePath.createNewFile();

                ((IDataSourceAdapter<SqliteConfig>) adapter).prepare(new SqliteConfig(profilePath.getAbsolutePath()));
            }
        }
    }

    @Nullable
    public ProfileDataController getProfileDataController() {
        return ControllerHolder.getController(ProfileDataController.class, storageType);
    }

    public BlockDataController getBlockDataController() {
        return ControllerHolder.getController(BlockDataController.class, storageType);
    }

    public void shutdown() {
        getProfileDataController().shutdown();
        adapter.shutdown();
        ControllerHolder.clearControllers();
    }
}
