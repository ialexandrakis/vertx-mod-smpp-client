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
import com.cloudhopper.smpp.SmppServerConfiguration;
import com.cloudhopper.smpp.SmppServerHandler;
import com.cloudhopper.smpp.SmppServerSession;
import com.cloudhopper.smpp.SmppSession;
import com.cloudhopper.smpp.SmppSessionConfiguration;
import com.cloudhopper.smpp.impl.DefaultSmppServer;
import com.cloudhopper.smpp.impl.DefaultSmppSessionHandler;
import com.cloudhopper.smpp.pdu.BaseBind;
import com.cloudhopper.smpp.pdu.BaseBindResp;
import com.cloudhopper.smpp.pdu.PduRequest;
import com.cloudhopper.smpp.pdu.PduResponse;
import com.cloudhopper.smpp.type.SmppChannelException;
import com.cloudhopper.smpp.type.SmppProcessingException;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;
import org.vertx.testtools.TestVerticle;

import java.lang.ref.WeakReference;

/**
 * @author <a href="http://www.fuzzypattern.org/">Ioannis Alexandrakis</a>
 */
public abstract class AbstractSmppTest extends TestVerticle {
    private static final Logger log = LoggerFactory.getLogger(AbstractSmppTest.class);
    static final String ADDRESS = "vertx.mod-smpp";

    public static SmppServer createSmppServer() {
        SmppServerConfiguration configuration = new SmppServerConfiguration();
        configuration.setPort(Integer.valueOf(System.getProperty("port")));
        configuration.setMaxConnectionSize(10);
        configuration.setNonBlockingSocketsEnabled(true);
        configuration.setDefaultRequestExpiryTimeout(30000);
        configuration.setDefaultWindowMonitorInterval(15000);
        configuration.setDefaultWindowSize(5);
        configuration.setDefaultWindowWaitTimeout(configuration.getDefaultRequestExpiryTimeout());
        configuration.setDefaultSessionCountersEnabled(true);
        configuration.setJmxEnabled(true);

        DefaultSmppServer smppServer = new DefaultSmppServer(configuration, new DefaultSmppServerHandler());

        log.info("Starting SMPP server...");
        try {
            smppServer.start();
        } catch (SmppChannelException e) {
            throw new RuntimeException(e);
        }
        log.info("SMPP server started");

        return smppServer;
    }

    private static class DefaultSmppServerHandler implements SmppServerHandler {

        @Override
        public void sessionBindRequested(Long sessionId, SmppSessionConfiguration sessionConfiguration, final BaseBind bindRequest) throws SmppProcessingException {
            sessionConfiguration.setName("Application.SMPP." + sessionConfiguration.getSystemId());
            log.info("Session bound");
            //throw new SmppProcessingException(SmppConstants.STATUS_BINDFAIL, null);
        }

        @Override
        public void sessionCreated(Long sessionId, SmppServerSession session, BaseBindResp preparedBindResponse) throws SmppProcessingException {
            log.info("Session created: " + session.getBindType());
            session.serverReady(new TestSmppSessionHandler(session));
        }

        @Override
        public void sessionDestroyed(Long sessionId, SmppServerSession session) {
            log.info("Session destroyed: " + session);
            session.destroy();
        }

    }

    public static class TestSmppSessionHandler extends DefaultSmppSessionHandler {

        private final WeakReference<SmppSession> sessionRef;

        public TestSmppSessionHandler(SmppSession session) {
            this.sessionRef = new WeakReference<>(session);
        }

        @Override
        public PduResponse firePduRequestReceived(PduRequest pduRequest) {
            SmppSession session = sessionRef.get();
            log.info(String.format("Received: %s from session: %s", pduRequest, session));
            return pduRequest.createResponse();
        }
    }

    JsonObject getConfig() {
        JsonObject config = new JsonObject();
        config.putString("address", ADDRESS);
        config.putString("host", System.getProperty("host"));
        config.putNumber("port", Integer.valueOf(System.getProperty("port")));
        config.putString("username", System.getProperty("username"));
        config.putString("password", System.getProperty("password"));
        return config;
    }
}