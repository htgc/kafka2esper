package com.makeitsmartjp.kafka2esper;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

public class EsperEventListener implements UpdateListener {

	@Override
	public void update(EventBean[] newEvents, EventBean[] oldEvents) {
		EventBean event = newEvents[0];
		System.out.println("EventUpdate: " + event.getUnderlying());
		try {
			String avg_response = event.get("avg(upstream_response)").toString();
			String max_response = event.get("max(upstream_response)").toString();
			String num_count = event.get("count(*)").toString();
			System.out.println("Avg. UpstreamResponseTime: " + avg_response);
			System.out.println("Max. UpstreamResponseTime: " + max_response);
			System.out.println("Number of requests: " + num_count);
		} catch (Exception e) {
			System.out.println("EventListener Exception: " + e);
		}
	}

}
