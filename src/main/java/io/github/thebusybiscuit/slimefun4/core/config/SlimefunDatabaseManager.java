package io.github.thebusybiscuit.slimefun4.core.config;

import com.xzavier0722.mc.plugin.slimefun4.storage.adapter.IDataSourceAdapter;
import com.xzavier0722.mc.plugin.slimefun4.storage.adapter.mysql.MysqlAdapter;
import com.xzavier0722.mc.plugin.slimefun4.storage.adapter.mysql.MysqlConfig;
import com.xzavier0722.mc.plugin.slimefun4.storage.adapter.sqlite.SqliteAdapter;
import com.xzavier0722.mc.plugin.slimefun4.storage.adapter.sqlite.SqliteConfig;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.ControllerHolder;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.PlayerProfileDataController;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.StorageType;
import io.github.bakedlibs.dough.config.Config;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import javax.annotation.Nullable;

public class SlimefunDatabaseManager {
    private final Slimefun plugin;
    private final Config databaseConfig;

    private int readExecutorThread;
    private int writeExecutorThread;
    private StorageType storageType;
    private IDataSourceAdapter<?> adapter;


    public SlimefunDatabaseManager(Slimefun plugin) {
        this.plugin = plugin;
        databaseConfig = new Config(plugin, "database");

        readExecutorThread = databaseConfig.getInt("readExecutorThread");
        writeExecutorThread = databaseConfig.getInt("writeExecutorThread");

        var type = databaseConfig.getValueAs(StorageType.class, "storageType");

        if (type.isEmpty()) {
            Slimefun.logger().log(Level.SEVERE, "加载数据库配置失败: 数据库类型填写错误");
            return;
        }

        storageType = type.get();
    }

    public void init() {
        try {
            initAdapter();
        } catch (IOException e) {
            Slimefun.logger().log(Level.SEVERE, "加载数据库适配器失败", e);
            return;
        }

        var profileController = ControllerHolder.createController(PlayerProfileDataController.class, storageType);
        profileController.init(adapter, readExecutorThread, writeExecutorThread);
    }

    public void initAdapter() throws IOException {
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
                var dbPath = new File("plugins/" + plugin.getName().replace(" ", "_"), "database.db");
                dbPath.createNewFile();

                ((IDataSourceAdapter<SqliteConfig>) adapter).prepare(
                        new SqliteConfig(
                                dbPath.getAbsolutePath(),
                                databaseConfig.getInt("sqlite.maxReadConnection")
                        ));
            }
        }
    }

    @Nullable
    public PlayerProfileDataController getProfileDataController() {
        return ControllerHolder.getController(PlayerProfileDataController.class, storageType);
    }
}
