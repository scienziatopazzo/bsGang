package dev.betterstudios.it.team.component;

import dev.betterstudios.it.commands.TeamCommand;
import dev.betterstudios.it.team.Team;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatEvent implements Listener {

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();
        String message = e.getMessage();
        if(!TeamCommand.getPlayersChat().contains(player)) return;
        if(!Team.isInTeam(player)) {
            TeamCommand.getPlayersChat().remove(player);
            return;
        }
        Team.getTeam(player).chat(player, message);
        e.setCancelled(true);
    }

}
