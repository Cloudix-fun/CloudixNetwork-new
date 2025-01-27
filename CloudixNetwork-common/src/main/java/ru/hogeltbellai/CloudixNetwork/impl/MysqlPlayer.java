package ru.hogeltbellai.CloudixNetwork.impl;
import ru.hogeltbellai.CloudixNetwork.mysql.Database;

import java.util.HashMap;
import java.util.Map;

public class MysqlPlayer {
    private static Database database;

    public MysqlPlayer(Database database) {
        MysqlPlayer.database = database;
    }

    public boolean playerExists(String username) {
        String sql = "SELECT COUNT(*) AS count FROM users WHERE username = ?";
        return database.executeQuery(sql, rs -> rs.next() && rs.getInt("count") > 0, username);
    }

    public void addPlayer(String username) {
        String sql = "INSERT INTO users (username, coins, exp, online) VALUES (?, 0, 0, 1)";
        database.executeUpdate(sql, username);
    }

    public Integer getUserId(String username) {
        String sql = "SELECT id FROM users WHERE username = ?";
        return database.executeQuery(sql, rs -> rs.next() ? rs.getInt("id") : null, username);
    }

    public void setMeta(int userId, String key, String value) {
        String sql = "INSERT INTO users_meta (userid, `key`, value) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE value = VALUES(value)";
        database.executeUpdate(sql, userId, key, value);
    }

    public String getMeta(int userId, String key) {
        String sql = "SELECT value FROM users_meta WHERE userid = ? AND `key` = ?";
        return database.executeQuery(sql, rs -> rs.next() ? rs.getString("value") : null, userId, key);
    }

    public void deleteMeta(int userId, String key) {
        String sql = "DELETE FROM users_meta WHERE userid = ? AND `key` = ?";
        database.executeUpdate(sql, userId, key);
    }

    public Map<String, String> getAllMeta(int userId) {
        String sql = "SELECT `key`, value FROM users_meta WHERE userid = ?";
        return database.executeQuery(sql, rs -> {
            Map<String, String> metaMap = new HashMap<>();
            while (rs.next()) {
                metaMap.put(rs.getString("key"), rs.getString("value"));
            }
            return metaMap;
        }, userId);
    }

    public boolean hasMeta(int userId, String key) {
        String sql = "SELECT COUNT(*) AS count FROM users_meta WHERE userid = ? AND `key` = ?";
        return database.executeQuery(sql, rs -> rs.next() && rs.getInt("count") > 0, userId, key);
    }
}