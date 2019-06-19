package org.r10r.sqlify;

import org.r10r.core.*;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Optional;
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

public class SqlifyTest {
  
  Database database;
  
  @Before
  public void init() {
    DataSource dataSource = ARunningPostgresServer.getDatasource();
    this.database = Database.use(dataSource);
    
  }
  
  @Test(expected = SqlifyException.class)
  public void withParameterThrowsExceptionWhenKeyIsNull() {
        
    database.withConnection(connection ->
        // given
        Sqlify
            .sql("SELECT * FROM TABLE")
            .withParameter(null, "anything")
            .executeSelect(connection)
     );
  }
  
  @Test(expected = SqlifyException.class)
  public void withParameterThrowsExceptionWhenValueIsNull() {
        
    database.withConnection(connection ->
        // given
        Sqlify
            .sql("SELECT * FROM TABLE")
            .withParameter("anything", null)
            .executeSelect(connection)
     );
  }
  
  @Test
  public void withParameterProperlyHandlesOptionals() {
        
    database.withConnection(connection -> {
        // given
        Sqlify.sql("CREATE TABLE test_table (id BIGINT)").executeUpdate(connection);

        // given
        Optional<Long> idOpt = Optional.of(123L);
        Sqlify
            .sql("INSERT INTO test_table (id) VALUES ({id})")
            .withParameter("id", idOpt)
            .executeUpdate(connection);
        
        // then
        Long idInDatabase = Sqlify
            .sql("SELECT * FROM test_table")
            .parseResultWith(SingleResultParser.of(Long.class))
            .executeSelect(connection);
        
        assertThat(idInDatabase).isEqualTo(idOpt.get());

        return null;
    });
  }
  
  
}
