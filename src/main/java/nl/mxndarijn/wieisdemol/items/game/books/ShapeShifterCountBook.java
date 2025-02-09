package nl.mxndarijn.wieisdemol.items.game.books;

import nl.mxndarijn.api.util.MxWorldFilter;
import nl.mxndarijn.wieisdemol.data.AvailablePerson;
import nl.mxndarijn.wieisdemol.data.BookFailurePlayersHolder;
import nl.mxndarijn.wieisdemol.data.Role;
import nl.mxndarijn.wieisdemol.game.GamePlayer;
import nl.mxndarijn.wieisdemol.game.UpcomingGameStatus;
import nl.mxndarijn.wieisdemol.managers.language.LanguageManager;
import nl.mxndarijn.wieisdemol.managers.language.LanguageText;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public class ShapeShifterCountBook extends Book {
    public ShapeShifterCountBook(ItemStack is, MxWorldFilter worldFilter, boolean gameItem, Action... actions) {
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

            for (Map.Entry<Integer, ? extends ItemStack> entry : p.getInventory().all(is.getType()).entrySet()) {
                Integer key = entry.getKey();
                ItemStack value = entry.getValue();
                if (isItemTheSame(value)) {
                    if(!canItemExecute(p, key, value, BookFailurePlayersHolder.create().setData(AvailablePerson.EXECUTOR, p)))
                        return;
                    int count = 0;
                    for (GamePlayer g : game.getColors()) {
                        if(!g.isAlive()) continue;
                        if(g.getPlayer().isEmpty()) continue;
                        if(g.getMapPlayer().getRole() != Role.SHAPESHIFTER) continue;
                        count++;
                    }
                    p.sendMessage(LanguageManager.getInstance().getLanguageString(LanguageText.GAME_SHAPESHIFTER_MESSAGE, Collections.singletonList(count + "")));
                    break;
                }
            }

        }
    }
}
