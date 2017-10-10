package com.indbiz.books.verticles;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.indbiz.books.APIConstants;
import com.indbiz.books.entity.Books;
import com.indbiz.books.service.BooksService;
import com.indbiz.books.service.BooksServiceImpl;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.StaticHandler;

public class BooksVerticle extends AbstractVerticle {

	private static final Logger LOGGER = LoggerFactory.getLogger(BooksVerticle.class);
	private static final String HOST = "0.0.0.0";
	private static final int PORT = 8082;
	private ObjectMapper mapper;

	private BooksService books;

	private void initData() {
		mapper = new ObjectMapper();
		LOGGER.info("Config :  " + config());
		books = new BooksServiceImpl(vertx, config());

		books.initData().setHandler(res -> {
			if (res.failed()) {
				LOGGER.error("Persistence service is not running!");
				res.cause().printStackTrace();
			}
		});
	}

	@Override
	public void start(Future<Void> future) throws Exception {
		initData();

		Router router = Router.router(vertx);
		// CORS support
		Set<String> allowHeaders = new HashSet<>();
		allowHeaders.add("x-requested-with");
		allowHeaders.add("Access-Control-Allow-Origin");
		allowHeaders.add("origin");
		allowHeaders.add("Content-Type");
		allowHeaders.add("accept");
		Set<HttpMethod> allowMethods = new HashSet<>();
		allowMethods.add(HttpMethod.GET);
		allowMethods.add(HttpMethod.POST);
		allowMethods.add(HttpMethod.DELETE);
		allowMethods.add(HttpMethod.PATCH);

		router.route().handler(BodyHandler.create());
		router.route().handler(CorsHandler.create("*").allowedHeaders(allowHeaders).allowedMethods(allowMethods));

		// routes
		router.route("/").handler(routingContext -> {
			HttpServerResponse response = routingContext.response();
			response.putHeader("content-type", "text/html").sendFile("assets/welcome.html");
		});
		router.route("/ui/*").handler(StaticHandler.create("assets"));
		router.get(APIConstants.API_GET).handler(this::getBook);
		router.get(APIConstants.API_LIST_ALL).handler(this::getAllBooks);
		router.post(APIConstants.API_CREATE).handler(this::createBook);
		router.patch(APIConstants.API_UPDATE).handler(this::updateBook);
		router.delete(APIConstants.API_DELETE).handler(this::deleteBook);

		vertx.createHttpServer().requestHandler(router::accept).listen(config().getInteger("http.port", PORT),
				config().getString("http.address", HOST), result -> {
					if (result.succeeded())
						future.complete();
					else
						future.fail(result.cause());
				});
		LOGGER.info("Server started at - http://localhost:"+PORT);
	}

	private <T> Handler<AsyncResult<T>> resultHandler(RoutingContext context, Consumer<T> consumer) {
		return res -> {
			if (res.succeeded()) {
				consumer.accept(res.result());
			} else {
				serviceUnavailable(context);
			}
		};
	}

	private void createBook(RoutingContext context) {
		try {
			final Books book = wrapObject(mapper.readValue(context.getBodyAsString(), Books.class));
			final String encoded = mapper.writeValueAsString(Json.encodePrettily(book));
			LOGGER.info("Book to be Added : " + book);
			books.insert(book).setHandler(resultHandler(context, res -> {
				if (res) {
					context.response().setStatusCode(201).putHeader("content-type", "application/json").end(encoded);
				} else {
					serviceUnavailable(context);
				}
			}));
		} catch (Exception e) {
			sendError(400, context.response());
		}
	}

	private void getBook(RoutingContext context) {
		int bookId = Integer.parseInt(context.request().getParam("id"));
		if (bookId == 0) {
			sendError(400, context.response());
			return;
		}
		LOGGER.info("Requested book id: " + bookId);
		books.getCertain(bookId).setHandler(resultHandler(context, res -> {
			if (!res.isPresent())
				notFound(context);
			else {
				final String encoded = Json.encodePrettily(res.get());
				LOGGER.info("Book found - " + encoded);
				context.response().putHeader("content-type", "application/json").end(encoded);
			}
		}));
	}

	private void getAllBooks(RoutingContext context) {
		books.getAll().setHandler(resultHandler(context, res -> {
			if (res == null) {
				serviceUnavailable(context);
			} else {
				final String encoded = Json.encodePrettily(res);
				context.response().putHeader("content-type", "application/json").end(encoded);
			}
		}));
	}

	private void updateBook(RoutingContext context) {
		try {
			int bookId = Integer.parseInt(context.request().getParam("id"));
			final Books updatedBook = mapper.readValue(context.getBodyAsString(), Books.class);
			LOGGER.info("Requested book id: " + bookId);
			// handle error
			if (bookId == 0) {
				sendError(400, context.response());
				return;
			}
			books.update(bookId, updatedBook).setHandler(resultHandler(context, res -> {
				if (res == null)
					notFound(context);
				else {
					final String encoded = Json.encodePrettily(res);
					LOGGER.info("Updated book details - " + encoded);
					context.response().putHeader("content-type", "application/json").end(encoded);
				}
			}));
		} catch (Exception e) {
			badRequest(context);
		}
	}

	private Handler<AsyncResult<Boolean>> deleteResultHandler(RoutingContext context) {
		return res -> {
			if (res.succeeded()) {
				if (res.result()) {
					context.response().setStatusCode(204).end();
				} else {
					serviceUnavailable(context);
				}
			} else {
				serviceUnavailable(context);
			}
		};
	}

	private void deleteBook(RoutingContext context) {
		int bookId = Integer.parseInt(context.request().getParam("id"));
		LOGGER.info("Book id to be deleted: " + bookId);
		books.delete(bookId).setHandler(deleteResultHandler(context));
	}

	private void sendError(int statusCode, HttpServerResponse response) {
		response.setStatusCode(statusCode).end();
	}

	private void notFound(RoutingContext context) {
		context.response().setStatusCode(404).end();
	}

	private void badRequest(RoutingContext context) {
		context.response().setStatusCode(400).end();
	}

	private void serviceUnavailable(RoutingContext context) {
		context.response().setStatusCode(503).end();
	}

	private Books wrapObject(Books book) {
		int id = book.getId();
		if (id > book.getIncId()) {
			Books.setIncIdWith(id);
		} else if (id == 0)
			book.setIncId();
		return book;
	}

}
