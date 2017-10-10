package com.indbiz.books.entity;

import java.util.concurrent.atomic.AtomicInteger;

import io.vertx.core.json.JsonObject;

public class Books {
	
	private static final AtomicInteger acc = new AtomicInteger(0);
	
	private int id;
	private String name;
	private String author;
	private String genre;
	private String status;
	
	public Books(Books book) {
		this.name = book.getName();
		this.author = book.getAuthor();
		this.genre = book.getGenre();
		this.status = book.getStatus();
		this.id = book.getId();
	}
	
	public Books(int id, String name, String author, String genre, String status) {
		this.name = name;
		this.author = author;
		this.genre = genre;
		this.status = status;
		this.id = id;
	}
	
	public Books(String name, String author, String genre, String status) {
		this.name = name;
		this.author = author;
		this.genre = genre;
		this.status = status;
	}
	
	public Books(JsonObject book) {
		this.name = book.getString("name");
	    this.author = book.getString("author");
	    this.genre = book.getString("genre");
	    this.status = book.getString("status");
	    this.id = book.getInteger("id");
	}
	
	public Books() {
	}
	
	public JsonObject toJson() {
	    JsonObject json = new JsonObject()
	        .put("name", this.name)
	        .put("author", this.author)
	        .put("genre", this.genre)
	        .put("status", this.status);
	    if (id != 0 ) {
	      json.put("id", this.id);
	    }
	    return json;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public int getId() {
		return this.id;
	}
	
	public void setIncId() {
	  this.id = acc.incrementAndGet();
	}

	public static int getIncId() {
	  return acc.get();
	}

	public static void setIncIdWith(int n) {
	  acc.set(n);
	}
	
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setAuthor(String author) {
		this.author = author;
	}
	
	public String getAuthor() {
		return this.author;
	}
	
	public void setGenre(String genre) {
		this.genre = genre;
	}
	
	public String getGenre() {
		return this.genre;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}
	
	public String getStatus() {
		return this.status;
	}

	public Books merge(Books updatedBook) {
		return new Books(id,
				getOrElse(updatedBook.name,name),
				getOrElse(updatedBook.author,author),
				getOrElse(updatedBook.genre,genre),
				getOrElse(updatedBook.status,status));
	}
	
	private <T> T getOrElse(T value, T defaultValue) {
	    return value == null ? defaultValue : value;
	}

}
