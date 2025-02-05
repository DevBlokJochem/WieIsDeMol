package nl.mxndarijn.wieisdemol.items.maps;

import nl.mxndarijn.api.chatinput.MxChatInputManager;
import nl.mxndarijn.api.inventory.*;
import nl.mxndarijn.api.inventory.heads.MxHeadManager;
import nl.mxndarijn.api.inventory.heads.MxHeadSection;
import nl.mxndarijn.api.inventory.menu.MxDefaultMenuBuilder;
import nl.mxndarijn.api.inventory.menu.MxListInventoryBuilder;
import nl.mxndarijn.api.item.MxDefaultItemStackBuilder;
import nl.mxndarijn.api.item.MxSkullItemStackBuilder;
import nl.mxndarijn.api.item.Pair;
import nl.mxndarijn.api.logger.LogLevel;
import nl.mxndarijn.api.logger.Logger;
import nl.mxndarijn.api.logger.Prefix;
import nl.mxndarijn.api.mxitem.MxItem;
import nl.mxndarijn.api.mxworld.MxLocation;
import nl.mxndarijn.api.util.Functions;
import nl.mxndarijn.api.util.MxWorldFilter;
import nl.mxndarijn.wieisdemol.WieIsDeMol;
import nl.mxndarijn.wieisdemol.data.*;
import nl.mxndarijn.wieisdemol.managers.InteractionManager;
import nl.mxndarijn.wieisdemol.managers.MapManager;
import nl.mxndarijn.wieisdemol.managers.language.LanguageManager;
import nl.mxndarijn.wieisdemol.managers.language.LanguageText;
import nl.mxndarijn.wieisdemol.managers.warps.Warp;
import nl.mxndarijn.wieisdemol.managers.warps.WarpManager;
import nl.mxndarijn.wieisdemol.map.Map;
import nl.mxndarijn.wieisdemol.map.MapConfig;
import nl.mxndarijn.wieisdemol.map.mapplayer.MapPlayer;
import nl.mxndarijn.wieisdemol.presets.PresetConfig;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.ShulkerBox;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

public class VulTool extends MxItem {


    public VulTool(ItemStack is, MxWorldFilter worldFilter, boolean gameItem, Action... actions) {
        super(is, worldFilter, gameItem, actions);
    }

    @Override
    public void execute(Player p, PlayerInteractEvent e) {
        Optional<Map> optionalMap = MapManager.getInstance().getMapByWorldUID(p.getWorld().getUID());
        if (optionalMap.isEmpty()) {
            return;
        }
        Map map = optionalMap.get();
        MapConfig mapConfig = map.getMapConfig();
        PresetConfig presetConfig = mapConfig.getPresetConfig();

        MxInventoryManager.getInstance().addAndOpenInventory(p, MxDefaultMenuBuilder.create(ChatColor.GRAY + "Vul Tool", MxInventorySlots.THREE_ROWS)
                .setItem(MxDefaultItemStackBuilder.create(Material.DIAMOND_SWORD)
                                .setName(ChatColor.GRAY + "Peacekeeper-Kills")
                                .addBlankLore()
                                .addLore(ChatColor.GRAY + "Huidig: " + map.getMapConfig().getPeacekeeperKills())
                                .addLore(ChatColor.YELLOW + "Klik hier om het aantal peacekeeper kills te veranderen.")
                                .build(),
                        25,
                        (mxInv, e12) -> {
                            p.sendMessage(ChatPrefix.WIDM + LanguageManager.getInstance().getLanguageString(LanguageText.MAP_ENTER_PEACEKEEPER_KILLS));
                            p.closeInventory();
                            MxChatInputManager.getInstance().addChatInputCallback(p.getUniqueId(), message -> {
                                try {
                                    int i = Integer.parseInt(message);
                                    map.getMapConfig().setPeacekeeperKills(i);
                                    p.sendMessage(ChatPrefix.WIDM + LanguageManager.getInstance().getLanguageString(LanguageText.MAP_PEACEKEEPER_KILLS_CHANGED, Collections.singletonList(i + "")));
                                } catch (NumberFormatException a) {
                                    p.sendMessage(ChatPrefix.WIDM + LanguageManager.getInstance().getLanguageString(LanguageText.MAP_PEACEKEEPER_KILLS_CHANGED_ERROR));
                                }
                            });
                        })
                .setItem(getNameItemStack(mapConfig), 4, (mxInv, e1) -> {
                    changeName(p, mapConfig, mxInv);
                })
                .setItem(getSkull(presetConfig), 8, (mxInv, e1) -> {

                    changeSkull(p, mxInv, e1, presetConfig);

                })
                .setItem(getColorsItemStack(), 12, (mxInv, e1) -> {
                    openColorsMenu(p, mxInv, map);
                })
                .setItem(getItems(), 13, (mxInv, e1) -> {
                    p.performCommand("items");
                })
                .setItem(getWarpItemStack(), 14, (mxInv, e1) -> {
                    openWarpsMenu(p, mxInv, map);

                })
                .setItem(getChangeSpawnItemStack(), 17, (mxInv, e1) -> {
                    changeSpawn(p, map);
                })
                .setItem(getDeleteMapSkull(), 18, (mxInv, e1) -> {
                    deleteMap(p, map, mxInv);
                })
                .setItem(getManageSharedPlayersItemStack(), 22, (mxInv, e1) -> {
                    openManageSharedPlayers(p, mxInv, map);
                })
                .setItem(getInteractionItemStack(), 26, (mxInv, e1) -> {
                    openInteractionsMenu(p, mxInv, map);
                })

                .build());
    }


    public ItemStack getColorsItemStack() {
        return MxDefaultItemStackBuilder.create(Material.LIGHT_BLUE_SHULKER_BOX)
                .setName(ChatColor.GRAY + "Beheer kleuren")
                .addBlankLore()
                .addLore(ChatColor.GRAY + "Hier kan je kleuren toevoegen of verwijderen.")
                .addLore(ChatColor.GRAY + "Maar ook rollen aanpassen of spawnpoints veranderen.")
                .addBlankLore()
                .addLore(ChatColor.YELLOW + "Klik hier om aanpassingen te doen aan de kleuren.")
                .build();
    }

    public ItemStack getManageSharedPlayersItemStack() {
        return MxDefaultItemStackBuilder.create(Material.PLAYER_HEAD)
                .setName(ChatColor.GRAY + "Beheer vullers")
                .addBlankLore()
                .addLore(ChatColor.YELLOW + "Klik hier om de vullers aan te passen.")
                .build();
    }

    private ItemStack getSkull(PresetConfig config) {
        return MxDefaultItemStackBuilder.create(Material.SKELETON_SKULL)
                .setName(ChatColor.GRAY + "Verander skull")
                .addBlankLore()
                .addLore(ChatColor.GRAY + "Status: " + (MxHeadManager.getInstance().getHeadSection(config.getSkullId()).isPresent() ? MxHeadManager.getInstance().getHeadSection(config.getSkullId()).get().getName().get() : "Niet-gevonden"))
                .addBlankLore()
                .addLore(ChatColor.YELLOW + "Klik hier om de skull van de map te veranderen.")
                .addLore(ChatColor.YELLOW + "Je krijgt een lijst met skulls van het commands /skulls.")
                .build();
    }

    private ItemStack getDeleteMapSkull() {
        return MxSkullItemStackBuilder.create(1)
                .setSkinFromHeadsData("red-minus")
                .setName(ChatColor.GRAY + "Verwijder Map")
                .addBlankLore()
                .addLore(ChatColor.YELLOW + "Klik hier om de map permanent te verwijderen.")
                .build();
    }

    private ItemStack getNameItemStack(MapConfig config) {
        return MxDefaultItemStackBuilder.create(Material.NAME_TAG)
                .setName(ChatColor.GRAY + "Verander naam")
                .addBlankLore()
                .addLore(ChatColor.GRAY + "Status: " + config.getName())
                .addBlankLore()
                .addLore(ChatColor.YELLOW + "Klik hier om de naam van de map te veranderen.")
                .addLore(ChatColor.YELLOW + "Vervolgens moet je in de chat de nieuwe naam sturen.")
                .build();
    }

    private ItemStack getInteractionItem(Interaction i, boolean b) {
        return MxDefaultItemStackBuilder.create(i.getMat())
                .setName(b ? ChatColor.GREEN + "Aan" : ChatColor.RED + "Uit")
                .addBlankLore()
                .addLore(ChatColor.GRAY + i.getMat().toString().replace("_", " ").toLowerCase())
                .addBlankLore()
                .addLore(ChatColor.YELLOW + "Klik hier om het block te togglen.")
                .build();
    }

    private ItemStack getColorItemStack(Colors c, PresetConfig config) {
        return MxSkullItemStackBuilder.create(1)
                .setSkinFromHeadsData(c.getHeadKey())
                .setName(c.getColor() + c.getDisplayName())
                .addBlankLore()
                .addLore(config.getColors().containsKey(c) ? ChatColor.GREEN + "Kleur is toegevoegd" : ChatColor.RED + "Kleur is niet toegevoegd")
                .addBlankLore()
                .addLore(config.getColors().containsKey(c) ? ChatColor.YELLOW + "Klik om kleur te beheren" : ChatColor.YELLOW + "Klik om kleur toe te voegen")
                .build();
    }

    private ItemStack getInteractionItemStack() {
        return MxDefaultItemStackBuilder.create(Material.OAK_TRAPDOOR)
                .setName(ChatColor.GRAY + "Interactions")
                .addBlankLore()
                .addLore(ChatColor.YELLOW + "Klik hier om de interactions van de map te bekijken en te veranderen.")
                .build();
    }


    private ItemStack getChangeSpawnItemStack() {
        return MxDefaultItemStackBuilder.create(Material.ENDER_PEARL)
                .setName(ChatColor.GRAY + "Verander spawn")
                .addBlankLore()
                .addLore(ChatColor.YELLOW + "Klik hier om de spawn locatie aan te passen.")
                .build();
    }

    private ItemStack getItems() {
        return MxDefaultItemStackBuilder.create(Material.CHEST)
                .setName(ChatColor.GRAY + "Bekijk Items")
                .addBlankLore()
                .addLore(ChatColor.YELLOW + "Klik hier om de items van /items te bekijken.")
                .build();
    }

    private ItemStack getWarpItemStack() {
        return MxDefaultItemStackBuilder.create(Material.COMPASS)
                .setName(ChatColor.GRAY + "Warps")
                .addBlankLore()
                .addLore(ChatColor.YELLOW + "Klik hier om de warps van de map te bekijken en te veranderen.")
                .build();
    }

    private void openWarpsMenu(Player p, MxInventory mainInv, Map map) {
        List<Warp> warps = map.getWarpManager().getWarps();
        MxItemClicked clicked = (mxInv, e) -> {
            ItemStack is = e.getCurrentItem();
            ItemMeta im = is.getItemMeta();
            PersistentDataContainer container = im.getPersistentDataContainer();
            String warpName = container.get(new NamespacedKey(JavaPlugin.getPlugin(WieIsDeMol.class), "warp-name"), PersistentDataType.STRING);

            Optional<Warp> optW = map.getWarpManager().getWarpByName(warpName);
            if (optW.isEmpty()) {
                return;
            }
            Warp w = optW.get();
            if (e.isLeftClick()) {
                p.teleport(w.getMxLocation().getLocation(p.getWorld()));
                p.sendMessage(ChatPrefix.WIDM + LanguageManager.getInstance().getLanguageString(LanguageText.PRESET_CONFIGURE_TOOL_WARPS_WARP_TELEPORTED));
                return;
            }

            if (!e.isRightClick()) {
                return;
            }
            MxLocation l = w.getMxLocation();
            MxInventoryManager.getInstance().addAndOpenInventory(p, MxDefaultMenuBuilder.create("Vul-Tool", MxInventorySlots.THREE_ROWS)
                    .setPrevious(mxInv)
                    .setItem(MxDefaultItemStackBuilder.create(Material.ENDER_PEARL)
                                    .setName(ChatColor.GRAY + "Verander warp locatie")
                                    .addBlankLore()
                                    .addLore(ChatColor.YELLOW + "Verander de warp locatie naar je huidige locatie.")
                                    .build(),
                            12,
                            (mxInv1, e12) -> {
                                map.getWarpManager().removeWarp(w);
                                w.setMxLocation(MxLocation.getFromLocation(p.getLocation()));
                                map.getWarpManager().addWarp(w);

                                p.sendMessage(LanguageManager.getInstance().getLanguageString(LanguageText.PRESET_CONFIGURE_TOOL_WARPS_WARP_NAME_CHANGED, ChatPrefix.WIDM));
                                p.closeInventory();
                            }
                    )
                    .setItem(MxDefaultItemStackBuilder.create(Material.COMPASS)
                                    .setName(ChatColor.GRAY + "Teleporteer")
                                    .addBlankLore()
                                    .addLore(ChatColor.YELLOW + "Teleporteer naar de warp.")
                                    .build(),
                            14,
                            (mxInv1, e12) -> {
                                p.teleport(w.getMxLocation().getLocation(p.getWorld()));
                                p.sendMessage(LanguageManager.getInstance().getLanguageString(LanguageText.PRESET_CONFIGURE_TOOL_WARPS_WARP_TELEPORTED, ChatPrefix.WIDM));
                                p.closeInventory();
                            }
                    )

                    .setItem(MxSkullItemStackBuilder.create(1)
                                    .setSkinFromHeadsData("red-minus")
                                    .setName(ChatColor.GRAY + "Verwijder")
                                    .addBlankLore()
                                    .addLore(ChatColor.YELLOW + "Verwijder de warp.")
                                    .build(),
                            26,
                            (mxInv1, e12) -> {
                                map.getWarpManager().removeWarp(w);
                                p.closeInventory();
                                p.sendMessage(LanguageManager.getInstance().getLanguageString(LanguageText.PRESET_CONFIGURE_TOOL_WARPS_WARP_DELETED));
                            }
                    )
                    .setItem(MxSkullItemStackBuilder.create(1)
                                    .setSkinFromHeadsData(w.getSkullId())
                                    .setName(ChatColor.GRAY + "Info")
                                    .addBlankLore()
                                    .addLore(ChatColor.GRAY + "Naam: " + w.getName())
                                    .addBlankLore()
                                    .addLore(ChatColor.GRAY + "X: " + l.getX())
                                    .addLore(ChatColor.GRAY + "Y: " + l.getY())
                                    .addLore(ChatColor.GRAY + "Z: " + l.getZ())
                                    .addLore(ChatColor.GRAY + "Pitch: " + l.getPitch())
                                    .addLore(ChatColor.GRAY + "Yaw: " + l.getYaw())
                                    .build(),
                            22,
                            (mxInv1, e12) -> {
                                p.teleport(w.getMxLocation().getLocation(p.getWorld()));
                                p.sendMessage(LanguageManager.getInstance().getLanguageString(LanguageText.PRESET_CONFIGURE_TOOL_WARPS_WARP_TELEPORTED, ChatPrefix.WIDM));
                                p.closeInventory();
                            }
                    )
                    .build());

        };

        ArrayList<Pair<ItemStack, MxItemClicked>> list = new ArrayList<>();
        warps.forEach(warp -> {
            list.add(new Pair<>(
                    MxSkullItemStackBuilder.create(1)
                            .setSkinFromHeadsData(warp.getSkullId())
                            .setName(ChatColor.GRAY + warp.getName())
                            .addBlankLore()
                            .addCustomTagString("warp-name", warp.getName())
                            .addLore(ChatColor.YELLOW + "Linkermuis-knop om naar de warp te teleporteren.")
                            .addLore(ChatColor.YELLOW + "Rechtermuis-knop op de warp om deze aan te passen.")
                            .build(),
                    clicked
            ));
        });

        MxInventoryManager.getInstance().addAndOpenInventory(p, MxListInventoryBuilder.create(ChatColor.GRAY + "Vul-Tool", MxInventorySlots.THREE_ROWS)
                .setAvailableSlots(MxInventoryIndex.ROW_ONE_TO_TWO)
                .addListItems(list)
                .setItem(MxDefaultItemStackBuilder.create(Material.PAPER)
                        .setName(ChatColor.GRAY + "Info")
                        .addBlankLore()
                        .addLore(ChatColor.YELLOW + "Hier staan alle warps, je kan er ook zelf toevoegen.")
                        .build(), 22, null)
                .setItem(MxDefaultItemStackBuilder.create(Material.CRAFTING_TABLE)
                        .setName(ChatColor.GRAY + "Nieuwe warp")
                        .addBlankLore()
                        .addLore(ChatColor.YELLOW + "Klik hier om een nieuwe warp te te voegen.")
                        .addLore(ChatColor.YELLOW + "De locatie zal je huidige locatie worden.")
                        .build(), 26, (mxInv, e) -> {
                    Location loc = p.getLocation();
                    MxHeadManager mxHeadManager = MxHeadManager.getInstance();
                    ArrayList<Pair<ItemStack, MxItemClicked>> listSkulls = new ArrayList<>();
                    MxItemClicked clickedOnSkull = (skullInv, e1) -> {
                        ItemStack is = e1.getCurrentItem();
                        ItemMeta im = is.getItemMeta();
                        PersistentDataContainer container = im.getPersistentDataContainer();
                        String warpHeadKey = container.get(new NamespacedKey(JavaPlugin.getPlugin(WieIsDeMol.class), "skull_key"), PersistentDataType.STRING);

                        p.sendMessage(LanguageManager.getInstance().getLanguageString(LanguageText.PRESET_CONFIGURE_TOOL_WARPS_CHANGE_NAME, ChatPrefix.WIDM));
                        p.closeInventory();
                        WarpManager warpManager = map.getWarpManager();
                        MxChatInputManager.getInstance().addChatInputCallback(p.getUniqueId(), message -> {
                            if (warps.stream().anyMatch(w -> w.getName().equalsIgnoreCase(message))) {
                                p.sendMessage(ChatPrefix.WIDM + LanguageManager.getInstance().getLanguageString(LanguageText.PRESET_CONFIGURE_TOOL_WARPS_WARP_NAME_ALREADY_EXISTS, Collections.singletonList(message)));
                                return;
                            }
                            Warp w = new Warp(message, warpHeadKey, MxLocation.getFromLocation(loc));
                            warpManager.addWarp(w);
                            p.sendMessage(ChatPrefix.WIDM + LanguageManager.getInstance().getLanguageString(LanguageText.PRESET_CONFIGURE_TOOL_WARPS_WARP_CREATED, Collections.singletonList(message)));
                        });

                    };

                    MxHeadManager.getInstance().getAllHeadKeys().forEach(key -> {
                        Optional<MxHeadSection> section = mxHeadManager.getHeadSection(key);
                        section.ifPresent(mxHeadSection -> {
                            MxSkullItemStackBuilder b = MxSkullItemStackBuilder.create(1)
                                    .setSkinFromHeadsData(key)
                                    .setName(ChatColor.GRAY + mxHeadSection.getName().get())
                                    .addBlankLore()
                                    .addLore(ChatColor.YELLOW + "Klik om te selecteren.")
                                    .addCustomTagString("skull_key", mxHeadSection.getKey());
                            listSkulls.add(new Pair<>(b.build(), clickedOnSkull));
                        });
                    });

                    MxInventoryManager.getInstance().addAndOpenInventory(p,
                            MxListInventoryBuilder.create(ChatColor.GRAY + "Vul-Tool", MxInventorySlots.SIX_ROWS)
                                    .setAvailableSlots(MxInventoryIndex.ROW_ONE_TO_FIVE)
                                    .addListItems(listSkulls)
                                    .setItem(MxDefaultItemStackBuilder.create(Material.PAPER)
                                            .setName(ChatColor.GRAY + "Info")
                                            .addBlankLore()
                                            .addLore(ChatColor.YELLOW + "Klik op een skull om dat de nieuwe skull van de preset te maken.")
                                            .build(), 48, null)
                                    .setPrevious(mxInv)
                                    .build());
                })
                .setPrevious(mainInv)
                .build()
        );

    }

    private void changeName(Player p, MapConfig config, MxInventory mainInv) {
        p.sendMessage(LanguageManager.getInstance().getLanguageString(LanguageText.MAP_VUL_TOOL_ENTER_NEW_NAME, ChatPrefix.WIDM));
        p.closeInventory();
        MxChatInputManager.getInstance().addChatInputCallback(p.getUniqueId(), message -> {
            config.setName(message);
            config.save();
            p.sendMessage(LanguageManager.getInstance().getLanguageString(LanguageText.MAP_VUL_TOOL_NAME_CHANGED, Collections.singletonList(message), ChatPrefix.WIDM));
            mainInv.getInv().setItem(10, getNameItemStack(config));
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                MxInventoryManager.getInstance().addAndOpenInventory(p.getUniqueId(), mainInv);
            });
        });
    }

    private void changeSkull(Player p, MxInventory mainInv, InventoryClickEvent clickMain, PresetConfig config) {
        MxHeadManager mxHeadManager = MxHeadManager.getInstance();
        ArrayList<Pair<ItemStack, MxItemClicked>> list = new ArrayList<>();
        MxItemClicked clicked = (mxInv, e1) -> {
            ItemStack is = e1.getCurrentItem();
            ItemMeta im = is.getItemMeta();
            PersistentDataContainer container = im.getPersistentDataContainer();
            String key = container.get(new NamespacedKey(JavaPlugin.getPlugin(WieIsDeMol.class), "skull_key"), PersistentDataType.STRING);

            Optional<MxHeadSection> section = MxHeadManager.getInstance().getHeadSection(key);
            if (section.isPresent() && section.get().getName().isPresent()) {
                clickMain.getWhoClicked().sendMessage(ChatPrefix.WIDM + LanguageManager.getInstance().getLanguageString(LanguageText.PRESET_CONFIGURE_TOOL_SKULL_CHANGED, Collections.singletonList(section.get().getName().get())));
            }
            config.setSkullId(key);
            config.save();
            mainInv.getInv().setItem(9, getSkull(config));
            MxInventoryManager.getInstance().addAndOpenInventory(p, mainInv);

        };

        MxHeadManager.getInstance().getAllHeadKeys().forEach(key -> {
            Optional<MxHeadSection> section = mxHeadManager.getHeadSection(key);
            section.ifPresent(mxHeadSection -> {
                MxSkullItemStackBuilder b = MxSkullItemStackBuilder.create(1)
                        .setSkinFromHeadsData(key)
                        .setName(ChatColor.GRAY + mxHeadSection.getName().get())
                        .addBlankLore()
                        .addLore(ChatColor.YELLOW + "Klik om de skull te selecteren.")
                        .addCustomTagString("skull_key", mxHeadSection.getKey());
                list.add(new Pair<>(b.build(), clicked));
            });
        });

        MxInventoryManager.getInstance().addAndOpenInventory(p,
                MxListInventoryBuilder.create(ChatColor.GRAY + "Vul-Tool", MxInventorySlots.SIX_ROWS)
                        .setAvailableSlots(MxInventoryIndex.ROW_ONE_TO_FIVE)
                        .addListItems(list)
                        .setItem(MxDefaultItemStackBuilder.create(Material.PAPER)
                                .setName(ChatColor.GRAY + "Info")
                                .addBlankLore()
                                .addLore(ChatColor.YELLOW + "Klik op de skull om dat de nieuwe skull van de map te maken.")
                                .build(), 48, null)
                        .setPrevious(mainInv)
                        .build());
    }

    private void openInteractionsMenu(Player p, MxInventory mainInv, Map map) {
        ArrayList<Pair<ItemStack, MxItemClicked>> list = new ArrayList<>();
        InteractionManager manager = map.getInteractionManager();
        manager.getInteractions().forEach((i, b) -> {
            list.add(new Pair<>(getInteractionItem(i, b),
                    (mxInv, e) -> {
                        manager.setInteraction(i, !manager.getInteractions().get(i));
                        manager.save();
                        mxInv.getInv().setItem(e.getSlot(), getInteractionItem(i, manager.getInteractions().get(i)));
                    }
            ));
        });

        list.sort(Comparator.comparing(o -> new StringBuilder(o.first.getType().toString()).reverse().toString()));

        MxInventoryManager.getInstance().addAndOpenInventory(p, MxListInventoryBuilder.create(ChatColor.GRAY + "Vul-Tool", MxInventorySlots.SIX_ROWS)
                .setAvailableSlots(MxInventoryIndex.ROW_ONE_TO_FIVE)
                .setPrevious(mainInv)
                .setListItems(list)
                .build());
    }

    private void openManageSharedPlayers(Player p, MxInventory mainInv, Map map) {
        MapConfig config = map.getMapConfig();

        if (!config.getOwner().equals(p.getUniqueId()) || p.hasPermission(Permissions.VULTOOL.getPermission())) {
            p.sendMessage(ChatPrefix.WIDM + LanguageManager.getInstance().getLanguageString(LanguageText.MAP_VUL_TOOL_NOT_OWNER_OF_MAP));
            return;
        }

        ArrayList<Pair<ItemStack, MxItemClicked>> list = new ArrayList<>();
        config.getSharedPlayers().forEach(sharedPlayer -> {
            OfflinePlayer player = Bukkit.getOfflinePlayer(sharedPlayer);
            list.add(new Pair<>(
                    MxSkullItemStackBuilder.create(1)
                            .setSkinFromHeadsData(sharedPlayer.toString())
                            .setName(ChatColor.GRAY + player.getName())
                            .addBlankLore()
                            .addLore(ChatColor.YELLOW + "Shift + Linkermuis-knop om " + player.getName() + " te verwijderen als vuller.")
                            .build(),
                    (mxInv, e) -> {
                        if (e.isLeftClick() && e.isShiftClick()) {
                            config.getSharedPlayers().remove(sharedPlayer);
                            p.sendMessage(ChatPrefix.WIDM + LanguageManager.getInstance().getLanguageString(LanguageText.MAP_VUL_TOOL_PLAYER_REMOVED, Collections.singletonList(player.getName())));
                            openManageSharedPlayers(p, mainInv, map);
                        }
                    }
            ));
        });
        MxInventoryManager.getInstance().addAndOpenInventory(p, MxListInventoryBuilder.create("Vul-Tool Beheer Vullers", MxInventorySlots.THREE_ROWS)
                .setAvailableSlots(MxInventoryIndex.ROW_ONE_TO_TWO)
                .setListItems(list)
                .setNextPageItemStackSlot(25)
                .setPreviousItemStackSlot(20)
                .setPrevious(mainInv)
                .setItem(
                        MxSkullItemStackBuilder.create(1)
                                .setSkinFromHeadsData("wooden-plus")
                                .setName(ChatColor.GRAY + "Voeg vuller toe")
                                .addBlankLore()
                                .addLore(ChatColor.YELLOW + "Klik hier om een vuller toe te voegen")
                                .build(),
                        26,
                        (mxInv, e) -> {
                            p.closeInventory();
                            p.sendMessage(ChatPrefix.WIDM + LanguageManager.getInstance().getLanguageString(LanguageText.MAP_VUL_TOOL_ENTER_NAME_OF_VULLER));
                            MxChatInputManager.getInstance().addChatInputCallback(p.getUniqueId(), message -> {
                                Player offlinePlayer = Bukkit.getPlayer(message);
                                if (offlinePlayer == null) {
                                    p.sendMessage(ChatPrefix.WIDM + LanguageManager.getInstance().getLanguageString(LanguageText.MAP_VUL_TOOL_PLAYER_NOT_FOUND));
                                    return;
                                }
                                UUID uuid = offlinePlayer.getUniqueId();
                                if (uuid == p.getUniqueId()) {
                                    p.sendMessage(ChatPrefix.WIDM + LanguageManager.getInstance().getLanguageString(LanguageText.MAP_VUL_TOOL_YOU_CANT_ADD_YOURSELF));
                                    return;
                                }
                                if (containsUUID(config.getSharedPlayers(), uuid)) {
                                    p.sendMessage(ChatPrefix.WIDM + LanguageManager.getInstance().getLanguageString(LanguageText.MAP_VUL_TOOL_PLAYER_ALREADY_ADDED));
                                    return;
                                }
                                config.getSharedPlayers().add(uuid);
                                p.sendMessage(ChatPrefix.WIDM + LanguageManager.getInstance().getLanguageString(LanguageText.MAP_VUL_TOOL_PLAYER_ADDED, Collections.singletonList(offlinePlayer.getName())));
                            });
                        }
                )
                .build());
    }

    private boolean containsUUID(List<UUID> uuidList, UUID uuid) {
        for (UUID listUUID : uuidList) {
            if (listUUID.equals(uuid)) {
                return true;
            }
        }
        return false;
    }

    private void changeSpawn(Player p, Map map) {
        File settings = new File(map.getDirectory(), "worldsettings.yml");
        if (settings.exists()) {
            FileConfiguration fc = YamlConfiguration.loadConfiguration(settings);
            Location l = p.getLocation();
            ConfigurationSection section = fc.createSection("spawn");
            section.set("x", l.getBlockX());
            section.set("y", l.getBlockY());
            section.set("z", l.getBlockZ());
            try {
                fc.save(settings);
                p.sendMessage(LanguageManager.getInstance().getLanguageString(LanguageText.MAP_VUL_TOOL_SPAWN_CHANGED));
            } catch (IOException ex) {
                Logger.logMessage(LogLevel.ERROR, Prefix.CONFIG_FILES, "Could not save file. (" + settings.getAbsolutePath() + ")");
                ex.printStackTrace();
            }
            p.closeInventory();
        } else {
            Logger.logMessage(LogLevel.ERROR, Prefix.CONFIG_FILES, "Could not find file. (" + settings.getAbsolutePath() + ")");
        }
    }

    private void deleteMap(Player p, Map map, MxInventory mainInv) {
        if (!map.getMapConfig().getOwner().equals(p.getUniqueId()) && !p.hasPermission(Permissions.MAP_DELETE_OTHERS.getPermission())) {
            p.sendMessage(ChatPrefix.WIDM + LanguageManager.getInstance().getLanguageString(LanguageText.MAP_VUL_TOOL_NOT_OWNER_OF_MAP));
            return;
        }
        MxInventoryManager.getInstance().addAndOpenInventory(p, MxDefaultMenuBuilder.create(ChatColor.GRAY + "Vul-Tool Verwijder map", MxInventorySlots.THREE_ROWS)
                .setPrevious(mainInv)
                .setItem(MxDefaultItemStackBuilder.create(Material.GREEN_WOOL)
                                .setName(ChatColor.GREEN + "Behoud map")
                                .build(),
                        11,
                        (mxInv, e) -> {
                            p.closeInventory();
                        }
                )
                .setItem(MxDefaultItemStackBuilder.create(Material.RED_WOOL)
                                .setName(ChatColor.RED + "Verwijder map")
                                .build(),
                        15,
                        (mxInv, e) -> {
                            map.getMxWorld().ifPresent(mxWorld -> {
                                World w = Bukkit.getWorld(mxWorld.getWorldUID());
                                if (w == null) {
                                    return;
                                }
                                w.getPlayers().forEach(player -> {
                                    p.teleport(Functions.getSpawnLocation());
                                    p.sendMessage(ChatPrefix.WIDM + LanguageManager.getInstance().getLanguageString(LanguageText.MAP_TELEPORTED_BECAUSE_WORLD_DELETED));
                                });
                                map.delete();
                            });
                        }
                ).build());
    }

    private void openColorsMenu(Player p, MxInventory mainInv, Map map) {
        ArrayList<Pair<ItemStack, MxItemClicked>> list = new ArrayList<>();
        MapConfig config = map.getMapConfig();
        config.getColors().forEach(mapPlayer -> {
            Colors color = mapPlayer.getColor();
            list.add(new Pair<>(
                    getColor(mapPlayer, color),
                    (mxInv, e) -> {
                        openSpecificColorMenu(p, mapPlayer, map, mainInv, mxInv);
                    }
            ));
        });

        MxInventoryManager.getInstance().addAndOpenInventory(p, MxListInventoryBuilder.create(ChatColor.GRAY + "Kleuren", MxInventorySlots.THREE_ROWS)
                .setAvailableSlots(MxInventoryIndex.ROW_ONE_TO_TWO)
                .setPrevious(mainInv)
                .setListItems(list)
                .setItem(MxSkullItemStackBuilder.create(1)
                                .setSkinFromHeadsData("wooden-plus")
                                .setName(ChatColor.GRAY + "Voeg een kleur toe")
                                .addBlankLore()
                                .addLore(ChatColor.YELLOW + "Klik hier om een kleur toe te voegen.")
                                .build(),
                        25,
                        (mxInv, e) -> {
                            // TODO add color
                            addColorMenu(p, map, mainInv);
                        }
                )
                .build()
        );
    }

    private ItemStack getColor(MapPlayer mapPlayer, Colors color) {
        return MxSkullItemStackBuilder.create(1)
                .setSkinFromHeadsData(color.getHeadKey())
                .setName(color.getDisplayName())
                .addBlankLore()
                .addLore(ChatColor.GRAY + "Rol: " + mapPlayer.getRoleDisplayString())
                .addBlankLore()
                .addLore(ChatColor.YELLOW + "Klik hier om de kleur aan te passen.")
                .build();
    }


    private void addColorMenu(Player p, Map map, MxInventory mainInv) {
        ArrayList<Pair<ItemStack, MxItemClicked>> list = new ArrayList<>();
        ArrayList<Colors> existingColors = new ArrayList<>();
        MapConfig config = map.getMapConfig();
        config.getColors().forEach(mapPlayer -> {
            existingColors.add(mapPlayer.getColor());
        });

        for (Colors color : Colors.values()) {
            if (existingColors.contains(color))
                continue;
            list.add(new Pair<>(
                    MxSkullItemStackBuilder.create(1)
                            .setSkinFromHeadsData(color.getHeadKey())
                            .setName(color.getDisplayName())
                            .addBlankLore()
                            .addLore(ChatColor.YELLOW + "Klik hier om de kleur toe te voegen.")
                            .build(),
                    (mxInv, e) -> {
                        MapPlayer mapPlayer = new MapPlayer(color, MxLocation.getFromLocation(p.getLocation()));
                        config.getColors().add(mapPlayer);
                        p.sendMessage(ChatPrefix.WIDM + LanguageManager.getInstance().getLanguageString(LanguageText.MAP_COLOR_ADDED, Collections.singletonList(color.getDisplayName())));
                        openColorsMenu(p, mainInv, map);
                    }
            ));
        }
        MxInventoryManager.getInstance().addAndOpenInventory(p, MxListInventoryBuilder.create(ChatColor.GRAY + "Kleur toevoegen", MxInventorySlots.THREE_ROWS)
                .setAvailableSlots(MxInventoryIndex.ROW_ONE_TO_TWO)
                .setPrevious(mainInv)
                .setListItems(list)
                .build()
        );
    }

    private void openSpecificColorMenu(Player p, MapPlayer mapPlayer, Map map, MxInventory mainInv, MxInventory colorInv) {
        Colors color = mapPlayer.getColor();
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        MxInventoryManager.getInstance().addAndOpenInventory(p, MxDefaultMenuBuilder.create(ChatColor.GRAY + "Kleur " + mapPlayer.getColor().getDisplayName(), MxInventorySlots.THREE_ROWS)
                .setPrevious(mainInv)
                .setPreviousItemStackSlot(21)
                .setItem(MxSkullItemStackBuilder.create(1)
                                .setSkinFromHeadsData(color.getHeadKey())
                                .setName(color.getDisplayName())
                                .addBlankLore()
                                .addLore(ChatColor.GRAY + "Rol: " + mapPlayer.getRoleDisplayString())
                                .build(),
                        23,
                        (mxInv, e) -> {
                        })
                .setItem(MxDefaultItemStackBuilder.create(Material.ENDER_PEARL)
                                .setName(ChatColor.GRAY + "Verander spawnpoint")
                                .addBlankLore()
                                .addLore(ChatColor.GRAY + "Status: " + decimalFormat.format(mapPlayer.getLocation().getX()) + ", " + decimalFormat.format(mapPlayer.getLocation().getY()) + ", " + decimalFormat.format(mapPlayer.getLocation().getZ()))
                                .addBlankLore()
                                .addLore(ChatColor.YELLOW + "Klik hier om de spawnpoint van de kleur naar je huidige locatie te zetten.")
                                .build(),
                        10,
                        (mxInv, e) -> {
                            mapPlayer.setLocation(MxLocation.getFromLocation(p.getLocation()));
                            p.sendMessage(ChatPrefix.WIDM + LanguageManager.getInstance().getLanguageString(LanguageText.MAP_COLOR_SPAWN_CHANGED_TO_CURRENT));
                            p.closeInventory();
                        })
                .setItem(MxSkullItemStackBuilder.create(1)
                                .setSkinFromHeadsData("red-minus")
                                .setName(ChatColor.RED + "Verwijder kleur")
                                .addBlankLore()
                                .addLore(ChatColor.YELLOW + "Klik hier om de kleur te verwijderen")
                                .build(),
                        18,
                        (mxInv, e) -> {
                            map.getMapConfig().getColors().remove(mapPlayer);
                            openColorsMenu(p, colorInv, map);
                        })
                .setItem(MxDefaultItemStackBuilder.create(mapPlayer.getColor().getShulkerBlock())
                                .setName(ChatColor.GRAY + "Bekijk shulkers")
                                .addBlankLore()
                                .addLore(ChatColor.YELLOW + "Klik hier om de shulkers van " + mapPlayer.getColor().getDisplayName() + ChatColor.YELLOW + " te bekijken.")
                                .build(),
                        16,
                        (mxInv, e) -> {
                            ArrayList<Pair<ItemStack, MxItemClicked>> list = new ArrayList<>();
                            map.getShulkerManager().getShulkers().forEach(shulkerInformation -> {
                                if (shulkerInformation.getMaterial() == mapPlayer.getColor().getShulkerBlock()) {
                                    list.add(new Pair<>(
                                            MxDefaultItemStackBuilder.create(shulkerInformation.getMaterial())
                                                    .setName(ChatColor.GRAY + shulkerInformation.getName())
                                                    .addBlankLore()
                                                    .addLore(ChatColor.GRAY + "Location: " + shulkerInformation.getLocation().getX() + " " + shulkerInformation.getLocation().getY() + " " + shulkerInformation.getLocation().getZ())
                                                    .addBlankLore()
                                                    .addLore(ChatColor.YELLOW + "Klik om de shulker op afstand te openen.")
                                                    .build(),
                                            (mxInv1, e12) -> {
                                                if (map.getMxWorld().isEmpty()) {
                                                    return;
                                                }
                                                World w = Bukkit.getWorld(map.getMxWorld().get().getWorldUID());
                                                Location loc = shulkerInformation.getLocation().getLocation(w);
                                                Block block = loc.getBlock();
                                                if (block.getState() instanceof ShulkerBox shulkerBox) {
                                                    p.openInventory(shulkerBox.getInventory());
                                                } else {
                                                    p.sendMessage(ChatPrefix.WIDM + LanguageManager.getInstance().getLanguageString(LanguageText.MAP_SHULKER_IS_NOT_A_SHULKER));
                                                }
                                            }
                                    ));
                                }
                            });
                            MxInventoryManager.getInstance().addAndOpenInventory(p, MxListInventoryBuilder.create(ChatColor.GRAY + " Shulkers - " + mapPlayer.getColor().getDisplayName(), MxInventorySlots.THREE_ROWS)
                                    .setPrevious(mxInv)
                                    .setAvailableSlots(MxInventoryIndex.ROW_ONE_TO_TWO)
                                    .setListItems(list)
                                    .build());
                        })
                .setItem(MxDefaultItemStackBuilder.create(Material.BOOK)
                                .setName(ChatColor.GRAY + "Verander Rol")
                                .addBlankLore()
                                .addLore(ChatColor.GRAY + "Status: " + mapPlayer.getRoleDisplayString())
                                .addBlankLore()
                                .addLore(ChatColor.YELLOW + "Klik hier om de rol aan te passen.")
                                .build(),
                        12,
                        (mxInv, e) -> {
                            MxInventoryManager.getInstance().addAndOpenInventory(p, MxDefaultMenuBuilder.create(ChatColor.GRAY + "Rol aannpassen " + mapPlayer.getColor().getDisplayName(), MxInventorySlots.THREE_ROWS)
                                    .setPrevious(mxInv)
                                    .setItem(getItemForRole(Role.SPELER), 11, getClickForRole(p, mapPlayer, Role.SPELER, colorInv))
                                    .setItem(getItemForRole(Role.MOL), 13, getClickForRole(p, mapPlayer, Role.MOL, colorInv))
                                    .setItem(getItemForRole(Role.EGO), 15, getClickForRole(p, mapPlayer, Role.EGO, colorInv))
                                    .setItem(getItemForRole(Role.SHAPESHIFTER), 17, getClickForRole(p, mapPlayer, Role.SHAPESHIFTER, colorInv))
                                    .build());
                        })
                .setItem(MxDefaultItemStackBuilder.create(Material.DIAMOND_SWORD)
                                .setName(ChatColor.GRAY + "Toggle peacekeeper")
                                .addBlankLore()
                                .addLore(ChatColor.GRAY + "Status: " + (mapPlayer.isPeacekeeper() ? "Is Peacekeeper" : "Is geen Peacekeeper"))
                                .addBlankLore()
                                .addLore(ChatColor.YELLOW + "Klik hier om peacekeeper te togglen.")
                                .build(),
                        14,
                        (mxInv, e) -> {
                            mapPlayer.setPeacekeeper(!mapPlayer.isPeacekeeper());
                            if (mapPlayer.isPeacekeeper()) {
                                p.sendMessage(ChatPrefix.WIDM + LanguageManager.getInstance().getLanguageString(LanguageText.MAP_COLOR_IS_NOW_PEACEKEEPER, Collections.singletonList(color.getDisplayName())));
                            } else {
                                p.sendMessage(ChatPrefix.WIDM + LanguageManager.getInstance().getLanguageString(LanguageText.MAP_COLOR_IS_NOT_PEACEKEEPER, Collections.singletonList(color.getDisplayName())));
                            }
                            for (MapPlayer mapPlayer1 : map.getMapConfig().getColors()) {
                                if (mapPlayer1.getColor() == mapPlayer.getColor()) {
                                    continue;
                                }
                                if (mapPlayer1.isPeacekeeper()) {
                                    mapPlayer1.setPeacekeeper(!mapPlayer1.isPeacekeeper());
                                    p.sendMessage(ChatPrefix.WIDM + LanguageManager.getInstance().getLanguageString(LanguageText.MAP_COLOR_IS_NOT_PEACEKEEPER, Collections.singletonList(mapPlayer1.getColor().getDisplayName())));
                                }
                            }
                            openSpecificColorMenu(p, mapPlayer, map, mainInv, colorInv);
                        })

                .build());
    }

    private ItemStack getItemForRole(Role role) {
        return MxSkullItemStackBuilder.create(1)
                .setSkinFromHeadsData(role.getHeadKey())
                .setName(role.getRolName())
                .addBlankLore()
                .addLore(ChatColor.YELLOW + "Klik om de rol te veranderen naar " + role.getRolName() + ChatColor.YELLOW + ".")
                .build();
    }

    private MxItemClicked getClickForRole(Player p, MapPlayer player, Role role, MxInventory colorInv) {
        return (mxInv, e) -> {
            player.setRole(role);
            @Nullable ItemStack @NotNull [] contents = colorInv.getInv().getContents();
            for (int i = 0; i < contents.length; i++) {
                ItemStack content = contents[i];
                if (content == null || !content.hasItemMeta() || !content.getItemMeta().hasDisplayName())
                    continue;
                if (Functions.convertComponentToString(content.displayName()).contains((player.getColor().getDisplayNameWithoutColor()))) {
                    colorInv.getInv().setItem(i, getColor(player, player.getColor()));
                }
            }
            MxInventoryManager.getInstance().addAndOpenInventory(p, colorInv);
            p.sendMessage(ChatPrefix.WIDM + LanguageManager.getInstance().getLanguageString(LanguageText.MAP_ROLE_CHANGED, new ArrayList<>(Arrays.asList(player.getColor().getDisplayName(), role.getRolName()))));
        };
    }
}
