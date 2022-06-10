package measurement.client.kafka;

import java.util.Properties;
import java.util.concurrent.CompletableFuture;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import measurement.client.Measurement;
import measurement.client.base.AbstractPublisher;
import measurement.client.base.Payload;
import measurement.client.base.Record;

public class KafkaPublisher extends AbstractPublisher{
    private String topicName;
    private Producer<String, byte[]> producer;
    public KafkaPublisher(String clientId, long interval, int messageSize, Boolean pubAsync, String topicName, Properties properties){
        super(clientId, interval, messageSize, pubAsync);
        this.topicName = topicName;

        properties.setProperty("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        properties.setProperty("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");
        producer = new KafkaProducer<>(properties);
    }

    @Override
    public Record publish() {
        Payload payload = createPayload();
        Record record = null;
        try {
            String json = mapper.writeValueAsString(payload);
            ProducerRecord<String, byte[]> message = new ProducerRecord<String,byte[]>(topicName, json.getBytes());
            // RecordMetadata rMetadata = producer.send(message);
            producer.send(message);
            record = new Record(payload, json.length(), clientId);
        } catch (Exception e) {
            Measurement.logger.warning("Error sending message.\n" + e.getMessage());
            e.printStackTrace();
        }
        return record;
    }

    public CompletableFuture<Record> publishAsync(){
        CompletableFuture<Record> future = new CompletableFuture<>();
        Payload payload = createPayload();
        try {
            String json = mapper.writeValueAsString(payload);
            ProducerRecord<String, byte[]> message = new ProducerRecord<String,byte[]>(topicName, json.getBytes());
            final Record record = new Record(payload, json.length(), clientId);
            producer.send(message, (metadata, exeception) -> {
                if (exeception == null){
                    future.complete(record);
                }else{
                    future.completeExceptionally(exeception);
                }
            });
        } catch (Exception e) {
            Measurement.logger.warning("Error sending message.\n" + e.getMessage());
            e.printStackTrace();
        }
        return future;
    }

    @Override
    public void close() {
        if (producer != null){
            producer.close();
        }
    }

    @Override
    public Boolean isConnected() {
        if (producer == null){
            return false;
        }
        return true;
    }
}
