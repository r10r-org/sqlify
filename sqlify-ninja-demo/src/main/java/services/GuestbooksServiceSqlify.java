package services;

import com.google.inject.Inject;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import models.Guestbook;
import ninja.jdbc.NinjaDatasource;
import ninja.jdbc.NinjaDatasources;
import org.sqlify.ConnectionManager;
import org.sqlify.Sqlify;
import org.sqlify.resultparser.ListResultParser;

public class GuestbooksServiceSqlify {

    private final NinjaDatasource ninjaDatasource;

    @Inject
    public GuestbooksServiceSqlify(NinjaDatasources ninjaDatasources) {
        this.ninjaDatasource = ninjaDatasources.getDatasource("default");
    }

    public List<Guestbook> listGuestBookEntries() {
        return ConnectionManager.withConnection(ninjaDatasource.getDataSource(), connection ->
              Sqlify.<List<Guestbook>>
                      sql("SELECT id, email, content FROM guestbooks")
                        .parseResultWith(ListResultParser.of(Guestbook.class))
                        .executeSelect(connection)
        );
    }

    public void createGuestbook(Guestbook guestbook) {
        ConnectionManager.withTransaction(ninjaDatasource.getDataSource(), connection
                -> Sqlify.sql("INSERT INTO guestbooks (email, content) VALUES ({email}, {content})")
                        .withParameter("email", guestbook.email)
                        .withParameter("content", guestbook.content)
                        .executeUpdate(connection)
        );
    }

}
