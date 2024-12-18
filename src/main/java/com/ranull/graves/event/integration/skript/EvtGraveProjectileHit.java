package com.ranull.graves.event.integration.skript;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.ranull.graves.event.GraveProjectileHitEvent;
import com.ranull.graves.type.Grave;
import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.EventValues;
import ch.njol.util.Checker;
import ch.njol.skript.util.Getter;

@Name("Grave Projectile Hit Event")
@Description("Triggered when a grave block is broken. Provides access to the grave, player, block, and block type.")
@Examples({
        "on grave projectile hit:",
        "\tbroadcast \"%event-grave% \"",
})
public class EvtGraveProjectileHit extends SkriptEvent {

    static {
        Skript.registerEvent("Grave Projectile Hit", EvtGraveProjectileHit.class, GraveProjectileHitEvent.class, "[grave] projectil(e|es) hi(t|tting)");

        // Registering event values
        EventValues.registerEventValue(GraveProjectileHitEvent.class, Player.class, new Getter<Player, GraveProjectileHitEvent>() {
            @Override
            public Player get(GraveProjectileHitEvent e) {
                return e.getPlayer();
            }
        }, 0);
        EventValues.registerEventValue(GraveProjectileHitEvent.class, Grave.class, new Getter<Grave, GraveProjectileHitEvent>() {
            @Override
            public Grave get(GraveProjectileHitEvent e) {
                return e.getGrave();
            }
        }, 0);
        EventValues.registerEventValue(GraveProjectileHitEvent.class, Block.class, new Getter<Block, GraveProjectileHitEvent>() {
            @Override
            public Block get(GraveProjectileHitEvent e) {
                return e.getBlock();
            }
        }, 0);
        EventValues.registerEventValue(GraveProjectileHitEvent.class, Entity.class, new Getter<Entity, GraveProjectileHitEvent>() {
            @Override
            public Entity get(GraveProjectileHitEvent e) {
                return e.getEntity();
            }
        }, 0);
        EventValues.registerEventValue(GraveProjectileHitEvent.class, LivingEntity.class, new Getter<LivingEntity, GraveProjectileHitEvent>() {
            @Override
            public LivingEntity get(GraveProjectileHitEvent e) {
                return e.getLivingEntity();
            }
        }, 0);
    }

    private Literal<Player> player;
    private Literal<Grave> grave;
    private Literal<Block> block;
    private Literal<Entity> entity;
    private Literal<LivingEntity> livingEntity;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Literal<?> @NotNull [] args, int matchedPattern, @NotNull ParseResult parseResult) {
        //player = (Literal<Player>) args[0];
        //grave = (Literal<Grave>) args[0];
        //block = (Literal<Block>) args[0];
        //entity = (Literal<Entity>) args[0];
        //livingEntity = (Literal<LivingEntity>) args[0];
        return true;
    }

    @Override
    public boolean check(Event e) {
        if (e instanceof GraveProjectileHitEvent) {
            GraveProjectileHitEvent event = (GraveProjectileHitEvent) e;

            // Check for player
            if (player != null && !player.check(event, new Checker<Player>() {
                @Override
                public boolean check(Player p) {
                    return p.equals(event.getPlayer());
                }
            })) {
                return false;
            }

            // Check for grave
            if (grave != null && !grave.check(event, new Checker<Grave>() {
                @Override
                public boolean check(Grave g) {
                    return g.equals(event.getGrave());
                }
            })) {
                return false;
            }

            // Check for block
            if (block != null && !block.check(event, new Checker<Block>() {
                @Override
                public boolean check(Block b) {
                    return b.equals(event.getBlock());
                }
            })) {
                return false;
            }

            // Check for entity
            if (entity != null && !entity.check(event, new Checker<Entity>() {
                @Override
                public boolean check(Entity e) {
                    return e.equals(event.getEntity());
                }
            })) {
                return false;
            }

            // Check for living entity
            if (livingEntity != null && !livingEntity.check(event, new Checker<LivingEntity>() {
                @Override
                public boolean check(LivingEntity le) {
                    return le.equals(event.getLivingEntity());
                }
            })) {
                return false;
            }

            return true;
        }
        return false;
    }

    @Override
    public String toString(@Nullable Event e, boolean debug) {
        return "Grave break event " +
                (player != null ? " with player " + player.toString(e, debug) : "") +
                (grave != null ? " with grave " + grave.toString(e, debug) : "") +
                (block != null ? " with block " + block.toString(e, debug) : "") +
                (entity != null ? " with entity " + entity.toString(e, debug) : "") +
                (livingEntity != null ? " with living entity " + livingEntity.toString(e, debug) : "");
    }
}