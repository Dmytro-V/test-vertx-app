package com.test.books.jdbc;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLClient;

import java.util.List;

public class JDBCBookRepository {

  private SQLClient sql;

  public JDBCBookRepository(final Vertx vertx) {
    final JsonObject config = new JsonObject();
    config.put("url", "jdbc:postgresql://127.0.0.1/books");
    config.put("driver_class", "org.postgresql.Driver");
    config.put("user", "postgres");
    config.put("password", "secret");  //only for test on the local machine
    sql = JDBCClient.createShared(vertx, config);
  }

  public Future<JsonArray> getAll() {
    final Future<JsonArray> getAll = Future.future();
    sql.query("SELECT * FROM books", ar -> {
      //return error
      if (ar.failed()) {
        getAll.fail(ar.cause());
        return;
      } else {
        //return result
        final List<JsonObject> rows = ar.result().getRows();
        final JsonArray result = new JsonArray();
        rows.forEach(result::add);
        getAll.complete(result);
      }
    });

    return getAll;
  }
}
