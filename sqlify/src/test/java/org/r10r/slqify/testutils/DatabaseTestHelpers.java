package org.r10r.slqify.testutils;

import org.r10r.sqlify.Database;
import org.r10r.sqlify.Sqlify;

public class DatabaseTestHelpers {

    public static void cleanDatabase(Database database) {
      database.withConnection(connection -> {
        Sqlify.sql("DELETE FROM dashboards").executeUpdate(connection);
        return null;
      });
    }

}
