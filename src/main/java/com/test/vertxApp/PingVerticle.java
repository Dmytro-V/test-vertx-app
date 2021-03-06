package com.test.vertxApp;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

public class PingVerticle extends AbstractVerticle {

  @Override
  public void start() throws Exception {
    vertx.setPeriodic(1000, id ->{
      sendPing();
    });
  }

  private void sendPing() {
    vertx.eventBus(). request("ping-pong", new JsonObject().put("msg", "ping"), ar -> {
        System.out.println("ping-verticle get result: " + ar.result().body());
    });
  }

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(new PingVerticle());
    vertx.deployVerticle(new PongVerticle());
  }
}
