package nl.mxndarijn.wieisdemol.items.maps;

import nl.mxndarijn.api.inventory.MxInventoryIndex;
import nl.mxndarijn.api.inventory.MxInventoryManager;
import nl.mxndarijn.api.inventory.MxInventorySlots;
import nl.mxndarijn.api.inventory.MxItemClicked;
import nl.mxndarijn.api.inventory.menu.MxListInventoryBuilder;
import nl.mxndarijn.api.item.MxDefaultItemStackBuilder;
import nl.mxndarijn.api.item.Pair;
import nl.mxndarijn.api.mxitem.MxItem;
import nl.mxndarijn.api.mxworld.MxLocation;
import nl.mxndarijn.api.util.MxWorldFilter;
import nl.mxndarijn.wieisdemol.data.ChatPrefix;
import nl.mxndarijn.wieisdemol.managers.MapManager;
import nl.mxndarijn.wieisdemol.managers.chests.ChestInformation;
import nl.mxndarijn.wieisdemol.managers.language.LanguageManager;
import nl.mxndarijn.wieisdemol.managers.language.LanguageText;
import nl.mxndarijn.wieisdemol.map.Map;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Optional;

public class MapChestItem extends MxItem {


    public MapChestItem(ItemStack is, MxWorldFilter worldFilter, boolean gameItem, Action... actions) {
        super(is, worldFilter, gameItem, actions);
    }

    @Override
    public void execute(Player p, PlayerInteractEvent e) {
        Optional<Map> mapOptional = MapManager.getInstance().getMapByWorldUID(p.getWorld().getUID());

        if (mapOptional.isEmpty()) {
            return;
        }

        Map map = mapOptional.get();

        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            assert e.getClickedBlock() != null;
            if (e.getClickedBlock().getType() != Material.CHEST && e.getClickedBlock().getType() != Material.TRAPPED_CHEST) {
                return;
            }
            Optional<ChestInformation> inf = map.getChestManager().getChestByLocation(MxLocation.getFromLocation(e.getClickedBlock().getLocation()));
            if (inf.isPresent()) {
                inf.get().openAttachmentsInventory(e.getPlayer());
            } else {
                p.sendMessage(ChatPrefix.WIDM + LanguageManager.getInstance().getLanguageString(LanguageText.MAP_CHEST_COULD_NOT_BE_FOUND));
            }
            return;
        }

        ArrayList<Pair<ItemStack, MxItemClicked>> list = new ArrayList<>();
        map.getChestManager().getChests().forEach(chest -> {
            list.add(new Pair<>(
                    MxDefaultItemStackBuilder.create(Material.CHEST, 1)
                            .setName(ChatColor.GRAY + chest.getName())
                            .addBlankLore()
                            .addLore(ChatColor.GRAY + "Location: " + chest.getLocation().getX() + " " + chest.getLocation().getY() + " " + chest.getLocation().getZ())
                            .addBlankLore()
                            .addLore(ChatColor.YELLOW + "Klik om de kist op afstand te openen.")
                            .build(),
                    (mxInv, e12) -> {
                        Location loc = chest.getLocation().getLocation(p.getWorld());
                        Block block = p.getWorld().getBlockAt(loc);
                        if (block.getType() == Material.CHEST || block.getType() == Material.TRAPPED_CHEST) {
                            Chest chestBlock = (Chest) block.getState();
                            p.openInventory(chestBlock.getBlockInventory());
                        } else {
                            p.sendMessage(ChatPrefix.WIDM + LanguageManager.getInstance().getLanguageString(LanguageText.MAP_CHEST_IS_NOT_A_CHEST));
                            p.closeInventory();
                        }
                    }
            ));
        });


        MxInventoryManager.getInstance().addAndOpenInventory(p, MxListInventoryBuilder.create(ChatColor.GRAY + "Chest Hulp Tool", MxInventorySlots.SIX_ROWS)
                .setAvailableSlots(MxInventoryIndex.ROW_ONE_TO_FIVE)
                .setListItems(list)
                .build());

    }

    @EventHandler
    public void blockPlace(BlockPlaceEvent e) {
        if (e.getBlockPlaced().getType() != Material.CHEST && e.getBlockPlaced().getType() != Material.TRAPPED_CHEST) {
            return;
        }
        Player p = e.getPlayer();
        Optional<Map> mapOptional = MapManager.getInstance().getMapByWorldUID(p.getWorld().getUID());

        if (mapOptional.isEmpty()) {
            return;
        }

        Map map = mapOptional.get();
        map.getChestManager().addChest(new ChestInformation("Automatisch toegevoegde kist", MxLocation.getFromLocation(e.getBlockPlaced().getLocation())));
        p.sendMessage(ChatPrefix.WIDM + LanguageManager.getInstance().getLanguageString(LanguageText.MAP_AUTOMATED_CHEST_ADDED));
    }

    @EventHandler
    public void blockBreak(BlockBreakEvent e) {
        if (e.getBlock().getType() != Material.CHEST && e.getBlock().getType() != Material.TRAPPED_CHEST) {
            return;
        }
        Player p = e.getPlayer();
        Optional<Map> mapOptional = MapManager.getInstance().getMapByWorldUID(p.getWorld().getUID());

        if (mapOptional.isEmpty()) {
            return;
        }

        Map map = mapOptional.get();
        Optional<ChestInformation> info = map.getChestManager().getChestByLocation(MxLocation.getFromLocation(e.getBlock().getLocation()));
        if (info.isEmpty()) {
            return;
        }

        map.getChestManager().removeChest(info.get());
        p.sendMessage(ChatPrefix.WIDM + LanguageManager.getInstance().getLanguageString(LanguageText.MAP_AUTOMATED_CHEST_REMOVED));
    }

    @EventHandler
    public void open(InventoryOpenEvent e) {
        Location loc = e.getInventory().getLocation();
        if (loc == null) {
            return;
        }
        if (!(e.getInventory().getHolder() instanceof Chest))
            return;
        Block b = loc.getBlock();

        Player p = (Player) e.getPlayer();
        Optional<Map> mapOptional = MapManager.getInstance().getMapByWorldUID(p.getWorld().getUID());

        if (mapOptional.isEmpty()) {
            return;
        }

        Map map = mapOptional.get();
        Optional<ChestInformation> optionalChestInformation = map.getChestManager().getChestByLocation(MxLocation.getFromLocation(b.getLocation()));
        if (optionalChestInformation.isEmpty()) {
            return;
        }

        ChestInformation chestInformation = optionalChestInformation.get();
        chestInformation.getChestAttachmentList().forEach(a -> {
            a.onOpenChest(e);
        });

    }
}
