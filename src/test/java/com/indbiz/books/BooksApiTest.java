package com.indbiz.books;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.indbiz.books.entity.Books;
import com.indbiz.books.verticles.BooksVerticle;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(VertxUnitRunner.class)
public class BooksApiTest {

	private final static int PORT = 8084;
	private Vertx vertx;

	private static final Books bookExpected = new Books("I Do What I Do", "Raghuram Rajan", "Non-Fiction, Economics",
			"Bought, yet to read");
	private static final Books bookUpdated = new Books("I Do What I Do", "Raghuram Rajan", "Non-Fiction, Economics",
			"Currently reading");
	
	private static final Logger LOGGER = LoggerFactory.getLogger(BooksVerticle.class);

	@Before
	public void before(TestContext context) {
		vertx = Vertx.vertx();
		JsonObject dbConfig = new JsonObject("{\"url\":\"jdbc:mysql://localhost/books?characterEncoding=UTF-8&useSSL=false\","
				+ "\"driver_class\": \"com.mysql.cj.jdbc.Driver\","
				+ "\"user\": \"books\","
				+ "\"password\": \"books\","
				+ "\"max_pool_size\": 30}");
		final DeploymentOptions options = new DeploymentOptions().setConfig(new JsonObject().put("url", "jdbc:mysql://localhost/books?characterEncoding=UTF-8&useSSL=false")
				.put("driver_class", "com.mysql.cj.jdbc.Driver").put("user","books").put("password", "books").put("max_pool_size", 30).put("http.port", PORT));
		// default config
		BooksVerticle verticle = new BooksVerticle();
		vertx.deployVerticle(verticle, options, context.asyncAssertSuccess());
	}

	@After
	public void after(TestContext context) {
		vertx.close(context.asyncAssertSuccess());
	}

	@Test(timeout = 3000L)
	public void testAdd(TestContext context) throws Exception {
		HttpClient client = vertx.createHttpClient();
		Async async = context.async();
		Books newBook =new Books("I Do What I Do", "Raghuram Rajan", "Non-Fiction, Economics",
				"Bought, yet to read");
		client.post(PORT, "localhost", "/api/books", response -> {
			context.assertEquals(201, response.statusCode());
			LOGGER.info("Insert Successful");
			client.close();
			async.complete();
		}).putHeader("content-type", "application/json").end(Json.encodePrettily(newBook));
	}

	@Test(timeout = 3000L)
	public void testGet(TestContext context) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		HttpClient client = vertx.createHttpClient();
		Async async = context.async();
		client.getNow(PORT, "localhost", "/api/books/1", response -> response.bodyHandler(body -> {
			try {
				Books book = mapper.readValue(body.toString(), Books.class);
				context.assertEquals(book.getName(), bookExpected.getName());
				context.assertEquals(book.getAuthor(), bookExpected.getAuthor());
				context.assertEquals(book.getGenre(), bookExpected.getGenre());
				context.assertEquals(book.getStatus(), bookExpected.getStatus());
				LOGGER.info("Get Successful");
			} catch (Exception e) {
				e.printStackTrace();
			}
			client.close();
			async.complete();
		}));
	}

	@Test(timeout = 3000L)
	public void testUpdateAndDelete(TestContext context) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		HttpClient client = vertx.createHttpClient();
		Async async = context.async();
		Books newBook = new Books("I Do What I Do", "Raghuram Rajan", "Non-Fiction, Economics",
				"Currently reading");
		client.request(HttpMethod.PATCH, PORT, "localhost", "/api/books/1", response -> response.bodyHandler(body -> {
			try {
				Books book = mapper.readValue(body.toString(), Books.class);
				context.assertEquals(book.getStatus(), bookUpdated.getStatus());
				LOGGER.info("Update Successful");
			} catch (Exception e) {
				e.printStackTrace();
			}
			client.request(HttpMethod.DELETE, PORT, "localhost", "/api/books/1", rsp -> {
				context.assertEquals(204, rsp.statusCode());
				LOGGER.info("Delete Successful");
				async.complete();
			}).end();
		})).putHeader("content-type", "application/json").end(Json.encodePrettily(newBook));
	}
}
