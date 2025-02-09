package nl.mxndarijn.wieisdemol.items.game.books;

import nl.mxndarijn.api.inventory.MxInventory;
import nl.mxndarijn.api.inventory.MxInventoryManager;
import nl.mxndarijn.api.inventory.MxInventorySlots;
import nl.mxndarijn.api.inventory.MxItemClicked;
import nl.mxndarijn.api.inventory.menu.MxDefaultMenuBuilder;
import nl.mxndarijn.api.inventory.menu.MxListInventoryBuilder;
import nl.mxndarijn.api.item.MxDefaultItemStackBuilder;
import nl.mxndarijn.api.item.MxSkullItemStackBuilder;
import nl.mxndarijn.api.item.Pair;
import nl.mxndarijn.api.util.MxWorldFilter;
import nl.mxndarijn.wieisdemol.data.AvailablePerson;
import nl.mxndarijn.wieisdemol.data.BookFailurePlayersHolder;
import nl.mxndarijn.wieisdemol.data.CustomInventoryOverlay;
import nl.mxndarijn.wieisdemol.data.ItemTag;
import nl.mxndarijn.wieisdemol.game.GamePlayer;
import nl.mxndarijn.wieisdemol.game.UpcomingGameStatus;
import nl.mxndarijn.wieisdemol.managers.language.LanguageManager;
import nl.mxndarijn.wieisdemol.managers.language.LanguageText;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class InvCheckBook extends Book {
    public InvCheckBook(ItemStack is, MxWorldFilter worldFilter, boolean gameItem, Action... actions) {
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
                                .addLore(ChatColor.YELLOW + "Klik hier om " + player.getName() + " te selecteren.")
                                .addLore(ChatColor.YELLOW + "Hierna kan je het item selecteren")
                                .build(),
                        (mxInv, e1) -> {
                            MxInventoryManager.getInstance().addAndOpenInventory(p, MxDefaultMenuBuilder.create(ChatColor.GRAY + "Selecteer Item", MxInventorySlots.THREE_ROWS)

                                    .setPrevious(mxInv)
                                    .setItem(MxDefaultItemStackBuilder.create(Material.GOLD_BLOCK)
                                            .setName(ChatColor.GRAY + "Gold Block")
                                            .addBlankLore()
                                            .addLore(ChatColor.YELLOW + "Klik hier om te zoeken op")
                                            .addLore(ChatColor.YELLOW + "een gold block.")
                                            .build(), 11, (mxInv1, e2) -> {
                                        invCheck(p, player, gp, gamePlayer, Material.GOLD_BLOCK);
                                    })
                                    .setItem(MxDefaultItemStackBuilder.create(Material.DIAMOND_BLOCK)
                                            .setName(ChatColor.GRAY + "Diamond Block")
                                            .addBlankLore()
                                            .addLore(ChatColor.YELLOW + "Klik hier om te zoeken op")
                                            .addLore(ChatColor.YELLOW + "een diamond block.")
                                            .build(), 13, (mxInv1, e2) -> {
                                        invCheck(p, player, gp, gamePlayer, Material.DIAMOND_BLOCK);
                                    })
                                    .setItem(MxDefaultItemStackBuilder.create(Material.BOOK)
                                            .setName(ChatColor.GRAY + "Book")
                                            .addBlankLore()
                                            .addLore(ChatColor.YELLOW + "Klik hier om te zoeken op")
                                            .addLore(ChatColor.YELLOW + "een book.")
                                            .build(), 15, (mxInv1, e2) -> {
                                        invCheck(p, player, gp, gamePlayer, Material.BOOK);
                                    })
                                    .build());
                        }
                ));
            });

            MxInventoryManager.getInstance().addAndOpenInventory(p, MxListInventoryBuilder.create(CustomInventoryOverlay.GAME_INVCLEAR.getUnicodeCharacter(), MxInventorySlots.SIX_ROWS)
                    .setAvailableSlots(12, 13, 14, 20, 21, 22, 23, 24, 25, 30, 31, 32, 33, 34, 35, 41, 42, 43)
                    .setShowPageNumbers(false)
                    .setListItems(list)
                    .build());


        }
    }

    private void invCheck(Player p, Player player, GamePlayer gp, GamePlayer gamePlayer, Material type) {
        p.closeInventory();
        for (Map.Entry<Integer, ? extends ItemStack> entry : (p.getInventory()).all(is.getType()).entrySet()) {
            Integer key = entry.getKey();
            ItemStack value = entry.getValue();
            if (isItemTheSame(value)) {
                if(!canItemExecute(p, key, value, BookFailurePlayersHolder.create().setData(AvailablePerson.EXECUTOR, p)))
                    return;
                ItemStack[] inv = player.getInventory().getContents().clone();
                List<ItemStack> foundItems = new ArrayList<>();
                for (ItemStack itemStack : inv) {
                    if(itemStack != null && itemStack.getItemMeta() != null && itemStack.getType() == type) {
                        String data = itemStack.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(plugin, ItemTag.DETECTABLE.getPersistentDataTag()), PersistentDataType.STRING);
                        if (data == null || !data.equalsIgnoreCase("false")) {
                            foundItems.add(itemStack);
                        }
                    }
                }
                if(player.getItemOnCursor().getType() == type) {
                    foundItems.add(player.getItemOnCursor());
                }
                sendBookMessageToAll(LanguageManager.getInstance().getLanguageString(LanguageText.GAME_INVCHECK_RESULT, Arrays.asList(gp.getMapPlayer().getColor().getColor() + p.getName(), gamePlayer.getMapPlayer().getColor().getColor() + player.getName(), type.toString().toLowerCase(), foundItems.isEmpty() ? ChatColor.RED + "niet gevonden" : ChatColor.GREEN + "gevonden")));
                break;
            }
        }
    }
}
