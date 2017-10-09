package com.indbiz.books.service;

import java.util.List;
import java.util.Optional;

import com.indbiz.books.entity.Books;

import io.vertx.core.Future;

public interface BooksService {

	Future<Boolean> initData();
	Future<Boolean> insert(Books books);
	Future<List<Books>> getAll();
	Future<Optional<Books>> getCertain(String todoID);
	Future<Books> update(int id, Books newBook);
	Future<Boolean> delete(int id);
	
}
