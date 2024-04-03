package dev.betterstudios.it.database.component;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.betterstudios.it.Main;
import dev.betterstudios.it.database.Database;
import dev.betterstudios.it.team.Team;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

import java.sql.*;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Getter
public class MySQLDatabase implements Database {

    private HikariDataSource source;
    private Connection connection;

    public MySQLDatabase() {
        try {
            HikariConfig config = new HikariConfig();
            ConfigurationSection configuration = Main.getInstance().getConfiguration().getConfigurationSection("database.mysql");
            String host = configuration.getString("host");
            String port = configuration.getString("port");
            String databaseName = configuration.getString("database");
            String username = configuration.getString("username");
            String password = configuration.getString("password");

            config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + databaseName);
            config.setUsername(username);
            config.setPassword(password);
            config.setMaximumPoolSize(10);

            this.source = new HikariDataSource(config);

            this.connection = source.getConnection();
        } catch (Exception e) {
            Main.getInstance().getLogger().log(Level.SEVERE, "PLEASE, RECHECK THE DB INFO (MYSQL)");
            Main.getInstance().getServer().getPluginManager().disablePlugin(Main.getInstance());
            return;
        }

        createTable();
    }


    @Override
    public void save(List<Team> teams) {
        if(teams == null) return;
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
                            kills,
                            death,
                            points
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createTable() {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS teams (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "name VARCHAR(255) NOT NULL," +
                    "leader VARCHAR(255) NOT NULL," +
                    "moderator VARCHAR(255) NOT NULL," +
                    "members VARCHAR(255) NOT NULL," +
                    "kills INT NOT NULL," +
                    "death INT NOT NULL," +
                    "points INT NOT NULL" +
                    ")");
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


    public void reset() {
        String query = "DELETE FROM teams";
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
