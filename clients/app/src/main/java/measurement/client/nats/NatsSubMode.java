package measurement.client.nats;

import io.nats.client.api.AckPolicy;

public enum NatsSubMode {
    pull("pull"),
    push("push");

    private String mode;
    private NatsSubMode(String mode){
        this.mode = mode;
    }
}
