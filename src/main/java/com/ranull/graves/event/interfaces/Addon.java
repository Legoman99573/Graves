package com.ranull.graves.event.interfaces;

/**
 * Interface representing an addon feature for the GravesX plugin.
 * Provides methods to check and set the addon status.
 */
public interface Addon {

    /**
     * Checks if the current instance is an addon.
     *
     * @return {@code true} if this is an addon, {@code false} otherwise.
     */
    boolean isAddon();

    /**
     * Sets the addon status for the current instance.
     *
     * @param addon {@code true} to mark as an addon, {@code false} otherwise.
     */
    void setAddon(boolean addon);
}