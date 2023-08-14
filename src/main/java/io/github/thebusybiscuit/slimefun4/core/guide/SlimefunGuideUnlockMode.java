package io.github.thebusybiscuit.slimefun4.core.guide;

import io.github.thebusybiscuit.slimefun4.api.guide.SlimefunGuideUnlockProvider;
import javax.annotation.Nonnull;

import io.github.thebusybiscuit.slimefun4.core.guide.unlockprovider.CurrencyUnlockProvider;
import io.github.thebusybiscuit.slimefun4.core.guide.unlockprovider.ExperienceUnlockProvider;
import javax.annotation.Nullable;

/**
 * This enum holds the different unlock research modes a {@link SlimefunGuide} can have.
 * Each constant corresponds to a research unlock mode and a unlock provider
 *
 * @author StarWishsama
 * @see SlimefunGuide
 * @see SlimefunGuideImplementation
 * @see SlimefunGuideUnlockProvider
 */
public enum SlimefunGuideUnlockMode {

    EXPERIENCE(new ExperienceUnlockProvider()),

    /**
     * Unlock research by withdrawing player's balance.
     */
    CURRENCY(new CurrencyUnlockProvider());

    /**
     * Research unlock provider
     * <p>
     * Process player can unlock research and process research payment.
     *
     * @see SlimefunGuideUnlockProvider
     */
    @Nonnull
    private final SlimefunGuideUnlockProvider unlockProvider;

    SlimefunGuideUnlockMode(@Nonnull SlimefunGuideUnlockProvider unlockProvider) {
        this.unlockProvider = unlockProvider;
    }

    /**
     * Convert string to certain {@link SlimefunGuideUnlockMode}.
     * If string is invalid will fall back to default one (player level)
     *
     * @param s text to validate
     * @return {@link SlimefunGuideUnlockMode}
     */
    public static @Nonnull SlimefunGuideUnlockMode check(@Nullable String s) {
        if (s == null) {
            return SlimefunGuideUnlockMode.EXPERIENCE;
        }

        for (SlimefunGuideUnlockMode value : SlimefunGuideUnlockMode.values()) {
            if (value.toString().equalsIgnoreCase(s)) {
                return value;
            }
        }

        return SlimefunGuideUnlockMode.EXPERIENCE;
    }

    public @Nonnull SlimefunGuideUnlockProvider getUnlockProvider() {
        return unlockProvider;
    }
}
