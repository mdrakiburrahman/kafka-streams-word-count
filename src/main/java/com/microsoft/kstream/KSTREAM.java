package com.microsoft.kstream;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;

import java.util.Arrays;
import java.util.Properties;

public final class KSTREAM {

    public static void main(String[] args) {
        System.out.println("\n==========================================================");
        System.out.println("➡  KSTREAM");
        System.out.println("==========================================================");

        Properties props = getConfig();

        StreamsBuilder streamsBuilder = new StreamsBuilder();
        // Build the Topology
        streamsBuilder.<String, String>stream("sentences") // <- Input Topic
                .flatMapValues((key, value) ->
                        Arrays.asList(value.toLowerCase()
                            .split(" ")))
                .groupBy((key, value) -> value)
                .count(Materialized.with(Serdes.String(), Serdes.Long()))
                .toStream()
                .to("word-count", Produced.with(Serdes.String(), Serdes.Long())); // <- Output Topic
        
        // Print to stdout
        streamsBuilder.stream("sentences")
                .foreach((key, value) -> System.out.println(key + ": " + value));

        // Create the Kafka Streams Application
        KafkaStreams kafkaStreams = new KafkaStreams(streamsBuilder.build(), props);
        // Start the application
        System.out.println("➡  STARTING STREAM...");
        kafkaStreams.start();

        // attach shutdown handler to catch control-c
        Runtime.getRuntime().addShutdownHook(new Thread( () -> {
            System.out.println("➡  STOPPING STREAM...");
            kafkaStreams.close();
        }));

        System.out.println("\n==========================================================");
        System.out.println("➡  STARTED");
        System.out.println("==========================================================");
    }

    private static Properties getConfig() {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "word-count-app");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:9092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, "0");
        return props;
    }
}