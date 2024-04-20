package dev.betterstudios.it.team.component.runnable;

import dev.betterstudios.it.Main;
import dev.betterstudios.it.team.Team;
import dev.betterstudios.it.team.component.implementation.BadlionSupport;
import dev.betterstudios.it.team.component.implementation.LunarSupport;
import org.bukkit.scheduler.BukkitRunnable;

public class TeamRunnable extends BukkitRunnable {

    @Override
    public void run() {
        if(Team.getTeams() == null) return;
        if(Main.getInstance().isBadLionSupport())
            BadlionSupport.refreshTeam();
        if(Main.getInstance().isLunarClientSupport())
            LunarSupport.refreshTeam();
    }

}
