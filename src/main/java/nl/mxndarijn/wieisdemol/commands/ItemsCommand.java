package nl.mxndarijn.wieisdemol.commands;

import net.kyori.adventure.text.Component;
import nl.mxndarijn.api.chatinput.MxChatInputManager;
import nl.mxndarijn.api.inventory.*;
import nl.mxndarijn.api.inventory.heads.MxHeadManager;
import nl.mxndarijn.api.inventory.heads.MxHeadSection;
import nl.mxndarijn.api.inventory.menu.MxDefaultInventoryBuilder;
import nl.mxndarijn.api.inventory.menu.MxListInventoryBuilder;
import nl.mxndarijn.api.item.MxDefaultItemStackBuilder;
import nl.mxndarijn.api.item.MxSkullItemStackBuilder;
import nl.mxndarijn.api.item.Pair;
import nl.mxndarijn.api.mxcommand.MxCommand;
import nl.mxndarijn.wieisdemol.WieIsDeMol;
import nl.mxndarijn.wieisdemol.data.ChatPrefix;
import nl.mxndarijn.wieisdemol.data.Permissions;
import nl.mxndarijn.wieisdemol.data.SpecialDirectories;
import nl.mxndarijn.wieisdemol.game.Game;
import nl.mxndarijn.wieisdemol.items.util.storage.StorageContainer;
import nl.mxndarijn.wieisdemol.items.util.storage.StorageManager;
import nl.mxndarijn.wieisdemol.managers.MapManager;
import nl.mxndarijn.wieisdemol.managers.PresetsManager;
import nl.mxndarijn.wieisdemol.managers.language.LanguageManager;
import nl.mxndarijn.wieisdemol.managers.language.LanguageText;
import nl.mxndarijn.wieisdemol.managers.world.GameWorldManager;
import nl.mxndarijn.wieisdemol.map.Map;
import nl.mxndarijn.wieisdemol.presets.Preset;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ItemsCommand extends MxCommand {
    private final StorageManager manager = StorageManager.getInstance();


    public ItemsCommand() {
        super(Permissions.COMMAND_MAPS_ITEMS, true, false, p -> {
            UUID worldUID = p.getWorld().getUID();
            Optional<Map> map = MapManager.getInstance().getMapByWorldUID(worldUID);
            Optional<Game> game = GameWorldManager.getInstance().getGameByWorldUID(worldUID);
            Optional<Preset> preset = PresetsManager.getInstance().getPresetByWorldUID(worldUID);
            //TODO Add Games
            return map.isPresent() || preset.isPresent() || game.isPresent();
        });
    }

    @Override
    public void execute(CommandSender sender, Command command, String label, String[] args) {
        Player p = (Player) sender;

        Optional<Game> game = GameWorldManager.getInstance().getGameByWorldUID(p.getWorld().getUID());
        if (game.isPresent()) {
            if (!game.get().getHosts().contains(p.getUniqueId()))
                return;
        }

        MxInventoryManager.getInstance().addAndOpenInventory(p, MxDefaultInventoryBuilder.create(ChatColor.GRAY + "Items", MxInventorySlots.THREE_ROWS)
                .setItem(MxDefaultItemStackBuilder.create(Material.ENDER_CHEST)
                                .setName(ChatColor.GRAY + "Server Opslag")
                                .addBlankLore()
                                .addLore(ChatColor.YELLOW + "Klik hier op de server opslagen te bekijken.")
                                .build(),
                        10,
                        (mxInv, e) -> {
                            openServerStorages(p, mxInv);
                        })
                .setItem(MxDefaultItemStackBuilder.create(Material.CHEST)
                                .setName(ChatColor.GRAY + "Eigen Opslag")
                                .addBlankLore()
                                .addLore(ChatColor.YELLOW + "Klik hier op je eigen opslagen te bekijken.")
                                .build(),
                        13,
                        (mxInv, e) -> {
                            openPlayerStorages(p, mxInv);
                        })
                .setItem(MxDefaultItemStackBuilder.create(Material.BOOKSHELF)
                                .setName(ChatColor.GRAY + "Publieke Opslag")
                                .addBlankLore()
                                .addLore(ChatColor.YELLOW + "Klik hier om alle publieke opslagen te bekijken.")
                                .build(),
                        16,
                        (mxInv, e) -> {
                            openPublicStorages(p, mxInv);
                        })

                .build());
    }

    private void openServerStorages(Player p, MxInventory mainInv) {
        ArrayList<Pair<ItemStack, MxItemClicked>> containers = new ArrayList<>();
        manager.getServerContainers().forEach(container -> {
            containers.add(new Pair<>(
                    container.getItemStack(),
                    (mxInv, e) -> {
                        openContainer(p, container, mainInv);
                    }
            ));
        });

        MxListInventoryBuilder builder = MxListInventoryBuilder.create("Server Opslagen", MxInventorySlots.THREE_ROWS)
                .setAvailableSlots(MxInventoryIndex.ROW_ONE_TO_TWO)
                .setListItems(containers)
                .setPrevious(mainInv);

        if (p.hasPermission(Permissions.ITEM_ITEMS_EDIT_SERVER_CONTAINERS.getPermission())) {
            builder.setItem(MxSkullItemStackBuilder.create(1)
                            .setSkinFromHeadsData("wooden-plus")
                            .setName(ChatColor.GRAY + "Voeg opslag toe")
                            .addBlankLore()
                            .addLore(ChatColor.YELLOW + "Klik hier om een opslag toe te voegen")
                            .build(),
                    25,
                    (mxInv, e) -> {
                        p.closeInventory();
                        p.sendMessage(ChatPrefix.WIDM + LanguageManager.getInstance().getLanguageString(LanguageText.COMMAND_ITEMS_ENTER_NAME_FOR_CONTAINER));
                        MxChatInputManager.getInstance().addChatInputCallback(p.getUniqueId(), message -> {
                            MxHeadManager mxHeadManager = MxHeadManager.getInstance();
                            ArrayList<Pair<ItemStack, MxItemClicked>> list = new ArrayList<>();
                            MxItemClicked clicked = (mxInv1, e1) -> {
                                ItemStack is = e1.getCurrentItem();
                                ItemMeta im = is.getItemMeta();
                                PersistentDataContainer container = im.getPersistentDataContainer();
                                String key = container.get(new NamespacedKey(JavaPlugin.getPlugin(WieIsDeMol.class), "skull_key"), PersistentDataType.STRING);

                                Optional<MxHeadSection> section = MxHeadManager.getInstance().getHeadSection(key);
                                if (section.isPresent()) {
                                    StorageContainer newContainer = new StorageContainer(message, section.get().getKey(), "server", false, new File(SpecialDirectories.STORAGE_FILES.getDirectory() + File.separator + "server" + File.separator + UUID.randomUUID() + ".yml"));
                                    StorageManager.getInstance().addServerContainer(newContainer);
                                    p.sendMessage(ChatPrefix.WIDM + LanguageManager.getInstance().getLanguageString(LanguageText.COMMAND_ITEMS_CONTAINER_ADDED));
                                    p.closeInventory();
                                    openContainer(p, newContainer, mainInv);
                                }

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
                                                    .addLore(ChatColor.YELLOW + "Klik op een skull om dat het logo te maken van de opslag.")
                                                    .build(), 48, null)
                                            .build());
                        });
                    }
            );
        }

        MxInventoryManager.getInstance().addAndOpenInventory(p, builder
                .build());
    }

    private void openPlayerStorages(Player p, MxInventory mainInv) {
        ArrayList<Pair<ItemStack, MxItemClicked>> containers = new ArrayList<>();
        manager.getPlayerContainers(p.getUniqueId().toString()).forEach(container -> {
            containers.add(new Pair<>(
                    container.getItemStack(),
                    (mxInv, e) -> {
                        openContainer(p, container, mainInv);
                    }
            ));
        });

        MxInventoryManager.getInstance().addAndOpenInventory(p, MxListInventoryBuilder.create("Prive Opslagen", MxInventorySlots.THREE_ROWS)
                .setAvailableSlots(MxInventoryIndex.ROW_ONE_TO_TWO)
                .setPrevious(mainInv)
                .setListItems(containers)
                .setItem(MxSkullItemStackBuilder.create(1)
                                .setSkinFromHeadsData("wooden-plus")
                                .setName(ChatColor.GRAY + "Voeg opslag toe")
                                .addBlankLore()
                                .addLore(ChatColor.YELLOW + "Klik hier om een opslag toe te voegen")
                                .build(),
                        25,
                        (mxInv, e) -> {
                            p.closeInventory();
                            p.sendMessage(ChatPrefix.WIDM + LanguageManager.getInstance().getLanguageString(LanguageText.COMMAND_ITEMS_ENTER_NAME_FOR_CONTAINER));
                            MxChatInputManager.getInstance().addChatInputCallback(p.getUniqueId(), message -> {
                                MxHeadManager mxHeadManager = MxHeadManager.getInstance();
                                ArrayList<Pair<ItemStack, MxItemClicked>> list = new ArrayList<>();
                                MxItemClicked clicked = (mxInv1, e1) -> {
                                    ItemStack is = e1.getCurrentItem();
                                    ItemMeta im = is.getItemMeta();
                                    PersistentDataContainer container = im.getPersistentDataContainer();
                                    String key = container.get(new NamespacedKey(JavaPlugin.getPlugin(WieIsDeMol.class), "skull_key"), PersistentDataType.STRING);

                                    Optional<MxHeadSection> section = MxHeadManager.getInstance().getHeadSection(key);
                                    if (section.isPresent()) {
                                        StorageContainer newContainer = new StorageContainer(message, section.get().getKey(), p.getUniqueId().toString(), false, new File(SpecialDirectories.STORAGE_FILES.getDirectory() + File.separator + p.getUniqueId() + File.separator + UUID.randomUUID() + ".yml"));
                                        StorageManager.getInstance().addPlayerContainer(p.getUniqueId().toString(), newContainer);
                                        p.sendMessage(ChatPrefix.WIDM + LanguageManager.getInstance().getLanguageString(LanguageText.COMMAND_ITEMS_CONTAINER_ADDED));
                                        p.closeInventory();
                                        openContainer(p, newContainer, mainInv);
                                    }

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
                                                        .addLore(ChatColor.YELLOW + "Klik op een skull om dat het logo te maken van de opslag.")
                                                        .build(), 48, null)
                                                .build());
                            });
                        }
                )
                .build());
    }

    private void openPublicStorages(Player p, MxInventory mainInv) {
        ArrayList<Pair<ItemStack, MxItemClicked>> containers = new ArrayList<>();
        manager.getPublicContainers().forEach(container -> {
            containers.add(new Pair<>(
                    container.getItemStack(),
                    (mxInv, e) -> {
                        openContainer(p, container, mainInv);
                    }
            ));
        });

        MxInventoryManager.getInstance().addAndOpenInventory(p, MxListInventoryBuilder.create("Publieke opslagen", MxInventorySlots.THREE_ROWS)
                .setAvailableSlots(MxInventoryIndex.ROW_ONE_TO_TWO)
                .setListItems(containers)
                .setPrevious(mainInv)
                .build());
    }

    private void openContainer(Player p, StorageContainer container, MxInventory mainInv) {
        ArrayList<Pair<ItemStack, MxItemClicked>> items = new ArrayList<>();
        container.getContents().forEach(item -> {
            ItemStack is = item.clone();
            ItemMeta im = is.getItemMeta();
            List<Component> lores = im.hasLore() ? im.lore() : new ArrayList<>();
            if (lores == null)
                lores = new ArrayList<>();
            lores.add(Component.text(" "));
            lores.add(Component.text(ChatColor.YELLOW + "Linker-muisknop om het item in je inventory te krijgen"));
            if (container.hasPermissionToEdit(p)) {
                lores.add(Component.text(ChatColor.YELLOW + "Shift + Rechter-muisknop om het item te verwijderen"));
            }

            im.lore(lores);
            is.setItemMeta(im);
            items.add(new Pair<>(is, (mxInv, e) -> {
                if (e.isLeftClick() && !e.isShiftClick()) {
                    p.getInventory().addItem(item);
                }

                if (e.isRightClick() && e.isShiftClick()) {
                    container.getContents().remove(item);
                    openContainer(p, container, mainInv);
                }
            }));
        });
        MxListInventoryBuilder builder = MxListInventoryBuilder.create(ChatColor.GRAY + container.getName(), MxInventorySlots.SIX_ROWS)
                .setAvailableSlots(MxInventoryIndex.ROW_ONE_TO_FIVE)
                .setListItems(items);
        if (container.hasPermissionToEdit(p)) {
            builder.setItem(MxSkullItemStackBuilder.create(1)
                                    .setSkinFromHeadsData("wooden-plus")
                                    .setName(ChatColor.GRAY + "Voeg items toe")
                                    .addBlankLore()
                                    .addLore(ChatColor.YELLOW + "Klik om items aan de opslag toe te voegen.")
                                    .build(),
                            52, (mxInv, e) -> {
                                p.closeInventory();
                                MxInventoryManager.getInstance().addAndOpenInventory(p, MxDefaultInventoryBuilder.create(ChatColor.GRAY + "Voeg Items toe", MxInventorySlots.SIX_ROWS)
                                        .defaultCancelEvent(false)
                                        .setOnInventoryCloseEvent((p1, inv, e1) -> {
                                            for (ItemStack content : inv.getInv().getContents()) {
                                                if (content != null) {
                                                    container.getContents().add(content);
                                                }
                                            }
                                            p.sendMessage(ChatPrefix.WIDM + LanguageManager.getInstance().getLanguageString(LanguageText.COMMAND_ITEMS_ADDED_NEW_ITEMS));
                                            openContainer(p, container, mainInv);
                                        })
                                        .build());
                            }
                    )
            ;
            if (!container.getOwner().equalsIgnoreCase("server")) {
                builder.setItem(MxSkullItemStackBuilder.create(1)
                                .setSkinFromHeadsData("minecraft-world")
                                .setName(ChatColor.GRAY + "Toggle openbaar")
                                .addBlankLore()
                                .addLore(ChatColor.GRAY + "Status: " + (container.isPublic() ? "Openbaar" : "Prive"))
                                .addBlankLore()
                                .addLore(ChatColor.YELLOW + (container.isPublic() ? "Klik hier om de opslag prive te maken" : "Klik hier om de opslag openbaar te maken"))
                                .build(),
                        51, (mxInv, e) -> {
                            container.setPublic(!container.isPublic());
                            openContainer(p, container, mainInv);
                        }
                );
            }
        }

        MxInventoryManager.getInstance().addAndOpenInventory(p,

                builder.setPrevious(mainInv).setPreviousItemStackSlot(49).build()
        );
    }
}
