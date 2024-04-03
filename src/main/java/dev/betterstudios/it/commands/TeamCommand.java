package dev.betterstudios.it.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.Subcommand;
import dev.betterstudios.it.Main;
import dev.betterstudios.it.team.Team;
import dev.betterstudios.it.utils.ChatUtils;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@CommandAlias("team|gang|bsgang")
public class TeamCommand extends BaseCommand {


    @Subcommand("create")
    public void onCreate(Player player, String[] args) {
        if(checkSyntax(player, args, 1)) return;
        if(Team.isInTeam(player)) {
            ChatUtils.sendMessage(player, "areal_in_team", new HashMap<>());
            return;
        }
        int maxLength = Main.getInstance().getConfiguration().getInt("team.length.max");
        int minLength = Main.getInstance().getConfiguration().getInt("team.length.min");
        List<String> badWord = Main.getInstance().getConfiguration().getStringList("team.badWord");
        String name = args[0];
        if(badWord.contains(name)) {
            ChatUtils.sendMessage(player, "name_contain_bad_word", new HashMap<>());
            return;
        }
        if(name.length() < minLength) {
            ChatUtils.sendMessage(player, "min_length", new HashMap<>());
            return;
        }
        if(name.length() > maxLength) {
            ChatUtils.sendMessage(player, "max_length", new HashMap<>());
            return;
        }
        new Team(player.getName(), name);
        HashMap<String, String> placeholders = new HashMap<>();
        placeholders.put("team", name);
        ChatUtils.sendMessage(player, "team_created", placeholders);
    }

    @Subcommand("disband")
    public void onDisband(Player player) {
        if(checkLeader(player)) return;
        Team team = Team.getTeam(player);
        team.disband();
        invites.remove(player);
        ChatUtils.sendMessage(player, "team_disband", new HashMap<>());
    }

    private static final HashMap<Player, Player> invites = new HashMap<>();

    @Subcommand("invite")
    public void onInvite(Player player, String[] args) {
        if(checkModerator(player)) return;
        if(checkPlayer(player, args)) return;
        Player target = getPlayer(args);
        if(invites.containsKey(player)) {
            ChatUtils.sendMessage(player, "areal_invite", new HashMap<>());
            return;
        }
        if(Team.isInTeam(target)) {
            ChatUtils.sendMessage(player, "target_in_team", new HashMap<>());
            return;
        }
        if(!Team.getTeam(player).canHaveAnotherPlayer()){
            ChatUtils.sendMessage(player, "team_has_max_player", new HashMap<>());
            return;
        }
        invites.put(player, target);
        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> invites.remove(player), 20L * 10);
        HashMap<String, String> placeholders = new HashMap<>();
        placeholders.put("player", target.getName());
        placeholders.put("send", player.getName());
        placeholders.put("team", Team.getTeam(player).getName());
        ChatUtils.sendMessage(player, "invite_send", placeholders);
        ChatUtils.sendMessage(target, "invited_received", placeholders);
    }


    @Subcommand("accept|join")
    public void onAccept(Player player, String[] args) {
        if(checkPlayer(player, args)) return;
        Player target = getPlayer(args);
        if(!invites.containsKey(target) && invites.get(target) != player) {
            ChatUtils.sendMessage(player, "not_invited", new HashMap<>());
            return;
        }
        if(!Team.isInTeam(target) || !Team.getTeam(target).isModerator(target)) {
            ChatUtils.sendMessage(player, "error_during_accepting", new HashMap<>());
            return;
        }
        Team team = Team.getTeam(target);
        if(!team.canHaveAnotherPlayer()) {
            ChatUtils.sendMessage(player, "error_during_accepting", new HashMap<>());
            return;
        }
        team.getMembers().add(player.getName());
        invites.remove(target);
        ChatUtils.sendMessage(player, "message_accepted", new HashMap<>());
    }

    @Subcommand("promote")
    public void onPromote(Player player, String[] args) {
        if(checkLeader(player)) return;
        if(checkPlayer(player, args)) return;
        Player target = getPlayer(args);
        if(!isEqualTeam(player, target)) {
            ChatUtils.sendMessage(player, "target_not_in_your_team", new HashMap<>());
            return;
        }
        Team team = Team.getTeam(player);
        team.getModerators().add(target.getName());
        ChatUtils.sendMessage(player, "moderator_added", new HashMap<>());
    }

    @Subcommand("demote")
    public void onDemote(Player player, String[] args) {
        if(checkLeader(player)) return;
        if(checkPlayer(player, args)) return;
        Player target = getPlayer(args);
        if(!isEqualTeam(player, target)) {
            ChatUtils.sendMessage(player, "target_not_in_your_team", new HashMap<>());
            return;
        }
        Team team = Team.getTeam(player);
        if(!team.getModerators().contains(target.getName())) {
            ChatUtils.sendMessage(player, "not_moderator", new HashMap<>());
            return;
        }
        team.getModerators().remove(target.getName());
        HashMap<String, String> placeholders = new HashMap<>();
        placeholders.put("player", target.getName());
        ChatUtils.sendMessage(player, "demoted", placeholders);
    }


    @Subcommand("kick")
    public void onKick(Player player, String[] args) {
        if(checkLeader(player)) return;
        if(checkPlayer(player, args)) return;
        Player target = getPlayer(args);
        if(!isEqualTeam(player, target)) {
            ChatUtils.sendMessage(player, "target_not_in_your_team", new HashMap<>());
            return;
        }
        Team team = Team.getTeam(player);
        team.kick(target);
        HashMap<String, String> placeholders = new HashMap<>();
        placeholders.put("player", target.getName());
        ChatUtils.sendMessage(player, "player_removed", placeholders);
    }

    @Subcommand("leave")
    public void onLeave(Player player) {
        if(!Team.isInTeam(player)) {
            ChatUtils.sendMessage(player, "target_not_in_your_team", new HashMap<>());
            return;
        }
        if(Team.getTeam(player).getLeader().equalsIgnoreCase(player.getName())) {
            ChatUtils.sendMessage(player, "you_do_disband", new HashMap<>());
            return;
        }
        Team.getTeam(player).kick(player);
        ChatUtils.sendMessage(player, "you_are_leaved", new HashMap<>());
    }

    @Subcommand("rename")
    public void onRename(Player player, String[] args) {
        if(checkSyntax(player, args, 1)) return;
        if(checkLeader(player)) return;
        int maxLength = Main.getInstance().getConfiguration().getInt("team.length.max");
        int minLength = Main.getInstance().getConfiguration().getInt("team.length.min");
        List<String> badWord = Main.getInstance().getConfiguration().getStringList("team.badWord");
        String name = args[0];
        if(badWord.contains(name)) {
            ChatUtils.sendMessage(player, "name_contain_bad_word", new HashMap<>());
            return;
        }
        if(name.length() < minLength) {
            ChatUtils.sendMessage(player, "min_length", new HashMap<>());
            return;
        }
        if(name.length() > maxLength) {
            ChatUtils.sendMessage(player, "max_length", new HashMap<>());
            return;
        }
        if(Team.getTeam(player).getName().equalsIgnoreCase(name)) {
            ChatUtils.sendMessage(player, "team_equal_name", new HashMap<>());
            return;
        }
        Team.getTeam(player).setName(name);
        ChatUtils.sendMessage(player, "team_renamed", new HashMap<>());
    }

    @Subcommand("show")
    public void onShow(Player player, String[] args) {
        if(checkSyntax(player, args, 1)) return;
        Team team = null;
        Player target = Bukkit.getPlayer(args[0]);
        if(target != null) {
            team = Team.getTeam(target);
        } else {
            team = Team.getTeam(args[0]);
        }
        if(team == null) {
            ChatUtils.sendMessage(player, "team_not_found", new HashMap<>());
            return;
        }
        printTeamInfo(player, team);
    }

    @Subcommand("info")
    public void onInfo(Player player) {
        if(!Team.isInTeam(player)) {
            ChatUtils.sendMessage(player, "team_not_found", new HashMap<>());
            return;
        }
        Team team = Team.getTeam(player);
        printTeamInfo(player, team);
    }

    @Getter
    private static List<Player> playersChat = new ArrayList<>();

    @Subcommand("chat")
    public void onChat(Player player, String[] args) {
        if(!Team.isInTeam(player)) {
            ChatUtils.sendMessage(player, "team_not_found", new HashMap<>());
            return;
        }
        Team team = Team.getTeam(player);
        if(args.length >= 1) {
            StringBuilder m = new StringBuilder();
            for (int i = 0; i < args.length; i++)
                m.append(args[i]).append(" ");

            team.chat(player, m.toString());
            return;
        }

        if(playersChat.contains(player)) {
            playersChat.remove(player);
            ChatUtils.sendMessage(player, "chat_deactivated", new HashMap<>());
            return;
        }
        playersChat.add(player);
        ChatUtils.sendMessage(player, "chat_active", new HashMap<>());
    }

    @HelpCommand
    public void onHelp(Player player) {
        if(player.hasPermission("bsgang.admin")) {
            for (String help : Main.getInstance().getMessageConfiguration().getStringList("helpAdmin"))
                player.sendMessage(ChatUtils.color(help));
          return;
        }
        for (String help : Main.getInstance().getMessageConfiguration().getStringList("help"))
            player.sendMessage(ChatUtils.color(help));
    }

    @Subcommand("set")
    @CommandPermission("bsgang.admin")
    public void onSet(Player player, String[] args) {
        if(checkSyntax(player, args, 3)) return;
        String teamName = args[0];
        if(Team.getTeam(teamName) == null) {
            ChatUtils.sendMessage(player, "team_not_found", new HashMap<>());
            return;
        }
        String stats = args[1];
        try {
            Integer.parseInt(args[2]);
        }catch (Exception e) {
            ChatUtils.sendMessage(player, "syntax_error", new HashMap<>());
            return;
        }
        Team team = Team.getTeam(teamName);
        int quantity = Integer.parseInt(args[2]);
        if(stats.equalsIgnoreCase("kills")) {
            team.setKills(quantity);
        } else if (stats.equalsIgnoreCase("deaths")) {
            team.setDeaths(quantity);
        }else if (stats.equalsIgnoreCase("points")) {
            team.setPoints(quantity);
        } else {
            ChatUtils.sendMessage(player, "syntax_error", new HashMap<>());
            return;
        }
        HashMap<String, String> placeholders = new HashMap<>();
        placeholders.put("clan", team.getName());
        placeholders.put("value", String.valueOf(quantity));
        placeholders.put("stats", stats);
        ChatUtils.sendMessage(player, "set", placeholders);
    }

    @Subcommand("recalculate points")
    @CommandPermission("bsgang.admin")
    public void onRecalculate(Player player) {
        for (Team team : Team.getTeams()) {
            team.setPoints(0);
            for (int i = 0; i < team.getKills() + 1; i++)
                for (int j = 0; j < Main.getInstance().getConfiguration().getInt("team.points.kill") + 1; j++)
                    team.setPoints(team.getPoints() + 1);
            for (int i = 0; i < team.getDeaths() + 1; i++)
                for (int j = 0; j < Main.getInstance().getConfiguration().getInt("team.points.death") + 1; j++)
                    team.setPoints(team.getPoints() - 1);
            if(team.getPoints() < 0)
                team.setPoints(0);
        }

        HashMap<String, String> placeholders = new HashMap<>();
        placeholders.put("stats", "points");
        ChatUtils.sendMessage(player, "recalculated", placeholders);
    }

    /*
    @Subcommand("recalculate kills")
    @CommandPermission("bsgang.admin")
    public void onRecalculateKills(Player player) {
        int killPoints = Main.getInstance().getConfiguration().getInt("team.points.kill");

        for (Team team : Team.getTeams())
            team.setKills(team.getPoints() / killPoints);

        HashMap<String, String> placeholders = new HashMap<>();
        placeholders.put("stats", "kills");
        ChatUtils.sendMessage(player, "recalculated", placeholders);
    }

    @Subcommand("recalculate deaths")
    @CommandPermission("bsgang.admin")
    public void onRecalculateDeaths(Player player) {
        int deathPoints = Main.getInstance().getConfiguration().getInt("team.points.death");

        for (Team team : Team.getTeams())
            team.setDeaths(team.getPoints() / deathPoints);

        HashMap<String, String> placeholders = new HashMap<>();
        placeholders.put("stats", "deaths");
        ChatUtils.sendMessage(player, "recalculated", placeholders);
    }
     */

    @Subcommand("save")
    @CommandPermission("bsgang.admin")
    public void onSave(Player player) {
        Main.getInstance().getDatabase().save(Team.getTeams());
        ChatUtils.sendMessage(player, "save", new HashMap<>());
    }


    @Subcommand("credits")
    public void onCredits(Player player) {
        player.sendMessage(ChatUtils.color("&bbsGang &7created by &bdiscord.gg/betterstudios &7(Plugin available on SpigotMC)"));
    }

    private void printTeamInfo(Player player, Team team) {
        HashMap<String, String> placeholders = new HashMap<>();
        placeholders.put("leader", team.getLeader());
        placeholders.put("members", team.getMembers().size() == 1 ? String.join(", ", team.getMembers().toArray(new String[0])) : String.join(", ", team.getMembers().toArray(new String[0])).substring(2));
        placeholders.put("moderators", team.getModerators().size() == 1 ? String.join(", ", team.getModerators().toArray(new String[0])) : String.join(", ", team.getModerators().toArray(new String[0])).substring(2));
        placeholders.put("kills", String.valueOf(team.getKills()));
        placeholders.put("deaths", String.valueOf(team.getDeaths()));
        placeholders.put("points", String.valueOf(team.getPoints()));
        placeholders.put("name", team.getName());
        for (String help : ChatUtils.replace(Main.getInstance().getMessageConfiguration().getStringList("info"), placeholders))
            player.sendMessage(help);
    }


    private boolean isEqualTeam(Player player, Player target) {
        if(!Team.isInTeam(target)) return false;
        if(!Team.isInTeam(player)) return false;
        return Team.getTeam(player).equals(Team.getTeam(target));
    }

    private boolean checkPlayer(Player player, String[] args) {
        if(checkSyntax(player, args, 1)) return true;
        Player target = Bukkit.getPlayer(args[0]);
        if(target == null) {
            ChatUtils.sendMessage(player, "player_not_found", new HashMap<>());
            return true;
        }
        if(target == player) {
            ChatUtils.sendMessage(player, "player_is_you", new HashMap<>());
            return true;
        }
        return false;
    }

    private Player getPlayer(String[] args) {
        return Bukkit.getPlayer(args[0]);
    }

    private boolean checkLeader(Player player) {
        if(!Team.isInTeam(player)) {
            ChatUtils.sendMessage(player, "team_not_found", new HashMap<>());
            return true;
        }
        if(!Team.isLeader(player)) {
            ChatUtils.sendMessage(player, "your_must_be_leader", new HashMap<>());
            return true;
        }
        return false;
    }

    private boolean checkModerator(Player player) {
        if(!Team.isInTeam(player)) {
            ChatUtils.sendMessage(player, "team_not_found", new HashMap<>());
            return true;
        }
        if(!Team.getTeam(player).isModerator(player)) {
            ChatUtils.sendMessage(player, "you_must_be_moderator", new HashMap<>());
            return true;
        }
        return false;
    }

    private boolean checkSyntax(Player player, String[] args, int min) {
        if(args.length < min) {
            ChatUtils.sendMessage(player, "syntax_error", new HashMap<>());
            return true;
        }
        return false;
    }


}
