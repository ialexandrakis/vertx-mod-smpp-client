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
public class AsyncModuleSetupTest extends AbstractSmppTest {


    @Test
    public void testSendSms() {
        JsonObject object = new JsonObject();
        addOptionalConfig(object);

//        object.putBinary("textBytes", CharsetUtil.encode("This is a vert.x-smpp test message. \u20AC", CharsetUtil.CHARSET_GSM));
        object.putString("textString", "This is a vert.x-smpp async test message. \u20AC"); // one of the two is mandatory, bytes or string
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
        JsonObject newConf = getConfig();
        newConf.putString("mode", "async");
        container.deployModule(System.getProperty("vertx.modulename"), newConf, new AsyncResultHandler<String>() {
            @Override
            public void handle(AsyncResult<String> asyncResult) {
                assertTrue(asyncResult.succeeded());
                assertNotNull("deploymentID should not be null", asyncResult.result());
                startTests();
            }
        });
    }
}
