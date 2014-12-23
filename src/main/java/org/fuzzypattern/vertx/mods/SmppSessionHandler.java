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

import com.cloudhopper.smpp.impl.DefaultSmppSessionHandler;
import com.cloudhopper.smpp.pdu.PduRequest;
import com.cloudhopper.smpp.pdu.PduResponse;
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
        log.debug(String.format("Received pdu request: %s", pduRequest.toString()));
        return response;
    }
}
