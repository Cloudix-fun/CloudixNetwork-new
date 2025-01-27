package ru.hogeltbellai.CloudixNetwork.impl;

import org.bukkit.scheduler.BukkitRunnable;
import ru.hogeltbellai.CloudixNetwork.CNPluginSpigot;

import java.util.concurrent.TimeUnit;

public class CPlayerManager {

    public static boolean mutePlayer(String playerName, long duration, TimeUnit unit, String reason, String sender) {
        long endTimeMillis = System.currentTimeMillis() + unit.toMillis(duration);
        long startTimeMillis = System.currentTimeMillis();
        String sql = "INSERT INTO mutes (username, start_time, end_time, reason, muted_by, status) VALUES (?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE end_time = ?, reason = ?, muted_by = ?, status = ?";
        CNPluginSpigot.core().getDatabase().executeUpdate(sql, playerName, startTimeMillis, endTimeMillis, reason, sender, 1, endTimeMillis, reason, sender, 1);
        return true;
    }

    public static boolean unmutePlayer(String playerName) {
        String sql = "UPDATE mutes SET status = 0 WHERE username = ?";
        CNPluginSpigot.core().getDatabase().executeUpdate(sql, playerName);
        return true;
    }

    public static boolean isMuted(String playerName) {
        String sql = "SELECT COUNT(*) FROM mutes WHERE username = ? AND status = 1 AND end_time > ?";
        long currentTimeMillis = System.currentTimeMillis();
        Integer count = CNPluginSpigot.core().getDatabase().executeQuery(sql, resultSet -> {
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
            return 0;
        }, playerName, currentTimeMillis);
        return count != null && count > 0;
    }

    public static boolean banPlayer(String playerName, long duration, TimeUnit unit, String reason, String sender) {
        long startTimeMillis = System.currentTimeMillis();
        long endTimeMillis = startTimeMillis + unit.toMillis(duration);
        String sql = "INSERT INTO bans (username, start_time, end_time, reason, banned_by, status) VALUES (?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE end_time = ?, reason = ?, banned_by = ?";
        CNPluginSpigot.core().getDatabase().executeUpdate(sql, playerName, startTimeMillis, endTimeMillis, reason, sender, 1, endTimeMillis, reason, sender);
        return true;
    }

    public static boolean unbanPlayer(String playerName) {
        String sql = "UPDATE bans SET status = 0 WHERE username = ?";
        CNPluginSpigot.core().getDatabase().executeUpdate(sql, playerName);
        return true;
    }

    public static boolean isBanned(String playerName) {
        String sql = "SELECT COUNT(*) FROM bans WHERE username = ? AND status = 1 AND end_time > ?";
        long currentTimeMillis = System.currentTimeMillis();
        Integer count = CNPluginSpigot.core().getDatabase().executeQuery(sql, resultSet -> {
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
            return 0;
        }, playerName, currentTimeMillis);
        return count != null && count > 0;
    }

    public static String getMutedBy(String playerName) {
        String sql = "SELECT muted_by FROM mutes WHERE username = ? AND status = 1";
        return CNPluginSpigot.core().getDatabase().executeQuery(sql, resultSet -> {
            if (resultSet.next()) {
                return resultSet.getString("muted_by");
            }
            return null;
        }, playerName);
    }

    public static String getBannedBy(String playerName) {
        String sql = "SELECT banned_by FROM bans WHERE username = ? AND status = 1";
        return CNPluginSpigot.core().getDatabase().executeQuery(sql, resultSet -> {
            if (resultSet.next()) {
                return resultSet.getString("banned_by");
            }
            return null;
        }, playerName);
    }

    public static String getMuteReason(String playerName) {
        String sql = "SELECT reason FROM mutes WHERE username = ? AND status = 1";
        return CNPluginSpigot.core().getDatabase().executeQuery(sql, resultSet -> {
            if (resultSet.next()) {
                return resultSet.getString("reason");
            }
            return null;
        }, playerName);
    }

    public static String getBanReason(String playerName) {
        String sql = "SELECT reason FROM bans WHERE username = ? AND status = 1";
        return CNPluginSpigot.core().getDatabase().executeQuery(sql, resultSet -> {
            if (resultSet.next()) {
                return resultSet.getString("reason");
            }
            return null;
        }, playerName);
    }

    public static String getRemainingMuteTime(String playerName) {
        String sql = "SELECT end_time FROM mutes WHERE username = ? AND status = 1";
        Long endTimeMillis = CNPluginSpigot.core().getDatabase().executeQuery(sql, resultSet -> {
            if (resultSet.next()) {
                return resultSet.getLong("end_time");
            }
            return null;
        }, playerName);

        if (endTimeMillis != null) {
            long remainingTimeMillis = endTimeMillis - System.currentTimeMillis();
            if (remainingTimeMillis > 0) {
                return formatDuration(remainingTimeMillis);
            }
        }
        return "0";
    }

    public static String getRemainingBanTime(String playerName) {
        String sql = "SELECT end_time FROM bans WHERE username = ? AND status = 1";
        Long endTimeMillis = CNPluginSpigot.core().getDatabase().executeQuery(sql, resultSet -> {
            if (resultSet.next()) {
                return resultSet.getLong("end_time");
            }
            return null;
        }, playerName);

        if (endTimeMillis != null) {
            long remainingTimeMillis = endTimeMillis - System.currentTimeMillis();
            if (remainingTimeMillis > 0) {
                return formatDuration(remainingTimeMillis);
            }
        }
        return "0";
    }

    public static void startExpirationCheckTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                long currentTimeMillis = System.currentTimeMillis();

                String muteSQL = "UPDATE mutes SET status = 0 WHERE end_time <= ? AND status = 1";
                String banSQL = "UPDATE bans SET status = 0 WHERE end_time <= ? AND status = 1";

                CNPluginSpigot.core().getDatabase().executeUpdate(muteSQL, currentTimeMillis);
                CNPluginSpigot.core().getDatabase().executeUpdate(banSQL, currentTimeMillis);
            }
        }.runTaskTimer(CNPluginSpigot.core(), 20L, 20L);
    }

    public static String formatDuration(long durationMillis) {
        long days = TimeUnit.MILLISECONDS.toDays(durationMillis);
        long hours = TimeUnit.MILLISECONDS.toHours(durationMillis) % 24;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis) % 60;
        long seconds = (long) Math.ceil((double) durationMillis / 1000) % 60;

        StringBuilder formattedDuration = new StringBuilder();
        if (days > 0) {
            formattedDuration.append(days).append("д ");
        }
        if (hours > 0) {
            formattedDuration.append(hours).append("ч ");
        }
        if (minutes > 0) {
            formattedDuration.append(minutes).append("м ");
        }
        if (seconds > 0) {
            formattedDuration.append(seconds).append("с");
        }

        if (seconds == 0) {
            return "сейчас";
        }

        return formattedDuration.toString().trim();
    }

    public static long parseDuration(String input) {
        long milliseconds = 0;
        try {
            char unit = input.charAt(input.length() - 1);
            long value = Long.parseLong(input.substring(0, input.length() - 1));

            switch (unit) {
                case 's':
                    milliseconds = TimeUnit.SECONDS.toMillis(value);
                    break;
                case 'm':
                    milliseconds = TimeUnit.MINUTES.toMillis(value);
                    break;
                case 'h':
                    milliseconds = TimeUnit.HOURS.toMillis(value);
                    break;
                case 'd':
                    milliseconds = TimeUnit.DAYS.toMillis(value);
                    break;
                default:
                    milliseconds = 0;
            }
        } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
            milliseconds = 0;
        }
        return milliseconds;
    }
}