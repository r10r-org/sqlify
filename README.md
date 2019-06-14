# Sqlify - Java's finest SQL library [![Build Status](https://api.travis-ci.org/r10r-org/sqlify.svg)](https://travis-ci.org/r10r-org/sqlify)

# Intro

Sqlify is highly inspired by the awesome [Scala Anorm library](https://github.com/playframework/anorm). Unfortunately (or fortunately?) 
Java is quite different than Scala. 
Sqlify is our best attempt to provide a library that is as close to SQL as possible - while at the same time being fun and easy to use for Java developers.

Goals:
- Simplicity. Just a thin wrapper to execute SQL queries and map results to nice Java objects.
- Modern syntax using Java 8 goodies.
- No magic. No annotations. Easy to use and easy to debug in case of problems.

Non-Goals:
- Sqlify does NOT provide an abstraction of the database. Check out Hibernate if you need something like that.
- Sqlify does NOT Provide support for type-safe SQL queries. Look for [Jooq](https://www.jooq.org/) and friends in that case.

# Quick start

First import the depdendency:

    <dependency>
        <groupId>org.r10r</groupId>
        <artifactId>sqlify</artifactId>
        <version>1.X.X</version>
    </dependency>


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

# Concepts

## Unchecked exceptions

Sqlify does not use any checked exceptions. 
That makes default operation and usage of Sqlify straight forward. 
If you want to recover from certain error conditions you can catch Sqlify's SqlifyException. 
SqlifyException always contains the exception that caused it.

## Database - a utility to get connections and transactions

In order to execute SQL queries you generally need a connection or a transaction.
Sqlify provides a utility called "Database" that can provide connections to Sqlify.

```
public class GuestbooksServiceSqlify {

  private final Database database;

  @Inject
  public GuestbooksServiceSqlify(NinjaDatasources ninjaDatasources) {
    // an example from Ninja. But any jdbc Datasource (Spring, JEE...) works.
    database = Database.use(ninjaDatasources.getDatasource("default").getDataSource());
  }

  public List<Guestbook> listGuestBookEntries() {
    return database.withConnection(connection ->
      Sqlify.sql(
        "SELECT id, email, content FROM guestbooks")
        .parseResultWith(ListResultParser.of(Guestbook.class))
        .executeSelect(connection)
    );
  }

  ...

```

The Database utility can also run sql queries inside one transaction:

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
}
```

## SQL and parameters

Sqlify uses named parameters to map values to something inside an Sql statement.
Named parameters are escaped and protect your from any form of Sql injection.

```
Sqlify.sql(
  "INSERT INTO guestbooks (email, content) VALUES ({email}, {content})")
  .withParameter("email", guestbook.email)
  .withParameter("content", guestbook.content)
  .parseResultWith(SingleResultParser.of(Long.class))
  .executeUpdateAndReturnGeneratedKey(connection)
);
```

Named parameters in Sql queries are enclosed in curly braces ('{email}'). The
parameters themselves can be set via .withParameter("content", guestbook.content)
for instance.

## Result parsers

Sql queries often create some kind of output. Sqlify can map the output to what the user expects.

There are basically three different result parsers the user can choose from:

* ListResultParser
* SingleResultParser
* SingleOptionalResultParser

### ListResultParser

The ListResultParser will return a List<...> of items. 

```
return database.withConnection(connection -> {
  List<Guestbook> guestbooks = Sqlify.sql(
    "SELECT id, email, content FROM guestbooks")
    .parseResultWith(ListResultParser.of(Guestbook.class))
    .executeSelect(connection)
  return guestbooks;
});
```

In that example a select query is executed that will return all "guestbook" items stored in the database. 
Also note that ListResultParser.of(Guestbook.class) will automatically map column names to field names in the Guestbook class.

### SingleResultParser

The SingleResultParser expects exactly one result. If the query returns zero results, then an exception is thrown.

The folloing example shows an insert statement that returns a Long as generated key via '.parseResultWith(SingleResultParser.of(Long.class))'.

```
Sqlify.sql(
  "INSERT INTO guestbooks (email, content) VALUES ({email}, {content})")
  .withParameter("email", guestbook.email)
  .withParameter("content", guestbook.content)
  .parseResultWith(SingleResultParser.of(Long.class))
  .executeUpdateAndReturnGeneratedKey(connection)
);
```

### SingleOptionalResultParser

The SingleOptionalResultParser may return one or zero values. It works similar to the SingleResultParser, 
but returns an Optional<...>. The Optional is empty if nothing can be found, or contains the result.

## ResultParser and RowParser

ListResultParser, SingleResultParser and SingleOptionalResultParser are examples of a ResultParser. 
A ResultParser is responsible to parse an entire result - basically all rows of a resultset and iterate over it.

The ResultPareser itself does not parse individual rows - that's where RowParsers come into play.

We have have already seen that - for instance - the ListResultParser can automatically determine the mapping of the individual 
rows via ListResultParser.of(Guestbook.class) or ListResultParser.of(Long.class). 
For many use-cases that is enough, but if you have very specific requirements you can also implement your own RowParser.

```
RowParser mySpecialRowParser = new MySpecialRowPaser();

return database.withConnection(connection -> {
  List<Guestbook> guestbooks = Sqlify.sql(
    "SELECT id, email, content FROM guestbooks")
    .parseResultWith(ListResultParser.of(mySpecialRowParser))     // <-- Tell Sqlify to use your own RowParser
    .executeSelect(connection)
  return guestbooks;
});

```

Both ResultPaser and RowParser are only interfaces and you can specify any mapping you 
want in method '.parseResultWith(mySpecialCustomResultParser))'.


## Batched execution when it comes to performance

JDBC supports a so called batched mode. Instead of sending eg the same INSERT
statement 1000 times you can just send the statement once and provide the 
parameters as batch.

This dramatically improves the performance for a large set of INSERT / UPDATE
statements.

### Example

Let's say we got some people we want to create in the database...

```
List<Person> peopleToCreate = myService.getPeopleToCreate();
```

We then create a list where we can add our batches for efficient
creation of these people
```
List<Batch> batches = new ArrayList<>();

// For each person we create a batch with parameters...
for (Person person: peopleToCreate) {
  Batch batch = Batch.create()
    .withParameter("name", person.name).
    .withParameter("age", person.age);

  batches.add(batch);
}
```

You can then use Sqlify to create and execute the statement in batched mode...

```
database.withConnection(connection -> {
  Sqlify.sqlBatch("INSERT INTO person(name, age) VALUES ({name}, {age})")
    .withBatches(batches)
    .executeUpdate(connection)
});
```

# Releasing (committers only)

Make sure you got gpg installed on your machine. Gpg is as good as gpg2, so
there's not much difference. But the gpg plugin in maven works better with gpg,
so we go with that one

    brew install gpg

Make sure to create a key

    gpg --gen-key

Then list the keys and send the public key to a keyserver so that people can
verify that it's you:

    gpg --keyserver hkp://pool.sks-keyservers.net --send-keys YOUR_PUBLIC_KEY

Make sure to set 

    export GPG_TTY=$(tty)

... that way any input of gpg will be properly shown (entering your passphrase for instance)...

Make sure you set the sonatype credentials in your ~/.m2/settings.xml:

```
<settings>

  <servers>
    <server>
      <id>ossrh</id>
      <username>USERNAME</username>
      <password>PASSWORD</password>
    </server>
  </servers>

</settings>
```


Then you can create  a new release like so:

    mvn release:clean -Prelease
    mvn release:prepare -Prelease
    mvn release:perform -Prelease
