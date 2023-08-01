package com.xzavier0722.mc.plugin.slimefun4.storage.common;

public enum DataScope {
    NONE,
    PLAYER_RESEARCH,
    PLAYER_PROFILE(new FieldKey[]{FieldKey.PLAYER_NAME}),
    BACKPACK_PROFILE(new FieldKey[]{FieldKey.PLAYER_UUID, FieldKey.BACKPACK_NAME}),
    BACKPACK_INVENTORY(new FieldKey[]{FieldKey.BACKPACK_ID, FieldKey.INVENTORY_SLOT}),
    BLOCK_RECORD(new FieldKey[]{FieldKey.LOCATION}),
    BLOCK_DATA(new FieldKey[]{FieldKey.LOCATION, FieldKey.DATA_KEY}),
    CHUNK_DATA(new FieldKey[]{FieldKey.CHUNK, FieldKey.DATA_KEY}),
    BLOCK_INVENTORY(new FieldKey[]{FieldKey.LOCATION, FieldKey.INVENTORY_SLOT});

    private final FieldKey[] indexKey;

    DataScope() {
        indexKey = new FieldKey[0];
    }

    DataScope(FieldKey[] indexKey) {
        this.indexKey = indexKey;
    }

    public FieldKey[] getIndexKey() {
        return indexKey;
    }
}
