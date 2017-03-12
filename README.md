# sqlify - Finest Java SQL library

- Continuous integration: [![Build Status](https://api.travis-ci.org/raphaelbauer/sqlify.svg)](https://travis-ci.org/raphaelbauer/sqlify)

# Intro

Goals:
- Use SQL to run queries.
- Simple mapping of results to Pojo objects.
- Modern syntax using Java8 goodies.
- No magic. Easy to debug in case of problems.

# Code

But code speaks more than 1000 words. So how does it look like?

## SELECT query

A simple query looks like:
```
public List<Guestbook> listGuestBookEntries() {
  return ConnectionManager.withConnection(ninjaDatasource.getDataSource(), connection
      -> {
    List<Guestbook> result = Sqlify.<List<Guestbook>>sql("SELECT id, email, content FROM guestbooks")
        .parseResultWith(ListResultParser.of(Guestbook.class))
        .executeSelect(connection);
    return result;

  }
  );
}
```

This query highlights Sqlify's straight forward way to query the database and
map the result to user defined Java objects (Guestbook.class). There is nothing
magic about that and every line is almost self-explanatory. That's the way it should be.


## INSERT statements

And an INSERT statement looks like that:

```
public Long createGuestbook(Guestbook guestbook) {
  return ConnectionManager.withTransaction(ninjaDatasource.getDataSource(), connection
      -> Sqlify.<Long>sql("INSERT INTO guestbooks (email, content) VALUES ({email}, {content})")
          .withParameter("email", guestbook.email)
          .withParameter("content", guestbook.content)
          .parseResultWith(SingleResultParser.of(Long.class))
          .executeUpdateAndReturnGeneratedKey(connection)
  );
}
```

Note that Sqlify supports named parameters and also allows to return generated
keys using a simple command.

