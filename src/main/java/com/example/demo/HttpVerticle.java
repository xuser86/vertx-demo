package com.example.demo;

import com.example.demo.model.Item;
import com.example.demo.model.User;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpVerticle extends AbstractVerticle {
  final static String AUTH_FAILED_MSG = "You have not provided an authentication token, the one provided has expired, was revoked or is not authentic";
  final static String LOGIN_FAILED_MSG = "Login failed";
  final static String REGISTRATION_SUCCESS_MSG = "Registering successfull";
  final static String CREATION_SUCCESS = "Item created successfull";

  private MongoClient mongo;

  private void login(RoutingContext routingContext) {
    final User reqUser = new User(routingContext.getBodyAsJson());
    final JsonObject query = new JsonObject()
      .put("login", reqUser.getLogin())
      .put("password", reqUser.getPassword());

    mongo.findOne("users", query, null, res -> {
      if (res.succeeded()) {
        if (res.result() == null) {
          routingContext.response()
            .setStatusCode(401)
            .setStatusMessage(LOGIN_FAILED_MSG)
            .end();
        } else {
          User dbUser = new User(res.result());
          routingContext.response()
            .setStatusCode(200)
            .putHeader("content-type", "application/json; charset=utf-8")
            .end(
              new JsonObject()
                .put("token", "TesT." + dbUser.getId() + ".TesT")
                .toString()
            );
        }
      } else {
        res.cause().printStackTrace();
        // NOTE: api extended with login failure response
        routingContext.response()
          .setStatusCode(401)
          .setStatusMessage(LOGIN_FAILED_MSG)
          .end();
      }
    });
  }

  private void register(RoutingContext routingContext) {
    final User reqUser = new User(routingContext.getBodyAsJson());

    mongo.insert("users", reqUser.toJson(), res -> {
      if (res.succeeded()) {
        routingContext.response()
          .setStatusCode(204)
          .setStatusMessage(REGISTRATION_SUCCESS_MSG)
          .end();
      }
    });
  }

  private void getAllItems(RoutingContext routingContext) {
    String authBearer = routingContext.request()
      .getHeader("Authorization");

    Pattern pattern = Pattern.compile("Bearer TesT\\.(.*)\\.TesT");
    Matcher m = pattern.matcher(authBearer);
    if (m.find()) {
      mongo.find("items", new JsonObject().put("owner",m.group(1)), res -> {
        routingContext.response()
          .setStatusCode(200)
          .putHeader("content-type", "application/json; charset=utf-8")
          .end(Json.encodePrettily(res.result()));
      });
    } else {
      routingContext.response()
        .setStatusCode(401)
        .setStatusMessage(AUTH_FAILED_MSG)
        .end();
    }
  }

  private void putItem(RoutingContext routingContext) {
    final Item reqItem = new Item(routingContext.getBodyAsJson());
    String authBearer = routingContext.request()
      .getHeader("Authorization");

    Pattern pattern = Pattern.compile("Bearer TesT\\.(.*)\\.TesT");
    Matcher m = pattern.matcher(authBearer);
    if (m.find()) {
      mongo.insert("items", reqItem.toJson().put("owner", m.group(1)), res -> {
        routingContext.response()
          .setStatusCode(204)
          .setStatusMessage(CREATION_SUCCESS)
          .end();
      });
    } else {
      routingContext.response()
        .setStatusCode(401)
        .setStatusMessage(AUTH_FAILED_MSG)
        .end();
    }
  }

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    JsonObject config = new JsonObject();
    config.put("db_name", config().getString("mongo.db_name", "demo"));
    config.put("connection_string", config().getString("mongo.connection_string", "mongodb://localhost:27017"));

    mongo = MongoClient.create(vertx, config);

    Router router = Router.router(vertx);

    router.get("/items").handler(this::getAllItems);
    router.route("/*").handler(BodyHandler.create());
    router.post("/login").handler(this::login);
    router.post("/register").handler(this::register);
    router.post("/items").handler(this::putItem);

    int port = config().getInteger("http.port", 8080);

    vertx.createHttpServer()
      .requestHandler(router::handle)
      .listen(
        port,
        http -> {
          if (http.succeeded()) {
            startPromise.complete();
            System.out.println("HTTP server started on port " + port);
          } else {
            startPromise.fail(http.cause());
          }
        }
      );
  }

  @Override
  public void stop() {
    mongo.close();
  }
}
