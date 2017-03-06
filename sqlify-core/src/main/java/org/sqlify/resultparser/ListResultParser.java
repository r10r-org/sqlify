package org.sqlify.resultparser;

import org.sqlify.ResultParser;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import org.sqlify.PojoHelper;

public class ListResultParser<T> implements ResultParser<List<T>> {
    
    private final Class<T> clazz;

    private ListResultParser(Class<T> clazz) {
        this.clazz = clazz;
    }
    
    public static <E> ListResultParser<E> of(Class<E> clazz) {
        return new ListResultParser<>(clazz);
    }

    @Override
    public List<T> parseResultSet(ResultSet resultSet) throws Exception {
        List<T> list = new ArrayList<>();
        while (resultSet.next()) {
            T t = new PojoHelper<>(clazz).convertIntoPojo(resultSet);
            list.add(t);
        }
        return list;
    }
    
}
