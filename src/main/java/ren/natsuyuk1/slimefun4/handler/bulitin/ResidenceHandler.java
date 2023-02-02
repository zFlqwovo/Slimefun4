package ren.natsuyuk1.slimefun4.handler.bulitin;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import io.github.bakedlibs.dough.protection.ActionType;
import io.github.bakedlibs.dough.protection.Interaction;
import io.github.thebusybiscuit.slimefun4.api.events.AndroidFarmEvent;
import io.github.thebusybiscuit.slimefun4.api.events.AndroidMineEvent;
import io.github.thebusybiscuit.slimefun4.api.events.ExplosiveToolBreakBlocksEvent;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import ren.natsuyuk1.slimefun4.event.AndroidMoveEvent;
import ren.natsuyuk1.slimefun4.handler.IExtendedInteractHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ResidenceHandler implements IExtendedInteractHandler {
    @Override
    public String name() {
        return "Residence";
    }

    @Override
    public boolean checkEnvironment() {
        return Bukkit.getPluginManager().getPlugin("Residence") != null;
    }

    @Override
    public void initEnvironment() {
    }

    @Override
    public void onAndroidMine(@Nonnull AndroidMineEvent event, @Nonnull OfflinePlayer owner) {
        var android = event.getAndroid().getBlock();

        if (checkResidence(owner, android, Interaction.BREAK_BLOCK)) {
            event.setCancelled(true);
            if (owner.isOnline() && owner.getPlayer() != null) {
                Slimefun.getLocalization().sendMessage(owner.getPlayer(), "android.no-permission");
            }
        }
    }

    @Override
    public void onAndroidFarm(@Nonnull AndroidFarmEvent event, @Nonnull OfflinePlayer owner) {
        var android = event.getAndroid().getBlock();

        if (checkResidence(owner, android, Interaction.BREAK_BLOCK)) {
            event.setCancelled(true);
            if (owner.isOnline() && owner.getPlayer() != null) {
                Slimefun.getLocalization().sendMessage(owner.getPlayer(), "android.no-permission");
            }
        }
    }

    @Override
    public void onAndroidMove(@Nonnull AndroidMoveEvent event, @Nullable OfflinePlayer owner) {
        if (owner != null) {
            if (!checkResidence(owner, event.getTo(), Interaction.PLACE_BLOCK)) {
                event.setCancelled(true);

                BlockStorage.addBlockInfo(event.getAndroid().getBlock(), "paused", "false");
                if (owner.isOnline()) {
                    Slimefun.getLocalization().sendMessage(owner.getPlayer(), "messages.android-no-permission", true);
                }
            }
        }
    }

    @Override
    public void onExplosiveToolBreakBlocks(@Nonnull ExplosiveToolBreakBlocksEvent event) {
        if (!checkResidence(event.getPlayer(), event.getPrimaryBlock(), Interaction.BREAK_BLOCK)) {
            event.setCancelled(true);
        }

        event.getAdditionalBlocks().removeIf(block -> checkResidence(event.getPlayer(), block, Interaction.BREAK_BLOCK));
    }

    /**
     * 检查是否可以在领地内破坏/交互方块
     * <p>
     * 领地已支持 Slimefun
     * <p>
     * 详见: <a href="https://github.com/Zrips/Residence/blob/master/src/com/bekvon/bukkit/residence/slimeFun/SlimeFunResidenceModule.java">...</a>
     *
     * @param p      玩家
     * @param block  被破坏的方块
     * @param action 交互类型
     * @return 是否可以破坏
     */
    public static boolean checkResidence(@Nullable OfflinePlayer p, @Nonnull Block block, Interaction action) {
        if (!Bukkit.getPluginManager().isPluginEnabled("Residence") || p == null || !p.isOnline() || p.isOp()) {
            return true;
        }

        var res = Residence.getInstance().getResidenceManager().getByLoc(block.getLocation());

        if (res != null) {
            if (res.getOwnerUUID() == p.getUniqueId()) {
                return true;
            }

            var onlinePlayer = p.getPlayer();

            if (onlinePlayer == null) {
                return false;
            }

            if (onlinePlayer.hasPermission("residence.admin")) {
                return true;
            }

            var perms = res.getPermissions();

            if (perms != null) {
                if (action.getType() == ActionType.BLOCK && perms.playerHas(onlinePlayer, Flags.admin, FlagPermissions.FlagCombo.OnlyTrue)) {
                    return true;
                }

                switch (action) {
                    case BREAK_BLOCK:
                        return perms.playerHas(onlinePlayer, Flags.destroy, FlagPermissions.FlagCombo.OnlyTrue);
                    case INTERACT_BLOCK:
                        return perms.playerHas(onlinePlayer, Flags.container, FlagPermissions.FlagCombo.OnlyTrue);
                    case PLACE_BLOCK:
                        // move 是为了机器人而检查的, 防止机器人跑进别人领地然后还出不来
                        return perms.playerHas(onlinePlayer, Flags.place, FlagPermissions.FlagCombo.OnlyTrue) || perms.playerHas(onlinePlayer, Flags.build, FlagPermissions.FlagCombo.OnlyTrue) && perms.playerHas(onlinePlayer, Flags.move, FlagPermissions.FlagCombo.TrueOrNone);
                }
            }
        }
        return true;
    }
}
