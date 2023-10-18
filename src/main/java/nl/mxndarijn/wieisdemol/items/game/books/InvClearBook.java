package nl.mxndarijn.wieisdemol.items.game.books;

import nl.mxndarijn.api.inventory.MxInventoryManager;
import nl.mxndarijn.api.inventory.MxInventorySlots;
import nl.mxndarijn.api.inventory.MxItemClicked;
import nl.mxndarijn.api.inventory.menu.MxListInventoryBuilder;
import nl.mxndarijn.api.item.MxSkullItemStackBuilder;
import nl.mxndarijn.api.item.Pair;
import nl.mxndarijn.api.util.MxWorldFilter;
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
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class InvClearBook extends Book {
    public InvClearBook(ItemStack is, MxWorldFilter worldFilter, boolean gameItem, Action... actions) {
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
                                .addLore(ChatColor.YELLOW + "Klik hier om " + player.getName() + " zijn inventory te clearen.")
                                .build(),
                        (mxInv, e1) -> {
                            p.closeInventory();
                            for (Map.Entry<Integer, ? extends ItemStack> entry : p.getInventory().all(is.getType()).entrySet()) {
                                Integer key = entry.getKey();
                                ItemStack value = entry.getValue();
                                if (isItemTheSame(value)) {
                                    if (value.getAmount() > 1) {
                                        value.setAmount(value.getAmount() - 1);

                                        p.getInventory().setItem(key, value);
                                    } else {
                                        p.getInventory().setItem(key, new ItemStack(Material.AIR));
                                    }
                                    ItemStack[] inv = player.getInventory().getContents().clone();
                                    List<ItemStack> clearItems = new ArrayList<>();
                                    for (ItemStack itemStack : inv) {
                                        if(itemStack != null && itemStack.getItemMeta() != null) {
                                            String data = itemStack.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(plugin, ItemTag.CLEARABLE.getPersistentDataTag()), PersistentDataType.STRING);
                                            if (data == null || !data.equalsIgnoreCase("false")) {
                                                clearItems.add(itemStack);
                                            }
                                        }
                                    }
                                    player.getInventory().removeItem(clearItems.toArray(new ItemStack[0]));
                                    sendBookMessageToAll(LanguageManager.getInstance().getLanguageString(LanguageText.GAME_INVCLEAR_MESSAGE, Arrays.asList(gp.getMapPlayer().getColor().getColor() + p.getName(), gamePlayer.getMapPlayer().getColor().getColor() + player.getName())));
                                    break;
                                }
                            }
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
}
