package nl.mxndarijn.wieisdemol.map;

import nl.mxndarijn.api.logger.LogLevel;
import nl.mxndarijn.api.logger.Logger;
import nl.mxndarijn.api.logger.Prefix;
import nl.mxndarijn.wieisdemol.data.Colors;
import nl.mxndarijn.wieisdemol.map.mapplayer.MapPlayer;
import nl.mxndarijn.wieisdemol.presets.PresetConfig;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class MapConfig {
    private final ArrayList<MapPlayer> colors;
    private File file;
    private String name;
    private UUID owner;

    private ArrayList<UUID> sharedPlayers;

    private LocalDateTime dateCreated;
    private LocalDateTime dateModified;

    private PresetConfig presetConfig;
    private int peacekeeperKills;

    public MapConfig(File file, String name, UUID owner) {
        this.file = file;
        FileConfiguration fc = YamlConfiguration.loadConfiguration(file);

        File presetConfigFile = new File(file.getParent(), "preset.yml");
        this.presetConfig = new PresetConfig(presetConfigFile);

        this.name = name;
        this.owner = owner;
        this.dateCreated = LocalDateTime.now();
        this.dateModified = LocalDateTime.now();
        this.peacekeeperKills = 2;
        this.sharedPlayers = new ArrayList<>();
        this.colors = new ArrayList<>();
        presetConfig.getColors().forEach((c, l) -> {
            this.colors.add(new MapPlayer(c, l));
        });
        save();
    }

    public MapConfig(File file) {
        this.file = file;
        FileConfiguration fc = YamlConfiguration.loadConfiguration(file);
        Arrays.stream(MapConfigValue.values()).forEach(value -> {
            if (!fc.contains(value.getConfigValue())) {
                Logger.logMessage(LogLevel.ERROR, Prefix.MAPS_MANAGER, "Could not find config value: " + value + " (" + file.getAbsolutePath() + ")");
            }
        });
        File presetConfigFile = new File(file.getParent(), "preset.yml");
        this.presetConfig = new PresetConfig(presetConfigFile);

        this.name = fc.getString(MapConfigValue.NAME.getConfigValue());
        this.owner = UUID.fromString(fc.getString(MapConfigValue.OWNER.getConfigValue(), new File(file.getParent()).getName()));
        this.sharedPlayers = new ArrayList<>();
        this.sharedPlayers.addAll(fc.getStringList(MapConfigValue.SHARED_PLAYERS.getConfigValue())
                .stream()
                .map(UUID::fromString)
                .toList());
        this.dateModified = LocalDateTime.parse(fc.getString(MapConfigValue.DATE_MODIFIED.getConfigValue(), LocalDateTime.MIN.toString()));
        this.dateCreated = LocalDateTime.parse(fc.getString(MapConfigValue.DATE_CREATED.getConfigValue(), LocalDateTime.MIN.toString()));
        this.peacekeeperKills = fc.getInt(MapConfigValue.PEACEKEEPER_KILLS.getConfigValue(), 2);

        this.colors = new ArrayList<>();
        ConfigurationSection colorSection = fc.getConfigurationSection(MapConfigValue.COLORS.getConfigValue());
        if (colorSection != null) {
            colorSection.getKeys(false).forEach(key -> {
                ConfigurationSection sec = colorSection.getConfigurationSection(key);
                if (sec != null) {
                    Optional<MapPlayer> mp = MapPlayer.loadMapPlayerFromConfigurationSection(sec);
                    mp.ifPresent(mapPlayer -> {
                        colors.add(mapPlayer);
                    });
                }
            });
        }
    }

    public void save() {
        FileConfiguration fc = YamlConfiguration.loadConfiguration(file);
        if (file == null || fc == null) {
            Logger.logMessage(LogLevel.ERROR, Prefix.MAPS_MANAGER, "Cannot save MapConfig. File or FileConfiguration is null.");
            return;
        }

        fc.set(MapConfigValue.NAME.getConfigValue(), name);
        fc.set(MapConfigValue.OWNER.getConfigValue(), owner.toString());
        fc.set(MapConfigValue.SHARED_PLAYERS.getConfigValue(), sharedPlayers.stream().map(UUID::toString).collect(Collectors.toList()));
        fc.set(MapConfigValue.DATE_CREATED.getConfigValue(), dateCreated.toString());
        fc.set(MapConfigValue.DATE_MODIFIED.getConfigValue(), dateModified.toString());
        fc.set(MapConfigValue.PEACEKEEPER_KILLS.getConfigValue(), peacekeeperKills);
        ConfigurationSection section = fc.createSection(MapConfigValue.COLORS.getConfigValue());
        colors.forEach((color) -> {
            // Save color
            color.save(section);
        });

        try {
            fc.save(file);
        } catch (IOException e) {
            Logger.logMessage(LogLevel.ERROR, Prefix.MAPS_MANAGER, "Failed to save MapConfig: " + e.getMessage());
        }
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public ArrayList<MapPlayer> getColors() {
        return colors;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public ArrayList<UUID> getSharedPlayers() {
        return sharedPlayers;
    }

    public void setSharedPlayers(ArrayList<UUID> sharedPlayers) {
        this.sharedPlayers = sharedPlayers;
    }

    public LocalDateTime getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(LocalDateTime dateCreated) {
        this.dateCreated = dateCreated;
    }

    public LocalDateTime getDateModified() {
        return dateModified;
    }

    public void setDateModified(LocalDateTime dateModified) {
        this.dateModified = dateModified;
    }

    public PresetConfig getPresetConfig() {
        return presetConfig;
    }

    public void setPresetConfig(PresetConfig presetConfig) {
        this.presetConfig = presetConfig;
    }

    public Optional<MapPlayer> getMapPlayerOfColor(Colors color) {
        for (MapPlayer mapPlayer : colors) {
            if (mapPlayer.getColor().equals(color))
                return Optional.of(mapPlayer);
        }
        return Optional.empty();
    }

    public int getPeacekeeperKills() {
        return peacekeeperKills;
    }

    public void setPeacekeeperKills(int peacekeeperKills) {
        this.peacekeeperKills = peacekeeperKills;
    }
}
