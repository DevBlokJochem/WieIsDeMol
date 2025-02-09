package nl.mxndarijn.wieisdemol.game.events;

import de.Herbystar.TTA.TTA_Methods;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import nl.mxndarijn.api.inventory.saver.InventoryManager;
import nl.mxndarijn.api.mxworld.MxLocation;
import nl.mxndarijn.api.util.Functions;
import nl.mxndarijn.wieisdemol.data.ItemTag;
import nl.mxndarijn.wieisdemol.data.Role;
import nl.mxndarijn.wieisdemol.game.Game;
import nl.mxndarijn.wieisdemol.game.GamePlayer;
import nl.mxndarijn.wieisdemol.game.UpcomingGameStatus;
import nl.mxndarijn.wieisdemol.items.Items;
import nl.mxndarijn.wieisdemol.managers.chests.ChestInformation;
import nl.mxndarijn.wieisdemol.managers.chests.chestattachments.ChestAttachments;
import nl.mxndarijn.wieisdemol.managers.items.ItemManager;
import nl.mxndarijn.wieisdemol.managers.language.LanguageManager;
import nl.mxndarijn.wieisdemol.managers.language.LanguageText;
import nl.mxndarijn.wieisdemol.managers.shulkers.ShulkerInformation;
import nl.mxndarijn.wieisdemol.map.mapplayer.MapPlayer;
import org.bukkit.*;
import org.bukkit.block.Chest;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class GamePlayingEvents extends GameEvent {
    private final HashMap<Location, ArmorStand> blocks = new HashMap<>();


    public GamePlayingEvents(Game g, JavaPlugin plugin) {
        super(g, plugin);
    }

    @EventHandler
    public void damage(PlayerArmorStandManipulateEvent e) {
        if (!validateWorld(e.getPlayer().getWorld()))
            return;
        if (Functions.convertComponentToString(e.getRightClicked().customName()).equals("attachment")) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void interactShulkerBox(PlayerInteractEvent e) {
        if (game.getGameInfo().getStatus() != UpcomingGameStatus.PLAYING)
            return;
        if (!validateWorld(e.getPlayer().getWorld()))
            return;

        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        assert e.getClickedBlock() != null;
        if (!(e.getClickedBlock().getState() instanceof ShulkerBox shulkerBox)) {
            return;
        }
        Player p = e.getPlayer();
        Optional<GamePlayer> optionalGamePlayer = game.getGamePlayerOfPlayer(p.getUniqueId());
        if (optionalGamePlayer.isEmpty())
            return;

        if (!e.getClickedBlock().getType().equals(optionalGamePlayer.get().getMapPlayer().getColor().getShulkerBlock())) {
            e.setCancelled(true);
            p.sendMessage(LanguageManager.getInstance().getLanguageString(LanguageText.GAME_ONLY_OPEN_OWN_SHULKER));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void changePlayerStateOnShulkerOpen(PlayerInteractEvent e) {
        if (game.getGameInfo().getStatus() != UpcomingGameStatus.PLAYING)
            return;
        if (!validateWorld(e.getPlayer().getWorld()))
            return;

        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        assert e.getClickedBlock() != null;
        if (!(e.getClickedBlock().getState() instanceof ShulkerBox shulkerBox)) {
            return;
        }
        Player p = e.getPlayer();
        Optional<GamePlayer> optionalGamePlayer = game.getGamePlayerOfPlayer(p.getUniqueId());
        if (optionalGamePlayer.isEmpty())
            return;
        if (e.getClickedBlock().getType().equals(optionalGamePlayer.get().getMapPlayer().getColor().getShulkerBlock())) {
            if (e.useInteractedBlock() == Event.Result.ALLOW) {
                Optional<ShulkerInformation> inf = game.getShulkerManager().getShulkerByLocation(MxLocation.getFromLocation(e.getClickedBlock().getLocation()));
                if (inf.isPresent()) {
                    if (inf.get().isStartingRoom()) {
                        optionalGamePlayer.get().setBeginChestOpened(true);
                    }
                    if (!inf.get().isStartingRoom() && !optionalGamePlayer.get().isPeacekeeperChestOpened()) {
                        // Peacekeeper LOOT
                        optionalGamePlayer.get().setPeacekeeperChestOpened(true);
                        if (optionalGamePlayer.get().getMapPlayer().isPeacekeeper()) {
                            optionalGamePlayer.get().givePeacekeeperLoot();
                            game.sendMessageToAll(LanguageManager.getInstance().getLanguageString(LanguageText.GAME_PLAYER_IS_PEACEKEEPER, Collections.singletonList(optionalGamePlayer.get().getMapPlayer().getColor().getColor() + p.getName())));
                            game.sendMessageToAll(LanguageManager.getInstance().getLanguageString(LanguageText.GAME_PEACEKEEPER_KILLS, Collections.singletonList(game.getPeacekeeperKills() + "")));
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void chestAttachmentCanOpenChest(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            assert e.getClickedBlock() != null;
            if (e.getClickedBlock().getType() != Material.CHEST && e.getClickedBlock().getType() != Material.TRAPPED_CHEST) {
                return;
            }
            Optional<ChestInformation> inf = game.getChestManager().getChestByLocation(MxLocation.getFromLocation(e.getClickedBlock().getLocation()));
            Optional<GamePlayer> optionalGamePlayer = game.getGamePlayerOfPlayer(e.getPlayer().getUniqueId());
            if (optionalGamePlayer.isPresent()) {
                if (inf.isPresent()) {
                    inf.get().onChestInteract(optionalGamePlayer.get(), e, game, e.getPlayer());

                    if (!inf.get().canOpenChest(optionalGamePlayer.get())) {
                        e.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClickChest(InventoryClickEvent e) {
        if (e.getClickedInventory() == null)
            return;
        Optional<GamePlayer> gamePlayer = game.getGamePlayerOfPlayer(e.getWhoClicked().getUniqueId());
        if (gamePlayer.isEmpty())
            return;
        game.getChestManager().getChests().forEach(chestInformation -> {
            Location l = chestInformation.getLocation().getLocation(e.getWhoClicked().getWorld());
            if (l.getBlock().getState() instanceof Chest c) {
                if (c.getInventory().equals(e.getClickedInventory())) {
                    chestInformation.onChestInventoryClick(gamePlayer.get(), e, game, (Player) e.getWhoClicked());
                }
                if (e.getWhoClicked().getOpenInventory().getTopInventory().equals(c.getInventory()) && chestInformation.containsChestAttachment(ChestAttachments.CHEST_LIMITED_CHOICE)) {
                    if (e.getAction() == InventoryAction.HOTBAR_SWAP || e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY || e.getAction() == InventoryAction.SWAP_WITH_CURSOR) {
                        e.setCancelled(true);
                    }
                }
            }
        });
    }

    @EventHandler
    public void playerKilled(PlayerDeathEvent e) {
        Optional<GamePlayer> gamePlayer = game.getGamePlayerOfPlayer(e.getPlayer().getUniqueId());
        if (gamePlayer.isEmpty())
            return;
        gamePlayer.get().setAlive(false);
        game.addSpectatorSettings(e.getPlayer().getUniqueId(), e.getPlayer().getLocation());

        e.deathMessage(Component.text(""));
        if (e.getEntity().getKiller() != null) {
            Player killer = e.getEntity().getKiller();
            Optional<GamePlayer> optionalKiller = game.getGamePlayerOfPlayer(killer.getUniqueId());
            if (optionalKiller.isPresent()) {
                if (optionalKiller.get().getMapPlayer().isPeacekeeper() && optionalKiller.get().isPeacekeeperChestOpened()) {
                    game.setPeacekeeperKills(game.getPeacekeeperKills() - 1);
                    if (game.getPeacekeeperKills() == 0) {
                        optionalKiller.get().setAlive(false);
                        game.addSpectatorSettings(killer.getUniqueId(), killer.getLocation());
                        game.sendMessageToAll(LanguageManager.getInstance().getLanguageString(LanguageText.GAME_PEACEKEEPER_DISAPPEARED));
                    }
                }
            }
            game.sendMessageToAll(LanguageManager.getInstance().getLanguageString(LanguageText.GAME_PLAYER_KILLED_BY_PLAYER, new ArrayList<>(Arrays.asList(e.getPlayer().getName(), killer.getName()))));
        } else {
            game.sendMessageToAll(LanguageManager.getInstance().getLanguageString(LanguageText.GAME_PLAYER_DIED, Collections.singletonList(e.getPlayer().getName())));
        }
    }

    @EventHandler
    public void breakBlock(BlockBreakEvent e) {
        if (game.getGameInfo().getStatus() != UpcomingGameStatus.PLAYING)
            return;
        if (!validateWorld(e.getPlayer().getWorld()))
            return;

        Optional<GamePlayer> gamePlayer = game.getGamePlayerOfPlayer(e.getPlayer().getUniqueId());
        if (gamePlayer.isEmpty())
            return;

        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void breakBlockCorrector(BlockBreakEvent e) {
        if (!validateWorld(e.getPlayer().getWorld()))
            return;
        if (e.isCancelled())
            return;
        if (blocks.containsKey(e.getBlock().getLocation())) {
            blocks.get(e.getBlock().getLocation()).remove();
            blocks.remove(e.getBlock().getLocation());
        }
    }

    @EventHandler
    public void place(BlockPlaceEvent e) {
        if (game.getGameInfo().getStatus() != UpcomingGameStatus.PLAYING)
            return;
        if (!validateWorld(e.getPlayer().getWorld()))
            return;

        Optional<GamePlayer> gamePlayer = game.getGamePlayerOfPlayer(e.getPlayer().getUniqueId());
        if (gamePlayer.isEmpty())
            return;

        Location location = e.getBlock().getLocation().clone().add(0, -1, 0);
        Material type = location.getBlock().getType();
        Material t = e.getBlock().getType();
        if (type == Material.END_STONE) {
            if (t != Material.GOLD_BLOCK && t != Material.DIAMOND_BLOCK) {
                e.setCancelled(true);
                return;
            }
            if (t != gamePlayer.get().getMapPlayer().getRole().getType()) {
                e.setCancelled(true);
                return;
            }
            List<UUID> list = new ArrayList<>();
            list.addAll(game.getSpectators());
            list.addAll(game.getHosts());
            game.getColors().forEach(c -> {
                if (c.getPlayer().isPresent())
                    list.add(c.getPlayer().get());
            });

            Role role = gamePlayer.get().getMapPlayer().getRole();
            list.forEach(uuid -> {
                Player p = Bukkit.getPlayer(uuid);
                if (p != null) {
                    TTA_Methods.sendTitle(p, role.getTitle(), 10, 100, 10, role.getSubTitle(), 20, 90, 10);
                }
            });
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                game.setGameStatus(UpcomingGameStatus.FINISHED, Optional.of(role));
            }, 20L * 10L);
            return;
        }
        if(e.getItemInHand().getItemMeta() != null) {
            String data = e.getItemInHand().getItemMeta().getPersistentDataContainer().get(new NamespacedKey(plugin, ItemTag.PLACEABLE.getPersistentDataTag()), PersistentDataType.STRING);
            if(data != null && data.equalsIgnoreCase("false")) {
                e.setCancelled(true);
                return;
            }
        }

        if (e.getBlock().getType() == Material.EMERALD_BLOCK || e.getBlock().getType() == Material.DIAMOND_BLOCK || e.getBlock().getType() == Material.GOLD_BLOCK) {
            Location loc = e.getBlock().getLocation().clone().add(0.5, -0.1, 0.5);
            AtomicLong timer = new AtomicLong(5 * 60 * 1000);
            if (e.getBlock().getType() == Material.DIAMOND_BLOCK || e.getBlock().getType() == Material.GOLD_BLOCK) {
                timer.set(60 * 1000);
            }
            AtomicLong currentMillis = new AtomicLong(System.currentTimeMillis());
            ArmorStand ar = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);


            ar.setCustomNameVisible(true);
            ar.setSmall(true);
            ar.setArms(false);
            ar.setBasePlate(false);
            ar.setInvisible(true);
            ar.setInvulnerable(true);
            ar.setGravity(false);
            ar.customName(Component.text("removeableBlock"));
            ar.setCollidable(false);
            AtomicInteger taskID = new AtomicInteger(Integer.MAX_VALUE);
            blocks.put(loc, ar);
            taskID.set(Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                if (!blocks.containsKey(loc)) {
                    Bukkit.getScheduler().cancelTask(taskID.get());
                }
                long now = System.currentTimeMillis();
                long delta = now - currentMillis.get();
                timer.addAndGet(-delta);
                ar.customName(Component.text(ChatColor.AQUA + Functions.formatGameTime(timer.get())));
                if (timer.get() <= 0) {
                    e.getBlock().setType(Material.AIR);
                    ar.remove();
                    Bukkit.getScheduler().cancelTask(taskID.get());
                }
                currentMillis.set(now);
            }, 0L, 10L).getTaskId());
        }


    }

    @EventHandler
    public void interactSettings(PlayerInteractEvent e) {
        if (game.getGameInfo().getStatus() != UpcomingGameStatus.PLAYING)
            return;
        if (!validateWorld(e.getPlayer().getWorld()))
            return;

        Optional<GamePlayer> gamePlayer = game.getGamePlayerOfPlayer(e.getPlayer().getUniqueId());
        if (gamePlayer.isEmpty())
            return;
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK && e.getAction() != Action.PHYSICAL)
            return;
        Material type = e.getClickedBlock().getType();

        if (!game.getInteractionManager().isInteractionWithTypeAllowed(type)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void damageEntity(EntityDamageByEntityEvent e) {
        if (game.getGameInfo().getStatus() != UpcomingGameStatus.PLAYING)
            return;
        if (!validateWorld(e.getEntity().getWorld()))
            return;
        Material type = null;
        if(e.getEntity() instanceof ItemFrame) {
            type = Material.ITEM_FRAME;
        }
        if(e.getEntity() instanceof GlowItemFrame) {
            type = Material.GLOW_ITEM_FRAME;
        }
        if(type != null) {
            if(game.getGamePlayerOfPlayer(e.getDamager().getUniqueId()).isPresent())
                if (!game.getInteractionManager().isInteractionWithTypeAllowed(type)) {
                    e.setCancelled(true);
                }
        }

    }

    @EventHandler
    public void paintingBreak(HangingBreakByEntityEvent e) {
        if (game.getGameInfo().getStatus() != UpcomingGameStatus.PLAYING)
            return;
        if (!validateWorld(e.getEntity().getWorld()))
            return;
        if(game.getGamePlayerOfPlayer(e.getRemover().getUniqueId()).isPresent())
            if(e.getEntity() instanceof ItemFrame || e.getEntity() instanceof GlowItemFrame) {
                e.setCancelled(true);
            }
    }

    @EventHandler
    public void paintingBreak(PlayerInteractEntityEvent e) {
        if (game.getGameInfo().getStatus() != UpcomingGameStatus.PLAYING)
            return;
        if (!validateWorld(e.getRightClicked().getWorld()))
            return;
        Material type = null;
        if(e.getRightClicked() instanceof ItemFrame) {
            type = Material.ITEM_FRAME;
        }
        if(e.getRightClicked() instanceof GlowItemFrame) {
            type = Material.GLOW_ITEM_FRAME;
        }
        if(type != null) {
            if(game.getGamePlayerOfPlayer(e.getRightClicked().getUniqueId()).isPresent())
                if (!game.getInteractionManager().isInteractionWithTypeAllowed(type)) {
                    e.setCancelled(true);
                }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void chat(AsyncChatEvent e) {
        Player p = e.getPlayer();
        if(e.isCancelled())
            return;
        if (!validateWorld(p.getWorld()))
            return;
        Optional<GamePlayer> optionalGamePlayer = game.getGamePlayerOfPlayer(p.getUniqueId());
        if(optionalGamePlayer.isEmpty())
            return;
        if(!optionalGamePlayer.get().isAlive())
            return;

        e.setCancelled(true);
        GamePlayer gp = optionalGamePlayer.get();
        game.sendMessageToAll(LanguageManager.getInstance().getLanguageString(LanguageText.GAME_CHAT_PLAYER, Arrays.asList(gp.getMapPlayer().getColor().getDisplayName(), p.getName(), Functions.convertComponentToString(e.message()))));
    }

    @EventHandler
    public void chatHost(AsyncChatEvent e) {
        Player p = e.getPlayer();
        if (!validateWorld(p.getWorld()))
            return;
        if(!game.getHosts().contains(p.getUniqueId()))
            return;

        e.setCancelled(true);
        game.sendMessageToAll(LanguageManager.getInstance().getLanguageString(LanguageText.GAME_CHAT_HOST, Arrays.asList(p.getName(), Functions.convertComponentToString(e.message()))));
    }

    @EventHandler
    public void playerDeadEvent(PlayerDeathEvent e) {
        Player p = e.getPlayer();
        if (!validateWorld(e.getPlayer().getWorld()))
            return;
        Optional<GamePlayer> optionalGamePlayer = game.getGamePlayerOfPlayer(p.getUniqueId());
        if(optionalGamePlayer.isEmpty())
            return;
        ArrayList<ItemStack> drops = new ArrayList<>(e.getDrops());
        for (ItemStack item : drops) {
            if (item.getItemMeta() == null)
                continue;
            String data = item.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(plugin, ItemTag.VANISHABLE.getPersistentDataTag()), PersistentDataType.STRING);
            if (data != null && data.equalsIgnoreCase("false"))
                e.getDrops().remove(item);
        }
        if(p.getKiller() == null) return;
        Game game = optionalGamePlayer.get().getGame();
        MapPlayer killer = getMapPlayer(p.getKiller(), game);
        MapPlayer player = getMapPlayer(p, game);

        if(killer == null || player == null) return;
        if(killer.getRole() != Role.SHAPESHIFTER) return;
        killer.setRole(player.getRole());
    }

    private MapPlayer getMapPlayer(Player player, Game game) {
        Optional<GamePlayer> gamePlayer = game.getColors().stream().filter(gPlayer -> gPlayer.getPlayer().isPresent() && gPlayer.getPlayer().get() == player.getUniqueId()).findFirst();
        return gamePlayer.map(GamePlayer::getMapPlayer).orElse(null);
    }
}
