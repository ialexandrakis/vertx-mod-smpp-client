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

import com.cloudhopper.smpp.PduAsyncResponse;
import com.cloudhopper.smpp.impl.DefaultSmppSessionHandler;
import com.cloudhopper.smpp.pdu.Pdu;
import com.cloudhopper.smpp.pdu.PduRequest;
import com.cloudhopper.smpp.pdu.PduResponse;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;

/**
 * @author <a href="http://www.fuzzypattern.org/">Ioannis Alexandrakis</a>
 */
class SmppSessionHandler extends DefaultSmppSessionHandler {
    private static final Logger log = LoggerFactory.getLogger(SmppSessionHandler.class);

    public SmppSessionHandler() {
        super();
    }

    @Override
    public void firePduRequestExpired(PduRequest pduRequest) {
        log.warn("PDU request expired: " + pduRequest);
    }

    @Override
    public PduResponse firePduRequestReceived(PduRequest pduRequest) {
        PduResponse response = pduRequest.createResponse();
        // do any logic here
        log.debug(String.format("Received pdu request: %s", pduRequest));
        return response;
    }

    @Override
    public boolean firePduReceived(Pdu pdu) {
        log.debug(String.format("Pdu received: %s, reference object: %s", pdu, pdu.getReferenceObject()));
        return super.firePduReceived(pdu);
    }

    @Override
    public void fireExpectedPduResponseReceived(PduAsyncResponse pduAsyncResponse) {
        log.debug(String.format("Expected pdu received: %s, reference object: %s", pduAsyncResponse, pduAsyncResponse.getRequest().getReferenceObject()));
        if (pduAsyncResponse.getRequest().getReferenceObject() instanceof Message) {
            JsonObject json = new JsonObject();
            json.putString("status", "ok");
            ((Message) pduAsyncResponse.getRequest().getReferenceObject()).reply(json);
        }
    }

    @Override
    public void fireUnexpectedPduResponseReceived(PduResponse pduResponse) {
        log.debug(String.format("Unexpected pdu received: %s, reference object: %s", pduResponse, pduResponse.getReferenceObject()));
        if (pduResponse.getReferenceObject() instanceof Message) {
            JsonObject json = new JsonObject();
            json.putString("status", "ok");
            ((Message) pduResponse.getReferenceObject()).reply(json);
        }
    }
}
