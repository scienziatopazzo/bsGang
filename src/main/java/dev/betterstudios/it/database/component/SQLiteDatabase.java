package dev.betterstudios.it.database.component;

import dev.betterstudios.it.Main;
import dev.betterstudios.it.database.Database;
import dev.betterstudios.it.team.Team;
import org.bukkit.Bukkit;

import java.io.File;
import java.sql.*;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class SQLiteDatabase implements Database {

    private Connection connection;

    public SQLiteDatabase() {
        try {
            Class.forName("org.sqlite.JDBC");

            String dbFileName = Main.getInstance().getConfiguration().getString("database.sqlite");

            File dataFolder = Main.getInstance().getDataFolder();
            if (!dataFolder.exists())
                dataFolder.mkdirs();

            File DBDataFolder = new File(dataFolder, "database");
            if (!DBDataFolder.exists())
                DBDataFolder.mkdirs();

            File dbFile = new File(DBDataFolder, dbFileName);

            if (!dbFile.exists())
                dbFile.createNewFile();

            this.connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());

            createTable();
        } catch (Exception e) {
            Main.getInstance().getLogger().log(Level.SEVERE, "PLEASE, RECHECK THE DB INFO (SQLITE)", e);
            Main.getInstance().getServer().getPluginManager().disablePlugin(Main.getInstance());
        }
    }


    @Override
    public void save(List<Team> teams) {
        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {
            reset();
            for (Team team : teams) {
                add(
                        team.getName(),
                        team.getLeader(),
                        String.join(",", team.getModerators()),
                        String.join(",", team.getMembers()),
                        team.getKills(),
                        team.getDeaths(),
                        team.getPoints()
                );
            }
        });
    }

    @Override
    public void load() {
        String query = "SELECT * FROM teams";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String name = resultSet.getString("name");
                    String leader = resultSet.getString("leader");
                    String moderator = resultSet.getString("moderator");
                    String members = resultSet.getString("members");
                    int kills = resultSet.getInt("kills");
                    int death = resultSet.getInt("death");
                    int points = resultSet.getInt("points");
                    new Team(
                            name,
                            leader,
                            Arrays.stream(moderator.split(",")).collect(Collectors.toList()),
                            Arrays.stream(members.split(",")).collect(Collectors.toList()),
                            points,
                            kills,
                            death
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createTable() {
        String query = "CREATE TABLE IF NOT EXISTS teams (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "leader TEXT NOT NULL," +
                "moderator TEXT NOT NULL," +
                "members TEXT NOT NULL," +
                "kills INTEGER NOT NULL," +
                "death INTEGER NOT NULL," +
                "points INTEGER NOT NULL" +
                ")";
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void add(String name, String leader, String moderator, String members, int kills, int death, int points) {
        String query = "INSERT INTO teams (name, leader, moderator, members, kills, death, points) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, name);
            statement.setString(2, leader);
            statement.setString(3, moderator);
            statement.setString(4, members);
            statement.setInt(5, kills);
            statement.setInt(6, death);
            statement.setInt(7, points);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void reset() {
        String query = "DELETE FROM teams";
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
