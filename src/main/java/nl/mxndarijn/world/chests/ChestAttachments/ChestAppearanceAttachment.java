package nl.mxndarijn.world.chests.ChestAttachments;

import nl.mxndarijn.data.ChestAppearance;
import nl.mxndarijn.inventory.MxItemClicked;
import nl.mxndarijn.inventory.item.MxSkullItemStackBuilder;
import nl.mxndarijn.inventory.item.Pair;
import nl.mxndarijn.util.logger.LogLevel;
import nl.mxndarijn.util.logger.Logger;
import nl.mxndarijn.util.logger.Prefix;
import nl.mxndarijn.world.chests.ChestInformation;
import nl.mxndarijn.world.mxworld.MxLocation;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class ChestAppearanceAttachment extends ChestAttachment {
    private ChestAppearance appearance;

    public static Optional<ChestAppearanceAttachment> createFromSection(Map<String, Object> section, ChestInformation inf) {
        ChestAppearanceAttachment attachment = new ChestAppearanceAttachment();
        if(!getDefaultValues(attachment, inf, section)) {
            return Optional.empty();
        }
        assert(section != null);

        if(!section.containsKey("appearance")) {
            Logger.logMessage(LogLevel.ERROR, Prefix.MAPS_MANAGER, "Could not load appearance.  Type: " + attachment.type);
            return Optional.empty();
        }
        if(!attachment.setAppearance(ChestAppearance.valueOf((String) section.get("appearance")))) {
            Logger.logMessage(LogLevel.ERROR, Prefix.MAPS_MANAGER, "Could not find appearance.  Type: " + attachment.type);
            return Optional.empty();
        }

        return Optional.of(attachment);
    }

    public boolean setAppearance(ChestAppearance appearance) {
        this.appearance = appearance;

        return appearance != null;
    }

    public static ChestAttachment createNewInstance(String type, ChestInformation inf) {
        ChestAppearanceAttachment attachment = new ChestAppearanceAttachment();
        attachment.setDefaults(type,  inf);
        attachment.appearance = ChestAppearance.CHOICE_THREE;
        return attachment;
    }


    @Override
    public Map<String, Object> getDataForSaving() {
        Map<String, Object> map = new HashMap<>();
        getDataDefaults(map);
        map.put("appearance", appearance.name());

        return map;
    }

    @Override
    public Pair<ItemStack, MxItemClicked> getEditAttachmentItem() {
        return new Pair<>(
                MxSkullItemStackBuilder.create(1)
                        .setSkinFromHeadsData("rainbow-chest")
                        .setName(ChatColor.GREEN + "Kist uiterlijk")
                        .addBlankLore()
                        .addLore(ChatColor.GRAY + "Uiterlijk: " + appearance.getName())
                        .addBlankLore()
                        .addLore(ChatColor.YELLOW + "Klik hier om deze chest attachment aan te passen.")
                        .build(),
                (mxInv, e) -> {
                    //TODO
                }

        );
    }
}
