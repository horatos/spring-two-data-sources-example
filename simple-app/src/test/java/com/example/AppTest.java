package com.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit test for simple App.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = App.class)
public class AppTest {

    @BeforeAll
    static void enableSpringDebugLogs() {
        java.util.logging.Logger root = java.util.logging.Logger.getLogger("");
        root.setLevel(java.util.logging.Level.FINE);
        for (java.util.logging.Handler h : root.getHandlers()) {
            h.setLevel(java.util.logging.Level.FINE);
        }
        java.util.logging.Logger.getLogger("org.springframework").setLevel(java.util.logging.Level.FINE);
    }
    @Autowired
    @Qualifier("dataSourceOne")
    private DataSource dataSourceOne;

    @Autowired
    @Qualifier("dataSourceTwo")
    private DataSource dataSourceTwo;

    @Autowired
    @Qualifier("shardingDataSource")
    private DataSource shardingDataSource;

    @Autowired
    private Controller controller;

    @Test
    public void testApp() {
        assertTrue(true);
    }

    @Test
    public void dataSourcesAreInjected() {
        assertNotNull(dataSourceOne);
        assertNotNull(dataSourceTwo);
        assertNotNull(shardingDataSource);
        assertNotNull(controller);
    }

    @Test
    public void routingDataSourceWorks() throws Exception {
        ShardContext.setShard("ONE");
        try (var conn = shardingDataSource.getConnection()) {
            assertTrue(conn.getMetaData().getURL().contains("db1"));
        }
        ShardContext.setShard("TWO");
        try (var conn = shardingDataSource.getConnection()) {
            assertTrue(conn.getMetaData().getURL().contains("db2"));
        }
        ShardContext.clear();
    }

    @Test
    public void postAndGetWorksAcrossShards() throws Exception {
        controller.post(100, "even");
        controller.post(101, "odd");
        assertEquals("even", controller.get(100));
        assertEquals("odd", controller.get(101));
    }
}
