package nl.mxndarijn.wieisdemol.items;

import nl.mxndarijn.api.util.Functions;
import nl.mxndarijn.api.util.MxWorldFilter;
import nl.mxndarijn.api.item.MxDefaultItemStackBuilder;
import nl.mxndarijn.wieisdemol.items.game.PlayerManagementItem;
import nl.mxndarijn.wieisdemol.items.game.VanishItem;
import nl.mxndarijn.wieisdemol.items.maps.ChestItem;
import nl.mxndarijn.wieisdemol.items.maps.DoorItem;
import nl.mxndarijn.wieisdemol.items.maps.ShulkerItem;
import nl.mxndarijn.wieisdemol.items.maps.VulTool;
import nl.mxndarijn.wieisdemol.items.presets.ChestConfigureTool;
import nl.mxndarijn.wieisdemol.items.presets.DoorConfigureTool;
import nl.mxndarijn.wieisdemol.items.presets.PresetConfigureTool;
import nl.mxndarijn.wieisdemol.items.presets.ShulkerConfigureTool;
import nl.mxndarijn.api.mxitem.MxItem;
import nl.mxndarijn.wieisdemol.items.spawn.GamesItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;

public enum Items {

    // Preset Items
    PRESET_CONFIGURE_TOOL(
            MxDefaultItemStackBuilder.create(Material.NETHER_STAR, 1)
                .setName(ChatColor.GRAY + "Preset Configure-Tool")
                .addLore(" ")
                .addLore(ChatColor.YELLOW + "Met dit item kan je instellingen in een preset aanpassen.")
                .build(),
            p -> {
                return true;
            },
            false,
            PresetConfigureTool.class,
            Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK
    ),

    CHEST_CONFIGURE_TOOL(
            MxDefaultItemStackBuilder.create(Material.STICK, 1)
                    .setName(ChatColor.GRAY + "Chest Configure-Tool")
                    .addLore(" ")
                    .addLore(ChatColor.YELLOW + "Met dit item kan je kisten in een preset aanpassen.")
                    .build(),
            p -> {
                return true;
            },
            false,
            ChestConfigureTool.class,
            Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK
    ),
    SHULKER_CONFIGURE_TOOL(
            MxDefaultItemStackBuilder.create(Material.SHULKER_SHELL, 1)
                    .setName(ChatColor.GRAY + "Shulker Configure-Tool")
                    .addLore(" ")
                    .addLore(ChatColor.YELLOW + "Met dit item kan je shulkers in een preset aanpassen.")
                    .build(),
            p -> {
                return true;
            },
            false,
            ShulkerConfigureTool.class,
            Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK
    ),
    DOOR_CONFIGURE_TOOL(
            MxDefaultItemStackBuilder.create(Material.BRICK, 1)
                    .setName(ChatColor.GRAY + "Door Configure-Tool")
                    .addLore(" ")
                    .addLore(ChatColor.YELLOW + "Met dit item kan je deuren in een preset aanpassen.")
                    .addLore(ChatColor.YELLOW + "Gebruik in de lucht om deuren te maken.")
                    .addLore(ChatColor.YELLOW + "Gebruik tegen een block om het block toe te voegen aan een deur.")
                    .build(),
            p -> {
                return true;
            },
            false,
            DoorConfigureTool.class,
            Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK
    ),

    // Map items

    CHEST_TOOL(
            MxDefaultItemStackBuilder.create(Material.STICK, 1)
                    .setName(ChatColor.GRAY + "Chest Tool")
                    .addLore(" ")
                    .addLore(ChatColor.YELLOW + "Met dit item kan je kisten op afstand openen.")
                    .build(),
            p -> {
                return true;
            },
            false,
            ChestItem.class,
            Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK
    ),
    SHULKER_TOOL(
            MxDefaultItemStackBuilder.create(Material.SHULKER_SHELL, 1)
                    .setName(ChatColor.GRAY + "Shulker Tool")
                    .addLore(" ")
                    .addLore(ChatColor.YELLOW + "Met dit item kan je shulkers op afstand openen.")
                    .build(),
            p -> {
                return true;
            },
            false,
            ShulkerItem.class,
            Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK
    ),
    DOOR_ITEM(
            MxDefaultItemStackBuilder.create(Material.BRICK, 1)
                    .setName(ChatColor.GRAY + "Door Tool")
                    .addLore(" ")
                    .addLore(ChatColor.YELLOW + "Met dit item kan je deuren openen en sluiten.")
                    .build(),
            p -> {
                return true;
            },
            false,
            DoorItem.class,
            Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK
    ),
    VUL_TOOL(
            MxDefaultItemStackBuilder.create(Material.NETHER_STAR, 1)
                    .setName(ChatColor.GRAY + "Vul Tool")
                    .addLore(" ")
                    .addLore(ChatColor.YELLOW + "Met dit item kan je instellingen in een map aanpassen.")
                    .build(),
            p -> {
                return true;
            },
            false,
            VulTool.class,
            Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK
    ),

    GAMES_ITEM(
            MxDefaultItemStackBuilder.create(Material.COMPASS, 1)
                    .setName(ChatColor.GRAY + "Game Menu")
                    .addLore(" ")
                    .addLore(ChatColor.YELLOW + "Met dit item kan je games joinen,")
                    .addLore(ChatColor.YELLOW + "en games aanmaken als je een host bent.")
                    .build(),
            p -> {
                return p.getWorld().getUID().equals(Functions.getSpawnLocation().getWorld().getUID());
            },
            false,
            GamesItem.class,
            Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK
    ),
    VANISH_ITEM(MxDefaultItemStackBuilder.create(Material.ENDER_EYE)
            .setName(ChatColor.GRAY + "Vanish")
            .addBlankLore()
            .addLore(ChatColor.YELLOW + "Met dit item kan je ontzichtbaar worden.")
            .build(),
            p -> {
                return true;
            },
            false,
            VanishItem.class,
            Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK),
    PLAYER_MANAGEMENT_ITEM(MxDefaultItemStackBuilder.create(Material.CYAN_DYE)
            .setName(ChatColor.GRAY + "Kleuren tool")
            .addBlankLore()
            .addLore(ChatColor.YELLOW + "Met dit item kan je kleuren beheren.")
            .build(),
            p -> {
                return true;
            },
            false,
            PlayerManagementItem.class,
            Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK),
    ;



    private final ItemStack itemStack;
    private final MxWorldFilter worldFilter;
    private final boolean gameItem;
    private final Class<? extends MxItem> classObject;
    private final Action[] actions;
    Items(ItemStack is, MxWorldFilter mxWorldFilter, boolean gameItem, Class<? extends MxItem> classObject, Action... actions) {
        this.itemStack = is;
        this.worldFilter = mxWorldFilter;
        this.gameItem = gameItem;
        this.classObject = classObject;
        this.actions = actions;
    }

    public ItemStack getItemStack() {
        return itemStack.clone();
    }

    public MxWorldFilter getWorldFilter() {
        return worldFilter;
    }

    public boolean isGameItem() {
        return gameItem;
    }

    public Class<? extends MxItem> getClassObject() {
        return classObject;
    }

    public Action[] getActions() {
        return actions;
    }
}
