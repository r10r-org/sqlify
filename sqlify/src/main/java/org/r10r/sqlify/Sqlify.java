package org.r10r.sqlify;

import org.r10r.sqlify.resultparser.ResultParser;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.r10r.sqlify.core.Batch;
import org.r10r.sqlify.core.SqlifyBatched;
import org.r10r.sqlify.core.SqlifySingle;

public final class Sqlify {

  ////////////////////////////////////////////////////////////////////////////
  // Builder pattern
  ////////////////////////////////////////////////////////////////////////////
  /**
   *
   * @param sql SQL with named parameters in curly braces. Example: SELECT *
   * FROM my_table WHERE id = {id}
   * @return a nice Builder for chaining
   */
  public static <E> Builder sql(String sql) {
    return new Builder(sql);
  }

  public static class Builder {

    private final String sql;
    private final Map<String, Object> parameterMap;
    private ResultParser<?> resultParser;

    private Builder(String sql) {
      this.sql = sql;
      this.parameterMap = new HashMap<>();
    }

    /**
     * Provide parameters for placeholders in query.
     * 
     * For instance "SELECT * FROM table WHERE id = {id}
     * 
     * => withParameter("id", 1234L);
     * 
     * @param key The key in your SQL query. Written in curly braces inside your SQL.
     * @param value The value for that query. If it is an Optional it will be unpacked automatically if it contains a value.
     * @return The builder for chaining
     */
    public Builder withParameter(String key, Object value) {
      if (key == null) {
        throw new SqlifyException("Calling withParameter(key, value) with a key that is null. That's not supported.");
      }
      
      if (key == value) {
        throw new SqlifyException("Calling withParameter(key, value) with a value that is is null. That's not supported. Consider using an Optional instead.");
      }
      
      // convenience => If the value for a parameter an empty Optional
      // we don't do anything we it, otherwise we unpack it
      // This makes the client code look nicer.
      
      if (value instanceof Optional) {
        Optional optional = (Optional) value;
        optional.ifPresent(optionalValue -> parameterMap.put(key, optionalValue));
        return this;
      }
      
      parameterMap.put(key, value);
      return this;
    }

    public Builder parseResultWith(ResultParser<?> resultParser) {
      this.resultParser = resultParser;
      return this;
    }

    /**
     * Executes a select. Use 'parseResultWith' to specify a parser that will
     * map the result to nice Java objects.
     *
     * @param connection The connection to use for this query.
     * @return The result as specified via 'parseResultWith'
     */
    public <E> E executeSelect(Connection connection) {
      SqlifySingle sqlifySingle = new SqlifySingle(this.sql, this.resultParser, this.parameterMap);
      return sqlifySingle.<E>executeSelect(connection);
    }

    /**
     * Executes an update (insert, delete statement)
     *
     * @param connection The connection to use for this query.
     * @return The number of lines affected by this query.
     */
    public int executeUpdate(Connection connection) {
      SqlifySingle sqlifySingle = new SqlifySingle(this.sql, this.resultParser, this.parameterMap);
      return sqlifySingle.executeUpdate(connection);
    }

    /**
     * Executes an update (insert, delete statement) and returns the generated
     * key. Define the mapping via a ResultParser. For instance if you expect
     * one Long as result you can use
     * parseResultWith(SingleResultParser.of(Long.class))
     *
     * @param connection The connection to use for this query.
     * @return The generated key.
     */
    public <E> E executeUpdateAndReturnGeneratedKey(Connection connection) {
      SqlifySingle sqlifySingle = new SqlifySingle(this.sql, this.resultParser, this.parameterMap);
      return sqlifySingle.<E>executeUpdateAndReturnGeneratedKey(connection);
    }

  }

  ////////////////////////////////////////////////////////////////////////////
  // Builder pattern for batch mode
  ////////////////////////////////////////////////////////////////////////////
  /**
   *
   * @param sql SQL with named parameters in curly braces. 
   * Example: INSERT INTO keyword_rankings(id, name) values({id}, {name})
   * @return a nice Builder for creating the sql statement
   */
  public static BatchBuilder sqlBatch(String sql) {
    return new BatchBuilder(sql);
  }

  public static class BatchBuilder {

    private final String sql;
    private final List<Batch> batches;

    private BatchBuilder(String sql) {
      this.sql = sql;
      this.batches = new ArrayList<>();
    }

    public BatchBuilder withBatches(List<Batch> batches) {
      this.batches.addAll(batches);
      return this;
    }

    public int[] executeUpdate(Connection connection) {
      SqlifyBatched sqlifyBatched = new SqlifyBatched(this.sql, batches);
      return sqlifyBatched.executeUpdateBatch(connection);
    }

  }

}
