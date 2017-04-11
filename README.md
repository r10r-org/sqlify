# sqlify - Finest Java SQL library

- Continuous integration: [![Build Status](https://api.travis-ci.org/raphaelbauer/sqlify.svg)](https://travis-ci.org/raphaelbauer/sqlify)

# Intro

Goals:
- Simplicity. Just a thin wrapper to execute SQL queries and map results to nice
  Java objects.
- Modern syntax using Java 8 goodies.
- No magic. Easy to debug in case of problems.
- Exceptions are not checked by default. But you can catch them if needed.

Non-Goals:
- It does NOT provide an abstraction of the database.
- It does NOT Provide support for typesafe SQL queries (look for jooq and friends in that case).

# Code

## SELECT query

A simple query looks like:

```
public List<Guestbook> listGuestBookEntries() {
  return database.withConnection(connection -> 
    Sqlify.sql(
      "SELECT id, email, content FROM guestbooks")
      .parseResultWith(ListResultParser.of(Guestbook.class))
      .executeSelect(connection)
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
  return database.withTransaction(connection -> 
    Sqlify.sql(
      "INSERT INTO guestbooks (email, content) VALUES ({email}, {content})")
      .withParameter("email", guestbook.email)
      .withParameter("content", guestbook.content)
      .parseResultWith(SingleResultParser.of(Long.class))
      .executeUpdateAndReturnGeneratedKey(connection)
  );
```

Note that Sqlify supports named parameters and also allows to return generated
keys using a simple command. In addition this query is executed inside
a transaction (database.withTransaction(...()). 

