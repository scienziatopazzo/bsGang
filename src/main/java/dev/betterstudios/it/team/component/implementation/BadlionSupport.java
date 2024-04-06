package dev.betterstudios.it.team.component.implementation;

import dev.betterstudios.it.team.Team;
import net.badlion.modapicommon.mods.TeamMarker;
import net.badlion.modapicommon.utility.TeamMemberLocation;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class BadlionSupport {

    public static void refreshTeam() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Team team = Team.getTeam(player);
            if(team == null) {
                resetTeam(player);
                continue;
            }
            resetTeam(player);
            sendTeammates(player, team);
        }
    }

    private static void sendTeammates(Player source, Team team) {
        List<TeamMemberLocation> locationList = new ArrayList<>();

        for (String member : team.getMembers()) {

            Player player = Bukkit.getPlayer(member);
            if (player == null) continue;

            Location memberLocation = player.getLocation();

            locationList.add(new TeamMemberLocation(player.getUniqueId(), 3,
                    memberLocation.getX(), memberLocation.getY(), memberLocation.getZ()));
        }

        TeamMarker.sendLocations(source.getUniqueId(), locationList);
    }

    private static void resetTeam(Player player) {
        TeamMarker.sendLocations(player.getUniqueId(), new ArrayList<>());
    }
}
