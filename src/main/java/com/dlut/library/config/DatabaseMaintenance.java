package com.dlut.library.config;

import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.Statement;

public final class DatabaseMaintenance {

    private DatabaseMaintenance() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1 || !"cleanup-test-data".equals(args[0])) {
            throw new IllegalArgumentException("Supported command: cleanup-test-data");
        }
        String databaseUrl = System.getenv("DATABASE_URL");
        if (databaseUrl == null || databaseUrl.isBlank()) {
            throw new IllegalStateException("DATABASE_URL is required");
        }

        try (HikariDataSource dataSource = (HikariDataSource)
                new CloudDataSourceConfig().dataSource(databaseUrl);
             Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(
                    "DELETE FROM loans WHERE reader_username IN ('qa_lib_1103', 'qa_reader_1103')");
            statement.executeUpdate(
                    "DELETE FROM users WHERE username IN ('qa_lib_1103', 'qa_reader_1103')");
            statement.executeUpdate(
                    "DELETE FROM books WHERE isbn = '9780000001103'");
        }
        System.out.println("Test data cleanup completed.");
    }
}
