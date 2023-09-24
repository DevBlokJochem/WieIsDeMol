package nl.mxndarijn.api.changeworld;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChangedWorldEvent;

public interface MxChangeWorld {

    void enter(Player p, World w, PlayerChangedWorldEvent e);

    void leave(Player p, World w, PlayerChangedWorldEvent e);
}
