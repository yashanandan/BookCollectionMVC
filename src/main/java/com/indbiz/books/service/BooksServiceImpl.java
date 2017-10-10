package com.indbiz.books.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.indbiz.books.entity.Books;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;

public class BooksServiceImpl implements BooksService {

	private final Vertx vertx;
	private final JsonObject config;
	private final JDBCClient client;

	private static final String SQL_CREATE = "CREATE TABLE IF NOT EXISTS `books` (\n"
			+ "  `id` int(11) NOT NULL AUTO_INCREMENT,\n" + "  `name` varchar(255) DEFAULT NULL,\n"
			+ "  `author` varchar(255) DEFAULT NULL,\n" + "  `genre` varchar(255) DEFAULT NULL,\n"
			+ "  `status` varchar(255) DEFAULT NULL,\n" + "  PRIMARY KEY (`id`) )";
	private static final String SQL_INSERT = "INSERT INTO `books` "
			+ "(`name`, `author`, `genre`, `status`) VALUES (?, ?, ?, ?)";
	private static final String SQL_QUERY = "SELECT * FROM books WHERE id = ?";
	private static final String SQL_QUERY_ALL = "SELECT * FROM books";
	private static final String SQL_UPDATE = "UPDATE `books`\n" + "SET\n" + "`name` = ?,\n" + "`author` = ?,\n"
			+ "`genre` = ?,\n" + "`status` = ?\n" + "WHERE `id` = ?";
	private static final String SQL_DELETE = "DELETE FROM `books` WHERE `id` = ?";

	public BooksServiceImpl(Vertx vertx, JsonObject config) {
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
		Future<Boolean> result = Future.future();
		client.getConnection(connHandler(result, connection -> connection.execute(SQL_CREATE, create -> {
			if (create.succeeded()) {
				result.complete(true);
			} else {
				result.fail(create.cause());
			}
			connection.close();
		})));
		return result;
	}

	@Override
	public Future<Boolean> insert(Books book) {
		Future<Boolean> result = Future.future();
		client.getConnection(connHandler(result, connection -> {
			connection.updateWithParams(SQL_INSERT, new JsonArray().add(book.getName()).add(book.getAuthor())
					.add(book.getGenre()).add(book.getStatus()), r -> {
						if (r.failed()) {
							result.fail(r.cause());
						} else {
							result.complete(true);
						}
						connection.close();
					});
		}));
		return result;
	}

	@Override
	public Future<List<Books>> getAll() {
		Future<List<Books>> result = Future.future();
		client.getConnection(connHandler(result, connection -> connection.query(SQL_QUERY_ALL, r -> {
			if (r.failed()) {
				result.fail(r.cause());
			} else {
				List<Books> books = r.result().getRows().stream().map(Books::new).collect(Collectors.toList());
				result.complete(books);
			}
			connection.close();
		})));
		return result;
	}

	@Override
	public Future<Optional<Books>> getCertain(int id) {
		Future<Optional<Books>> result = Future.future();
		client.getConnection(connHandler(result, connection -> {
			connection.queryWithParams(SQL_QUERY, new JsonArray().add(id), r -> {
				if (r.failed()) {
					result.fail(r.cause());
				} else {
					List<JsonObject> list = r.result().getRows();
					if (list == null || list.isEmpty()) {
						result.complete(Optional.empty());
					} else {
						result.complete(Optional.of(new Books(list.get(0))));
					}
				}
				connection.close();
			});
		}));
		return result;
	}

	@Override
	public Future<Books> update(int id, Books updatedBook) {
		Future<Books> result = Future.future();
		client.getConnection(connHandler(result, connection -> {
			this.getCertain(id).setHandler(r -> {
				if (r.failed()) {
					result.fail(r.cause());
				} else {
					Optional<Books> oldBook = r.result();
					if (!oldBook.isPresent()) {
						result.complete(null);
						return;
					}
					Books newBook = oldBook.get().merge(updatedBook);
					int updateId = oldBook.get().getId();
					connection.updateWithParams(SQL_UPDATE, new JsonArray().add(newBook.getName())
							.add(newBook.getAuthor()).add(newBook.getGenre()).add(newBook.getStatus()).add(updateId),
							x -> {
								if (x.failed()) {
									result.fail(r.cause());
								} else {
									result.complete(newBook);
								}
								connection.close();
							});
				}
			});
		}));
		return result;
	}

	private Future<Boolean> deleteProcess(String sql, JsonArray params) {
		Future<Boolean> result = Future.future();
		client.getConnection(connHandler(result, connection -> connection.updateWithParams(sql, params, r -> {
			if (r.failed()) {
				result.complete(false);
			} else {
				result.complete(true);
			}
			connection.close();
		})));
		return result;
	}

	@Override
	public Future<Boolean> delete(int id) {
		return deleteProcess(SQL_DELETE, new JsonArray().add(id));
	}

}
