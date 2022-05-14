package measurement.client.nats;

import io.nats.client.api.AckPolicy;

public enum NatsSubMode {
    Pull("pull"),
    Push("push");

    private String mode;
    private NatsSubMode(String mode){
        this.mode = mode;
    }
}
