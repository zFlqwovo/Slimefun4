package com.xzavier0722.mc.plugin.slimefun4.storage.common;

public enum FieldKey {
    PLAYER_UUID(false, true),
    PLAYER_NAME,
    PLAYER_BACKPACK_NUM(true),

    RESEARCH_ID,

    BACKPACK_ID(false, true),
    BACKPACK_NUMBER(true),
    BACKPACK_NAME,
    BACKPACK_SIZE(true),

    INVENTORY_SLOT(true, true),
    INVENTORY_ITEM,

    LOCATION(false, true),
    CHUNK(false, true),
    SLIMEFUN_ID,

    DATA_KEY(false, true),
    DATA_VALUE;

    private final boolean isNumType;
    private final boolean isPrimary;

    FieldKey() {
        this(false, false);
    }

    FieldKey(boolean isNumType) {
        this.isNumType = isNumType;
        this.isPrimary = false;
    }

    FieldKey(boolean isNumType, boolean isPrimary) {
        this.isNumType = isNumType;
        this.isPrimary = isPrimary;
    }

    public boolean isNumType() {
        return isNumType;
    }

    public boolean isPrimary() {
        return isPrimary;
    }
}
