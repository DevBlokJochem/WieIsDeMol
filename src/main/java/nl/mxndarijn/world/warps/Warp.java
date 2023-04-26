package nl.mxndarijn.world.warps;

import nl.mxndarijn.world.mxworld.MxLocation;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Warp {

    private String name;
    private MxLocation mxLocation;
    private String skullId;

    private Warp(ConfigurationSection section) {
        name = section.getName();
        Optional<MxLocation> optionalMxLocation = MxLocation.loadFromConfigurationSection(section.getConfigurationSection("location"));
        optionalMxLocation.ifPresent(location -> mxLocation = location);
        skullId = section.getString("skull");
    }

    public Warp(String name, String skullId, MxLocation location) {
        this.name = name;
        this.mxLocation = location;
        this.skullId = skullId;
    }

    public void save(File file, FileConfiguration fc) {
        ConfigurationSection section = fc.createSection(name);
        section.set("skull", skullId);
        mxLocation.save(file, fc, section.createSection("location"));
    }

    public static Optional<Warp> create( ConfigurationSection section) {
        Warp w = new Warp(section);
        if(w.mxLocation != null) {
            return Optional.of(w);
        }
        return Optional.empty();
    }


    public static List<Warp> getWarpsFromFile(File f, FileConfiguration fc) {
        ArrayList<Warp> warps = new ArrayList<>();
        fc.getKeys(false).forEach(key -> {
            ConfigurationSection section = fc.getConfigurationSection(key);
            Optional<Warp> opt = create(section);
            opt.ifPresent(warps::add);
        });

        return warps;

    }

    public String getName() {
        return name;
    }

    public MxLocation getMxLocation() {
        return mxLocation;
    }

    public String getSkullId() {
        return skullId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMxLocation(MxLocation mxLocation) {
        this.mxLocation = mxLocation;
    }

    public void setSkullId(String skullId) {
        this.skullId = skullId;
    }
}
