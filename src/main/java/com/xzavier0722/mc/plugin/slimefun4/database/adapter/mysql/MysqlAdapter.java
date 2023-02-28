package com.xzavier0722.mc.plugin.slimefun4.database.adapter.mysql;

import com.xzavier0722.mc.plugin.slimefun4.database.adapter.IDataSourceAdapter;
import com.xzavier0722.mc.plugin.slimefun4.database.data.RecordKey;
import com.xzavier0722.mc.plugin.slimefun4.database.data.RecordSet;

import java.util.Set;

public class MysqlAdapter implements IDataSourceAdapter<MysqlConfig> {
    private MysqlConnectionPool pool;

    @Override
    public void prepare(MysqlConfig config) {
        pool = new MysqlConnectionPool(config);
    }

    @Override
    public void shutdown() {
        pool.destroy();
    }

    @Override
    public void setData(RecordKey key, RecordSet item) {
        // TODO
    }

    @Override
    public Set<RecordSet> getData(RecordKey key) {
        // TODO
        return null;
    }

    @Override
    public void deleteData(RecordKey key) {
        // TODO
    }
}
