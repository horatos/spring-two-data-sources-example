package com.example;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.beans.factory.annotation.Qualifier;

import org.h2.jdbcx.JdbcDataSource;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Hello world!
 *
 */
@Configuration
@ComponentScan("com.example")
@EnableTransactionManagement
public class App {

    public static void main( String[] args )
    {
    }

    @Bean(name = "dataSourceOne")
    public DataSource dataSourceOne() {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:db1;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=2");
        ds.setUser("sa");
        ds.setPassword("");
        return ds;
    }

    @Bean(name = "dataSourceTwo")
    public DataSource dataSourceTwo() {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:db2;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=2");
        ds.setUser("sa");
        ds.setPassword("");
        return ds;
    }

    @Bean(name = "shardingDataSource")
    public DataSource shardingDataSource() {
        ShardingRoutingDataSource ds = new ShardingRoutingDataSource();
        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put("ONE", dataSourceOne());
        targetDataSources.put("TWO", dataSourceTwo());
        ds.setTargetDataSources(targetDataSources);
        ds.setDefaultTargetDataSource(dataSourceOne());
        ds.afterPropertiesSet();
        return ds;
    }

    @Bean
    public PlatformTransactionManager transactionManager(
            @Qualifier("shardingDataSource") DataSource ds) {
        return new DataSourceTransactionManager(ds);
    }

    @Bean(name = "txTemplateOne")
    public TransactionTemplate txTemplateOne(@Qualifier("dataSourceOne") DataSource ds) {
        return new TransactionTemplate(new DataSourceTransactionManager(ds));
    }

    @Bean(name = "txTemplateTwo")
    public TransactionTemplate txTemplateTwo(@Qualifier("dataSourceTwo") DataSource ds) {
        return new TransactionTemplate(new DataSourceTransactionManager(ds));
    }
}
