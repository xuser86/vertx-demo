package com.example.demo;

import com.example.demo.model.Item;
import com.example.demo.model.User;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpVerticle extends AbstractVerticle {
  final static String AUTH_FAILED_MSG = "You have not provided an authentication token, the one provided has expired, was revoked or is not authentic";
  final Map<String, User> users = new HashMap<>();
  final Map<String, List<Item>> items = new HashMap<>();

  private void login(RoutingContext routingContext) {
    final User reqUser = new User(routingContext.getBodyAsJson());

    User user = users.get(reqUser.getLogin());
    if (user != null && user.getPassword().equals(reqUser.getPassword())) {
      routingContext.response()
        .setStatusCode(200)
        .putHeader("content-type", "application/json; charset=utf-8")
        .end("{\"token\": \"TesT."+user.getLogin()+".TesT\"}");
    } else {
      // NOTE: api extended with login failure response
      routingContext.response()
        .setStatusCode(401)
        .setStatusMessage("Login failed")
        .end();
    }
  }

  private void register(RoutingContext routingContext) {
    final User reqUser = new User(routingContext.getBodyAsJson());

    users.put(reqUser.getLogin(), reqUser);

    routingContext.response()
      .setStatusCode(204)
      .setStatusMessage("Registering successfull")
      .end();
  }

  private void getAllItems(RoutingContext routingContext) {
    String authBearer = routingContext.request()
      .getHeader("Authorization");

    Pattern pattern = Pattern.compile("Bearer TesT\\.(.*)\\.TesT");
    Matcher m = pattern.matcher(authBearer);
    if (m.find()) {
      routingContext.response()
        .setStatusCode(200)
        .putHeader("content-type", "application/json; charset=utf-8")
        .end(Json.encodePrettily(items.get(m.group(1))));
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
      List<Item> userItems = items.computeIfAbsent(m.group(1), k-> new ArrayList());
      userItems.add(reqItem);

      routingContext.response()
        .setStatusCode(204)
        .setStatusMessage("Item created successfull")
        .end();
    } else {
      routingContext.response()
        .setStatusCode(401)
        .setStatusMessage(AUTH_FAILED_MSG)
        .end();
    }
  }

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
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
}
