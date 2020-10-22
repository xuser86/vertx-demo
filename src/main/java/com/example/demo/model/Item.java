package com.example.demo.model;

import io.vertx.core.json.JsonObject;

public class Item {
  // NOTE: inconsistent spec, api.raml & exercise description differs
  // NOTE: no title filed in description
  private String id;
  private String owner;
  private String name;

  public Item(JsonObject json) {
    this.id = json.getString("_id");
    this.owner = json.getString("owner");
    this.name = json.getString("name");
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();

    json.put("owner", this.owner);
    json.put("name", this.name);

    if (this.id != null) {
      json.put("_id", this.id);
    }

    return json;
  }

  public String getId() {
    return id;
  }

  public Item setId(String id) {
    this.id = id;
    return this;
  }

  public String getOwner() {
    return owner;
  }

  public Item setOwner(String owner) {
    this.owner = owner;
    return this;
  }

  public String getName() {
    return name;
  }

  public Item setName(String name) {
    this.name = name;
    return this;
  }
}
