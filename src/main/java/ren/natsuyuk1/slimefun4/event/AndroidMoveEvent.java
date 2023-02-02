package ren.natsuyuk1.slimefun4.event;

import io.github.thebusybiscuit.slimefun4.implementation.items.androids.AndroidInstance;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import javax.annotation.Nonnull;

public class AndroidMoveEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private final AndroidInstance android;

    private final Block to;

    private boolean cancelled;

    /**
     * @param to      The {@link Location} where android move to
     * @param android The {@link AndroidInstance} that triggered this {@link Event}
     */
    public AndroidMoveEvent(AndroidInstance android, Block to) {
        this.android = android;
        this.to = to;
    }

    /**
     * This method returns the {@link AndroidInstance} who
     * triggered this {@link Event}
     *
     * @return the involved {@link AndroidInstance}
     */
    @Nonnull
    public AndroidInstance getAndroid() {
        return android;
    }

    @Nonnull
    public Block getTo() {
        return to;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }


    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }
}
