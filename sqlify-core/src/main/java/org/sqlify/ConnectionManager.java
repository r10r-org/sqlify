package org.sqlify;

import java.sql.Connection;
import javax.sql.DataSource;

public class ConnectionManager<T> {

    private final Connection connection;

    public static interface Executeable<T> {

        T executute(Connection connection);
    }

    public static <E> E withConnection(DataSource dataSource, Executeable<E> executeable) {
        return new ConnectionManager<E>(dataSource).withConnection(executeable);
    }

    public static <E> E withTransaction(DataSource dataSource, Executeable<E> executeable) {
        return new ConnectionManager<E>(dataSource).withTransaction(executeable);
    }

    public static <E> E withTransaction(Connection connection, Executeable<E> executeable) {
        return new ConnectionManager<E>(connection).withTransaction(executeable);
    }

    private ConnectionManager(DataSource dataSource) {
        try {
            this.connection = dataSource.getConnection();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private ConnectionManager(Connection connection) {
        this.connection = connection;
    }

    private T withConnection(Executeable<T> executable) {
        return withConnection(true, executable);
    }

    private T withConnection(boolean autocommit, Executeable<T> executable) {
        try {
            connection.setAutoCommit(autocommit);
            return executable.executute(connection);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private T withTransaction(Executeable<T> executeable) {
        return withConnection(false, connection -> {
            try {
                T t = executeable.executute(connection);
                connection.commit();
                return t;

            } catch (Exception e1) {
                try {
                    connection.rollback();
                } catch (Exception e2) {
                    throw new RuntimeException(e2);
                }

                throw new RuntimeException(e1);
            }

        });

    }
}
