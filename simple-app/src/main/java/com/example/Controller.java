package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Simple component acting as a controller in a web application.
 */
@Component
public class Controller {

    private final DataSource dataSource;
    private final DataSource dataSourceOne;
    private final DataSource dataSourceTwo;
    private final TransactionTemplate txTemplateOne;
    private final TransactionTemplate txTemplateTwo;

    @Autowired
    public Controller(@Qualifier("shardingDataSource") DataSource dataSource,
                      @Qualifier("dataSourceOne") DataSource dataSourceOne,
                      @Qualifier("dataSourceTwo") DataSource dataSourceTwo,
                      @Qualifier("txTemplateOne") TransactionTemplate txTemplateOne,
                      @Qualifier("txTemplateTwo") TransactionTemplate txTemplateTwo) {
        this.dataSource = dataSource;
        this.dataSourceOne = dataSourceOne;
        this.dataSourceTwo = dataSourceTwo;
        this.txTemplateOne = txTemplateOne;
        this.txTemplateTwo = txTemplateTwo;
    }


    @Transactional
    public String get(int id) throws SQLException {
        String shard = (id % 2 == 0) ? "ONE" : "TWO";
        ShardContext.setShard(shard);
        var conn = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement ps = conn.prepareStatement("SELECT s FROM entries WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                }
                return null;
            }
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
            ShardContext.clear();
        }
    }

    @Transactional
    public void post(int id, String s) throws SQLException {
        String shard = (id % 2 == 0) ? "ONE" : "TWO";
        ShardContext.setShard(shard);
        var conn = DataSourceUtils.getConnection(dataSource);
        try (var ps = conn.prepareStatement("INSERT INTO entries(id, s) VALUES(?, ?)")) {
            ps.setInt(1, id);
            ps.setString(2, s);
            ps.executeUpdate();
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
            ShardContext.clear();
        }
    }

    public String getWithTransactionTemplate(int id) throws SQLException {
        DataSource ds = (id % 2 == 0) ? dataSourceOne : dataSourceTwo;
        TransactionTemplate tt = (id % 2 == 0) ? txTemplateOne : txTemplateTwo;
        try {
            return tt.execute(status -> {
                var conn = DataSourceUtils.getConnection(ds);
                try (PreparedStatement ps = conn.prepareStatement("SELECT s FROM entries WHERE id = ?")) {
                    ps.setInt(1, id);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            return rs.getString(1);
                        }
                        return null;
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                } finally {
                    DataSourceUtils.releaseConnection(conn, ds);
                }
            });
        } catch (RuntimeException e) {
            if (e.getCause() instanceof SQLException se) {
                throw se;
            }
            throw e;
        }
    }

    public void postWithTransactionTemplate(int id, String s) throws SQLException {
        DataSource ds = (id % 2 == 0) ? dataSourceOne : dataSourceTwo;
        TransactionTemplate tt = (id % 2 == 0) ? txTemplateOne : txTemplateTwo;
        try {
            tt.executeWithoutResult(status -> {
                var conn = DataSourceUtils.getConnection(ds);
                try (var ps = conn.prepareStatement("INSERT INTO entries(id, s) VALUES(?, ?)")) {
                    ps.setInt(1, id);
                    ps.setString(2, s);
                    ps.executeUpdate();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                } finally {
                    DataSourceUtils.releaseConnection(conn, ds);
                }
            });
        } catch (RuntimeException e) {
            if (e.getCause() instanceof SQLException se) {
                throw se;
            }
            throw e;
        }
    }
}
