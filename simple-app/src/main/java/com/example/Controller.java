package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.example.ShardContext;

/**
 * Simple component acting as a controller in a web application.
 */
@Component
public class Controller {

    private final DataSource dataSource;

    @Autowired
    public Controller(@Qualifier("shardingDataSource") DataSource dataSource) {
        this.dataSource = dataSource;
    }


    public String get(int id) throws SQLException {
        String shard = (id % 2 == 0) ? "ONE" : "TWO";
        ShardContext.setShard(shard);
        try (var conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT s FROM entries WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                }
                return null;
            }
        } finally {
            ShardContext.clear();
        }
    }

    public void post(int id, String s) throws SQLException {
        String shard = (id % 2 == 0) ? "ONE" : "TWO";
        ShardContext.setShard(shard);
        try (var conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO entries(id, s) VALUES(?, ?)")) {
            ps.setInt(1, id);
            ps.setString(2, s);
            ps.executeUpdate();
        } finally {
            ShardContext.clear();
        }
    }

    public DataSource getDataSource() {
        return dataSource;
    }
}
