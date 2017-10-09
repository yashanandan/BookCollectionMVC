package com.indbiz.books.service;

import java.util.List;
import java.util.Optional;

import com.indbiz.books.entity.Books;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;

public class BooksServiceImpl implements BooksService {

	private final Vertx vertx;
	private final JsonObject config;
	private final JDBCClient client;
	
	private static final String SQL_CREATE = "CREATE TABLE IF NOT EXISTS `books` (\n" +
		    "  `id` int(11) NOT NULL AUTO_INCREMENT,\n" +
		    "  `name` varchar(255) DEFAULT NULL,\n" +
		    "  `author` varchar(255) DEFAULT NULL,\n" +
		    "  `genre` varchar(255) DEFAULT NULL,\n" +
		    "  `status` varchar(255) DEFAULT NULL,\n" +
		    "  PRIMARY KEY (`id`) )";
	private static final String SQL_INSERT = "INSERT INTO `books` " +
		    "(`name`, `author`, `genre`, `status`) VALUES (?, ?, ?, ?)";
	private static final String SQL_QUERY = "SELECT * FROM books WHERE id = ?";
	private static final String SQL_QUERY_ALL = "SELECT * FROM books";
	private static final String SQL_UPDATE = "UPDATE `books`\n" +
		    "SET\n" +
		    "`name` = ?,\n" +
		    "`author` = ?,\n" +
		    "`genre` = ?\n" +
		    "`status` = ?\n" +
		    "WHERE `id` = ?;";
	private static final String SQL_DELETE = "DELETE FROM `books` WHERE `id` = ?";
	
	public BooksServiceImpl (Vertx vertx, JsonObject config) {
	    this.vertx = vertx;
	    this.config = config;
	    this.client = JDBCClient.createShared(vertx, config);
	}
	
	private Handler<AsyncResult<SQLConnection>> connHandler(Future future, Handler<SQLConnection> handler) {
	    return conn -> {
	      if (conn.succeeded()) {
	        final SQLConnection connection = conn.result();
	        handler.handle(connection);
	      } else {
	        future.fail(conn.cause());
	      }
	    };
	}
	
	@Override
	public Future<Boolean> initData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Future<Boolean> insert(Books books) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Future<List<Books>> getAll() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Future<Optional<Books>> getCertain(String todoID) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Future<Books> update(int id, Books newBook) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Future<Boolean> delete(int id) {
		// TODO Auto-generated method stub
		return null;
	}

	  
}
