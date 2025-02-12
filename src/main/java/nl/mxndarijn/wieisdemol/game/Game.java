package nl.mxndarijn.wieisdemol.game;

import de.Herbystar.TTA.TTA_Methods;
import nl.mxndarijn.api.changeworld.ChangeWorldManager;
import nl.mxndarijn.api.changeworld.MxChangeWorld;
import nl.mxndarijn.api.mxscoreboard.MxSupplierScoreBoard;
import nl.mxndarijn.api.mxworld.MxAtlas;
import nl.mxndarijn.api.mxworld.MxWorld;
import nl.mxndarijn.api.util.Functions;
import nl.mxndarijn.wieisdemol.WieIsDeMol;
import nl.mxndarijn.wieisdemol.data.ChatPrefix;
import nl.mxndarijn.wieisdemol.data.Role;
import nl.mxndarijn.wieisdemol.data.ScoreBoard;
import nl.mxndarijn.wieisdemol.data.SpecialDirectories;
import nl.mxndarijn.wieisdemol.game.events.GameEvent;
import nl.mxndarijn.wieisdemol.game.events.*;
import nl.mxndarijn.wieisdemol.items.Items;
import nl.mxndarijn.wieisdemol.managers.*;
import nl.mxndarijn.wieisdemol.managers.chests.ChestManager;
import nl.mxndarijn.wieisdemol.managers.database.DatabaseManager;
import nl.mxndarijn.wieisdemol.managers.database.PlayerData;
import nl.mxndarijn.wieisdemol.managers.doors.DoorManager;
import nl.mxndarijn.wieisdemol.managers.language.LanguageManager;
import nl.mxndarijn.wieisdemol.managers.language.LanguageText;
import nl.mxndarijn.wieisdemol.managers.shulkers.ShulkerManager;
import nl.mxndarijn.wieisdemol.managers.warps.WarpManager;
import nl.mxndarijn.wieisdemol.managers.world.GameWorldManager;
import nl.mxndarijn.wieisdemol.map.Map;
import nl.mxndarijn.wieisdemol.map.MapConfig;
import nl.mxndarijn.wieisdemol.map.mapplayer.MapPlayer;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Game {

    private final GameInfo gameInfo;
    private final UUID mainHost;
    private final WarpManager warpManager;
    private final ChestManager chestManager;
    private final ShulkerManager shulkerManager;
    private final DoorManager doorManager;
    private final InteractionManager interactionManager;
    private final File directory;
    private final ArrayList<UUID> hosts;
    private final MapConfig config;
    private final List<GamePlayer> colors;
    private final MxSupplierScoreBoard hostScoreboard;
    private final MxSupplierScoreBoard spectatorScoreboard;
    private final List<UUID> spectators;
    private final HashMap<UUID, Location> respawnLocations;
    private final JavaPlugin plugin;
    private Optional<MxWorld> mxWorld;
    private boolean firstStart = false;
    private long gameTime = 0;
    private int peacekeeperKills;
    private boolean playersCanEndVote = true;
    private List<GameEvent> events;
    private BukkitTask chestAttachmentUpdater;
    private BukkitTask updateGameUpdater;


    public Game(UUID mainHost, GameInfo gameInfo, MapConfig mapConfig, MxWorld mxWorld) {
        this.gameInfo = gameInfo;
        this.mainHost = mainHost;
        this.mxWorld = Optional.of(mxWorld);
        this.config = mapConfig;
        this.directory = mxWorld.getDir();

        this.hosts = new ArrayList<>();
        this.respawnLocations = new HashMap<>();

        this.warpManager = new WarpManager(new File(getDirectory(), "warps.yml"));
        this.chestManager = new ChestManager(new File(getDirectory(), "chests.yml"));
        this.shulkerManager = new ShulkerManager(new File(getDirectory(), "shulkers.yml"));
        this.doorManager = new DoorManager(new File(getDirectory(), "doors.yml"));
        this.interactionManager = new InteractionManager(new File(getDirectory(), "interactions.yml"));

        this.plugin = JavaPlugin.getPlugin(WieIsDeMol.class);
        this.peacekeeperKills = mapConfig.getPeacekeeperKills();
        this.spectators = new ArrayList<>();
        this.colors = new ArrayList<>();
        mapConfig.getColors().forEach(mapPlayer -> {
            colors.add(new GamePlayer(mapPlayer, plugin, this));
        });

        this.hostScoreboard = new MxSupplierScoreBoard(plugin, () -> {
            return ScoreBoard.GAME_HOST.getTitle(new HashMap<>() {{
                put("%%map_name%%", mapConfig.getPresetConfig().getName());
            }});
        }, () -> {

            AtomicInteger alivePlayers = new AtomicInteger();
            AtomicInteger deadPlayers = new AtomicInteger();
            colors.forEach(g -> {
                if (g.isAlive())
                    alivePlayers.getAndIncrement();
                else
                    deadPlayers.getAndIncrement();
            });
            return ScoreBoard.GAME_HOST.getLines(new HashMap<>() {{
                put("%%map_name%%", mapConfig.getPresetConfig().getName());
                put("%%game_status%%", gameInfo.getStatus().getStatus());
                put("%%game_time%%", formatGameTime(gameTime));
                put("%%players_alive%%", colors.size() + "");
                put("%%mollen_alive%%", "0");
                put("%%ego_alive%%", "0");
                put("%%alive_players%%", alivePlayers.get() + "");
                put("%%dead_players%%", deadPlayers.get() + "");
                put("%%spectator_count%%", "0");

            }});
        });
        this.hostScoreboard.setUpdateTimer(10);

        this.spectatorScoreboard = new MxSupplierScoreBoard(plugin, () -> {
            return ScoreBoard.GAME_SPECTATOR.getTitle(new HashMap<>() {{
                put("%%map_name%%", mapConfig.getPresetConfig().getName());
            }});
        }, () -> {
            String host = Bukkit.getOfflinePlayer(getMainHost()).getName();
            return ScoreBoard.GAME_SPECTATOR.getLines(new HashMap<>() {{
                put("%%map_name%%", mapConfig.getPresetConfig().getName());
                put("%%game_status%%", gameInfo.getStatus().getStatus());
                put("%%game_time%%", formatGameTime(gameTime));
                put("%%players_alive%%", colors.size() + "");
                put("%%mollen_alive%%", "0");
                put("%%ego_alive%%", "0");
                put("%%spectator_count%%", "0");
                put("%%host%%", host);

            }});
        });
        loadWorld().thenAccept(loaded -> {
            if (!loaded) {
                stopGame();
            }
        });
        this.spectatorScoreboard.setUpdateTimer(10);

        GameWorldManager.getInstance().addGame(this);
    }

    public static Optional<Game> createGameFromGameInfo(UUID mainHost, GameInfo gameInfo) {
        Optional<Map> map = MapManager.getInstance().getMapById(gameInfo.getMapId());
        if (map.isEmpty() || map.get().getMxWorld().isEmpty()) {
            return Optional.empty();
        }

        File newDir = new File(SpecialDirectories.GAMES_WORLDS.getDirectory() + "");
        Optional<MxWorld> optionalWorld = MxAtlas.getInstance().duplicateMxWorld(map.get().getMxWorld().get(), newDir);

        if (optionalWorld.isEmpty()) {
            return Optional.empty();
        }

        Game g = new Game(mainHost, gameInfo, map.get().getMapConfig(), optionalWorld.get());

        return Optional.of(g);
    }

    public String getGameTime() {
        return formatGameTime(gameTime);
    }

    private String formatGameTime(long timeInMillis) {
        // Converteer milliseconden naar seconden
        long timeInSeconds = timeInMillis / 1000;

        // Bereken het aantal minuten en seconden van de gegeven tijd
        long minutes = timeInSeconds / 60;
        long seconds = timeInSeconds % 60;

        // Gebruik String.format om de tijd in het juiste formaat weer te geven
        return String.format("%02d:%02d", minutes, seconds);
    }

    private File getDirectory() {
        return directory;
    }

    public CompletableFuture<Boolean> loadWorld() {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        if (this.mxWorld.isEmpty()) {
            future.complete(false);
            return future;
        }
        if (this.mxWorld.get().isLoaded()) {
            future.complete(true);
            return future;
        }
        MxAtlas.getInstance().loadMxWorld(this.mxWorld.get()).thenAccept(loaded -> {
            future.complete(loaded);
            if (loaded) {
                registerEvents();
                updateChestAttachments();
                updateGame();
                ChangeWorldManager.getInstance().addWorld(this.mxWorld.get().getWorldUID(), new MxChangeWorld() {
                    @Override
                    public void enter(Player p, World w, PlayerChangedWorldEvent e) {

                    }

                    @Override
                    public void leave(Player p, World w, PlayerChangedWorldEvent e) {
                        hosts.remove(p.getUniqueId());
                        removePlayer(p.getUniqueId());
                        ScoreBoardManager.getInstance().removePlayerScoreboard(p.getUniqueId(), hostScoreboard);

                        if (hosts.isEmpty()) {
                            setGameStatus(UpcomingGameStatus.FINISHED, Optional.empty());
                        }

                    }
                });
            }
        });
        return future;
    }

    public void registerEvents() {
        unregisterEvents();
        events = new ArrayList<>(Arrays.asList(
                new GamePreStartEvents(this, plugin),
                new GameFreezeEvents(this, plugin),
                new GamePlayingEvents(this, plugin),
                new GameSpectatorEvents(this, plugin),
                new GamePlayingPeacekeeperEvents(this, plugin),
                new GameDefaultEvents(this, plugin),
                new GameColorBindEvents(this, plugin)
        ));
    }

    public void unregisterEvents() {
        if (events == null)
            return;
        events.forEach(HandlerList::unregisterAll);
    }

    public int getTotalVotes() {
        AtomicInteger i = new AtomicInteger();
        colors.forEach(gp -> {
            if(gp.getVotedOn().isPresent())
                i.getAndIncrement();
        });

        return i.get();
    }

    public CompletableFuture<Boolean> unloadWorld() {
        if(this.mxWorld.isPresent() && Bukkit.getWorld(this.mxWorld.get().getWorldUID()) != null) {
            World w = Bukkit.getWorld(this.mxWorld.get().getWorldUID());
            w.getPlayers().forEach(p -> {
                p.teleport(Functions.getSpawnLocation());
            });
        }
        unregisterEvents();

        CompletableFuture<Boolean> future = new CompletableFuture<>();
        Bukkit.getScheduler().scheduleSyncDelayedTask(JavaPlugin.getPlugin(WieIsDeMol.class), () -> {
            if (this.mxWorld.isEmpty()) {
                future.complete(true);
                return;
            }
            MxAtlas.getInstance().unloadMxWorld(this.mxWorld.get(), true);
            future.complete(true);
        });
        return future;
    }

    public void addHost(UUID uuid) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                hosts.add(uuid);
                gameInfo.getQueue().remove(uuid);
                if (this.mxWorld.isEmpty() || !this.mxWorld.get().isLoaded()) {
                    AtomicInteger i = new AtomicInteger();
                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                        if (this.mxWorld.isPresent() && this.mxWorld.get().isLoaded()) {
                            addHostItems(p);
                        } else {
                            if (i.get() < 100) {
                                i.getAndIncrement();
                                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                                    if (this.mxWorld.isPresent() && this.mxWorld.get().isLoaded()) {
                                        addHostItems(p);
                                    } else {
                                        if (i.get() < 100) {
                                            i.getAndIncrement();
                                        }
                                    }
                                }, 5L);
                            }
                        }
                    }, 10L);
                } else {
                    addHostItems(p);
                }
            }
        });
    }

    public void addHostItems(Player p) {
        if (this.mxWorld.isEmpty())
            return;
        World w = Bukkit.getWorld(mxWorld.get().getWorldUID());
        p.teleport(w.getSpawnLocation());
        ScoreBoardManager.getInstance().setPlayerScoreboard(p.getUniqueId(), hostScoreboard);
        p.setGameMode(GameMode.CREATIVE);
        p.sendMessage(ChatPrefix.WIDM + LanguageManager.getInstance().getLanguageString(LanguageText.GAME_YOU_ARE_NOW_HOST));
        p.getInventory().clear();
        p.getInventory().addItem(Items.PLAYER_MANAGEMENT_ITEM.getItemStack());
        p.getInventory().addItem(Items.HOST_TOOL.getItemStack());
        p.getInventory().addItem(Items.GAME_CHEST_TOOL.getItemStack());
        p.getInventory().addItem(Items.GAME_SHULKER_TOOL.getItemStack());
        p.getInventory().addItem(Items.GAME_DOOR_ITEM.getItemStack());
        p.getInventory().addItem(Items.VANISH_ITEM.getItemStack());
    }

    public GameInfo getGameInfo() {
        return gameInfo;
    }

    public UUID getMainHost() {
        return mainHost;
    }

    public Optional<MxWorld> getMxWorld() {
        return mxWorld;
    }

    public WarpManager getWarpManager() {
        return warpManager;
    }

    public ChestManager getChestManager() {
        return chestManager;
    }

    public ShulkerManager getShulkerManager() {
        return shulkerManager;
    }

    public DoorManager getDoorManager() {
        return doorManager;
    }

    public InteractionManager getInteractionManager() {
        return interactionManager;
    }

    public ArrayList<UUID> getHosts() {
        return hosts;
    }

    public MapConfig getConfig() {
        return config;
    }

    public MxSupplierScoreBoard getHostScoreboard() {
        return hostScoreboard;
    }

    public JavaPlugin getPlugin() {
        return plugin;
    }

    public List<GamePlayer> getColors() {
        return colors;
    }

    public boolean addPlayer(UUID playerUUID, GamePlayer gamePlayer) {
        //TODO Change Inventory
        Player p = Bukkit.getPlayer(playerUUID);
        if (p == null || mxWorld.isEmpty())
            return false;

        if (gamePlayer.getPlayer().isPresent()) {
            removePlayer(gamePlayer.getPlayer().get());
        }

        gamePlayer.setPlayingPlayer(playerUUID);
        gameInfo.getQueue().remove(playerUUID);
        MapPlayer mp = gamePlayer.getMapPlayer();
        p.getInventory().clear();
        p.teleport(mp.getLocation().getLocation(Bukkit.getWorld(mxWorld.get().getWorldUID())));
        p.setHealth(20);
        p.setFoodLevel(20);
        p.setExp(0);
        p.getInventory().addItem(Items.GAME_PLAYER_TOOL.getItemStack());
        p.setGameMode(GameMode.SURVIVAL);
        sendMessageToAll(LanguageManager.getInstance().getLanguageString(LanguageText.GAME_PLAYER_JOINED, new ArrayList<>(Arrays.asList(p.getName(), mp.getColor().getDisplayName()))));
        //Add Scoreboard

        return true;
    }

    public Optional<GamePlayer> getGamePlayerOfPlayer(UUID uuid) {
        for (GamePlayer color : colors) {
            if (color.getPlayer().isPresent() && color.getPlayer().get().equals(uuid)) {
                return Optional.of(color);
            }
        }
        return Optional.empty();
    }

    public boolean removePlayer(UUID playerUUID) {
        //TODO replace player
        Optional<GamePlayer> player = getGamePlayerOfPlayer(playerUUID);
        if (player.isEmpty())
            return false;

        GamePlayer gamePlayer = player.get();
        gamePlayer.setPlayingPlayer(null);

        Player p = Bukkit.getPlayer(playerUUID);
        if (p != null) {
            p.teleport(Functions.getSpawnLocation());
            sendMessageToAll(LanguageManager.getInstance().getLanguageString(LanguageText.GAME_PLAYER_LEAVED, new ArrayList<>(Arrays.asList(p.getName(), gamePlayer.getMapPlayer().getColor().getDisplayName()))));
        }
        sendMessageToAll(LanguageManager.getInstance().getLanguageString(LanguageText.GAME_PLAYER_LEAVED, new ArrayList<>(Arrays.asList(Bukkit.getOfflinePlayer(playerUUID).getName(), gamePlayer.getMapPlayer().getColor().getDisplayName()))));
        //Add Scoreboard

        return true;
    }

    public void sendMessageToAll(String message) {
        sendMessageToHosts(message);
        sendMessageToPlayers(message);
        sendMessageToSpectators(message);

    }

    public void sendMessageToHosts(String message) {
        hosts.forEach(host -> {
            Player p = Bukkit.getPlayer(host);
            if (p != null) {
                p.sendMessage(message);
            }
        });
    }

    public void sendMessageToSpectators(String message) {
        spectators.forEach(host -> {
            Player p = Bukkit.getPlayer(host);
            if (p != null) {
                p.sendMessage(message);
            }
        });
    }

    public void sendMessageToPlayers(String message) {
        colors.forEach(color -> {
            if (color.getPlayer().isPresent()) {
                Player p = Bukkit.getPlayer(color.getPlayer().get());
                if (p != null) {
                    p.sendMessage(message);
                }
            }
        });
    }

    public void setGameStatus(UpcomingGameStatus upcomingGameStatus, Optional<Role> role) {
        getGameInfo().setStatus(upcomingGameStatus);
        if (upcomingGameStatus == UpcomingGameStatus.PLAYING && !firstStart) {
            firstStart = true;
            chestManager.onGameStart(this);
            gameInfo.getQueue().clear();
        }
        if (upcomingGameStatus == UpcomingGameStatus.FINISHED) {
            role.ifPresent(rol -> {
                sendMessageToAll(ChatPrefix.WIDM + "Rollen:");
                colors.forEach(color -> {
                    if(color.getPlayer().isPresent()) {
                        sendMessageToAll(ChatColor.GRAY + " - " + Bukkit.getOfflinePlayer(color.getPlayer().get()).getName() + " " + color.getMapPlayer().getColor().getDisplayName() + " " + color.getMapPlayer().getRoleDisplayString());
                    } else {
                        sendMessageToAll(ChatColor.GRAY + " - " + "Niemand " + color.getMapPlayer().getColor().getDisplayName() + " " + color.getMapPlayer().getRoleDisplayString());
                    }
                });
                List<UUID> list = new ArrayList<>();
                list.addAll(getSpectators());
                list.addAll(getHosts());
                getColors().forEach(c -> {
                    if (c.getPlayer().isPresent())
                        list.add(c.getPlayer().get());
                });

                list.forEach(uuid -> {
                    Player p = Bukkit.getPlayer(uuid);
                    if (p != null) {
                        TTA_Methods.sendTitle(p, rol.getTitle(), 10, 100, 10, rol.getSubTitle(), 20, 90, 10);
                    }
                });
                colors.forEach(color -> {

                    if (color.getPlayer().isPresent()) {
                        UUID uuid = color.getPlayer().get();
                        PlayerData pd = DatabaseManager.getInstance().getPlayerData(uuid);
                        if (color.getMapPlayer().getRole() == rol)  {
                            pd.updateData(new HashMap<>() {{
                                put(rol.getWinType(), pd.getData(rol.getWinType())+ 1);
                                put(PlayerData.UserDataType.GAMESPLAYED, pd.getData(PlayerData.UserDataType.GAMESPLAYED)+ 1);
                            }});

                        } else {
                            pd.updateData(new HashMap<>() {{
                                put(PlayerData.UserDataType.GAMESPLAYED, pd.getData(PlayerData.UserDataType.GAMESPLAYED)+ 1);
                            }});
                        }
                    }
                });
            });
            stopGame();
        }
        sendMessageToAll(LanguageManager.getInstance().getLanguageString(LanguageText.GAME_STATUS_CHANGED, Collections.singletonList(upcomingGameStatus.getStatus())));
    }

    public void updateChestAttachments() {
        if (chestAttachmentUpdater != null)
            return;
        AtomicLong lastUpdateTime = new AtomicLong(System.currentTimeMillis());
        chestAttachmentUpdater = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (this.mxWorld.isEmpty())
                chestAttachmentUpdater.cancel();
            if (gameInfo.getStatus() != UpcomingGameStatus.PLAYING)
                return;
            long l = System.currentTimeMillis();
            chestManager.getChests().forEach(chestInformation -> {
                chestInformation.getChestAttachmentList().forEach(chestAttachment -> {
                    chestAttachment.onGameUpdate(l - lastUpdateTime.get());
                });
            });
            lastUpdateTime.set(l);
        }, 0L, 20L);
    }

    public void updateGame() {
        if (updateGameUpdater != null)
            return;
        final AtomicLong[] lastUpdateTime = {null};
        updateGameUpdater = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (this.mxWorld.isEmpty())
                updateGameUpdater.cancel();
            if (lastUpdateTime[0] != null && gameInfo.getStatus() == UpcomingGameStatus.FREEZE)
                lastUpdateTime[0].set(System.currentTimeMillis());
            if (gameInfo.getStatus() != UpcomingGameStatus.PLAYING)
                return;
            if (lastUpdateTime[0] == null)
                lastUpdateTime[0] = new AtomicLong(System.currentTimeMillis());
            long l = System.currentTimeMillis();
            long delta = l - lastUpdateTime[0].get();
            gameTime += delta;

            lastUpdateTime[0].set(l);
        }, 0L, 10L);
    }

    public void stopGame() {
        sendMessageToAll(LanguageManager.getInstance().getLanguageString(LanguageText.GAME_GAME_STOPPED));
        this.mxWorld.ifPresent(world -> ChangeWorldManager.getInstance().removeWorld(world.getWorldUID()));

        hosts.forEach(u -> {
            Player p = Bukkit.getPlayer(u);
            if (p != null) {
                ScoreBoardManager.getInstance().removePlayerScoreboard(p.getUniqueId(), hostScoreboard);
                VanishManager.getInstance().showPlayerForAll(p);
                p.setHealth(20);
                p.getActivePotionEffects().clear();
                p.setFoodLevel(20);
            }
        });
        spectators.forEach(u -> {
            Player p = Bukkit.getPlayer(u);
            if (p != null) {
                VanishManager.getInstance().showPlayerForAll(p);
                ScoreBoardManager.getInstance().removePlayerScoreboard(u, spectatorScoreboard);
                p.setHealth(20);
                p.getActivePotionEffects().clear();
                p.setFoodLevel(20);
            }
        });
        colors.forEach(g -> {
            if (g.getPlayer().isPresent()) {
                Player p = Bukkit.getPlayer(g.getPlayer().get());
                if (p != null) {
                    ScoreBoardManager.getInstance().removePlayerScoreboard(p.getUniqueId(), g.getScoreboard());
                    VanishManager.getInstance().showPlayerForAll(p);
                    p.setHealth(20);
                    p.getActivePotionEffects().clear();
                    p.setFoodLevel(20);
                }
            }
        });
        World w = Bukkit.getWorld(this.mxWorld.get().getWorldUID());

        unloadWorld().thenAccept(unloaded -> {
            if(unloaded) this.mxWorld = Optional.empty();
            GameManager.getInstance().removeUpcomingGame(gameInfo);
            GameWorldManager.getInstance().removeGame(this);
        });
    }

    public int getPlayerCount() {
        AtomicInteger i = new AtomicInteger();
        colors.forEach(c -> {
            if (c.getPlayer().isPresent())
                i.getAndIncrement();
        });

        return i.get();
    }

    public void addSpectator(UUID uuid) {
        spectators.add(uuid);
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            player.getInventory().clear();
            ScoreBoardManager.getInstance().setPlayerScoreboard(uuid, spectatorScoreboard);
            player.sendMessage(LanguageManager.getInstance().getLanguageString(LanguageText.GAME_SPECTATOR_JOIN));
            sendMessageToHosts(LanguageManager.getInstance().getLanguageString(LanguageText.GAME_SPECTATOR_JOINED, Collections.singletonList(player.getName())));
        }
        addSpectatorSettings(uuid);
    }


    public void addSpectatorSettings(UUID uuid) {
        if (mxWorld.isEmpty())
            return;
        addSpectatorSettings(uuid, Bukkit.getWorld(mxWorld.get().getWorldUID()).getSpawnLocation());
    }

    public void addSpectatorSettings(UUID uuid, Location loc) {
        Player p = Bukkit.getPlayer(uuid);
        if (p != null) {
            if (!p.isDead()) {
                p.teleport(loc);
            } else {
                respawnLocations.put(uuid, loc);
            }
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                p.getInventory().clear();
                p.getInventory().setItem(0, Items.GAME_SPECTATOR_TELEPORT_ITEM.getItemStack());
                if (spectators.contains(p.getUniqueId())) {
                    p.getInventory().setItem(8, Items.GAME_SPECTATOR_LEAVE_ITEM.getItemStack());
                }
                VanishManager.getInstance().hidePlayerForAll(p);
            }, 40L);
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                VanishManager.getInstance().hidePlayerForAll(p);
            }, 80L);
            p.setHealth(20);
            p.setFoodLevel(20);
            p.setAllowFlight(true);
            VanishManager.getInstance().hidePlayerForAll(p);
        }
    }


    public boolean isFirstStart() {
        return firstStart;
    }

    public int getPeacekeeperKills() {
        return peacekeeperKills;
    }

    public void setPeacekeeperKills(int peacekeeperKills) {
        this.peacekeeperKills = peacekeeperKills;
    }

    public List<GameEvent> getEvents() {
        return events;
    }

    public BukkitTask getChestAttachmentUpdater() {
        return chestAttachmentUpdater;
    }

    public BukkitTask getUpdateGameUpdater() {
        return updateGameUpdater;
    }

    public List<UUID> getSpectators() {
        return spectators;
    }

    public HashMap<UUID, Location> getRespawnLocations() {
        return respawnLocations;
    }

    public void removeSpectator(UUID uniqueId, boolean teleport) {
        spectators.remove(uniqueId);
        Player p = Bukkit.getPlayer(uniqueId);
        if (p != null) {
            VanishManager.getInstance().showPlayerForAll(p);
            ScoreBoardManager.getInstance().removePlayerScoreboard(uniqueId, spectatorScoreboard);
            p.setHealth(20);
            p.getActivePotionEffects().clear();
            p.setFoodLevel(20);
            p.setAllowFlight(false);
            p.getInventory().clear();
        }
        if (teleport)
            p.teleport(Functions.getSpawnLocation());
    }

    public void removeSpectator(UUID uniqueId) {
        removeSpectator(uniqueId, true);
    }

    public void showVotingResults(String name) {
        HashMap<GamePlayer, Integer> votes = new HashMap<>();
        colors.forEach(gp -> {
            votes.put(gp, 0);
        });


        colors.forEach(gp -> {
            if (gp.getPlayer().isPresent()) {
                if (gp.getVotedOn().isPresent()) {
                    votes.put(gp.getVotedOn().get(), 1 + votes.get(gp.getVotedOn().get()));
                }
            }
        });

        votes.entrySet().removeIf(entry -> entry.getValue() == 0);

        List<GamePlayer> playerList = new ArrayList<>(votes.keySet());

        // Sorteer de lijst op basis van het aantal stemmen (van meest gestemd naar minst gestemd)
        playerList.sort(Comparator.comparingInt(votes::get).reversed());

        sendMessageToAll(LanguageManager.getInstance().getLanguageString(LanguageText.GAME_VOTES, Collections.singletonList(name)));
        if (playerList.isEmpty()) {
            sendMessageToAll(ChatColor.RED + "Geen stemmen.");
        }
        playerList.forEach(p -> {
            if (p.getPlayer().isEmpty())
                return;
            OfflinePlayer player = Bukkit.getOfflinePlayer(p.getPlayer().get());
            sendMessageToAll(LanguageManager.getInstance().getLanguageString(LanguageText.GAME_VOTES_SUBJECT, Arrays.asList(player.getName(), p.getMapPlayer().getColor().getDisplayName(), votes.get(p) + "")));
        });

        hosts.forEach(host -> {
            Player p = Bukkit.getPlayer(host);
            if (p != null) {
                for(GamePlayer gPlayer : playerList) {
                    if(gPlayer.getPlayer().isEmpty()) continue;
                    if(!gPlayer.isAlive()) continue;
                    OfflinePlayer player = Bukkit.getOfflinePlayer(gPlayer.getPlayer().get());
                    if(gPlayer.getVotedOn().isEmpty()) continue;
                    Optional<UUID> targetUUID = gPlayer.getVotedOn().get().getPlayer();
                    if(targetUUID.isEmpty()) continue;
                    OfflinePlayer target = Bukkit.getOfflinePlayer(targetUUID.get());
                    p.sendMessage(player.getName() + ": " + target.getName());
                }
            }
        });

        clearVotingResults();


    }

    public void clearVotingResults() {
        colors.forEach(gamePlayer -> {
            gamePlayer.setVotedOn(Optional.empty());
        });
    }

    public boolean isPlayersCanEndVote() {
        return playersCanEndVote;
    }

    public void setPlayersCanEndVote(boolean playersCanEndVote) {
        this.playersCanEndVote = playersCanEndVote;
    }
}
