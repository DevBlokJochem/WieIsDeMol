package nl.mxndarijn.wieisdemol.items.game.books;

import nl.mxndarijn.api.inventory.MxInventoryManager;
import nl.mxndarijn.api.inventory.MxInventorySlots;
import nl.mxndarijn.api.inventory.MxItemClicked;
import nl.mxndarijn.api.inventory.menu.MxListInventoryBuilder;
import nl.mxndarijn.api.item.MxSkullItemStackBuilder;
import nl.mxndarijn.api.item.Pair;
import nl.mxndarijn.api.util.MxWorldFilter;
import nl.mxndarijn.wieisdemol.data.AvailablePerson;
import nl.mxndarijn.wieisdemol.data.BookFailurePlayersHolder;
import nl.mxndarijn.wieisdemol.game.GamePlayer;
import nl.mxndarijn.wieisdemol.game.UpcomingGameStatus;
import nl.mxndarijn.wieisdemol.managers.language.LanguageManager;
import nl.mxndarijn.wieisdemol.managers.language.LanguageText;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class TeleportBook extends Book {
    public TeleportBook(ItemStack is, MxWorldFilter worldFilter, boolean gameItem, Action... actions) {
        super(is, worldFilter, gameItem, actions);
    }

    @Override
    public void execute(Player p, PlayerInteractEvent e) {
        getGame(p.getWorld());
        if (game == null)
            return;

        Optional<GamePlayer> optionalGamePlayer = getGamePlayer(p.getUniqueId());

        if (optionalGamePlayer.isPresent()) {
            if (game.getGameInfo().getStatus() != UpcomingGameStatus.PLAYING)
                return;
            GamePlayer gp = optionalGamePlayer.get();
            if (!gp.isAlive())
                return;
            List<Pair<ItemStack, MxItemClicked>> list = new ArrayList<>();
            game.getColors().forEach(gamePlayer -> {
                if (gamePlayer.getPlayer().isEmpty())
                    return;
                if (!gamePlayer.isAlive())
                    return;
                if (gamePlayer.getPlayer().get().equals(p.getUniqueId()))
                    return;
                if (gamePlayer.isPeacekeeperChestOpened() && gamePlayer.getMapPlayer().isPeacekeeper())
                    return;
                Player player = Bukkit.getPlayer(gamePlayer.getPlayer().get());
                if (player == null)
                    return;

                list.add(new Pair<>(
                        MxSkullItemStackBuilder.create(1)
                                .setSkinFromHeadsData(player.getUniqueId().toString())
                                .setName(ChatColor.GRAY + player.getName())
                                .addLore(gamePlayer.getMapPlayer().getColor().getDisplayName())
                                .addBlankLore()
                                .addLore(ChatColor.YELLOW + "Klik hier om " + player.getName() + " te teleporten")
                                .build(),
                        (mxInv, e1) -> {
                            List<Pair<ItemStack, MxItemClicked>> newList = new ArrayList<>();
                            game.getColors().forEach(gameplayer -> {
                                if (gameplayer.getPlayer().isEmpty())
                                    return;
                                if (!gameplayer.isAlive())
                                    return;
                                if (gameplayer.getPlayer().get().equals(p.getUniqueId()))
                                    return;
                                if (gameplayer.isPeacekeeperChestOpened() && gameplayer.getMapPlayer().isPeacekeeper())
                                    return;
                                Player player1 = Bukkit.getPlayer(gameplayer.getPlayer().get());
                                if (player1 == null)
                                    return;

                                newList.add(new Pair<>(
                                        MxSkullItemStackBuilder.create(1)
                                                .setSkinFromHeadsData(player1.getUniqueId().toString())
                                                .setName(ChatColor.GRAY + player1.getName())
                                                .addLore(gameplayer.getMapPlayer().getColor().getDisplayName())
                                                .addBlankLore()
                                                .addLore(ChatColor.YELLOW + "Klik hier om " + player1.getName())
                                                .addLore(" te teleporten naar " + player1.getName())
                                                .build(),
                                        (mxInv1, e11) -> {
                                            p.closeInventory();
                                            for (Map.Entry<Integer, ? extends ItemStack> entry : p.getInventory().all(is.getType()).entrySet()) {
                                                Integer key = entry.getKey();
                                                ItemStack value = entry.getValue();
                                                if (isItemTheSame(value)) {
                                                    if(!canItemExecute(p, key, value, BookFailurePlayersHolder.create().setData(AvailablePerson.EXECUTOR, p)))
                                                        return;
                                                    player.teleport(player1);
                                                    sendBookMessageToAll(LanguageManager.getInstance().getLanguageString(LanguageText.GAME_TELEPORT_MESSAGE, Arrays.asList(gp.getMapPlayer().getColor().getColor() + p.getName(), gamePlayer.getMapPlayer().getColor().getColor() + player1.getName())));
                                                    break;
                                                }
                                            }
                                        }
                                ));

                                MxInventoryManager.getInstance().addAndOpenInventory(p, MxListInventoryBuilder.create("Teleport", MxInventorySlots.SIX_ROWS)
                                        .setAvailableSlots(12, 13, 14, 20, 21, 22, 23, 24, 25, 30, 31, 32, 33, 34, 35, 41, 42, 43)
                                        .setShowPageNumbers(false)
                                        .setListItems(newList)
                                        .build());
                            });
                        }
                ));
            });

            MxInventoryManager.getInstance().addAndOpenInventory(p, MxListInventoryBuilder.create("Teleport", MxInventorySlots.SIX_ROWS)
                    .setAvailableSlots(12, 13, 14, 20, 21, 22, 23, 24, 25, 30, 31, 32, 33, 34, 35, 41, 42, 43)
                    .setShowPageNumbers(false)
                    .setListItems(list)
                    .build());


        }
    }
}
