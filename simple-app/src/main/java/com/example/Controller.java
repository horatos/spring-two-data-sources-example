package com.example;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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

    @PersistenceContext(unitName = "emfOne")
    private EntityManager entityManagerOne;

    @PersistenceContext(unitName = "emfTwo")
    private EntityManager entityManagerTwo;

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
        EntityManager em = (id % 2 == 0) ? entityManagerOne : entityManagerTwo;
        TransactionTemplate tt = (id % 2 == 0) ? txTemplateOne : txTemplateTwo;
        try {
            return tt.execute(status -> {
                Entry entry = em.find(Entry.class, id);
                if (entry == null) {
                    return null;
                } else {
                    return entry.s;
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
        EntityManager em = (id % 2 == 0) ? entityManagerOne : entityManagerTwo;
        TransactionTemplate tt = (id % 2 == 0) ? txTemplateOne : txTemplateTwo;
        try {
            tt.executeWithoutResult(status -> {
                Entry entry = new Entry();
                entry.id = id;
                entry.s = s;
                em.persist(entry);
            });
        } catch (RuntimeException e) {
            if (e.getCause() instanceof SQLException se) {
                throw se;
            }
            throw e;
        }
    }
}
