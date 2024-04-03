package dev.betterstudios.it;

import co.aikar.commands.BukkitCommandManager;
import dev.betterstudios.it.commands.TeamCommand;
import dev.betterstudios.it.database.Database;
import dev.betterstudios.it.database.component.MySQLDatabase;
import dev.betterstudios.it.database.component.SQLiteDatabase;
import dev.betterstudios.it.team.Team;
import dev.betterstudios.it.team.component.AntiPvPEvent;
import dev.betterstudios.it.team.component.ChatEvent;
import dev.betterstudios.it.team.component.PlaceHolderExpansion;
import dev.betterstudios.it.team.component.StatsEvent;
import dev.betterstudios.it.utils.GameFile;
import lombok.Data;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;
import java.util.logging.Level;

@Getter
public final class Main extends JavaPlugin {

    @Getter
    private static Main instance;

    private GameFile configFile;
    private GameFile messageFile;
    private FileConfiguration configuration;
    private FileConfiguration messageConfiguration;
    private Database database;

    @Override
    public void onEnable() {
        instance = this;
        configFile = new GameFile("config.yml");
        messageFile = new GameFile("messages.yml");
        configuration = configFile.getFileConfiguration();
        messageConfiguration = messageFile.getFileConfiguration();
        /*
        if(new LicenseChecker(configuration.getString("license"), "7157").verify()) {
            getLogger().log(Level.SEVERE, "LICENSE IS NOT SET OR IS INVALID");
            getServer().getPluginManager().disablePlugin(Main.getInstance());
            return;
        }
         */
        BukkitCommandManager commandManager = new BukkitCommandManager(this);
        commandManager.registerCommand(new TeamCommand());
        if(!configuration.getBoolean("team.pvp"))
            Bukkit.getPluginManager().registerEvents(new AntiPvPEvent(), this);
        Bukkit.getPluginManager().registerEvents(new ChatEvent(), this);
        Bukkit.getPluginManager().registerEvents(new StatsEvent(), this);
        String usage = configuration.getString("use");
        if(usage.equalsIgnoreCase("SQLITE")) {
            this.database = new SQLiteDatabase();
        } else if (usage.equalsIgnoreCase("MYSQL")) {
            this.database = new MySQLDatabase();
        } else {
            getLogger().log(Level.SEVERE, "PLEASE, RECHECK THE DB USE");
            getServer().getPluginManager().disablePlugin(Main.getInstance());
        }
        database.load();
        Bukkit.getScheduler().runTaskTimer(this, () -> database.save(Team.getTeams()), 0L, 20L * configuration.getInt("team.saveDelay"));
        if(configuration.getBoolean("placeholders.active") && getServer().getPluginManager().isPluginEnabled("PlaceholderAPI"))
            new PlaceHolderExpansion().register();
    }

    @Override
    public void onDisable() {
        database.save(Team.getTeams());
    }
}
