package org.r10r.core;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import javax.sql.DataSource;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.Before;
import org.junit.Test;
import org.r10r.slqify.testutils.ARunningPostgresServer;
import org.r10r.sqlify.Database;
import org.r10r.sqlify.Sqlify;
import org.r10r.sqlify.resultparser.SingleResultParser;
import static org.assertj.core.api.Assertions.*;
import org.r10r.sqlify.SqlifyException;

public class SqlifySingleTest {

  Database database;

  @Before
  public void init() {
    DataSource dataSource = ARunningPostgresServer.getDatasource();
    this.database = Database.use(dataSource);

  }

  @Test
  public void executeUpdateAndReturnGeneratedKeyWorks() {

    database.withConnection(connection -> {

      // given
      Sqlify.sql("CREATE TABLE test_table (id BIGSERIAL PRIMARY KEY, text TEXT)").executeUpdate(connection);

      // when
      OffsetDateTime offsetDateTime = OffsetDateTime.now(ZoneId.of("UTC"));
      Long id = Sqlify
          .sql("INSERT INTO test_table (text) VALUES ({a_text})")
          .withParameter("a_text", "a text")
          .parseResultWith(SingleResultParser.of(Long.class))
          .executeUpdateAndReturnGeneratedKey(connection);

      // then
      String text = Sqlify
          .sql("SELECT text from test_table WHERE id = {id}")
          .withParameter("id", id)
          .parseResultWith(SingleResultParser.of(String.class))
          .executeSelect(connection);

      assertThat(text).isEqualTo("a text");

      Sqlify.sql("DROP TABLE test_table").executeUpdate(connection);
      return null;
    });

  }
  
  @Test
  public void executeUpdateAndReturnGeneratedKey_throwsNiceExceptionIfParseResultWithIsMissing() {

    database.withConnection(connection -> {

      // given
      Sqlify.sql("CREATE TABLE test_table (id BIGSERIAL PRIMARY KEY, text TEXT)").executeUpdate(connection);

      // when
      try {
        Long id = Sqlify
          .sql("INSERT INTO test_table (text) VALUES ({a_text})")
          .withParameter("a_text", "a text")
          // => missing => .parseResultWith(SingleResultParser.of(Long.class))
          .executeUpdateAndReturnGeneratedKey(connection);
          fail("We are missing an exception here...");
      } catch (SqlifyException e) {
        // then
        assertThat(e.getMessage()).isEqualTo("Arg. I don't know how to parse the generated key. Please specify result parser. Example: '.parseResultWith(SingleResultParser.of(Long.class))'");
      }

      Sqlify.sql("DROP TABLE test_table").executeUpdate(connection);
      return null;
    });

  }

}
