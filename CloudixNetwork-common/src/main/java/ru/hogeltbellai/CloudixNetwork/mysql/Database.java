package ru.hogeltbellai.CloudixNetwork.mysql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

public class Database {
    private final String jdbcUrl;
    private final String username;
    private final String password;
    private final int poolMax;
    private final HikariDataSource hikariData;
    private int queryCount = 0;

    public Database(String jdbcUrl, String username, String password, int poolMax) {
        this.jdbcUrl = jdbcUrl;
        this.username = username;
        this.password = password;
        this.poolMax = poolMax;

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(jdbcUrl);
        hikariConfig.setUsername(username);
        hikariConfig.setPassword(password);
        hikariConfig.setMaximumPoolSize(poolMax);
        hikariConfig.setMinimumIdle(10);
        hikariConfig.setConnectionTimeout(30000);
        hikariConfig.setIdleTimeout(600000);
        hikariConfig.setMaxLifetime(1800000);
        hikariConfig.setLeakDetectionThreshold(15000);

        hikariData = new HikariDataSource(hikariConfig);
    }

    public void finish() {
        if (hikariData != null) {
            try {
                disconnect();
                System.out.println("Соединение с базой данных успешно закрыто");
            } catch (Exception ex) {
                System.err.println("Не удалось закрыть соединение с базой данных " + ex);
            }
        }
    }

    public Connection getConnection() throws SQLException {
        return hikariData.getConnection();
    }

    private void disconnect() {
        if (hikariData != null) {
            hikariData.close();
            System.out.println("Пул соединений с базой данных успешно закрыт");
        }
    }

    public boolean isConnected() {
        try (Connection connect = hikariData.getConnection()) {
            return connect != null;
        } catch (SQLException ex) {
            System.err.println("Проверка соединения с базой данных завершилась ошибкой " + ex);
            return false;
        }
    }

    public void executeUpdate(String sql, Object... params) {
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            setParameters(preparedStatement, params);
            long startTime = System.nanoTime();
            preparedStatement.executeUpdate();
            queryCount++;
            //logQueryExecution(sql, startTime, true);
        } catch (SQLException e) {
            handleException(e, sql);
        }
    }

    public <T> T executeQuery(String sql, ResultSetHandler<T> handler, Object... params) {
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            setParameters(preparedStatement, params);
            long startTime = System.nanoTime();
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                T result = handler.handle(resultSet);
                queryCount++;
                //logQueryExecution(sql, startTime, true);
                return result;
            }
        } catch (SQLException e) {
            handleException(e, sql);
            return null;
        }
    }

    public CompletableFuture<Void> executeUpdateAsync(String sql, Object... params) {
        return CompletableFuture.runAsync(() -> executeUpdate(sql, params));
    }

    public <T> CompletableFuture<T> executeQueryAsync(String sql, ResultSetHandler<T> handler, Object... params) {
        return CompletableFuture.supplyAsync(() -> executeQuery(sql, handler, params));
    }

    private void setParameters(PreparedStatement preparedStatement, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            preparedStatement.setObject(i + 1, params[i]);
        }
    }

    private void logQueryExecution(String sql, long startTime, boolean success) {
        long elapsedTime = System.nanoTime() - startTime;
        System.out.println(String.format("Выполнен запрос #%d: %s [Время: %d мс, Успешно: %s]",
                queryCount, sql, elapsedTime / 1_000_000, success));
    }

    private void handleException(SQLException e, String sql) {
        System.err.println(String.format("Ошибка при выполнении SQL запроса: %s. Сообщение: %s", sql, e.getMessage()) + ": " + e);
    }

    public String getPoolStatus() {
        return String.format("Активных соединений: %d, Ожидающих соединений: %d",
                hikariData.getHikariPoolMXBean().getActiveConnections(),
                hikariData.getHikariPoolMXBean().getIdleConnections());
    }

    @FunctionalInterface
    public interface ResultSetHandler<T> {
        T handle(ResultSet resultSet) throws SQLException;
    }

    public int getQueryCount() {
        return queryCount;
    }

    public void executeFile(Class<?> clazz, String fileSql) {
        InputStream inputStream = clazz.getResourceAsStream("/" + fileSql);

        if (inputStream == null) {
            System.err.println("Файл SQL не найден: " + fileSql + " в классе " + clazz.getName());
            return;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            StringBuilder sqlBuilder = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                sqlBuilder.append(line).append("\n");
            }

            String sql = sqlBuilder.toString();
            String[] queries = sql.split(";");

            for (String query : queries) {
                String trimmedQuery = query.trim();
                if (!trimmedQuery.isEmpty()) {
                    executeUpdate(trimmedQuery);
                }
            }

            System.out.println("SQL команды успешно выполнены из файла: " + fileSql);
        } catch (IOException e) {
            System.err.println("Ошибка при чтении SQL файла: " + fileSql + ": " + e);
        }
    }
}