package com.makeitsmartjp.kafka2esper;

import com.espertech.esper.client.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import lombok.extern.java.Log;

@Log
public class App 
{
    public static void main( String... args )
    {
    	if (args.length != 1) {
    		System.out.println("Usage: java com.makeitsmartjp.kafka2esper.App <property file>");
    		return;
    	}
    	String configFile = args[0];
    	Properties prop = new Properties();
    	
    	try {
    		prop.load(new FileInputStream(configFile));
    	} catch (IOException e) {
    		e.printStackTrace();
    		return;
    	}
    	
    	// Esper configuration
		EPServiceProvider epService = EPServiceProviderManager.getDefaultProvider();
		String expression = prop.getProperty("esper.query");
		log.log(Level.INFO, "EPL:" + expression);
		EPStatement state1 = epService.getEPAdministrator().createEPL(expression);
		
		// Set Listener
		EsperEventListener listener = new EsperEventListener();
		state1.addListener(listener);
		
		// Zookeeper configuration
		String zookeeper      = prop.getProperty("zookeeper.connect");
		String groupId        = prop.getProperty("group.id");
		String sessionTimeout = prop.getProperty("zookeeper.settion.timeout", "400");
		String syncTimeout    = prop.getProperty("zookeeper.sync.timeout", "200");
		String autoCommitItvl = prop.getProperty("auto.commit.interval", "1000");
		
		Properties zkProps    = new Properties();
		zkProps.put("zookeeper.connect", zookeeper);
		zkProps.put("group.id", groupId);
		zkProps.put("zookeeper.session.timeout", sessionTimeout);
		zkProps.put("zookeeper.sync.timeout", syncTimeout);
		zkProps.put("auto.commit.interval", autoCommitItvl);
		
		// Kafka Configuration
		String topic     = prop.getProperty("kafka.topic");
		int numThreads   = Integer.parseInt(prop.getProperty("consumer.numThreads"));
		log.log(Level.INFO, "zookeeper:" + zookeeper + "\tgroupId:" + groupId + "\ttopic:" + topic + "\tnumThreads:" + numThreads);
		
		KafkaConsumer consumer = new KafkaConsumer(zkProps, topic, epService);
		consumer.run(numThreads);
    }
}
