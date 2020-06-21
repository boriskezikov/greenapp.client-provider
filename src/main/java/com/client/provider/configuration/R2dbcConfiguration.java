package com.client.provider.configuration;

import com.client.provider.model.property.R2dbcProperties;
import io.r2dbc.client.R2dbc;
import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class R2dbcConfiguration {

    @Bean
    @ConfigurationProperties("db")
    R2dbcProperties postgresConnectionProperties() {
        return new R2dbcProperties();
    }

    @Bean
    static R2dbc r2dbc(ConnectionPool connectionPool) {
        return new R2dbc(connectionPool);
    }

    @Bean
    static ConnectionPool connectionPool(ConnectionFactory connectionFactory) {
        var config = ConnectionPoolConfiguration.builder(connectionFactory)
            .maxSize(2)
            .initialSize(2)
            .build();
        return new ConnectionPool(config);
    }

    @Bean
    static ConnectionFactory connectionFactory(R2dbcProperties properties) {
        var config = PostgresqlConnectionConfiguration.builder()
            .host(properties.host)
            .port(properties.port)
            .database(properties.database)
            .applicationName("client-provider")
            .username(properties.username)
            .password(properties.password)
            .build();
        return new PostgresqlConnectionFactory(config);
    }
}
