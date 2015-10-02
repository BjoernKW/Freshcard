package com.freshcard.backend.model;

import org.flywaydb.core.Flyway;

/**
 * Created by willy on 24.08.14.
 */
public class Migration {
    public Migration(String url, String username, String password) {
        Flyway flyway = new Flyway();
        flyway.setDataSource(url, username, password);
        flyway.setBaselineOnMigrate(true);
        flyway.migrate();
    }
}
