package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * Component responsible for creating the table on each shard when the
 * application starts.
 */
@Component
public class DatabaseInitializer {

    private final DataSource dataSource;

    @Autowired
    public DatabaseInitializer(@Qualifier("shardingDataSource") DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostConstruct
    public void initTables() throws SQLException {
        // create table on both shards
        createTableForShard("ONE");
        createTableForShard("TWO");
        ShardContext.clear();
    }

    private void createTableForShard(String shard) throws SQLException {
        ShardContext.setShard(shard);
        try (var conn = dataSource.getConnection();
             var stmt = conn.createStatement()) {
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS entries(" +
                            "id INT PRIMARY KEY, s VARCHAR(255))");
        }
    }
}
