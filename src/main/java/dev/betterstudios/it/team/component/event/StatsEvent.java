package dev.betterstudios.it.team.component.event;

import dev.betterstudios.it.team.Team;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class StatsEvent implements Listener {

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player player = e.getEntity();
        if(Team.isInTeam(player)) Team.getTeam(player).increaseDeath();
        if (player.getKiller() == null) return;
        Player killer = player.getKiller();
        if(Team.isInTeam(killer)) Team.getTeam(killer).increaseKills();
    }

}
