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

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getOwner() {
    return owner;
  }

  public void setOwner(String owner) {
    this.owner = owner;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
