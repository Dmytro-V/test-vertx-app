package com.test.books;

public class Book {

  private long isbn;
  private String title;

  public Book() {
  }

  public Book(final long isbn, final String title) {
    this.isbn = isbn;
    this.title = title;
  }

  public long getIsbn() {
    return isbn;
  }

  public void setIsbn(long isbn) {
    this.isbn = isbn;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }
}
