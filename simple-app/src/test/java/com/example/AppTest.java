package com.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.sql.DataSource;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for simple App.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = App.class)
public class AppTest {

    @BeforeAll
    static void enableSpringDebugLogs() {
        Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.DEBUG);
        Logger spring = (Logger) LoggerFactory.getLogger("org.springframework");
        spring.setLevel(Level.DEBUG);
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

    @Test
    public void postAndGetWorksAcrossShardsInTwoDifferentThreads() throws Exception {
        final var thread1Throwable = new AtomicReference<Throwable>();
        final var thread2Throwable = new AtomicReference<Throwable>();

        final var thread1 = new Thread(() -> {
            try {
                controller.post(110, "even");
                assertEquals("even", controller.get(100));
            } catch (Throwable e) {
                thread1Throwable.set(e);
            }
        });
        final var thread2 = new Thread(() -> {
            try {
                controller.post(111, "odd");
                assertEquals("odd", controller.get(101));
            } catch (Throwable e) {
                thread2Throwable.set(e);
            }
        });

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        assertNull(thread1Throwable.get(), "thread1 aborted");
        assertNull(thread2Throwable.get(), "thread2 aborted");
    }

    @Test
    public void postAndGetWorksAcrossShardsInSeveralThreads() throws Exception {
        final var tasks = new ArrayList<Callable<Object>>();

        for ( int i = 0 ; i < 50 ; i++ ) {
            int id = i;
            final var s = (i % 2) == 0 ? "even" : "odd";
            tasks.add(() -> {
                controller.post(id, s);
                Thread.sleep(10);
                assertEquals(s, controller.get(id));
                return null;
            });
        }

        final var executor = Executors.newFixedThreadPool(2);
        final var result = executor.invokeAll(tasks);
        for (Future<Object> future : result) {
            assertDoesNotThrow(() -> future.get());
        }
    }

    @Test
    public void postAndGetWithTransactionTemplate() throws Exception {
        controller.postWithTransactionTemplate(200, "even-tx");
        controller.postWithTransactionTemplate(201, "odd-tx");
        assertEquals("even-tx", controller.getWithTransactionTemplate(200));
        assertEquals("odd-tx", controller.getWithTransactionTemplate(201));
    }

    @Test
    public void postAndGetWithAnnotations() throws Exception {
        controller.postWithAnnotations(300, "even-ann");
        controller.postWithAnnotations(301, "odd-ann");
        assertEquals("even-ann", controller.getWithAnnotations(300));
        assertEquals("odd-ann", controller.getWithAnnotations(301));
    }
}
