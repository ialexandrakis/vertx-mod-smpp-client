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

import com.cloudhopper.commons.charset.Charset;
import com.cloudhopper.commons.charset.CharsetUtil;
import com.cloudhopper.smpp.SmppBindType;
import com.cloudhopper.smpp.SmppSessionConfiguration;
import com.cloudhopper.smpp.impl.DefaultSmppClient;
import com.cloudhopper.smpp.pdu.SubmitSm;
import com.cloudhopper.smpp.pdu.SubmitSmResp;
import com.cloudhopper.smpp.type.Address;
import com.cloudhopper.smpp.type.RecoverablePduException;
import com.cloudhopper.smpp.type.SmppChannelException;
import com.cloudhopper.smpp.type.SmppTimeoutException;
import com.cloudhopper.smpp.type.UnrecoverablePduException;
import org.vertx.java.busmods.BusModBase;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;

import java.util.concurrent.Executors;

/**
 * @author <a href="http://www.fuzzypattern.org/">Ioannis Alexandrakis</a>
 */
public class SmppClient extends BusModBase implements Handler<Message<JsonObject>> {

    private DefaultSmppClient smppClient;
    private SmppSessionConnector preservingSessionHandler;
    private Long preservingSessionTimerId = null;

    private Charset charset;

    @Override
    public void start() {
        super.start();
        String baseAddress = getOptionalStringConfig("address", "vertx.mod-smpp");

        String host = getMandatoryStringConfig("host");
        Integer port = getMandatoryIntConfig("port");
        String username = getOptionalStringConfig("username", null);
        String password = getOptionalStringConfig("password", null);

        SmppBindType type = SmppBindType.valueOf(getOptionalStringConfig("type", "TRANSMITTER"));
        Integer windowSize = getOptionalIntConfig("window.size", 1);
        Integer connectTimeout = getOptionalIntConfig("timeout.connect", 10000);
        Integer requestTimeout = getOptionalIntConfig("timeout.request", 30000);
        charset = CharsetUtil.charsets.get(getOptionalStringConfig("charset", "GSM"));

        smppClient = new DefaultSmppClient(Executors.newCachedThreadPool(), 1);

        SmppSessionConfiguration config = new SmppSessionConfiguration();
        config.setWindowSize(windowSize);
        config.setName("Client.Session.0");
        config.setType(type);
        config.setHost(host);
        config.setPort(port);
        config.setConnectTimeout(connectTimeout);
        if (username != null) {
            config.setSystemId(username);
        }
        if (password != null) {
            config.setPassword(password);
        }
        config.getLoggingOptions().setLogBytes(true);
        // to enable monitoring (request expiration)
        config.setRequestExpiryTimeout(requestTimeout);
        config.setWindowMonitorInterval(15000);
        config.setCountersEnabled(true);

        preservingSessionHandler = new SmppSessionConnector(smppClient, config);
        preservingSessionTimerId = vertx.setPeriodic(5000, preservingSessionHandler);

        eb.registerHandler(baseAddress, this);
    }


    @Override
    public void handle(Message<JsonObject> jsonObjectMessage) {
        logger.debug("Got: " + jsonObjectMessage);
        if (preservingSessionHandler.getSession() == null) {
            sendError(jsonObjectMessage, "Session is not available");
        } else {
            try {
                JsonObject object = jsonObjectMessage.body();
                byte[] textBytes = object.getBinary("textBytes");
                if (textBytes == null) {
                    textBytes = CharsetUtil.encode(object.getString("textString"), charset);
                }
                SubmitSm submit = new SubmitSm();
                //submit.setRegisteredDelivery(SmppConstants.REGISTERED_DELIVERY_SMSC_RECEIPT_REQUESTED);
                byte sourceTon = getOptionalByte(object, "sourceTon", (byte) 0x03);
                byte sourceNpi = getOptionalByte(object, "sourceNpi", (byte) 0x00);
                byte destTon = getOptionalByte(object, "destTon", (byte) 0x01);
                byte destNpi = getOptionalByte(object, "destNpi", (byte) 0x01);

                submit.setSourceAddress(new Address(sourceTon, sourceNpi, object.getString("source")));
                submit.setDestAddress(new Address(destTon, destNpi, object.getString("destination")));
                submit.setShortMessage(textBytes);
                SubmitSmResp resp = preservingSessionHandler.getSession().submit(submit, object.getLong("timeoutMillis", 10000));
                if (resp.getCommandStatus() == 0) {
                    sendOK(jsonObjectMessage);
                } else {
                    sendError(jsonObjectMessage, resp.getResultMessage());
                }
            } catch (RecoverablePduException | InterruptedException | SmppChannelException | UnrecoverablePduException | SmppTimeoutException | RuntimeException e) {
                logger.error(e.getMessage(), e);
                sendError(jsonObjectMessage, e.getMessage(), e);
            }
        }
    }

    @Override
    public void stop() {
        super.stop();
        if (preservingSessionTimerId != null) {
            vertx.cancelTimer(preservingSessionTimerId);
        }
        smppClient.destroy();
    }

    byte getOptionalByte(JsonObject map, String fieldName, byte def) {
        Byte encoded = map.getField(fieldName);
        return encoded == null ? def : encoded;
    }
}
