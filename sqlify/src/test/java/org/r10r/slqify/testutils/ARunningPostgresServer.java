package org.r10r.slqify.testutils;



import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.sql.DataSource;
import org.testcontainers.containers.PostgreSQLContainer;


/**
 * A class that starts a postgres server in a static way.
 *
 * Therefore this server can be re-used across different tests.
 * This reduces the time it takes to run integration tests dramatically.
 *
 * Notes:
 * - This does NOT WORK when tests run in parallel.
 * - Re-use of the postgres server can only be guaranteed if all tests run in the same JVM.
 */
public class ARunningPostgresServer {
    private static Logger logger = LoggerFactory.getLogger(ARunningPostgresServer.class);

    private static PostgreSQLContainer postgreSQLContainer;

    static {
        try {
            startPostgres();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        addShutdownHook();
    }

    public static DataSource getDatasource() {
      
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(postgreSQLContainer.getJdbcUrl());
        hikariConfig.setUsername(postgreSQLContainer.getUsername());
        hikariConfig.setPassword(postgreSQLContainer.getPassword());

        return new HikariDataSource(hikariConfig);

    }

    private static void startPostgres() throws Exception {
        String dockerImageName = PostgreSQLContainer.IMAGE + ":11";
        postgreSQLContainer = new PostgreSQLContainer<>(dockerImageName);
        postgreSQLContainer.start();
    }

    private static void addShutdownHook() {
        logger.info("Adding hook to shut-down postgres server");

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                logger.info("Shutting-down embedded postgres Server");
                postgreSQLContainer.stop();
            }
        });
    }
}
