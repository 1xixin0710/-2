package com.dlut.library.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Configuration
@Profile("postgres")
public class CloudDataSourceConfig {

    @Bean(destroyMethod = "close")
    DataSource dataSource(@Value("${DATABASE_URL}") String databaseUrl) {
        String rawUrl = databaseUrl.startsWith("jdbc:")
                ? databaseUrl.substring("jdbc:".length())
                : databaseUrl;
        if (rawUrl.startsWith("postgres://")) {
            rawUrl = "postgresql://" + rawUrl.substring("postgres://".length());
        }

        URI uri = URI.create(rawUrl);
        if (!"postgresql".equals(uri.getScheme()) || uri.getHost() == null) {
            throw new IllegalArgumentException("DATABASE_URL 不是有效的 PostgreSQL 连接地址");
        }

        String userInfo = uri.getRawUserInfo();
        if (userInfo == null || !userInfo.contains(":")) {
            throw new IllegalArgumentException("DATABASE_URL 缺少数据库账号");
        }
        String[] credentials = userInfo.split(":", 2);
        int port = uri.getPort() > 0 ? uri.getPort() : 5432;
        String jdbcUrl = "jdbc:postgresql://" + uri.getHost() + ":" + port + uri.getRawPath()
                + (uri.getRawQuery() == null ? "" : "?" + uri.getRawQuery());

        return DataSourceBuilder.create()
                .driverClassName("org.postgresql.Driver")
                .url(jdbcUrl)
                .username(URLDecoder.decode(credentials[0], StandardCharsets.UTF_8))
                .password(URLDecoder.decode(credentials[1], StandardCharsets.UTF_8))
                .build();
    }
}
