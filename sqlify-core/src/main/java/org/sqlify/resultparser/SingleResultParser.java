
package org.sqlify.resultparser;

import java.sql.ResultSet;
import java.util.Optional;
import org.sqlify.PojoHelper;
import org.sqlify.ResultParser;

public class SingleResultParser<T> implements ResultParser<Optional<T>> {
    
    private final Class<T> clazz;

    private SingleResultParser(Class<T> clazz) {
        this.clazz = clazz;
    }
    
    public static <E> SingleResultParser<E> of(Class<E> clazz) {
        return new SingleResultParser<>(clazz);
    }

    @Override
    public Optional<T> parseResultSet(ResultSet resultSet) throws Exception {
        Optional<T> optional;
        if (resultSet.next()) {
            T t = new PojoHelper<>(clazz).convertIntoPojo(resultSet);
            optional = Optional.of(t);
        } else {
            optional = Optional.empty();
        }
        return optional;
    }
    
}
