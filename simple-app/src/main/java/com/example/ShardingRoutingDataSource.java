package com.example;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * {@link AbstractRoutingDataSource} implementation that routes to a specific
 * {@link javax.sql.DataSource} based on the {@link ShardContext}.
 */
public class ShardingRoutingDataSource extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        return ShardContext.getShard();
    }
}
