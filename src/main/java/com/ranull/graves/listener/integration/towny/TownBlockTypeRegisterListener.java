package com.ranull.graves.listener.integration.towny;

import com.ranull.graves.integration.Towny;
import org.bukkit.event.Listener;

/**
 * Listens for TownBlockTypeRegisterEvent to register the block type in Towny.
 */
public class TownBlockTypeRegisterListener implements Listener {
    private final Towny towny;

    /**
     * Constructs a new TownBlockTypeRegisterListener with the specified Towny instance.
     *
     * @param towny The Towny instance to use.
     */
    public TownBlockTypeRegisterListener(Towny towny) {
        this.towny = towny;
    }
}