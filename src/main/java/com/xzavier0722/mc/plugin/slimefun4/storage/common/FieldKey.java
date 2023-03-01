package com.xzavier0722.mc.plugin.slimefun4.storage.common;

public enum FieldKey {
    PLAYER_UUID,
    PLAYER_NAME,
    PLAYER_BACKPACK_NUM(true),

    RESEARCH_ID,

    BACKPACK_ID(true),
    BACKPACK_NUMBER(true),
    BACKPACK_SIZE(true),

    INVENTORY_SLOT(true),
    INVENTORY_ITEM;

    private final boolean isNumType;
    FieldKey() {
        this(false);
    }

    FieldKey(boolean isNumType) {
        this.isNumType = isNumType;
    }

    public boolean isNumType() {
        return isNumType;
    }
}
