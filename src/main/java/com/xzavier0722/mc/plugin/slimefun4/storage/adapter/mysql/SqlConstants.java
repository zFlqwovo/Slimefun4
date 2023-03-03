package com.xzavier0722.mc.plugin.slimefun4.storage.adapter.mysql;

public interface SqlConstants {
    String TABLE_NAME_PLAYER_PROFILE = "player_profile";
    String TABLE_NAME_PLAYER_RESEARCH = "player_research";
    String TABLE_NAME_BACKPACK = "player_backpack";
    String TABLE_NAME_BACKPACK_INVENTORY = "backpack_inventory";

    String FIELD_PLAYER_UUID = "p_uuid";
    String FIELD_PLAYER_NAME = "p_name";

    String FIELD_RESEARCH_KEY = "research_id";

    String FIELD_BACKPACK_ID = "b_id";
    String FIELD_BACKPACK_SIZE = "b_size";
    String FIELD_BACKPACK_NAME = "b_name";
    String FIELD_BACKPACK_NUM = "b_num";

    String FIELD_INVENTORY_SLOT = "i_slot";
    String FIELD_INVENTORY_ITEM = "i_item";
}
