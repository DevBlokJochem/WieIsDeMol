package nl.mxndarijn.wieisdemol.game.events;

import io.papermc.paper.event.player.AsyncChatEvent;
import nl.mxndarijn.wieisdemol.game.Game;
import nl.mxndarijn.wieisdemol.game.GamePlayer;
import nl.mxndarijn.wieisdemol.items.Items;
import nl.mxndarijn.wieisdemol.managers.language.LanguageManager;
import nl.mxndarijn.wieisdemol.managers.language.LanguageText;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;

public class GameSpectatorEvents extends GameEvent {


    public GameSpectatorEvents(Game g, JavaPlugin plugin) {
        super(g, plugin);
    }

    @EventHandler
    public void interactSpectator(PlayerInteractEvent e) {
        if (!validateWorld(e.getPlayer().getWorld()))
            return;
        if (game.getSpectators().contains(e.getPlayer().getUniqueId())) {
            e.setCancelled(true);
            return;
        }
        Optional<GamePlayer> gp = game.getGamePlayerOfPlayer(e.getPlayer().getUniqueId());
        if (gp.isPresent() && !gp.get().isAlive()) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void damage(EntityDamageEvent e) {
        if (!validateWorld(e.getEntity().getWorld()))
            return;
        if (game.getSpectators().contains(e.getEntity().getUniqueId())) {
            e.setCancelled(true);
            return;
        }
        Optional<GamePlayer> gp = game.getGamePlayerOfPlayer(e.getEntity().getUniqueId());
        if (gp.isPresent() && !gp.get().isAlive()) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void damage(EntityDamageByEntityEvent e) {
        if (!validateWorld(e.getDamager().getWorld()))
            return;
        if (game.getSpectators().contains(e.getDamager().getUniqueId())) {
            e.setCancelled(true);
            return;
        }
        Optional<GamePlayer> gp = game.getGamePlayerOfPlayer(e.getDamager().getUniqueId());
        if (gp.isPresent() && !gp.get().isAlive()) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void hunger(FoodLevelChangeEvent e) {
        if (!validateWorld(e.getEntity().getWorld()))
            return;
        if (game.getSpectators().contains(e.getEntity().getUniqueId())) {
            e.setCancelled(true);
            return;
        }
        Optional<GamePlayer> gp = game.getGamePlayerOfPlayer(e.getEntity().getUniqueId());
        if (gp.isPresent() && !gp.get().isAlive()) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void pickup(EntityPickupItemEvent e) {
        if (!validateWorld(e.getEntity().getWorld()))
            return;
        if (game.getSpectators().contains(e.getEntity().getUniqueId())) {
            e.setCancelled(true);
            return;
        }
        Optional<GamePlayer> gp = game.getGamePlayerOfPlayer(e.getEntity().getUniqueId());
        if (gp.isPresent() && !gp.get().isAlive()) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void pickup(EntityDropItemEvent e) {
        if (!validateWorld(e.getEntity().getWorld()))
            return;
        if (game.getSpectators().contains(e.getEntity().getUniqueId())) {
            e.setCancelled(true);
            return;
        }
        Optional<GamePlayer> gp = game.getGamePlayerOfPlayer(e.getEntity().getUniqueId());
        if (gp.isPresent() && !gp.get().isAlive()) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void place(BlockPlaceEvent e) {
        if (!validateWorld(e.getPlayer().getWorld()))
            return;
        if (game.getSpectators().contains(e.getPlayer().getUniqueId())) {
            e.setCancelled(true);
            return;
        }
        Optional<GamePlayer> gp = game.getGamePlayerOfPlayer(e.getPlayer().getUniqueId());
        if (gp.isPresent() && !gp.get().isAlive()) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void place(BlockBreakEvent e) {
        if (!validateWorld(e.getPlayer().getWorld()))
            return;
        if (game.getSpectators().contains(e.getPlayer().getUniqueId())) {
            e.setCancelled(true);
            return;
        }
        Optional<GamePlayer> gp = game.getGamePlayerOfPlayer(e.getPlayer().getUniqueId());
        if (gp.isPresent() && !gp.get().isAlive()) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void respawn(PlayerRespawnEvent e) {
        if (!game.getSpectators().contains(e.getPlayer().getUniqueId())) {
            if (game.getRespawnLocations().containsKey(e.getPlayer().getUniqueId()))
                e.setRespawnLocation(game.getRespawnLocations().get(e.getPlayer().getUniqueId()));
            return;
        }
        Optional<GamePlayer> gp = game.getGamePlayerOfPlayer(e.getPlayer().getUniqueId());
        if (gp.isPresent() && !gp.get().isAlive()) {
            if (game.getRespawnLocations().containsKey(e.getPlayer().getUniqueId()))
                e.setRespawnLocation(game.getRespawnLocations().get(e.getPlayer().getUniqueId()));
        }

        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            Player p = e.getPlayer();
            p.getInventory().setItem(0, Items.GAME_SPECTATOR_TELEPORT_ITEM.getItemStack());
            if (game.getSpectators().contains(p.getUniqueId())) {
                p.getInventory().setItem(8, Items.GAME_SPECTATOR_LEAVE_ITEM.getItemStack());
            }
        }, 20L);
    }

    @EventHandler
    public void place(EntityDamageByEntityEvent e) {
        if (!validateWorld(e.getDamager().getWorld()))
            return;
        if (game.getSpectators().contains(e.getDamager().getUniqueId())) {
            e.setCancelled(true);
            return;
        }
        Optional<GamePlayer> gp = game.getGamePlayerOfPlayer(e.getDamager().getUniqueId());
        if (gp.isPresent() && !gp.get().isAlive()) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void drop(PlayerDropItemEvent e) {
        if (!validateWorld(e.getPlayer().getWorld()))
            return;
        if (game.getSpectators().contains(e.getPlayer().getUniqueId())) {
            e.setCancelled(true);
            return;
        }
        Optional<GamePlayer> gp = game.getGamePlayerOfPlayer(e.getPlayer().getUniqueId());
        if (gp.isPresent() && !gp.get().isAlive()) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void click(InventoryClickEvent e) {
        if (!validateWorld(e.getWhoClicked().getWorld()))
            return;
        if (game.getSpectators().contains(e.getWhoClicked().getUniqueId())) {
            e.setCancelled(true);
            return;
        }
        Optional<GamePlayer> gp = game.getGamePlayerOfPlayer(e.getWhoClicked().getUniqueId());
        if (gp.isPresent() && !gp.get().isAlive()) {
            if (e.getClickedInventory() == e.getWhoClicked().getInventory())
                e.setCancelled(true);
        }
    }

    @EventHandler
    public void join(PlayerJoinEvent e) {
        if (!validateWorld(e.getPlayer().getWorld()))
            return;
        Optional<GamePlayer> oGp = game.getGamePlayerOfPlayer(e.getPlayer().getUniqueId());
        if(oGp.isEmpty()) {
            if (!game.getSpectators().contains(e.getPlayer().getUniqueId())) {
                return;
            }
        } else {
            if(oGp.get().isAlive()) {
                return;
            }
        }
        game.addSpectatorSettings(e.getPlayer().getUniqueId(), e.getPlayer().getLocation());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void chat(AsyncChatEvent e) {
        if (!validateWorld(e.getPlayer().getWorld()))
            return;
        Optional<GamePlayer> oGp = game.getGamePlayerOfPlayer(e.getPlayer().getUniqueId());
        if(oGp.isEmpty()) {
            if (!game.getSpectators().contains(e.getPlayer().getUniqueId())) {
                return;
            }
        } else {
            if(oGp.get().isAlive()) {
                return;
            }
        }
        e.setCancelled(true);
        e.getPlayer().sendMessage(LanguageManager.getInstance().getLanguageString(LanguageText.GAME_SPECTATOR_TRY_CHAT));

    }
}
