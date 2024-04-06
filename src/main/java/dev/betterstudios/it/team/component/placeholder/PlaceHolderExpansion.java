package dev.betterstudios.it.team.component.placeholder;

import dev.betterstudios.it.Main;
import dev.betterstudios.it.team.Team;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

public class PlaceHolderExpansion extends PlaceholderExpansion {


    public String getIdentifier() {
        return "bsGang";
    }

    public String getAuthor() {
        return "discord.gg/betterstudios";
    }

    public String getVersion() {
        return "";
    }

    public String onPlaceholderRequest(Player player, String identifier) {
        if (identifier.contains("leaderboard")) {
            int number = Integer.parseInt(identifier.split("_")[1]);
            String type = identifier.split("_")[2];
            switch (type) {
                case "points":
                    return Team.getTopTeamPoints(number) != null ? String.valueOf(Team.getTopTeamPoints(number).getPoints()) : Main.getInstance().getConfiguration().getString("placeholders.no_team_leaderboard_stats");
                case "deaths":
                    return Team.getTopTeamDeath(number) != null ? String.valueOf(Team.getTopTeamDeath(number).getDeaths()) : Main.getInstance().getConfiguration().getString("placeholders.no_team_leaderboard_stats");
                case "kills":
                    return Team.getTopTeamKills(number) != null ? String.valueOf(Team.getTopTeamKills(number).getKills()) : Main.getInstance().getConfiguration().getString("placeholders.no_team_leaderboard_stats");
                case "points.name":
                    return Team.getTopTeamPoints(number) != null ? Team.getTopTeamPoints(number).getName() : Main.getInstance().getConfiguration().getString("placeholders.no_team_leaderboard_name");
                case "deaths.name":
                    return Team.getTopTeamDeath(number) != null ? Team.getTopTeamDeath(number).getName() : Main.getInstance().getConfiguration().getString("placeholders.no_team_leaderboard_name");
                case "kills.name":
                    return Team.getTopTeamKills(number) != null ? Team.getTopTeamKills(number).getName() : Main.getInstance().getConfiguration().getString("placeholders.no_team_leaderboard_name");
                default:
                    return "";
            }
        }
        if (player == null) return "";
        if (!Team.isInTeam(player)) return Main.getInstance().getConfiguration().getString("placeholders.no_team");
        switch (identifier) {
            case "name":
                return Team.getTeam(player).getName();
            case "kills":
                return String.valueOf(Team.getTeam(player).getKills());
            case "deaths":
                return String.valueOf(Team.getTeam(player).getDeaths());
            case "points":
                return String.valueOf(Team.getTeam(player).getPoints());
            default:
                return "";
        }
    }
}
