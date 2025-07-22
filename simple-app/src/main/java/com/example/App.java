package com.example;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.h2.jdbcx.JdbcDataSource;
import javax.sql.DataSource;

/**
 * Hello world!
 *
 */
@Configuration
public class App {
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
    }

    @Bean(name = "dataSourceOne")
    public DataSource dataSourceOne() {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:db1;DB_CLOSE_DELAY=-1");
        ds.setUser("sa");
        ds.setPassword("");
        return ds;
    }

    @Bean(name = "dataSourceTwo")
    public DataSource dataSourceTwo() {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:db2;DB_CLOSE_DELAY=-1");
        ds.setUser("sa");
        ds.setPassword("");
        return ds;
    }
}
