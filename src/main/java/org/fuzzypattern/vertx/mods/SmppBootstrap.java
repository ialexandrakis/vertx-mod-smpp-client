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

import org.vertx.java.busmods.BusModBase;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Future;

/**
 * @author <a href="http://www.fuzzypattern.org/">Ioannis Alexandrakis</a>
 */
public class SmppBootstrap extends BusModBase {

    public enum SmppMode {
        SYNC, ASYNC
    }

    @Override
    public void start(final Future<Void> startedResult) {
        super.start();

        SmppMode mode = SmppMode.valueOf(getOptionalStringConfig("mode", "sync").toUpperCase());
        switch(mode) {
            case ASYNC:
                container.deployVerticle(SmppClient.class.getName(), config, 1, new AsyncResultHandler<String>() {
                    public void handle(AsyncResult<String> deployResult) {
                        if (deployResult.succeeded()) {
                            startedResult.setResult(null);
                        } else {
                            startedResult.setFailure(deployResult.cause());
                        }
                    }
                });
                break;
            case SYNC:
                container.deployWorkerVerticle(SmppClient.class.getName(), config, 1, true, new AsyncResultHandler<String>() {
                    public void handle(AsyncResult<String> deployResult) {
                        if (deployResult.succeeded()) {
                            startedResult.setResult(null);
                        } else {
                            startedResult.setFailure(deployResult.cause());
                        }
                    }
                });
                break;
            default:
                break;
        }
    }

}
