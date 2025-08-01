package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Service
public class AnnotatedService {

    private final DataSource dataSource;

    @Autowired
    public AnnotatedService(@Qualifier("shardingDataSource") DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Transactional
    @UseDataSourceOne
    public String getFromOne(int id) throws SQLException {
        var conn = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement ps = conn.prepareStatement("SELECT s FROM entries WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                }
                return null;
            }
        }
    }

    @Transactional
    @UseDataSourceTwo
    public String getFromTwo(int id) throws SQLException {
        var conn = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement ps = conn.prepareStatement("SELECT s FROM entries WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                }
                return null;
            }
        }
    }

    @Transactional
    @UseDataSourceOne
    public void postToOne(int id, String s) throws SQLException {
        var conn = DataSourceUtils.getConnection(dataSource);
        try (var ps = conn.prepareStatement("INSERT INTO entries(id, s) VALUES(?, ?)")) {
            ps.setInt(1, id);
            ps.setString(2, s);
            ps.executeUpdate();
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }

    @Transactional
    @UseDataSourceTwo
    public void postToTwo(int id, String s) throws SQLException {
        var conn = DataSourceUtils.getConnection(dataSource);
        try (var ps = conn.prepareStatement("INSERT INTO entries(id, s) VALUES(?, ?)")) {
            ps.setInt(1, id);
            ps.setString(2, s);
            ps.executeUpdate();
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }
}
