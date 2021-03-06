package com.test.books;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public class InMemoryBooksStore {

  private Map<Long, Book> books = new HashMap<>();

  public InMemoryBooksStore() {
    books.put(1L, new Book(1L, "Vert.x in Action"));
    books.put(2L, new Book(2L, "Building Microservices.x in Action"));
  }

   public JsonArray getAll() {
     JsonArray all = new JsonArray();
     books.values().forEach(book -> {
       all.add(JsonObject.mapFrom(book));
     });
     return all;
   }

  public Book get(String isbn) {
    Long key = Long.parseLong(isbn);
    Book findingBook = books.get(key);
    if (findingBook == null) {
      throw new NoSuchElementException("ISBN not found");
    }
    return findingBook;
  }

  public void add(final Book entry) {
    books.put(entry.getIsbn(), entry);
  }

  public Book update(String isbn, Book entry) {
    Long key = Long.parseLong(isbn);
    if (!key.equals(entry.getIsbn())) {
      throw new IllegalArgumentException("ISBN does not match!");
    }

    books.put(key, entry);
    return entry;
  }

  public Book delete(String isbn) {
    Long key = Long.parseLong(isbn);
    Book deleteBook = books.remove(key);
    if (deleteBook == null) {
      throw new NoSuchElementException("ISBN not found");
    }
    return deleteBook;
  }
}
