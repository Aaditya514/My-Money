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
 * - Seamlessly connects to cloud PostgreSQL (Render, Neon, Supabase, Railway) from postgres:// or postgresql:// URLs.
 * - Automatically falls back to an embedded in-memory H2 database (PostgreSQL mode) when running in the cloud
 *   without an external database configured, guaranteeing instant, zero-failure deployment.
 * - Preserves support for local developer PostgreSQL and test suites.
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
        ds.setConnectionTimeout(10000); // 10s fail-fast timeout

        String url = (rawUrl != null) ? rawUrl.trim() : "";
        boolean isCloud = System.getenv("RENDER") != null || System.getenv("PORT") != null || System.getenv("RAILWAY_ENVIRONMENT") != null;

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

                    // Ensure sslmode=require for external cloud databases if not already present
                    if (!hostAndRest.contains("sslmode=") && !hostAndRest.startsWith("localhost") && !hostAndRest.startsWith("127.0.0.1")) {
                        hostAndRest += hostAndRest.contains("?") ? "&sslmode=require" : "?sslmode=require";
                    }

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

        // 2. Standard JDBC URL provided (e.g. jdbc:postgresql://..., jdbc:h2:..., jdbc:mysql:...)
        if (!url.isEmpty() && !url.contains("localhost:5432")) {
            ds.setJdbcUrl(url);
            ds.setUsername(defaultUsername);
            ds.setPassword(defaultPassword);
            if (driverClassName != null && !driverClassName.trim().isEmpty()) {
                ds.setDriverClassName(driverClassName.trim());
            } else if (url.contains(":h2:")) {
                ds.setDriverClassName("org.h2.Driver");
            } else if (url.contains(":mysql:")) {
                ds.setDriverClassName("com.mysql.cj.jdbc.Driver");
            } else {
                ds.setDriverClassName("org.postgresql.Driver");
            }
            log.info("Configured DataSource with provided JDBC URL: {}", url.replaceAll(":[^/@:]+@", ":****@"));
            return ds;
        }

        // 3. Cloud Fallback: If running in cloud and no remote database is configured, use embedded H2 with PostgreSQL mode
        if (isCloud && (url.isEmpty() || url.contains("localhost:5432"))) {
            log.warn("=== CLOUD CONTAINER DETECTED WITHOUT REMOTE DATABASE ===");
            log.warn("No remote PostgreSQL URL provided (SPRING_DATASOURCE_URL or DATABASE_URL).");
            log.warn("Starting with embedded in-memory H2 database (PostgreSQL compatibility mode) for instant zero-config startup.");
            log.warn("To persist data to PostgreSQL, add SPRING_DATASOURCE_URL in Render Environment settings.");
            log.warn("==========================================================");

            String h2Url = "jdbc:h2:mem:finance_db;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=PostgreSQL";
            ds.setJdbcUrl(h2Url);
            ds.setUsername("sa");
            ds.setPassword("");
            ds.setDriverClassName("org.h2.Driver");
            return ds;
        }

        // 4. Local development fallback (localhost PostgreSQL)
        String localJdbcUrl = !url.isEmpty() ? url : "jdbc:postgresql://localhost:5432/finance_db";
        ds.setJdbcUrl(localJdbcUrl);
        ds.setUsername(defaultUsername);
        ds.setPassword(defaultPassword);
        ds.setDriverClassName("org.postgresql.Driver");
        log.info("Configured local PostgreSQL DataSource: {}", localJdbcUrl);
        return ds;
    }
}
