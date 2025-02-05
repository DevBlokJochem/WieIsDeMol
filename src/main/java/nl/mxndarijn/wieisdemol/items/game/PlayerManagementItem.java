package nl.mxndarijn.wieisdemol.items.game;

import nl.mxndarijn.api.chatinput.MxChatInputManager;
import nl.mxndarijn.api.inventory.*;
import nl.mxndarijn.api.inventory.menu.MxDefaultMenuBuilder;
import nl.mxndarijn.api.inventory.menu.MxListInventoryBuilder;
import nl.mxndarijn.api.item.MxDefaultItemStackBuilder;
import nl.mxndarijn.api.item.MxSkullItemStackBuilder;
import nl.mxndarijn.api.item.Pair;
import nl.mxndarijn.api.mxitem.MxItem;
import nl.mxndarijn.api.util.MxWorldFilter;
import nl.mxndarijn.wieisdemol.data.ChatPrefix;
import nl.mxndarijn.wieisdemol.data.Colors;
import nl.mxndarijn.wieisdemol.data.Role;
import nl.mxndarijn.wieisdemol.game.Game;
import nl.mxndarijn.wieisdemol.game.GamePlayer;
import nl.mxndarijn.wieisdemol.items.Items;
import nl.mxndarijn.wieisdemol.managers.GameManager;
import nl.mxndarijn.wieisdemol.managers.language.LanguageManager;
import nl.mxndarijn.wieisdemol.managers.language.LanguageText;
import nl.mxndarijn.wieisdemol.managers.world.GameWorldManager;
import nl.mxndarijn.wieisdemol.map.MapConfig;
import nl.mxndarijn.wieisdemol.map.mapplayer.MapPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;


public class PlayerManagementItem extends MxItem {

    public PlayerManagementItem(ItemStack is, MxWorldFilter worldFilter, boolean gameItem, Action... actions) {
        super(is, worldFilter, gameItem, actions);
    }

    @Override
    public void execute(Player p, PlayerInteractEvent e) {
        Optional<Game> optionalGame = GameWorldManager.getInstance().getGameByWorldUID(p.getWorld().getUID());

        if (optionalGame.isEmpty())
            return;
        Game game = optionalGame.get();

        if (!game.getHosts().contains(p.getUniqueId()))
            return;

        ArrayList<Pair<ItemStack, MxItemClicked>> list = new ArrayList<>();
        game.getColors().forEach(((gamePlayer) -> {
            MapPlayer mapPlayer = gamePlayer.getMapPlayer();
            list.add(new Pair<>(
                    MxSkullItemStackBuilder.create(1)
                            .setSkinFromHeadsData(gamePlayer.getMapPlayer().getColor().getHeadKey())
                            .setName(gamePlayer.getPlayer().isPresent() && Bukkit.getPlayer(gamePlayer.getPlayer().get()) != null ? ChatColor.GRAY + Bukkit.getPlayer(gamePlayer.getPlayer().get()).getName() : ChatColor.GRAY + "Geen speler toegewezen")
                            .addBlankLore()
                            .addLore(ChatColor.GRAY + "Kleur: " + gamePlayer.getMapPlayer().getColor().getDisplayName())
                            .addLore(ChatColor.GRAY + "Rol: " + gamePlayer.getMapPlayer().getRoleDisplayString())
                            .addBlankLore()
                            .addLore(ChatColor.YELLOW + "Klik om deze kleur aan te passen.")
                            .build(),
                    (mxInv, e1) -> {
                        MxInventoryManager.getInstance().addAndOpenInventory(p, MxDefaultMenuBuilder.create(ChatColor.GRAY + "Beheer kleur: " + gamePlayer.getMapPlayer().getColor().getDisplayName(), MxInventorySlots.THREE_ROWS)
                                .setItem(MxDefaultItemStackBuilder.create(Material.BOOK)
                                                .setName(ChatColor.GRAY + "Wijzig rol")
                                                .addBlankLore()
                                                .addLore(ChatColor.GRAY + "Huidig: " + gamePlayer.getMapPlayer().getRoleDisplayString())
                                                .addBlankLore()
                                                .addLore(ChatColor.YELLOW + "Klik hier om de rol aan te passen.")
                                                .build(),
                                        10,
                                        (mxInv1, e2) -> {
                                            MxInventoryManager.getInstance().addAndOpenInventory(p, MxDefaultMenuBuilder.create(ChatColor.GRAY + "Rol aannpassen " + mapPlayer.getColor().getDisplayName(), MxInventorySlots.THREE_ROWS)
                                                    .setPrevious(mxInv)
                                                    .setItem(getItemForRole(Role.SPELER), 11, getClickForRole(p, mapPlayer, Role.SPELER, mxInv1))
                                                    .setItem(getItemForRole(Role.MOL), 13, getClickForRole(p, mapPlayer, Role.MOL, mxInv1))
                                                    .setItem(getItemForRole(Role.EGO), 15, getClickForRole(p, mapPlayer, Role.EGO, mxInv1))
                                                    .setItem(MxDefaultItemStackBuilder.create(Material.DIAMOND_SWORD)
                                                            .setName(ChatColor.GRAY + "Toggle peacekeeper")
                                                            .addBlankLore()
                                                            .addLore(ChatColor.GRAY + "Status: " + (mapPlayer.isPeacekeeper() ? "Is Peacekeeper" : "Is geen Peacekeeper"))
                                                            .addBlankLore()
                                                            .addLore(ChatColor.YELLOW + "Klik hier om peacekeeper te togglen.")
                                                            .build(), 22, getClickForPeacekeeper(p, mapPlayer, Role.EGO, mxInv1, game.getConfig()))
                                                    .build());

                                        })
                                .setItem(MxDefaultItemStackBuilder.create(Material.SKELETON_SKULL)
                                                .setName(ChatColor.GRAY + "Wijzig speler")
                                                .addBlankLore()
                                                .addLore(ChatColor.GRAY + "Huidig: " + (gamePlayer.getPlayer().isPresent() ? Bukkit.getPlayer(gamePlayer.getPlayer().get()).getName() : "Geen speler"))
                                                .addBlankLore()
                                                .addLore(ChatColor.YELLOW + "Klik hier om de speler aan te passen.")
                                                .build(),
                                        13,
                                        (mxInv1, e2) -> {
                                            List<Pair<ItemStack, MxItemClicked>> players = new ArrayList<>();
                                            AtomicInteger queueAmount = new AtomicInteger(1);
                                            game.getGameInfo().getQueue().forEach(playerUUID -> {
                                                if (playerUUID == null || Bukkit.getPlayer(playerUUID) == null)
                                                    return;
                                                players.add(new Pair<>(
                                                        MxSkullItemStackBuilder.create(1)
                                                                .setSkinFromHeadsData(playerUUID.toString())
                                                                .setName(ChatColor.GRAY + Bukkit.getPlayer(playerUUID).getName())
                                                                .addBlankLore()
                                                                .addLore(ChatColor.GRAY + "Nummer in wachtrij: " + queueAmount)
                                                                .addBlankLore()
                                                                .addLore(ChatColor.YELLOW + "Klik hier om deze speler te selecteren.")
                                                                .build(),
                                                        (mxInv22, e32) -> {
                                                            game.addPlayer(playerUUID, gamePlayer);
                                                            game.getGameInfo().getQueue().remove(p.getUniqueId());
                                                            p.closeInventory();
                                                        }
                                                ));
                                                queueAmount.getAndIncrement();
                                            });
                                            game.getSpectators().forEach(playerUUID -> {
                                                if (playerUUID == null || Bukkit.getPlayer(playerUUID) == null)
                                                    return;
                                                players.add(new Pair<>(
                                                        MxSkullItemStackBuilder.create(1)
                                                                .setSkinFromHeadsData(playerUUID.toString())
                                                                .setName(ChatColor.GRAY + Bukkit.getPlayer(playerUUID).getName())
                                                                .addBlankLore()
                                                                .addLore(ChatColor.GRAY + "Nummer in wachtrij: Spectator")
                                                                .addBlankLore()
                                                                .addLore(ChatColor.YELLOW + "Klik hier om deze speler te selecteren.")
                                                                .build(),
                                                        (mxInv22, e32) -> {
                                                            game.removeSpectator(playerUUID, false);
                                                            game.addPlayer(playerUUID, gamePlayer);
                                                            p.closeInventory();
                                                        }
                                                ));
                                            });
                                            MxInventoryManager.getInstance().addAndOpenInventory(p, new MxListInventoryBuilder(ChatColor.GRAY + "Kies speler (" + gamePlayer.getMapPlayer().getColor().getDisplayName() + ChatColor.GRAY + ")", MxInventorySlots.FOUR_ROWS)
                                                    .setAvailableSlots(MxInventoryIndex.ROW_ONE_TO_THREE)
                                                    .setPrevious(mxInv1)
                                                    .setListItems(players)
                                                    .setItem(MxDefaultItemStackBuilder.create(Material.NAME_TAG)
                                                                    .setName(ChatColor.GRAY + "Typ naam")
                                                                    .addBlankLore()
                                                                    .addLore(ChatColor.YELLOW + "Klik hier om de naam te typen ipv te selecteren.")
                                                                    .build(),
                                                            27,
                                                            (mxInv2, e3) -> {
                                                                p.closeInventory();
                                                                p.sendMessage(LanguageManager.getInstance().getLanguageString(LanguageText.GAME_ENTER_NAME, ChatPrefix.WIDM));
                                                                MxChatInputManager.getInstance().addChatInputCallback(p.getUniqueId(), message -> {
                                                                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                                                        Player enteredPlayer = Bukkit.getPlayer(message);
                                                                        if (enteredPlayer == null) {
                                                                            p.sendMessage(LanguageManager.getInstance().getLanguageString(LanguageText.GAME_ENTER_NAME_NOT_FOUND, ChatPrefix.WIDM));
                                                                            return;
                                                                        }
                                                                        if (!game.getGameInfo().getQueue().contains(enteredPlayer.getUniqueId())) {
                                                                            p.sendMessage(LanguageManager.getInstance().getLanguageString(LanguageText.GAME_ENTER_NAME_NOT_IN_QUEUE, ChatPrefix.WIDM));
                                                                            return;
                                                                        }
                                                                        game.addPlayer(enteredPlayer.getUniqueId(), gamePlayer);
                                                                    }, 1);
                                                                });

                                                            })
                                                    .setItem(MxDefaultItemStackBuilder.create(Material.SKELETON_SKULL)
                                                            .setName(ChatColor.GRAY + "Verwijder Speler")
                                                            .addBlankLore()
                                                            .addLore(ChatColor.YELLOW + "Verander de kleur naar niemand.")
                                                            .build(), 28, (mxInv23, e33) -> {
                                                        if (gamePlayer.getPlayer().isEmpty())
                                                            return;
                                                        game.removePlayer(gamePlayer.getPlayer().get());
                                                        p.closeInventory();
                                                        p.sendMessage(LanguageManager.getInstance().getLanguageString(LanguageText.GAME_COLOR_CLEARED, Collections.singletonList(gamePlayer.getMapPlayer().getColor().getDisplayName())));
                                                    })
                                                    .build());
                                        })
                                .setItem(MxDefaultItemStackBuilder.create(Material.GOLDEN_SWORD)
                                                .setName(ChatColor.GRAY + "Kill / Reborn")
                                                .addBlankLore()
                                                .addLore(ChatColor.GRAY + "Kill of reborn de speler.")
                                                .addBlankLore()
                                                .addLore(ChatColor.YELLOW + "Klik hier om de speler te killen / rebornen.")
                                                .build(),
                                        16,
                                        (mxInv1, e2) -> {
                                            gamePlayer.setAlive(!gamePlayer.isAlive());
                                            p.closeInventory();
                                            p.sendMessage(LanguageManager.getInstance().getLanguageString(LanguageText.GAME_PLAYERSTATE_CHANGED, Arrays.asList(gamePlayer.getMapPlayer().getColor().getDisplayName(), gamePlayer.isAlive() ? ChatColor.GREEN + "Levend" : ChatColor.RED + "Dood")));
                                            if (gamePlayer.getPlayer().isEmpty())
                                                return;
                                            Player player = Bukkit.getPlayer(gamePlayer.getPlayer().get());
                                            if (gamePlayer.isAlive()) {
                                                game.removeSpectator(player.getUniqueId(), false);
                                                player.getInventory().clear();
                                                player.teleport(p);
                                                player.getInventory().addItem(Items.GAME_PLAYER_TOOL.getItemStack());
                                                player.setAllowFlight(false);
                                            } else {
                                                player.setHealth(0);
                                            }
                                            //TODO
                                        })
                                .setItem(MxDefaultItemStackBuilder.create(Material.CHEST)
                                                .setName(ChatColor.GRAY + "Bekijk inventory")
                                                .addBlankLore()
                                                .addLore(ChatColor.YELLOW + "Klik hier om de inventory te bekijken.")
                                                .build(),
                                        24,
                                        (mxInv1, e2) -> {
                                            if (gamePlayer.getPlayer().isEmpty())
                                                return;
                                            Player player = Bukkit.getPlayer(gamePlayer.getPlayer().get());
                                            if (gamePlayer.isAlive()) {
                                            p.openInventory(player.getInventory());
                                            }
                                        })
                                .setItem(MxDefaultItemStackBuilder.create(Material.ENDER_PEARL)
                                                .setName(ChatColor.GRAY + "Teleporteer naar je toe")
                                                .addBlankLore()
                                                .addLore(ChatColor.YELLOW + "Klik hier om de speler naar je te tpen.")
                                                .build(),
                                        25,
                                        (mxInv1, e2) -> {
                                            if (gamePlayer.getPlayer().isEmpty())
                                                return;
                                            Player player = Bukkit.getPlayer(gamePlayer.getPlayer().get());
                                            if (gamePlayer.isAlive()) {
                                                player.teleport(p);
                                            }
                                        })
                                .setItem(MxDefaultItemStackBuilder.create(Material.ENDER_PEARL)
                                                .setName(ChatColor.GRAY + "Teleporteer naar speler")
                                                .addBlankLore()
                                                .addLore(ChatColor.YELLOW + "Klik hier om naar de speler te teleporten.")
                                                .build(),
                                        26,
                                        (mxInv1, e2) -> {
                                            if (gamePlayer.getPlayer().isEmpty())
                                                return;
                                            Player player = Bukkit.getPlayer(gamePlayer.getPlayer().get());
                                            if (gamePlayer.isAlive()) {
                                                p.teleport(player);
                                            }
                                        })
                                .setPrevious(mxInv)
                                .build()
                        );
                    }
            ));
        }));

        MxInventoryManager.getInstance().addAndOpenInventory(p, MxListInventoryBuilder.create(ChatColor.GRAY + "Spelers beheren", MxInventorySlots.THREE_ROWS)
                .setAvailableSlots(MxInventoryIndex.ROW_ONE_TO_TWO)
                .setListItems(list)
                .setShowPageNumbers(false)
                .setItem(MxSkullItemStackBuilder.create(1)
                                .setSkinFromHeadsData("command-block")
                                .setName(ChatColor.GRAY + "Automatisch Vullen")
                                .addBlankLore()
                                .addLore(ChatColor.YELLOW + "Klik hier om de game automatisch")
                                .addLore(ChatColor.YELLOW + "te vullen met mensen uit de queue")
                                .build(),
                        26,
                        (mxInv, e12) -> {
                            p.closeInventory();
                            List<GamePlayer> colors = new ArrayList<>(game.getColors());
                            Collections.shuffle(colors);

                            colors.forEach(gp -> {
                                if (gp.getPlayer().isPresent())
                                    return;
                                if (!game.getGameInfo().getQueue().isEmpty()) {
                                    game.addPlayer(game.getGameInfo().getQueue().get(0), gp);
                                }
                            });
                            game.sendMessageToAll(LanguageManager.getInstance().getLanguageString(LanguageText.GAME_AUTOMATICALLY_FILLED));

                        })
                .build()
        );
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
                if (content.getType().equals(Material.BOOK)) {
                    colorInv.getInv().setItem(i, getRolTypeItemStack(player));
                }
            }
            MxInventoryManager.getInstance().addAndOpenInventory(p, colorInv);
            p.sendMessage(ChatPrefix.WIDM + LanguageManager.getInstance().getLanguageString(LanguageText.MAP_ROLE_CHANGED, new ArrayList<>(Arrays.asList(player.getColor().getDisplayName(), role.getRolName()))));
        };
    }

    private MxItemClicked getClickForPeacekeeper(Player p, MapPlayer mapPlayer, Role role, MxInventory colorInv, MapConfig config) {
        return (mxInv, e) -> {
            Colors color = mapPlayer.getColor();
            mapPlayer.setPeacekeeper(!mapPlayer.isPeacekeeper());
            if (mapPlayer.isPeacekeeper()) {
                p.sendMessage(ChatPrefix.WIDM + LanguageManager.getInstance().getLanguageString(LanguageText.MAP_COLOR_IS_NOW_PEACEKEEPER, Collections.singletonList(color.getDisplayName())));
            } else {
                p.sendMessage(ChatPrefix.WIDM + LanguageManager.getInstance().getLanguageString(LanguageText.MAP_COLOR_IS_NOT_PEACEKEEPER, Collections.singletonList(color.getDisplayName())));
            }
            for (MapPlayer mapPlayer1 : config.getColors()) {
                if (mapPlayer1.getColor() == mapPlayer.getColor()) {
                    continue;
                }
                if (mapPlayer1.isPeacekeeper()) {
                    mapPlayer1.setPeacekeeper(!mapPlayer1.isPeacekeeper());
                    p.sendMessage(ChatPrefix.WIDM + LanguageManager.getInstance().getLanguageString(LanguageText.MAP_COLOR_IS_NOT_PEACEKEEPER, Collections.singletonList(mapPlayer1.getColor().getDisplayName())));
                }
            }
            @Nullable ItemStack @NotNull [] contents = colorInv.getInv().getContents();
            for (int i = 0; i < contents.length; i++) {
                ItemStack content = contents[i];
                if (content == null || !content.hasItemMeta() || !content.getItemMeta().hasDisplayName())
                    continue;
                if (content.getType().equals(Material.BOOK)) {
                    colorInv.getInv().setItem(i, getRolTypeItemStack(mapPlayer));
                }
            }
            MxInventoryManager.getInstance().addAndOpenInventory(p, colorInv);
        };
    }

    private ItemStack getRolTypeItemStack(MapPlayer mapPlayer) {
        return MxDefaultItemStackBuilder.create(Material.BOOK)
                .setName(ChatColor.GRAY + "Wijzig rol")
                .addBlankLore()
                .addLore(ChatColor.GRAY + "Huidig: " + mapPlayer.getRoleDisplayString())
                .addBlankLore()
                .addLore(ChatColor.YELLOW + "Klik hier om de rol aan te passen.")
                .build();
    }
}
