package dev.betterstudios.it.team.component.implementation;

import com.google.common.collect.Maps;
import com.lunarclient.bukkitapi.LunarClientAPI;
import com.lunarclient.bukkitapi.nethandler.client.LCPacketTeammates;
import dev.betterstudios.it.team.Team;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class LunarSupport {

    private static LunarClientAPI lunar = LunarClientAPI.getInstance();


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

    private static void sendTeammates(Player player, Team team) {
        Map<UUID, Map<String, Double>> members = new HashMap<>();


        if (team == null) return;

        for (String member : team.getMembers()) {
            Player memberPlayer = Bukkit.getPlayer(member);

            if (memberPlayer == null || memberPlayer.getUniqueId().equals(player.getUniqueId())) continue;
            Location memberLocation = memberPlayer.getLocation();
            Map<String, Double> locationMap = new HashMap<>();

            locationMap.put("x", memberLocation.getX() - 1);
            locationMap.put("y", memberLocation.getY() - 1);
            locationMap.put("z", memberLocation.getZ() - 1);

            members.put(memberPlayer.getUniqueId(), locationMap);
        }

        lunar.sendTeammates(player, new LCPacketTeammates(player.getUniqueId(), 5L, members));
    }

    private static void resetTeam(Player player) {
        lunar.sendTeammates(player, new LCPacketTeammates(player.getUniqueId(), 5L, new HashMap<>()));
    }
}
