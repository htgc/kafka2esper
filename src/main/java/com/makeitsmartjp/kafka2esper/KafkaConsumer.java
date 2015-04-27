package com.makeitsmartjp.kafka2esper;


import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import com.espertech.esper.client.EPServiceProvider;
import com.fasterxml.jackson.databind.ObjectMapper;

import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import lombok.extern.java.Log;

@Log
class EsperSubmitter implements Runnable {
	private final EPServiceProvider epService;
	private KafkaStream<byte[], byte[]> stream;
	private int threadNumber;
	private ObjectMapper objectMapper;
	
	public EsperSubmitter(KafkaStream<byte[], byte[]> stream, int threadNumber, EPServiceProvider epService) {
		this.stream = stream;
		this.threadNumber = threadNumber;
		objectMapper = new ObjectMapper();
		this.epService = epService;
	}
	
	@Override
	public void run() {
		ConsumerIterator<byte[], byte[]> it = stream.iterator();

		while(it.hasNext()) {
			NginxAccessLog eventObject = null;
			String json = new String(it.next().message());
			try {
				eventObject = objectMapper.readValue(json, NginxAccessLog.class);
			} catch (IOException e) {
				e.printStackTrace();
			}

			if (eventObject != null) {
				epService.getEPRuntime().sendEvent(eventObject);
			} else {
				log.log(Level.FINE, "Event is null");
			}

		}
		log.log(Level.FINE, "Shutting down Thread: " + threadNumber);
	}
	
}

@Log
public class KafkaConsumer {
	private final ConsumerConnector consumer;
	private final String topic;
	private final EPServiceProvider epService;
	private ExecutorService executor;

	public KafkaConsumer(Properties zkProps, String topic, EPServiceProvider epService) {
		consumer = kafka.consumer.Consumer.createJavaConsumerConnector(new ConsumerConfig(zkProps));
		this.topic = topic;
		this.epService = epService;
	}
	
	public void shutdown() {
		if (consumer != null) {
			consumer.shutdown();
		}
		if (executor != null) {
			executor.shutdown();
		}
		
		try {
			if (!executor.awaitTermination(5000, TimeUnit.MILLISECONDS)) {
				log.log(Level.WARNING, "Timed out waiting for consumer threads to shutdown, exiting uncleanly.");
			}
		} catch (InterruptedException iex) {
			log.log(Level.WARNING, "Interrupted during shutdown, exiting uncleanly.");
		}
	}
	
	public void run(int numThreads) {
		Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
		topicCountMap.put(topic, new Integer(numThreads));
		Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumer.createMessageStreams(topicCountMap);
		List<KafkaStream<byte[], byte[]>> streams = consumerMap.get(topic);
		
		// Launch all threads
		executor = Executors.newFixedThreadPool(numThreads);
		int threadNumber = 0;
		for (final KafkaStream<byte[], byte[]> stream : streams) {
			executor.submit(new EsperSubmitter(stream, threadNumber, epService));
			threadNumber++;
		}
	}
}
