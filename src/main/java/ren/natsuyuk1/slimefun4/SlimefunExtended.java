package ren.natsuyuk1.slimefun4;

import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;

import javax.annotation.Nonnull;

public final class SlimefunExtended {
    public static void register(@Nonnull Slimefun sf) {
        ExtendedInteractManager.init(sf);
        VaultHelper.register(sf);
    }

    public static void shutdown() {
        ExtendedInteractManager.shutdown();
        VaultHelper.shutdown();
    }
}
