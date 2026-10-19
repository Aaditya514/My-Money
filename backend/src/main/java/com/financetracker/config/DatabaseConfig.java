package com.financetracker.config;

import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;

/**
 * Universal Database Configuration.
 * Robustly parses Render, Neon, Supabase, Railway, or Heroku DATABASE_URL (postgres:// or postgresql://)
 * into standard JDBC format (jdbc:postgresql://), while preserving support for local PostgreSQL,
 * MySQL, and in-memory H2 test databases.
 */
@Configuration
public class DatabaseConfig {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConfig.class);

    @Value("${spring.datasource.url:${SPRING_DATASOURCE_URL:${DATABASE_URL:${POSTGRES_URL:}}}}")
    private String rawUrl;

    @Value("${spring.datasource.username:${SPRING_DATASOURCE_USERNAME:${POSTGRES_USER:postgres}}}")
    private String defaultUsername;

    @Value("${spring.datasource.password:${SPRING_DATASOURCE_PASSWORD:${POSTGRES_PASSWORD:postgres}}}")
    private String defaultPassword;

    @Value("${spring.datasource.driver-class-name:${spring.datasource.driverClassName:${SPRING_DATASOURCE_DRIVER:}}}")
    private String driverClassName;

    @Bean
    @Primary
    public DataSource dataSource() {
        HikariDataSource ds = new HikariDataSource();
        ds.setConnectionTimeout(15000); // 15s fail-fast timeout instead of waiting 30s

        String url = (rawUrl != null) ? rawUrl.trim() : "";

        // 1. Detect cloud PaaS DATABASE_URL format (postgres:// or postgresql://)
        if (!url.isEmpty() && (url.startsWith("postgres://") || url.startsWith("postgresql://"))) {
            try {
                String stripped = url.replaceFirst("^postgres(ql)?://", "");
                int atIndex = stripped.lastIndexOf('@');

                if (atIndex != -1) {
                    String userInfo = stripped.substring(0, atIndex);
                    String hostAndRest = stripped.substring(atIndex + 1);

                    String[] userPass = userInfo.split(":", 2);
                    String username = userPass[0];
                    String password = userPass.length > 1 ? userPass[1] : "";

                    try {
                        username = java.net.URLDecoder.decode(username, StandardCharsets.UTF_8);
                        password = java.net.URLDecoder.decode(password, StandardCharsets.UTF_8);
                    } catch (Exception ignored) {}

                    String jdbcUrl = "jdbc:postgresql://" + hostAndRest;
                    ds.setJdbcUrl(jdbcUrl);
                    ds.setUsername(username);
                    ds.setPassword(password);
                    ds.setDriverClassName("org.postgresql.Driver");

                    String host = hostAndRest.contains("/") ? hostAndRest.substring(0, hostAndRest.indexOf('/')) : hostAndRest;
                    log.info("Initialized cloud PostgreSQL DataSource for target host: {}", host);
                    return ds;
                }
            } catch (Exception e) {
                log.warn("Failed to parse cloud database URL, falling back: {}", e.getMessage());
            }
        }

        // 2. Standard JDBC Fallback (jdbc:postgresql://, jdbc:h2:, jdbc:mysql:)
        String jdbcUrl = !url.isEmpty() ? url : "jdbc:postgresql://localhost:5432/finance_db";

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

        log.info("Configured DataSource with JDBC URL: {}", jdbcUrl.replaceAll(":[^/@:]+@", ":****@"));
        return ds;
    }
}
