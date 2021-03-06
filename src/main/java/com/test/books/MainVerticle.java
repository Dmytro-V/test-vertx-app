package com.test.books;

import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

public class MainVerticle extends AbstractVerticle {
  private InMemoryBooksStore store = new InMemoryBooksStore();

  @Override
  public void start(Future<Void> startFuture) throws Exception {
    Router books = Router.router(vertx);
    books.route().handler(BodyHandler.create());

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
        startFuture.complete();
        System.out.println("HTTP server strated on port 8888");
      } else {
        startFuture.fail(http.cause());
      }
    });
  }

  private void registerErrorHandler(Router books) {
    books.errorHandler(500, event -> {
      System.err.println("Failed: " + event.failure());
      event.response()
        .putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
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
      System.out.println("Request Body: " + requestBody);
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
      //Return response
      req.response()
        .putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
        .end(store.getAll().encode());
    });
  }

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(new MainVerticle());
  }
}
