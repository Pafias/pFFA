package me.pafias.pffa.storage;

import com.zaxxer.hikari.HikariDataSource;
import me.pafias.pffa.objects.FfaData;
import me.pafias.pffa.objects.UserData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class MysqlUserDataStorage implements UserDataStorage {

    private final HikariDataSource dataSource;

    public MysqlUserDataStorage(HikariDataSource dataSource) {
        this.dataSource = dataSource;
        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS ffa (" +
                            "uuid varchar(36) NOT NULL, " +
                            "kills INT DEFAULT 0 NOT NULL, " +
                            "deaths INT DEFAULT 0 NOT NULL, " +
                            "killstreak INT DEFAULT 0 NOT NULL, " +
                            "PRIMARY KEY (uuid)" +
                            ");"
            );
            ps.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public UserData getUserData(String uuid) {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM ffa WHERE uuid = ?;");
            statement.setString(1, uuid);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                UUID u = UUID.fromString(resultSet.getString("uuid"));
                int kills = resultSet.getInt("kills");
                int deaths = resultSet.getInt("deaths");
                int killstreak = resultSet.getInt("killstreak");

                return new UserData(false, u, new FfaData(kills, deaths, killstreak));
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void setUserData(UserData userData) {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO ffa (uuid, kills, deaths, killstreak) " +
                            "VALUES (?, ?, ?, ?) " +
                            "ON DUPLICATE KEY UPDATE kills = ?, deaths = ?, killstreak = ?;"
            );
            statement.setString(1, userData.getUniqueId().toString());
            statement.setInt(2, userData.getFfaData().getKills());
            statement.setInt(3, userData.getFfaData().getDeaths());
            statement.setInt(4, userData.getFfaData().getKillstreak());
            statement.setInt(5, userData.getFfaData().getKills());
            statement.setInt(6, userData.getFfaData().getDeaths());
            statement.setInt(7, userData.getFfaData().getKillstreak());

            statement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
