package dev.betterstudios.it;

import co.aikar.commands.BukkitCommandManager;
import dev.betterstudios.it.commands.TeamCommand;
import dev.betterstudios.it.database.Database;
import dev.betterstudios.it.database.component.MySQLDatabase;
import dev.betterstudios.it.database.component.SQLiteDatabase;
import dev.betterstudios.it.team.Team;
import dev.betterstudios.it.team.component.event.AntiPvPEvent;
import dev.betterstudios.it.team.component.event.ChatEvent;
import dev.betterstudios.it.team.component.placeholder.PlaceHolderExpansion;
import dev.betterstudios.it.team.component.event.StatsEvent;
import dev.betterstudios.it.team.component.runnable.TeamRunnable;
import dev.betterstudios.it.utils.GameFile;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.sql.SQLException;
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
    private boolean badLionSupport;
    private boolean lunarClientSupport;

    @Override
    public void onEnable() {
        instance = this;
        loadConfig();
        /*
        if(new LicenseChecker(configuration.getString("license"), "7157").verify()) {
            getLogger().log(Level.SEVERE, "LICENSE IS NOT SET OR IS INVALID");
            disablePlugin();
            return;
        }
         */
        loadDatabase();
        BukkitCommandManager commandManager = new BukkitCommandManager(this);
        commandManager.registerCommand(new TeamCommand());
        if(!configuration.getBoolean("team.pvp"))
            Bukkit.getPluginManager().registerEvents(new AntiPvPEvent(), this);
        Bukkit.getPluginManager().registerEvents(new ChatEvent(), this);
        Bukkit.getPluginManager().registerEvents(new StatsEvent(), this);
                if(configuration.getBoolean("placeholders.active") && getServer().getPluginManager().isPluginEnabled("PlaceholderAPI"))
            new PlaceHolderExpansion().register();
        loadTeamIntegration();
        new TeamRunnable().runTaskTimerAsynchronously(this, 0L, 1L);
    }

    public void disablePlugin() {
        getServer().getPluginManager().disablePlugin(instance);
    }

    @Override
    public void onDisable() {
        database.save(Team.getTeams());
    }

    private void loadDatabase() {
        String usage = configuration.getString("use");
        if(usage.equalsIgnoreCase("SQLITE")) {
            try {
                this.database = new SQLiteDatabase();
            } catch (ClassNotFoundException | IOException | SQLException e) {
                Main.getInstance().getLogger().log(Level.SEVERE, "PLEASE, RECHECK THE DB INFO (SQLITE)", e);
                disablePlugin();
            }
        } else if (usage.equalsIgnoreCase("MYSQL")) {
            try {
                this.database = new MySQLDatabase();
            } catch (SQLException e) {
                Main.getInstance().getLogger().log(Level.SEVERE, "PLEASE, RECHECK THE DB INFO (MYSQL)");
                disablePlugin();
            }
        } else {
            getLogger().log(Level.SEVERE, "PLEASE, RECHECK THE DB USE");
            disablePlugin();
        }
        database.load();
        Bukkit.getScheduler().runTaskTimer(this, () -> database.save(Team.getTeams()), 0L, 20L * configuration.getInt("team.saveDelay"));
    }

    private void loadConfig() {
        configFile = new GameFile("config.yml");
        messageFile = new GameFile("messages.yml");
        configuration = configFile.getFileConfiguration();
        messageConfiguration = messageFile.getFileConfiguration();
    }

    private void loadTeamIntegration() {
        if(getServer().getPluginManager().isPluginEnabled("BadlionClientModAPI"))
            badLionSupport = true;


        if(getServer().getPluginManager().isPluginEnabled("LunarClient-API"))
            lunarClientSupport = true;


    }
}
