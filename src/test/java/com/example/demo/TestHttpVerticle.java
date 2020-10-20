package com.example.demo;

import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import java.util.concurrent.TimeUnit;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(VertxExtension.class)
public class TestHttpVerticle {
    @Test
    void httpServerRunnable() throws Throwable {
        VertxTestContext testContext = new VertxTestContext();
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new HttpVerticle(), testContext.completing());

        assertThat(testContext.awaitCompletion(5, TimeUnit.SECONDS)).isTrue();
        if (testContext.failed()) {
            throw testContext.causeOfFailure();
        }
    }

}
