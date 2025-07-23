package com.example;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.beans.factory.annotation.Qualifier;

import org.h2.jdbcx.JdbcDataSource;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@ComponentScan("com.example")
@EnableTransactionManagement
public class App {

    public static void main(String[] args) {
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

    @Bean(name = "emfOne")
    public LocalContainerEntityManagerFactoryBean entityManagerFactoryOne(@Qualifier("dataSourceOne") DataSource ds) {
        final var emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(ds);
        emf.setPackagesToScan("com.example");
        emf.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        emf.setJpaPropertyMap(Map.of(
                "hibernate.hbm2ddl.auto", "update",
                "hibernate.dialect", "org.hibernate.dialect.H2Dialect"
        ));
        emf.afterPropertiesSet();

        return emf;
    }

    @Bean(name = "emfTwo")
    public LocalContainerEntityManagerFactoryBean entityManagerFactoryTwo(@Qualifier("dataSourceTwo") DataSource ds) {
        final var emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(ds);
        emf.setPackagesToScan("com.example");
        emf.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        emf.setJpaPropertyMap(Map.of(
                "hibernate.hbm2ddl.auto", "update",
                "hibernate.dialect", "org.hibernate.dialect.H2Dialect"
        ));
        emf.afterPropertiesSet();

        return emf;
    }

    @Bean(name = "txTemplateOne")
    public TransactionTemplate txTemplateOne(@Qualifier("emfOne") EntityManagerFactory emf) {
        return new TransactionTemplate(new JpaTransactionManager(emf));
    }

    @Bean(name = "txTemplateTwo")
    public TransactionTemplate txTemplateTwo(@Qualifier("emfTwo") EntityManagerFactory emf) {
        return new TransactionTemplate(new JpaTransactionManager(emf));
    }
}
