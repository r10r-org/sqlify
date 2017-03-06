package org.sqlify;

import java.lang.reflect.Field;
import java.sql.ResultSet;

// Helper to fill a default class with values from a single result set

// first tries to fill fields of a pojo with matching names.
// TODO support beanspec
// TODO support inherited fields
public class PojoHelper<T> {

    Class<T> clazz;

    public PojoHelper(Class<T> clazz) {
        this.clazz = clazz;
    }

    public T convertIntoPojo(ResultSet resultSet) throws Exception {
        T t = clazz.newInstance();
        for (Field field : t.getClass().getDeclaredFields()) {
            String name = field.getName();
            Class<?> type = field.getType();
            boolean isFieldAccessible = field.isAccessible();
            field.setAccessible(true);
            if (type == String.class) {
                String value = resultSet.getString(name);
                field.set(t, value);
            } else if (type == Integer.class) {
                Integer value = resultSet.getInt(name);
                field.set(t, value);
            } else {
                // TODO. Add more conversions
                System.out.println("Ops. not supported... " + field.getName() + " -- " + field.getType());
            }
            field.setAccessible(isFieldAccessible);
        }
        return t;
    }
    
}
