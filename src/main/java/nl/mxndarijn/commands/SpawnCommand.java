package nl.mxndarijn.commands;

import nl.mxndarijn.commands.util.MxCommand;
import nl.mxndarijn.commands.util.MxWorldFilter;
import nl.mxndarijn.data.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpawnCommand extends MxCommand {

    public SpawnCommand(Permissions permission, boolean onlyPlayersCanExecute, boolean canBeExecutedInGame, MxWorldFilter worldFilter) {
        super(permission, onlyPlayersCanExecute, canBeExecutedInGame, worldFilter);
    }

    public SpawnCommand(Permissions permission, boolean onlyPlayersCanExecute, boolean canBeExecutedInGame) {
        super(permission, onlyPlayersCanExecute, canBeExecutedInGame);
    }
    @Override
    public void execute(CommandSender sender, Command command, String label, String[] args) {
        World w = Bukkit.getWorld("world");
        Player p = (Player) sender;
        p.teleport(w.getSpawnLocation());
        //TODO Improve this...
    }
}
