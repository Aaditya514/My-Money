package com.financetracker.config;

import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.URI;

/**
 * Universal Database Configuration.
 * Automatically adapts Render, Railway, or Heroku DATABASE_URL (postgres:// or postgresql://)
 * into standard JDBC format (jdbc:postgresql://), while preserving support for local PostgreSQL,
 * MySQL, and in-memory H2 test databases.
 */
@Configuration
public class DatabaseConfig {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConfig.class);

    @Value("${spring.datasource.url:${SPRING_DATASOURCE_URL:${DATABASE_URL:}}}")
    private String rawUrl;

    @Value("${spring.datasource.username:${SPRING_DATASOURCE_USERNAME:postgres}}")
    private String defaultUsername;

    @Value("${spring.datasource.password:${SPRING_DATASOURCE_PASSWORD:postgres}}")
    private String defaultPassword;

    @Value("${spring.datasource.driver-class-name:${spring.datasource.driverClassName:${SPRING_DATASOURCE_DRIVER:}}}")
    private String driverClassName;

    @Bean
    @Primary
    public DataSource dataSource() {
        HikariDataSource ds = new HikariDataSource();

        // 1. Detect cloud PaaS DATABASE_URL format (postgres:// or postgresql://)
        if (rawUrl != null && !rawUrl.isEmpty() && (rawUrl.startsWith("postgres://") || rawUrl.startsWith("postgresql://"))) {
            try {
                String cleanUrl = rawUrl.replaceFirst("^postgres(ql)?://", "http://");
                URI uri = new URI(cleanUrl);

                String host = uri.getHost();
                int port = uri.getPort() > 0 ? uri.getPort() : 5432;
                String path = uri.getPath(); // includes leading slash /dbname
                String query = uri.getQuery() != null ? "?" + uri.getQuery() : "";
                String jdbcUrl = "jdbc:postgresql://" + host + ":" + port + path + query;

                ds.setJdbcUrl(jdbcUrl);
                if (uri.getUserInfo() != null) {
                    String[] userInfo = uri.getUserInfo().split(":", 2);
                    ds.setUsername(userInfo[0]);
                    if (userInfo.length > 1) {
                        ds.setPassword(userInfo[1]);
                    }
                }
                ds.setDriverClassName("org.postgresql.Driver");
                log.info("Initialized cloud PostgreSQL DataSource for host '{}' and database '{}'", host, path);
                return ds;
            } catch (Exception e) {
                log.warn("Failed to parse cloud DATABASE_URL URI, falling back to raw JDBC configuration: {}", e.getMessage());
            }
        }

        // 2. Standard JDBC Fallback (jdbc:postgresql://, jdbc:h2:, jdbc:mysql:)
        String jdbcUrl = (rawUrl != null && !rawUrl.trim().isEmpty())
                ? rawUrl.trim()
                : "jdbc:postgresql://localhost:5432/finance_db";

        ds.setJdbcUrl(jdbcUrl);
        ds.setUsername(defaultUsername);
        ds.setPassword(defaultPassword);

        if (driverClassName != null && !driverClassName.trim().isEmpty()) {
            ds.setDriverClassName(driverClassName.trim());
        } else if (jdbcUrl.contains(":h2:")) {
            ds.setDriverClassName("org.h2.Driver");
        } else if (jdbcUrl.contains(":mysql:")) {
            ds.setDriverClassName("com.mysql.cj.jdbc.Driver");
        } else {
            ds.setDriverClassName("org.postgresql.Driver");
        }

        return ds;
    }
}
