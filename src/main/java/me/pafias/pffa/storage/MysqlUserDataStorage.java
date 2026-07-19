package me.pafias.pffa.storage;

import com.zaxxer.hikari.HikariDataSource;
import me.pafias.pffa.commands.subcommands.LeaderboardCommand;
import me.pafias.pffa.objects.FfaData;
import me.pafias.pffa.objects.UserData;
import me.pafias.pffa.objects.exceptions.StorageException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class MysqlUserDataStorage implements UserDataStorage {

    private final HikariDataSource dataSource;

    public MysqlUserDataStorage(HikariDataSource dataSource) {
        this.dataSource = dataSource;
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement ps = conn.prepareStatement("""
                     CREATE TABLE IF NOT EXISTS ffa (
                                                 uuid varchar(36) NOT NULL,
                                                 kills INT DEFAULT 0 NOT NULL,
                                                 deaths INT DEFAULT 0 NOT NULL,
                                                 killstreak INT DEFAULT 0 NOT NULL,
                                                 PRIMARY KEY (uuid)
                                                 );
                     """)) {
            ps.execute();
        } catch (SQLException e) {
            throw new StorageException("Failed to initialize the ffa table", e);
        }
    }

    private UserData fromResultSet(ResultSet resultSet) throws SQLException {
        return new UserData(
                UUID.fromString(resultSet.getString("uuid")),
                new FfaData(
                        resultSet.getInt("kills"),
                        resultSet.getInt("deaths"),
                        resultSet.getInt("killstreak")
                )
        );
    }

    @Override
    public UserData getUserData(String uuid) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("""
                     SELECT uuid, kills, deaths, killstreak
                     FROM ffa
                     WHERE uuid = ?
                     """)) {
            statement.setString(1, uuid);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next())
                    return null;

                return fromResultSet(resultSet);
            }
        } catch (SQLException e) {
            throw new StorageException("Failed to load data for " + uuid, e);
        }
    }

    @Override
    public void setUserData(UserData userData) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("""
                     INSERT INTO ffa (uuid, kills, deaths, killstreak)
                     VALUES (?, ?, ?, ?)
                     ON DUPLICATE KEY UPDATE
                         kills = VALUES(kills),
                         deaths = VALUES(deaths),
                         killstreak = VALUES(killstreak)
                     """)) {
            statement.setString(1, userData.getUniqueId().toString());
            statement.setInt(2, userData.getFfaData().getKills());
            statement.setInt(3, userData.getFfaData().getDeaths());
            statement.setInt(4, userData.getFfaData().getKillstreak());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new StorageException("Failed to save data for " + userData.getUniqueId(), e);
        }
    }

    @Override
    public void setUserDataBatch(Collection<UserData> userData) {
        if (userData == null || userData.isEmpty())
            return;
        try (Connection connection = dataSource.getConnection()) {

            final boolean previousAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            try (PreparedStatement statement =
                         connection.prepareStatement("""
                                 INSERT INTO ffa (uuid, kills, deaths, killstreak)
                                 VALUES (?, ?, ?, ?)
                                 ON DUPLICATE KEY UPDATE
                                     kills = VALUES(kills),
                                     deaths = VALUES(deaths),
                                     killstreak = VALUES(killstreak)
                                 """)) {
                for (UserData data : userData) {
                    if (data == null)
                        continue;
                    final FfaData ffaData = data.getFfaData();
                    statement.setString(
                            1,
                            data.getUniqueId().toString()
                    );
                    statement.setInt(2, ffaData.getKills());
                    statement.setInt(3, ffaData.getDeaths());
                    statement.setInt(4, ffaData.getKillstreak());
                    statement.addBatch();
                }

                statement.executeBatch();
                connection.commit();
            } catch (Exception e) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackException) {
                    e.addSuppressed(rollbackException);
                }

                throw e;
            } finally {
                try {
                    connection.setAutoCommit(previousAutoCommit);
                } catch (SQLException ignored) {
                }
            }
        } catch (Exception e) {
            throw new StorageException("Failed to batch-save player data", e);
        }
    }

    @Override
    public List<UserData> getTopStatistic(LeaderboardCommand.Statistic statistic, int resultLimit) {
        if (statistic == null || resultLimit <= 0)
            return Collections.emptyList();

        final List<UserData> list = new ArrayList<>();
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement statement = connection.prepareStatement("""
                     SELECT uuid, kills, deaths, killstreak
                     FROM ffa
                     ORDER BY %s DESC
                     LIMIT %s
                     """.formatted(statistic.getDbColumnName(), resultLimit))) {
            try (final ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    list.add(fromResultSet(resultSet));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

}
