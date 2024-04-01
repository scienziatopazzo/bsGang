package dev.betterstudios.it.team;


import dev.betterstudios.it.Main;
import dev.betterstudios.it.utils.ChatUtils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

@Getter
public class Team {

    @Getter
    @Setter
    private static List<Team> teams = new ArrayList<>();

    @Setter
    private String name;
    private final String leader;
    private final List<String> members;
    private final List<String> moderators;
    @Setter
    private int points;
    @Setter
    private int kills;
    @Setter
    private int deaths;


    public Team(String name, String leader, List<String> members, List<String> moderators, int points, int kills, int death) {
        this.name = name;
        this.leader = leader;
        this.members = members;
        this.moderators = moderators;
        if(!moderators.contains(leader))
            moderators.add(leader);
        this.points = points;
        this.kills = kills;
        this.deaths = death;
        teams.add(this);
    }

    public Team(String leader, String name) {
        this.name = name;
        this.leader = leader;
        this.members = new ArrayList<>();
        this.moderators = new ArrayList<>();
        moderators.add(leader);
        this.points = 0;
        this.kills = 0;
        this.deaths = 0;
        teams.add(this);
    }

    public void increaseKills() {
        kills++;
        for (int i = 0; i < Main.getInstance().getConfiguration().getInt("team.points.kill") + 1; i++)
            points++;
    }

    public void increaseDeath() {
        deaths++;
        for (int i = 0; i < Main.getInstance().getConfiguration().getInt("team.points.death") + 1; i++)
            points--;
        if(points < 0) points = 0;
    }


    public void kick(Player player) {
        members.remove(player.getName());
        moderators.remove(player.getName());
    }

    public void chat(Player player, String message) {
        String prefix = leader.equalsIgnoreCase(player.getName()) ? Main.getInstance().getConfiguration().getString("rank.leader") : moderators.contains(player.getName()) ? Main.getInstance().getConfiguration().getString("rank.moderator") : Main.getInstance().getConfiguration().getString("rank.member");
        for (Player team_member : getOnlinePlayers()) {
            HashMap<String, String> placeholders = new HashMap<>();
            placeholders.put("rank", prefix);
            placeholders.put("message", message);
            team_member.sendMessage(ChatUtils.replace(Main.getInstance().getMessageConfiguration().getString("chat_member"), placeholders));
        }
    }

    private List<Player> getOnlinePlayers() {
        List<Player> players = new ArrayList<>();
        for (String member : members) {
            Player player = Bukkit.getPlayer(member);
            if(player != null)
                players.add(player);
        }
        Player player = Bukkit.getPlayer(leader);
        if(player != null)
            players.add(player);
        return players;
    }
    
    public void disband() {
        teams.remove(this);
    }

    public boolean canHaveAnotherPlayer() {
        return Main.getInstance().getConfiguration().getBoolean("team.players.infinite") || members.size() != Main.getInstance().getConfiguration().getInt("team.players.max");
    }



    public boolean isModerator(Player player) {
        return moderators.contains(player.getName()) || leader.equalsIgnoreCase(player.getName());
    }

    public static boolean isInTeam(Player player) {
        return teams.stream().anyMatch(team -> team.getMembers().contains(player.getName()) || team.getLeader().equalsIgnoreCase(player.getName()));
    }

    public static boolean isLeader(Player player) {
        return teams.stream().anyMatch(team -> team.getLeader().equalsIgnoreCase(player.getName()));
    }

    public static Team getTeam(Player player) {
        return teams.stream().filter(team -> team.getMembers().contains(player.getName()) || team.getLeader().equalsIgnoreCase(player.getName())).findFirst().orElse(null);
    }

    public static Team getTeam(String name) {
        return teams.stream().filter(team -> team.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public static Team getTopTeamPoints(int teamNumber) {
        List<Team> teamsCopy = new ArrayList<>(teams);
        teamsCopy.sort(Comparator.comparingInt(Team::getPoints).reversed());

        if (teamNumber <= 0 || teamNumber > teamsCopy.size()) return null;

        return teamsCopy.get(teamNumber - 1);
    }

    public static Team getTopTeamKills(int teamNumber) {
        List<Team> teamsCopy = new ArrayList<>(teams);
        teamsCopy.sort(Comparator.comparingInt(Team::getKills).reversed());

        if (teamNumber <= 0 || teamNumber > teamsCopy.size()) return null;

        return teamsCopy.get(teamNumber - 1);
    }

    public static Team getTopTeamDeath(int teamNumber) {
        List<Team> teamsCopy = new ArrayList<>(teams);
        teamsCopy.sort(Comparator.comparingInt(Team::getDeaths).reversed());

        if (teamNumber <= 0 || teamNumber > teamsCopy.size()) return null;

        return teamsCopy.get(teamNumber - 1);
    }

}
