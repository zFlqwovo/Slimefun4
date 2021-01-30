package io.github.thebusybiscuit.slimefun4.implementation.items.electric.gadgets;

import io.github.thebusybiscuit.slimefun4.core.handlers.ItemUseHandler;
import io.github.thebusybiscuit.slimefun4.implementation.items.SimpleSlimefunItem;
import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config;
import me.mrCookieSlime.Slimefun.Lists.RecipeType;
import me.mrCookieSlime.Slimefun.Objects.Category;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.SlimefunItem;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.AContainer;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import me.mrCookieSlime.Slimefun.api.SlimefunItemStack;
import org.apache.commons.lang.Validate;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;
import java.util.Optional;

public class OverclockModule extends SimpleSlimefunItem<ItemUseHandler> {

    public OverclockModule(Category category, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(category, item, recipeType, recipe);
    }

    @Override
    public ItemUseHandler getItemHandler() {
        return e -> {
            Optional<Block> block = e.getClickedBlock();
            Optional<SlimefunItem> sfBlock = e.getSlimefunBlock();
            e.cancel();

            if (sfBlock.isPresent() && block.isPresent()) {
                SlimefunItem item = sfBlock.get();

                if (item instanceof AContainer) {
                    AContainer machine = (AContainer) item;
                    AContainer defaultItem = (AContainer) SlimefunItem.getByID(item.getId());

                    Validate.notNull(defaultItem, "超频模块获取默认机器参数失败");

                    int multiply = machine.getSpeed() - defaultItem.getSpeed();

                    if (multiply >= 10) {
                        e.getPlayer().sendMessage("超频倍率已达上限: 10x");
                    } else {
                        machine.setProcessingSpeed(machine.getSpeed() + 1);
                        e.getPlayer().sendMessage("超频机器成功, 目前倍率: " + machine.getSpeed() + "x");
                    }
                } else {
                    e.getPlayer().sendMessage("该机器无法超频");
                }
            }
        };
    }
}