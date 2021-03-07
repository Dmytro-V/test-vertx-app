package com.test.books.jdbc;

import com.test.books.Book;
import com.test.books.InMemoryBooksStore;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

public class JDBCMainVerticle extends AbstractVerticle {

  private static final Logger LOG = LoggerFactory.getLogger(JDBCMainVerticle.class);
  private InMemoryBooksStore store = new InMemoryBooksStore();
  private JDBCBookRepository bookRepository;

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    //initialize Database Connection
    bookRepository = new JDBCBookRepository(vertx);

    //initialize web server routes
    Router books = Router.router(vertx);
    books.route().handler(BodyHandler.create());
    books.route("/*").handler(StaticHandler.create());

    //GET /books
    getAll(books);
    //GET /books/:isbn
    getBookByISBN(books);
    //POST /books
    createBook(books);
    //PUT /books/:isbn
    updateBook(books);
    //DELETE /books/:isbn
    deleteBook(books);

    registerErrorHandler(books);

    vertx.createHttpServer()
      .requestHandler(books)
      .listen(8888, http -> {
      if (http.succeeded()) {
        startPromise.complete();
        LOG.info("HTTP server strated on port 8888");
      } else {
        startPromise.fail(http.cause());
      }
    });
  }

  private void registerErrorHandler(Router books) {
    books.errorHandler(500, event -> {
      LOG.error("Failed: ", event.failure());
      if (event.failure() instanceof IllegalArgumentException) {
        event.response()
          .putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
          .setStatusCode(HttpResponseStatus.BAD_REQUEST.code())
          .end(new JsonObject().put("error", event.failure().getMessage()).encode());
        return;
      }
      event.response()
        .putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
        .setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code())
        .end(new JsonObject().put("error", event.failure().getMessage()).encode());
    });
  }

  private void deleteBook(Router books) {
    books.delete("/books/:isbn").handler(req -> {
      final String key = req.pathParam("isbn");
      final Book deletedBook = store.delete(key);
      req.response()
        .putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
        .end(JsonObject.mapFrom(deletedBook).encode());
    });
  }

  private void updateBook(Router books) {
    books.put("/books/:isbn").handler(req -> {
      final String key = req.pathParam("isbn");
      final JsonObject requestBody = req.getBodyAsJson();
      final Book updatedBook = store.update(key, requestBody.mapTo(Book.class));
      req.response()
        .putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
        .end(JsonObject.mapFrom(updatedBook).encode());
    });
  }

  private void createBook(Router books) {
    books.post("/books").handler(req -> {
      //read body
      final JsonObject requestBody = req.getBodyAsJson();
      LOG.info("Request Body: ", requestBody);
      //store
      store.add(requestBody.mapTo(Book.class));
      //return response
      req.response()
        .putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
        .setStatusCode(HttpResponseStatus.CREATED.code())
        .end(requestBody.encode());
    });
  }

  private void getBookByISBN(Router books) {
    books.get("/books/:isbn").handler(req -> {
      final String key = req.pathParam("isbn");
      final Book findingBook = store.get(key);
      req.response()
        .putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
        .end(JsonObject.mapFrom(findingBook).encode());
    });
  }

  private void getAll(Router books) {
    books.get("/books").handler(req -> {
      bookRepository.getAll().setHandler(ar -> {
        if (ar.failed()) {
          req.fail(ar.cause());
        }
        req.response()
          .putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
          .end(ar.result().encode());
      });

    });
  }

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(new JDBCMainVerticle());
  }
}
