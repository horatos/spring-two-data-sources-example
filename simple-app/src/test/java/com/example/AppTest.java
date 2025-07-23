package com.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test for simple App.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = App.class)
public class AppTest {
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
}
