package measurement.client.nats;

import io.nats.client.Connection;
import io.nats.client.JetStream;

import measurement.client.AbstractSubscriber;

public class NatsSubscriber extends AbstractSubscriber{
    private Connection nc;
    private JetStream js;

    public NatsSubscriber(String clientId, String stream, String subject){
        super(clientId);
    }
}
