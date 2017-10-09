package com.indbiz.books.entity;

import io.vertx.core.json.JsonObject;

public class Books {
	
	private int id;
	private String name;
	private String author;
	private String genre;
	private String status;
	
	public Books(String name, String author, String genre, String status) {
		this.name = name;
		this.author = author;
		this.genre = genre;
		this.status = status;
		this.id = 0;
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

}
