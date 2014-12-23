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

import com.cloudhopper.smpp.SmppClient;
import com.cloudhopper.smpp.SmppSession;
import com.cloudhopper.smpp.SmppSessionConfiguration;
import com.cloudhopper.smpp.pdu.EnquireLink;
import com.cloudhopper.smpp.pdu.EnquireLinkResp;
import org.vertx.java.core.Handler;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;

/**
 * @author <a href="http://www.fuzzypattern.org/">Ioannis Alexandrakis</a>
 */
class SmppSessionConnector implements Handler<Long> {
    private static final Logger log = LoggerFactory.getLogger(SmppSessionConnector.class);
    private final SmppClient client;
    private final SmppSessionConfiguration config;

    private SmppSession session = null;

    public SmppSessionConnector(SmppClient client, SmppSessionConfiguration config) {
        this.client = client;
        this.config = config;
        rebind();
    }

    private void rebind() {
        try {
            resetSession();
            session = client.bind(config, new SmppSessionHandler());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
    }
}

    private void enquireLink() {
        try {
            EnquireLinkResp enquireLinkResp = session.enquireLink(new EnquireLink(), 5000);
            log.info("enquireLinkResp: command [" + enquireLinkResp.getCommandStatus() + "=" + enquireLinkResp.getResultMessage() + "], Thread ID: " + Thread.currentThread().getId());
//            if (enquireLinkResp.getCommandStatus() == 0) {
//                return true;
//            }
//        } catch (SmppTimeoutException | UnrecoverablePduException | SmppChannelException | InterruptedException | RecoverablePduSmppTimeoutException | UnrecoverablePduException | SmppChannelException | InterruptedException | RecoverablePduException | RuntimeException e) {
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            resetSession();
        }
    }

    @Override
    public void handle(Long timerId) {
//        rebind();
//        enquireLink();
        if (!session.isBound() && !session.isBinding() && !session.isOpen()) {
            rebind();
        } else if ("BOUND".equalsIgnoreCase(session.getStateName())) {
            enquireLink();
        } else if ("CLOSED".equalsIgnoreCase(session.getStateName()) || "UNBINDING".equalsIgnoreCase(session.getStateName())) {
            resetSession();
        }
    }

    private void resetSession() {
        if (session != null) {
            session.unbind(5000);
        }
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public SmppSession getSession() {
        return session;
    }
}
