package measurement.client.sample;

import java.util.Properties;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;

public class KafkaPub {
    public static void main(String[] args) {
        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "kafka-1:9092");
        properties.setProperty("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        properties.setProperty("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");
        properties.setProperty("acks", "1");
        Producer<String, byte[]> producer = new KafkaProducer<>(properties);
        String topicName = "test-topic-2";
        for (int i = 0; i < 100; i++) {
            String value = String.valueOf(i);
            ProducerRecord<String, byte[]> record = new ProducerRecord<String, byte[]>(topicName, value.getBytes());
            try {
                RecordMetadata rMetadata = producer.send(record).get();
                String meta = String.format("partition = %d, offset = %d, keySize = %d, valueSize = %d, value=%s",
                        rMetadata.partition(), rMetadata.offset(), rMetadata.serializedKeySize(),
                        rMetadata.serializedValueSize(), value);
                System.out.println(meta);
                rMetadata.partition();
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        producer.close();
    }
}
