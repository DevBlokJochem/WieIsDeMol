package nl.mxndarijn.wieisdemol.items.game;

import nl.mxndarijn.api.chatinput.MxChatInputCallback;
import nl.mxndarijn.api.chatinput.MxChatInputManager;
import nl.mxndarijn.api.inventory.MxInventory;
import nl.mxndarijn.api.inventory.MxInventoryManager;
import nl.mxndarijn.api.inventory.MxInventorySlots;
import nl.mxndarijn.api.inventory.MxItemClicked;
import nl.mxndarijn.api.inventory.menu.MxDefaultMenuBuilder;
import nl.mxndarijn.api.inventory.menu.MxListInventoryBuilder;
import nl.mxndarijn.api.item.MxDefaultItemStackBuilder;
import nl.mxndarijn.api.item.MxSkullItemStackBuilder;
import nl.mxndarijn.api.mxitem.MxItem;
import nl.mxndarijn.api.util.MxWorldFilter;
import nl.mxndarijn.wieisdemol.game.Game;
import nl.mxndarijn.wieisdemol.game.UpcomingGameStatus;
import nl.mxndarijn.wieisdemol.managers.language.LanguageManager;
import nl.mxndarijn.wieisdemol.managers.language.LanguageText;
import nl.mxndarijn.wieisdemol.managers.world.GameWorldManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.Optional;

public class GameHostItem extends MxItem {


    public GameHostItem(ItemStack is, MxWorldFilter worldFilter, boolean gameItem, Action... actions) {
        super(is, worldFilter, gameItem, actions);
    }

    @Override
    public void execute(Player p, PlayerInteractEvent e) {
        Optional<Game> optionalGame = GameWorldManager.getInstance().getGameByWorldUID(e.getPlayer().getWorld().getUID());
        if(optionalGame.isEmpty())
            return;
        Game game = optionalGame.get();

        if(!game.getHosts().contains(e.getPlayer().getUniqueId()))
            return;

        MxInventoryManager.getInstance().addAndOpenInventory(p, new MxDefaultMenuBuilder(ChatColor.GRAY + "Host-Tool", MxInventorySlots.THREE_ROWS)

                .setItem(MxDefaultItemStackBuilder.create(Material.COMPARATOR)
                                .setName(ChatColor.GRAY + "Verander Game status")
                                .addBlankLore()
                                .addLore(ChatColor.YELLOW + "Klik hier om de game status te veranderen.")
                                .build(), 13
                        , (mxInv, e1) -> {
                    MxInventoryManager.getInstance().addAndOpenInventory(p, new MxDefaultMenuBuilder(ChatColor.GRAY + "Verander Game Status", MxInventorySlots.THREE_ROWS)
                                    .setPrevious(mxInv)
                                    .setItem(MxSkullItemStackBuilder.create(1)
                                                    .setSkinFromHeadsData("ice-block")
                                                    .setName(UpcomingGameStatus.FREEZE.getStatus())
                                                    .addBlankLore()
                                                    .addLore(ChatColor.YELLOW + "Klik hier om de status te veranderen naar: " + UpcomingGameStatus.FREEZE.getStatus())
                                                    .build(),
                                            10,
                                            (mxInv1, e2) -> {
                                                game.setGameStatus(UpcomingGameStatus.FREEZE);
                                                p.closeInventory();
                                            })
                            .setItem(MxSkullItemStackBuilder.create(1)
                                                    .setSkinFromHeadsData("red-block")
                                                    .setName(UpcomingGameStatus.FINISHED.getStatus())
                                                    .addBlankLore()
                                                    .addLore(ChatColor.YELLOW + "Klik hier om de status te veranderen naar: " + UpcomingGameStatus.FINISHED.getStatus())
                                                    .build(),
                                            16,
                                            (mxInv1, e2) -> {
                                                game.setGameStatus(UpcomingGameStatus.FINISHED);
                                                p.closeInventory();
                                            })
                            .setItem(MxSkullItemStackBuilder.create(1)
                                                    .setSkinFromHeadsData("light-green-block")
                                                    .setName(UpcomingGameStatus.PLAYING.getStatus())
                                                    .addBlankLore()
                                                    .addLore(ChatColor.YELLOW + "Klik hier om de status te veranderen naar: " + UpcomingGameStatus.PLAYING.getStatus())
                                                    .build(),
                                            13,
                                            (mxInv1, e2) -> {
                                                game.setGameStatus(UpcomingGameStatus.PLAYING);
                                                p.closeInventory();
                                            })
                                    .build());

                        })
                        .setItem(MxSkullItemStackBuilder.create(1)
                                        .setSkinFromHeadsData("wooden-plus")
                                        .setName(ChatColor.GRAY + "Voeg host toe")
                                        .addBlankLore()
                                        .addLore(ChatColor.YELLOW + "Klik hier om een host toe te voegen.")
                                        .build(),
                                11,
                                (mxInv, e12) -> {
                                    p.sendMessage(LanguageManager.getInstance().getLanguageString(LanguageText.GAME_HOST_ADD_ENTER_NAME));
                                    p.closeInventory();
                                    MxChatInputManager.getInstance().addChatInputCallback(p.getUniqueId(), message -> {
                                        Player player = Bukkit.getPlayer(message);
                                        if(player == null) {
                                            p.sendMessage(LanguageManager.getInstance().getLanguageString(LanguageText.GAME_HOST_ADD_NOT_FOUND));
                                            return;
                                        }
                                        if(game.getHosts().contains(player.getUniqueId())) {
                                            p.sendMessage(LanguageManager.getInstance().getLanguageString(LanguageText.GAME_HOST_ADD_ALREADY_HOST));
                                            return;
                                        }
                                        if(game.getSpectators().contains(player.getUniqueId()) || game.getGameInfo().getQueue().contains(player.getUniqueId())) {
                                            if(game.getSpectators().contains(player.getUniqueId())) {
                                                game.removeSpectator(player.getUniqueId(), false);
                                            }
                                            p.sendMessage(LanguageManager.getInstance().getLanguageString(LanguageText.GAME_HOST_ADD_HOST_ADDED, Collections.singletonList(player.getName())));
                                            game.addHost(player.getUniqueId());
                                        } else {
                                            p.sendMessage(LanguageManager.getInstance().getLanguageString(LanguageText.GAME_HOST_ADD_NOT_IN_QUEUE, Collections.singletonList(player.getName())));
                                        }
                                    });
                                })
                        .setItem(MxSkullItemStackBuilder.create(1)
                                        .setSkinFromHeadsData("book")
                                        .setName(ChatColor.GRAY + "Votes Beheren")
                                        .addBlankLore()
                                        .addLore(ChatColor.YELLOW + "Klik hier om votes te beheren")
                                        .build(),
                                15,
                                (mxInv, e13) -> {
                                    MxInventoryManager.getInstance().addAndOpenInventory(p,
                                            MxDefaultMenuBuilder.create(ChatColor.GRAY + "Beheer Votes", MxInventorySlots.THREE_ROWS)
                                                    .setPrevious(mxInv)
                                                    .setItem(MxSkullItemStackBuilder.create(1)
                                                                    .setSkinFromHeadsData("book")
                                                                    .setName(ChatColor.GRAY + "Toggle Votes")
                                                                    .addBlankLore()
                                                                    .addLore(ChatColor.GRAY + "Status: " + (game.isPlayersCanEndVote() ? ChatColor.GREEN + "Spelers kunnen de votes beeindigen" : ChatColor.RED + "Spelers kunnen niet de votes beeindigen"))
                                                                    .addBlankLore()
                                                                    .addLore(ChatColor.YELLOW + "Klik hier om de status te togglen")
                                                                    .build(),
                                                            12,
                                                            (mxInv12, e14) -> {
                                                                game.setPlayersCanEndVote(!game.isPlayersCanEndVote());
                                                                p.closeInventory();
                                                                p.sendMessage( (game.isPlayersCanEndVote() ? ChatColor.GREEN + "Spelers kunnen de votes beeindigen." : ChatColor.RED + "Spelers kunnen niet de votes beeindigen."));
                                                            }
                                                    )
                                                    .setItem(MxSkullItemStackBuilder.create(1)
                                                                    .setSkinFromHeadsData("message-icon")
                                                                    .setName(ChatColor.GRAY + "Laat resultaten zien")
                                                                    .addBlankLore()
                                                                    .addLore(ChatColor.YELLOW + "Klik hier om de vote resultaten te bekijken.")
                                                                    .build(),
                                                            14,
                                                            (mxInv12, e14) -> {
                                                                game.showVotingResults("Host");
                                                                p.closeInventory();
                                                          }
                                                    )
                                                    .build()

                                    );
                                }
                        )
                .build());

    }
}
