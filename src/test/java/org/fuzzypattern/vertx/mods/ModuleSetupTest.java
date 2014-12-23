/*
 * Copyright 2014-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fuzzypattern.vertx.mods;

import com.cloudhopper.smpp.SmppServer;
import org.junit.AfterClass;
import org.junit.Test;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;

import static org.vertx.testtools.VertxAssert.assertEquals;
import static org.vertx.testtools.VertxAssert.assertNotNull;
import static org.vertx.testtools.VertxAssert.assertTrue;
import static org.vertx.testtools.VertxAssert.testComplete;


/**
 * @author <a href="http://www.fuzzypattern.org/">Ioannis Alexandrakis</a>
 */
public class ModuleSetupTest extends AbstractSmppTest {
    private static final SmppServer server = createSmppServer();

    @AfterClass
    public static void oneTimeTearDown() {
        server.stop();
    }

    @Test
    public void testSendSms() {
        JsonObject object = new JsonObject();

//        object.putBinary("textBytes", CharsetUtil.encode("This is a vert.x-smpp test message. \u20AC", CharsetUtil.CHARSET_GSM));
        object.putString("textString", "This is a vert.x-smpp test message. \u20AC"); // one of the two is mandatory, bytes or string

        object.putValue("sourceTon", (byte) 0x03); // optional
        object.putValue("sourceNpi", (byte) 0x00); // optional
        object.putValue("destTon", (byte) 0x01); // optional
        object.putValue("destNpi", (byte) 0x01); // optional
        object.putNumber("timeoutMillis", 10000L); // optional

        object.putString("source", "TEST"); // mandatory
        object.putString("destination", System.getProperty("destination", "301234567890")); // mandatory

        getVertx().eventBus().send(ADDRESS, object, new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> message) {
                getContainer().logger().info(message.body());
                assertEquals("ok", message.body().getString("status"));
                testComplete();
            }
        });
    }

    @Override
    public void start() {
        initialize();
        container.deployModule(System.getProperty("vertx.modulename"), getConfig(), new AsyncResultHandler<String>() {
            @Override
            public void handle(AsyncResult<String> asyncResult) {
                assertTrue(asyncResult.succeeded());
                assertNotNull("deploymentID should not be null", asyncResult.result());
                startTests();
            }
        });
    }
}
