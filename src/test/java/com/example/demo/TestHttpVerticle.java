package com.example.demo;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.net.ServerSocket;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(VertxExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestHttpVerticle {
  static int port;
  // NOTE: individual tests methods will fail, run in bulk only
  static String token1;
  static String token2;

  @BeforeAll
  public static void setupHttp(Vertx vertx, VertxTestContext testContext) throws Throwable {
    // detect free port
    ServerSocket socket = new ServerSocket(0);
    port = socket.getLocalPort();
    socket.close();

    DeploymentOptions options = new DeploymentOptions()
      .setConfig(new JsonObject().put("http.port", port));

    vertx.deployVerticle(new HttpVerticle(), options, testContext.completing());
  }

  @Test
  @Order(1)
  void registerUser1(Vertx vertx, VertxTestContext testContext) {
    WebClient client = WebClient.create(vertx);

    client
      .post(port, "localhost", "/register")
      .sendJsonObject(new JsonObject()
          .put("login", "user1@domain.com")
          .put("password", "SomePassword1"),
        testContext.succeeding(response -> testContext.verify(() -> {
          assertThat(response.statusCode()).isEqualTo(204);
          testContext.completeNow();
        }))
      );
  }

  @Test
  @Order(2)
  void registerUser2(Vertx vertx, VertxTestContext testContext) {
    WebClient client = WebClient.create(vertx);

    client
      .post(port, "localhost", "/register")
      .sendJsonObject(new JsonObject()
          .put("login", "user2@domain.com")
          .put("password", "Password2"),
        testContext.succeeding(response -> testContext.verify(() -> {
          assertThat(response.statusCode()).isEqualTo(204);
          testContext.completeNow();
        }))
      );
  }

  @Test
  @Order(3)
  void incorrectPassLogin(Vertx vertx, VertxTestContext testContext) {
    WebClient client = WebClient.create(vertx);

    client
      .post(port, "localhost", "/login")
      .sendJsonObject(new JsonObject()
          .put("login", "test")
          .put("password", "test"),
        testContext.succeeding(response -> testContext.verify(() -> {
          assertThat(response.statusCode()).isEqualTo(401);
          testContext.completeNow();
        }))
      );
  }

  @Test
  @Order(4)
  void getTokenUser1(Vertx vertx, VertxTestContext testContext) {
    WebClient client = WebClient.create(vertx);

    client
      .post(port, "localhost", "/login")
      .sendJsonObject(new JsonObject()
          .put("login", "user1@domain.com")
          .put("password", "SomePassword1"),
        testContext.succeeding(response -> testContext.verify(() -> {
          assertThat(response.statusCode()).isEqualTo(200);
          JsonObject body = response.bodyAsJsonObject();
          token1 = body.getString("token");
          testContext.completeNow();
        })));
  }

  @Test
  @Order(5)
  void getTokenUser2(Vertx vertx, VertxTestContext testContext) {
    WebClient client = WebClient.create(vertx);

    client
      .post(port, "localhost", "/login")
      .sendJsonObject(new JsonObject()
          .put("login", "user2@domain.com")
          .put("password", "Password2"),
        testContext.succeeding(response -> testContext.verify(() -> {
          assertThat(response.statusCode()).isEqualTo(200);
          JsonObject body = response.bodyAsJsonObject();
          token2 = body.getString("token");
          testContext.completeNow();
        })));
  }

  @Test
  @Order(6)
  void badTokenOnCreate(Vertx vertx, VertxTestContext testContext) {
    WebClient client = WebClient.create(vertx);

    client
      .post(port, "localhost", "/items")
      .putHeader("Authorization", "Bearer [0ejg-98ertjhnf")
      .sendJsonObject(new JsonObject()
          .put("owner", "Owner #1")
          .put("name", "Name #1"),
        testContext.succeeding(response -> testContext.verify(() -> {
          assertThat(response.statusCode()).isEqualTo(401);
          testContext.completeNow();
        })));
  }

  // NOTE: supplying items from list should simplify code - now working method used
  @Test
  @Order(7)
  void createItem1(Vertx vertx, VertxTestContext testContext) {
    WebClient client = WebClient.create(vertx);

    client
      .post(port, "localhost", "/items")
      .putHeader("Authorization", "Bearer " + token1)
      .sendJsonObject(new JsonObject()
          .put("owner", "Owner #1")
          .put("name", "Name #1"),
        testContext.succeeding(response -> testContext.verify(() -> {
          assertThat(response.statusCode()).isEqualTo(204);
          testContext.completeNow();
        }))
      );
  }

  @Test
  @Order(8)
  void createItem2(Vertx vertx, VertxTestContext testContext) {
    WebClient client = WebClient.create(vertx);

    client
      .post(port, "localhost", "/items")
      .putHeader("Authorization", "Bearer " + token1)
      .sendJsonObject(new JsonObject()
          .put("owner", "Owner #1")
          .put("name", "Name #2"),
        testContext.succeeding(response -> testContext.verify(() -> {
          assertThat(response.statusCode()).isEqualTo(204);
          testContext.completeNow();
        })));
  }

  @Test
  @Order(9)
  void createItem3(Vertx vertx, VertxTestContext testContext) {
    WebClient client = WebClient.create(vertx);

    client
      .post(port, "localhost", "/items")
      .putHeader("Authorization", "Bearer " + token2)
      .sendJsonObject(new JsonObject()
          .put("owner", "Owner #2")
          .put("name", "Name #3"),
        testContext.succeeding(response -> testContext.verify(() -> {
          assertThat(response.statusCode()).isEqualTo(204);
          testContext.completeNow();
        })));
  }

  @Test
  @Order(10)
  void getUser1Items(Vertx vertx, VertxTestContext testContext) {
    WebClient client = WebClient.create(vertx);

    client
      .get(port, "localhost", "/items")
      .putHeader("Authorization", "Bearer " + token1)
      .send(testContext.succeeding(response -> testContext.verify(() -> {
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.bodyAsJsonArray().size()).isEqualTo(2);
        testContext.completeNow();
      })));
  }

  @Test
  @Order(11)
  void getUser2Items(Vertx vertx, VertxTestContext testContext) {
    WebClient client = WebClient.create(vertx);

    client
      .get(port, "localhost", "/items")
      .putHeader("Authorization", "Bearer " + token2)
      .send(testContext.succeeding(response -> testContext.verify(() -> {
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.bodyAsJsonArray().size()).isEqualTo(1);
        testContext.completeNow();
      })));
  }

  @Test
  @Order(12)
  void badTokenOnRetrieve(Vertx vertx, VertxTestContext testContext) {
    WebClient client = WebClient.create(vertx);

    client
      .get(port, "localhost", "/items")
      .putHeader("Authorization", "Bearer dfhgr56u56")
      .send(testContext.succeeding(response -> testContext.verify(() -> {
        assertThat(response.statusCode()).isEqualTo(401);
        testContext.completeNow();
      })));
  }

  @AfterAll
  public static void stopHttp(Vertx vertx, VertxTestContext testContext) {
    vertx.close(testContext.completing());
  }
}
