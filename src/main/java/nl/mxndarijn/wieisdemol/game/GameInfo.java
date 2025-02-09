package nl.mxndarijn.wieisdemol.game;

import nl.mxndarijn.api.item.MxSkullItemStackBuilder;
import nl.mxndarijn.wieisdemol.data.ConfigFiles;
import nl.mxndarijn.wieisdemol.data.Permissions;
import nl.mxndarijn.wieisdemol.managers.GameManager;
import nl.mxndarijn.wieisdemol.managers.MapManager;
import nl.mxndarijn.wieisdemol.map.Map;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class GameInfo {

    private final List<UUID> queue;
    private String mapId;
    private UUID host;
    private LocalDateTime time;
    private UpcomingGameStatus status;

    private GameInfo() {
        queue = new ArrayList<>();
        status = UpcomingGameStatus.WAITING;
    }

    public static GameInfo create(Map map, UUID host, LocalDateTime time) {
        GameInfo game = new GameInfo();
        game.host = host;
        game.mapId = map.getDirectory().getName();
        game.time = time;

        return game;
    }

    public static Optional<GameInfo> loadFromFile(java.util.Map<String, Object> map) {
        GameInfo game = new GameInfo();
        game.host = UUID.fromString((String) map.get("host"));
        game.mapId = (String) map.get("mapId");
        game.time = LocalDateTime.parse((String) map.get("time"));

        if (game.time.isBefore(LocalDateTime.now()))
            return Optional.empty();


        Optional<Map> optionalMap = MapManager.getInstance().getMapById(game.mapId);
        if (optionalMap.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(game);
    }

    public String getMapId() {
        return mapId;
    }

    public UUID getHost() {
        return host;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public java.util.Map<String, Object> getDataForSaving() {
        java.util.Map<String, Object> map = new HashMap<>();
        map.put("host", host.toString());
        map.put("mapId", mapId);
        map.put("time", time.toString());

        return map;
    }

    public ItemStack getItemStack(Player p) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        String formattedTime = time.format(formatter);
        Optional<Map> optionalMap = MapManager.getInstance().getMapById(mapId);
        if (optionalMap.isEmpty()) {
            GameManager.getInstance().removeUpcomingGame(this);
        }
        Map map = optionalMap.get();
        MxSkullItemStackBuilder builder = MxSkullItemStackBuilder.create(1)
                .setSkinFromHeadsData(map.getMapConfig().getPresetConfig().getSkullId())
                .setName(ChatColor.GRAY + map.getMapConfig().getName())
                .addBlankLore()
                .addLore(ChatColor.GRAY + "Status: " + status.getStatus())
                .addLore(ChatColor.GRAY + "Host: " + Bukkit.getOfflinePlayer(map.getMapConfig().getOwner()).getName())
                .addBlankLore()
                .addLore(ChatColor.GRAY + "Speel-Moeilijkheid: " + map.getStars(map.getMapConfig().getPresetConfig().getPlayDifficulty()));

        Duration duration = Duration.between(time, LocalDateTime.now());
        Long minutes = Math.abs(duration.toMinutes());
        if (status.isCanJoinQueue()) {
            if (minutes < ConfigFiles.MAIN_CONFIG.getFileConfiguration().getInt("time-before-queue-is-open-in-hours") * 60L) {
                if(time.isAfter(LocalDateTime.now())) {
                    builder.addBlankLore()
                            .addLore(ChatColor.GRAY + "Begint om: " + formattedTime + " (Over " + minutes + (minutes > 1 ? " minuten)" : " minuut)"))
                            .addLore(ChatColor.GRAY + "Aantal wachtend: " + queue.size());

                } else {
                    builder.addBlankLore()
                            .addLore(ChatColor.GRAY + "Begon om: " + formattedTime + ChatColor.RED + " (Tijd al geweest).")
                            .addLore(ChatColor.GRAY + "Aantal wachtend: " + queue.size());
                }

                if(queue.contains(p.getUniqueId())) {
                    builder.addLore(ChatColor.YELLOW + "Klik hier om uit de wachtrij te gaan.");
                } else {
                    builder.addLore(ChatColor.YELLOW + "Klik hier om in de wachtrij te komen.");
                }
                if (host == p.getUniqueId() || p.hasPermission(Permissions.ITEM_GAMES_MANAGE_OTHER_GAMES.getPermission())) {
                    builder.addLore(ChatColor.YELLOW + "Shift-Klik om de game te beheren.");
                }
            } else {
                builder.addBlankLore();
                DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("EEEE HH:mm", new Locale("nl", "NL"));
                String formattedTime1 = time.format(formatter1);
                builder.addLore(ChatColor.GRAY + "Begint om: " + formattedTime1);
            }
        } else {
            builder.addLore(ChatColor.YELLOW + "Klik hier om de game te spectaten.");
        }

        return builder.build();

    }

    public List<UUID> getQueue() {
        return queue;
    }

    public UpcomingGameStatus getStatus() {
        return status;
    }

    public void setStatus(UpcomingGameStatus status) {
        this.status = status;
    }
}
