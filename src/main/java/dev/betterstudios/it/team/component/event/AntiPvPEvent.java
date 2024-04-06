package dev.betterstudios.it.team.component.event;

import dev.betterstudios.it.team.Team;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class AntiPvPEvent implements Listener {

    @EventHandler
    public void onPvP(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player) || !(e.getDamager() instanceof Player)) return;
        if(isEqualTeam((Player) e.getEntity(), (Player) e.getDamager()))
            e.setCancelled(true);
    }

    private boolean isEqualTeam(Player player, Player target) {
        if(!Team.isInTeam(target)) return false;
        if(!Team.isInTeam(player)) return false;
        return Team.getTeam(player).equals(Team.getTeam(target));
    }

}
