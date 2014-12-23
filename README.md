# SMPP Client

This module connects to an SMPP server (currently as a TRANSMITTER only) and sends SMSes. It tries to keep the session/connection alive by enquiring the link and resetting the session if needed. It uses cloudhopper-smpp internally.

## Dependencies

This module requires an SMPP server to be available on the network.

## Configuration

This `smpp-client` module takes the following configuration:

    {
        "address": <address>,
        "host": <host>,
        "port": <port>,
        "username": <username - optional>,
        "password": <password - optional>,
        "window.size": <int - optional default 1>,
        "timeout.connect": <int - optional default 10000>,
        "timeout.request": <int - optional default 30000>,
        "charset": <default - optional "GSM">
    }

For example:

    {
        "address": "vertx.mod-smpp",
        "host": "localhost",
        "port": 2776,
    }

More specifically:

* `address` The main address for the module. Every module has a main address. Defaults to `vertx.mod-smpp`.
* `host` Host name or ip address of the SMPP server.
* `port` Port at which the SMPP server is listening.
* `window.size` Maximum number of open transactions before requiring a response to at least one of those. Defaults to `1`.
* `timeout.connect` Connect timeout to the SMPP server. Default is `10000` millis.
* `timeout.request` Time to wait for an endpoint to respond to a request before it exmpires. Defaults to `30000` millis.
* `charset` The character set to encode/decode the message. Defaults to `GSM`, other values are (from the cloudhopper lib): `UTF-8`, `MODIFIED-UTF8`, `AIRWIDE-IA5`, `VFD2-GSM`, `VFTR-GSM`, `GSM7`, `GSM8`, `AIRWIDE-GSM`, `TMOBILE-NL-GSM`, `ISO-8859-1`, `ISO-8859-15`, `PACKED-GSM`, `UCS-2`.

## Operations

The module currently supports sending sms messages

### Send

Sends an SMS

To send an SMS, send a JSON message to the module main address:

    {
        "textString": <message as a string>,
        "textBytes": <message already encoded in bytes if needed - optional>,
        "source": <SMS source>,
        "destination": <SMS destination>,
        "sourceTon": <source ton - optional default 0x03>,
        "sourceNpi": <source npi - optional default 0x00>,
        "destTon": <dest ton - optional default 0x01>,
        "destNpi": <dest npi - optional default 0x01>,
        "timeoutMillis": <timeout before getting a response from the SMPP - optional default 10000>
    }

Where:
* `textString` or `textBytes` is the text that you wish to send. If it is in bytes, it does get re-encoded for the SMPP (used in special cases)
* `source` is the source that will be included in the SMS (e.g. the 'sender' of the SMS)
* `destination` is the destination of the SMS
* `sourceTon`, `sourceNpi`, `destTon` and `destNpi` are options for the source and destination addresses

An example would be (the special unicode character is the euro sign €):

    {
        "textString": "This is a vert.x-smpp test message. \u20AC",
        "source": "TEST",
        "destination": "301234567890"
    }

If the message was submitted successfully, a reply message is sent back to the sender with the following data:

    {
        "status": "ok"
    }

If an error occurs an erroneous reply is returned:

    {
        "status": "error",
        "message": <message>
    }

Where
* `message` is the error message.


