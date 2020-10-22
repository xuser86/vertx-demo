package com.example.demo.model;

import io.vertx.core.json.JsonObject;

public class User {
  private String id;
  private String login;
  private String password;

  public User() {
    this.id = null;
  }

  public User(String login, String password) {
    this.id = null;
    this.login = login;
    this.password = password;
  }

  public User(JsonObject json) {
    this.id = json.getString("_id");
    this.login = json.getString("login");
    this.password = json.getString("password");
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();

    json.put("login", this.login);
    json.put("password", this.password);

    if (this.id != null) {
      json.put("_id", this.id);
    }

    return json;
  }

  public String getId() {
    return id;
  }

  public User setId(String id) {
    this.id = id;
    return this;
  }

  public String getLogin() {
    return login;
  }

  public User setLogin(String login) {
    this.login = login;
    return this;
  }

  public String getPassword() {
    return password;
  }

  public User setPassword(String password) {
    this.password = password;
    return this;
  }
}
